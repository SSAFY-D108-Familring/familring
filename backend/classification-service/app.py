from fastapi import FastAPI, HTTPException, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse
from pydantic import BaseModel, Field
from typing import List, Dict, TypeVar, Generic, Optional
from contextlib import asynccontextmanager
import face_recognition
import numpy as np
import requests
from io import BytesIO
from PIL import Image, ImageEnhance, ExifTags
import cv2
import logging
import os
from dotenv import load_dotenv
import socket
import contextlib
import gc
from py_eureka_client import eureka_client
import asyncio
import concurrent.futures
import aiohttp

# .env 파일 로드
load_dotenv()

# 환경변수 설정
EUREKA_SERVER = os.getenv('EUREKA_SERVER')
APP_NAME = os.getenv('APP_NAME')
SERVER_HOST = os.getenv('SERVER_HOST','0.0.0.0')
INSTANCE_HOST = os.getenv('INSTANCE_HOST')
SERVER_PORT = int(os.getenv('SERVER_PORT'))

# CPU 코어 수에 기반한 스레드 풀 최적화 (4코어 서버 기준)
CPU_COUNT = 4  # 서버의 실제 코어 수
THREAD_POOL = concurrent.futures.ThreadPoolExecutor(
    max_workers=min(32, CPU_COUNT * 2),  # 최대 워커 수 제한
    thread_name_prefix="face_recognition_worker"
)

# 전역 변수로 세마포어 설정
MAX_CONCURRENT_IMAGES = min(4, CPU_COUNT)  # 4코어 서버에서는 4
image_semaphore = asyncio.Semaphore(MAX_CONCURRENT_IMAGES)

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    try:
        logger.info(f"Using port: {SERVER_PORT}")
        # Eureka 클라이언트 초기화
        eureka_config = {
            "eureka_server": EUREKA_SERVER,
            "app_name": APP_NAME,
            "instance_port": SERVER_PORT,
            "instance_host": INSTANCE_HOST
        }
        logger.info(f"Eureka configuration: {eureka_config}")
        
        # 필수 값 검증
        if not all([EUREKA_SERVER, APP_NAME, INSTANCE_HOST, SERVER_PORT]):
            missing_values = []
            if not EUREKA_SERVER: missing_values.append("EUREKA_SERVER")
            if not APP_NAME: missing_values.append("APP_NAME")
            if not INSTANCE_HOST: missing_values.append("INSTANCE_HOST")
            if not SERVER_PORT: missing_values.append("SERVER_PORT")
            raise ValueError(f"Missing required configuration: {', '.join(missing_values)}")

        await eureka_client.init_async(
            eureka_server=EUREKA_SERVER,
            app_name=APP_NAME,
            instance_port=SERVER_PORT,
            instance_host=INSTANCE_HOST,
            instance_ip=INSTANCE_HOST
        )
        logger.info(f"Successfully registered to Eureka server at {EUREKA_SERVER}")
    except Exception as e:
        logger.error(f"Failed to register to Eureka server: {str(e)}")
        raise
    
    yield
    
    # Shutdown
    try:
        await eureka_client.stop_async()
        logger.info("Successfully unregistered from Eureka server")
    except Exception as e:
        logger.error(f"Error during Eureka client shutdown: {str(e)}")

app = FastAPI(
    title="Face Classification API",
    description="얼굴 유사도 분석 API",
    version="1.0.0",
    lifespan=lifespan,
    openapi_url="/face-recognition/openapi.json",  
    docs_url="/face-recognition/docs",          
    redoc_url=None
)

# CORS 미들웨어 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

T = TypeVar('T')

class BaseResponse(BaseModel, Generic[T]):
    statusCode: int
    message: str
    data: Optional[T] = None

    class Config:
        arbitrary_types_allowed = True

    @classmethod
    def create(cls, status_code: int, message: str, data: Optional[T] = None):
        return cls(
            statusCode=status_code,
            message=message,
            data=data
        )

class Person(BaseModel):
    id: int
    photoUrl: str = Field(..., alias="photoUrl")

class AnalysisRequest(BaseModel):
    targetImages: List[str] = Field(..., alias="targetImages")
    people: List[Person]

class SimilarityResponse(BaseModel):
    imageUrl: str = Field(..., alias="imageUrl")
    similarities: Dict[int, float]
    faceCount: int = Field(..., alias="faceCount")

class CountResponse(BaseModel):
    faceCount: int = Field(..., alias="faceCount")

async def fix_image_rotation(image):
    """
    EXIF 정보를 기반으로 이미지 회전을 보정합니다.
    """
    try:
        loop = asyncio.get_event_loop()
        
        # PIL Image가 아닌 경우 변환
        if not isinstance(image, Image.Image):
            image = await loop.run_in_executor(
                THREAD_POOL,
                lambda: Image.fromarray(image)
            )

        try:
            # EXIF 정보 가져오기
            for orientation in ExifTags.TAGS.keys():
                if ExifTags.TAGS[orientation] == 'Orientation':
                    break
            
            exif = await loop.run_in_executor(
                THREAD_POOL,
                lambda: image._getexif()
            )
            
            if exif is not None:
                orientation_value = exif.get(orientation)
                
                # 방향에 따른 이미지 회전
                if orientation_value == 2:
                    image = await loop.run_in_executor(
                        THREAD_POOL,
                        lambda: image.transpose(Image.FLIP_LEFT_RIGHT)
                    )
                elif orientation_value == 3:
                    image = await loop.run_in_executor(
                        THREAD_POOL,
                        lambda: image.transpose(Image.ROTATE_180)
                    )
                elif orientation_value == 4:
                    image = await loop.run_in_executor(
                        THREAD_POOL,
                        lambda: image.transpose(Image.FLIP_TOP_BOTTOM)
                    )
                elif orientation_value == 5:
                    image = await loop.run_in_executor(
                        THREAD_POOL,
                        lambda: image.transpose(Image.FLIP_LEFT_RIGHT).transpose(Image.ROTATE_90)
                    )
                elif orientation_value == 6:
                    image = await loop.run_in_executor(
                        THREAD_POOL,
                        lambda: image.transpose(Image.ROTATE_270)
                    )
                elif orientation_value == 7:
                    image = await loop.run_in_executor(
                        THREAD_POOL,
                        lambda: image.transpose(Image.FLIP_LEFT_RIGHT).transpose(Image.ROTATE_270)
                    )
                elif orientation_value == 8:
                    image = await loop.run_in_executor(
                        THREAD_POOL,
                        lambda: image.transpose(Image.ROTATE_90)
                    )
        except (AttributeError, KeyError, IndexError):
            # EXIF 정보가 없거나 처리할 수 없는 경우 원본 이미지 반환
            pass

        # numpy 배열로 변환하여 반환
        return await loop.run_in_executor(
            THREAD_POOL,
            lambda: np.array(image)
        )
    except Exception as e:
        logger.error(f"이미지 회전 보정 실패: {str(e)}")
        return await loop.run_in_executor(
            THREAD_POOL,
            lambda: np.array(image)
        )
    
async def preprocess_image(image):
    """
    이미지 전처리 함수 (EXIF 회전 보정 추가)
    """
    try:
        loop = asyncio.get_event_loop()
        
        # PIL Image를 RGB로 변환
        if image.mode != 'RGB':
            image = await loop.run_in_executor(
                THREAD_POOL,
                lambda: image.convert('RGB')
            )
        
        # EXIF 기반 회전 보정
        rotated_image = await fix_image_rotation(image)
        
        # 히스토그램 평준화
        img_lab = await loop.run_in_executor(
            THREAD_POOL,
            lambda: cv2.cvtColor(rotated_image, cv2.COLOR_RGB2LAB)
        )
        
        l, a, b = cv2.split(img_lab)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        
        cl = await loop.run_in_executor(
            THREAD_POOL,
            lambda: clahe.apply(l)
        )
        
        img_lab = await loop.run_in_executor(
            THREAD_POOL,
            lambda: cv2.merge((cl,a,b))
        )
        
        img_rgb = await loop.run_in_executor(
            THREAD_POOL,
            lambda: cv2.cvtColor(img_lab, cv2.COLOR_LAB2RGB)
        )
        
        return img_rgb
        
    except Exception as e:
        logger.error(f"이미지 전처리 실패: {str(e)}")
        return await loop.run_in_executor(
            THREAD_POOL,
            lambda: np.array(image)
        )

async def load_image_from_url_async(url: str, session: aiohttp.ClientSession):
    """비동기적으로 URL에서 이미지를 로드"""
    try:
        async with session.get(url) as response:
            response.raise_for_status()
            content = await response.read()
            loop = asyncio.get_event_loop()
            img = await loop.run_in_executor(
                THREAD_POOL,
                lambda: Image.open(BytesIO(content))
            )
            # 여기가 수정된 부분
            processed_img = await preprocess_image(img)
            return processed_img
    except Exception as e:
        logger.error(f"이미지 로드 실패: {url} - {str(e)}")
        return None


async def load_image_from_bytes(file_content: bytes):
    """
    바이트 데이터로부터 이미지를 로드하고 전처리합니다.
    """
    try:
        loop = asyncio.get_event_loop()
        img = await loop.run_in_executor(
            THREAD_POOL,
            lambda: Image.open(BytesIO(file_content))
        )
        processed_img = await preprocess_image(img)
        return processed_img
    except Exception as e:
        logger.error(f"이미지 로드 실패: {str(e)}")
        return None

async def process_face_encodings(image):
    """비동기적으로 얼굴 인코딩 처리"""
    if image is None:
        logger.error("입력 이미지가 None입니다")
        return []
    
    try:
        # 현재 실행 중인 루프 가져오기
        loop = asyncio.get_running_loop()
        
        # 이미지 크기 조정
        height, width = image.shape[:2]
        max_dimension = 1300
        logger.info(f"원본 이미지 크기: {width}x{height}")
        
        if max(height, width) > max_dimension:
            scale = max_dimension / max(height, width)
            image = await loop.run_in_executor(
                THREAD_POOL,
                lambda: cv2.resize(image, None, fx=scale, fy=scale)
            )
            new_height, new_width = image.shape[:2]
            logger.info(f"이미지 크기 조정: {new_width}x{new_height} (scale: {scale:.2f})")

        # HOG로 얼굴 검출
        face_locations = await loop.run_in_executor(
            THREAD_POOL,
            lambda: face_recognition.face_locations(image, model="hog", number_of_times_to_upsample=1)
        )
        
        if not face_locations:
            logger.warning("얼굴 검출 실패")
            return []
            
        # 얼굴 인코딩
        face_encodings = await loop.run_in_executor(
            THREAD_POOL,
            lambda: face_recognition.face_encodings(image, face_locations, num_jitters=1)
        )
    
        return face_encodings
            
    except Exception as e:
        logger.error(f"얼굴 인코딩 중 에러 발생: {str(e)}")
        return []

# CPU 및 메모리 기반 세마포어 설정
def calculate_optimal_semaphore():
    """
    시스템 리소스를 기반으로 최적의 세마포어 값을 계산
    """
    try:
        import psutil
        
        # CPU 코어 수 확인
        cpu_count = os.cpu_count() or 4
        
        # 사용 가능한 메모리 확인 (GB)
        available_memory_gb = psutil.virtual_memory().available / (1024 ** 3)
        
        # 예상되는 이미지 처리당 메모리 사용량 (GB)
        estimated_memory_per_image = 0.5  # 0.5GB per image processing
        
        # CPU와 메모리 기반 동시 처리 가능 수 계산
        cpu_based = cpu_count - 1  # 1개는 시스템용으로 예약
        memory_based = int(available_memory_gb / estimated_memory_per_image)
        
        # 더 작은 값 선택 (병목 리소스 기준)
        optimal = min(cpu_based, memory_based)
        
        # 최소/최대 제한 설정
        MIN_CONCURRENT = 2
        MAX_CONCURRENT = 10
        optimal = max(MIN_CONCURRENT, min(optimal, MAX_CONCURRENT))
        
        logger.info(f"Calculated optimal semaphore: {optimal} (CPU: {cpu_based}, Memory: {memory_based})")
        return optimal
        
    except Exception as e:
        logger.warning(f"Error calculating optimal semaphore: {str(e)}. Using default value.")
        return 2  # 기본값

# 전역 변수로 세마포어 설정
MAX_CONCURRENT_IMAGES = calculate_optimal_semaphore()
image_semaphore = asyncio.Semaphore(MAX_CONCURRENT_IMAGES)

@app.post("/face-recognition/classification", response_model=BaseResponse[List[SimilarityResponse]])
async def classify_images(request: AnalysisRequest):
    try:
        # 현재 실행 중인 루프 가져오기
        loop = asyncio.get_running_loop()
        connector = aiohttp.TCPConnector(limit=5, force_close=True)
        timeout = aiohttp.ClientTimeout(total=60)
        
        async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
            async def process_image(url: str) -> tuple[str, List[np.ndarray]]:
                """이미지 로드부터 얼굴 인코딩까지 한번에 처리"""
                async with image_semaphore:
                    try:
                        async with session.get(url) as response:
                            if response.status != 200:
                                logger.warning(f"Invalid URL: {url}")
                                return url, []
                            content = await response.read()
                            img = await load_image_from_url_async(url, session)
                            if img is None:
                                return url, []
                            
                            # 얼굴 인코딩 처리
                            face_encodings = await process_face_encodings(img)
                            
                            # 메모리 해제
                            del content
                            del img
                            
                            return url, face_encodings
                    except Exception as e:
                        logger.error(f"Error processing image {url}: {str(e)}")
                        return url, []

            # 모든 이미지 동시 처리
            logger.info("모든 이미지 동시 처리 시작")
            tasks = []
            for person in request.people:
                tasks.append(process_image(person.photoUrl))
            for url in request.targetImages:
                tasks.append(process_image(url))
                
            all_results = await asyncio.gather(*tasks, return_exceptions=True)
            
            # 결과 분리
            people_count = len(request.people)
            people_results = all_results[:people_count]
            target_results = all_results[people_count:]
            
            # 에러 체크
            for result in all_results:
                if isinstance(result, Exception):
                    raise result
            
            # 인물 인코딩 매핑
            people_encodings = {}
            for (url, encodings), person in zip(people_results, request.people):
                if encodings and len(encodings) > 0:
                    people_encodings[person.id] = encodings[0]
                else:
                    logger.warning(f"인물 이미지에서 얼굴을 검출하지 못했습니다: {url}")

            if not people_encodings:
                return BaseResponse.create(
                    status_code=400,
                    message="유효한 얼굴이 포함된 인물 이미지가 없습니다."
                )

            # 유사도 계산을 위한 벡터화된 처리
            results = []
            person_encodings_array = np.array(list(people_encodings.values()))
            person_ids = list(people_encodings.keys())

            for url, target_encodings in target_results:
                if not target_encodings:
                    results.append(SimilarityResponse(
                        imageUrl=url,
                        similarities={person_id: 0.0 for person_id in people_encodings.keys()},
                        faceCount=0
                    ))
                    continue

                # 모든 얼굴에 대한 유사도 계산
                max_similarities = {person_id: 0.0 for person_id in person_ids}
                for target_encoding in target_encodings:
                    distances = face_recognition.face_distance(person_encodings_array, target_encoding)
                    similarities = 1 - distances
                    
                    for person_id, similarity in zip(person_ids, similarities):
                        max_similarities[person_id] = max(
                            max_similarities[person_id],
                            float(max(0, similarity))
                        )

                results.append(SimilarityResponse(
                    imageUrl=url,
                    similarities=max_similarities,
                    faceCount=len(target_encodings)
                ))

            # 메모리 정리
            del person_encodings_array
            gc.collect()

            response = BaseResponse.create(
                status_code=200,
                message="얼굴 유사도 분석이 완료되었습니다.",
                data=results
            )

            logger.info("얼굴 유사도 분석 완료")
            return response

    except Exception as e:
        error_msg = f"얼굴 유사도 분석 중 오류가 발생했습니다: {str(e)}"
        logger.error(error_msg)
        return BaseResponse.create(
            status_code=500,
            message=error_msg
        )

@app.post("/face-recognition/face-count", response_model=BaseResponse[CountResponse])
async def count_faces(file: UploadFile = File(...)):
    try:
        logger.info(f"얼굴 수 검출 요청 시작 - 파일명: {file.filename}")
        
        allowed_extensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"}
        file_ext = file.filename.lower()[file.filename.rfind("."):]
        
        if file_ext not in allowed_extensions:
            return BaseResponse.create(
                status_code=400,
                message="지원되지 않는 파일 형식입니다. JPG, JPEG, PNG, GIF, BMP, WEBP 파일만 허용됩니다."
            )
        
        MAX_FILE_SIZE = 20 * 1024 * 1024 
        file_content = await file.read()
        file_size = len(file_content)
        
        if file_size > MAX_FILE_SIZE:
            return BaseResponse.create(
                status_code=400,
                message="파일 크기가 너무 큽니다. 최대 20MB까지 허용됩니다."
            )

        img = await load_image_from_bytes(file_content)
        
        if img is None:
            return BaseResponse.create(
                status_code=400,
                message="이미지를 처리할 수 없습니다."
            )
        
        face_encodings = await process_face_encodings(img)
        face_count = len(face_encodings)
        
        response = BaseResponse.create(
            status_code=200,
            message="얼굴 수 검출이 완료되었습니다.",
            data=CountResponse(faceCount=face_count)
        )
        
        logger.info(f"Face Count API 응답: {response.model_dump_json(exclude_none=True)}")
        return response
        
    except Exception as e:
        error_msg = f"얼굴 수 검출 중 오류가 발생했습니다: {str(e)}"
        logger.error(error_msg)
        return BaseResponse.create(
            status_code=500,
            message=error_msg
        )

if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting server on port {SERVER_PORT}")
    uvicorn.run(
        app, 
        host=SERVER_HOST,
        port=SERVER_PORT,
        loop="auto",  # asyncio 이벤트 루프 정책 자동 설정
        workers=1     # 단일 워커로 실행하여 루프 충돌 방지
    )