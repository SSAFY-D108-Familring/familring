package com.familring.albumservice.service;

import com.familring.albumservice.domain.Album;
import com.familring.albumservice.domain.Album.AlbumBuilder;
import com.familring.albumservice.domain.AlbumType;
import com.familring.albumservice.domain.Photo;
import com.familring.albumservice.dto.client.*;
import com.familring.albumservice.dto.response.AlbumInfoResponse;
import com.familring.albumservice.dto.request.AlbumRequest;
import com.familring.albumservice.dto.request.AlbumUpdateRequest;
import com.familring.albumservice.dto.response.AlbumResponse;
import com.familring.albumservice.dto.response.PhotoItem;
import com.familring.albumservice.exception.album.AlbumNotFoundException;
import com.familring.albumservice.exception.album.InvalidAlbumParameterException;
import com.familring.albumservice.exception.album.InvalidAlbumRequestException;
import com.familring.albumservice.repository.AlbumQueryRepository;
import com.familring.albumservice.repository.AlbumRepository;
import com.familring.albumservice.repository.PhotoRepository;
import com.familring.albumservice.service.client.ClassificationServiceFeignClient;
import com.familring.albumservice.service.client.FamilyServiceFeignClient;
import com.familring.albumservice.service.client.FileServiceFeignClient;
import com.familring.albumservice.service.client.UserServiceFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.familring.albumservice.domain.AlbumType.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class AlbumService {

    private final FamilyServiceFeignClient familyServiceFeignClient;
    private final FileServiceFeignClient fileServiceFeignClient;
    private final ClassificationServiceFeignClient classificationServiceFeignClient;
    private final AlbumRepository albumRepository;
    private final AlbumQueryRepository albumQueryRepository;
    private final PhotoRepository photoRepository;
    private final UserServiceFeignClient userServiceFeignClient;

    @Value("${aws.s3.album-photo-path}")
    private String albumPhotoPath;

    /**
     * 일반 앨범 - UserId, ScheduleId가 null
     * 인물 앨범 - UserId만 있음
     * 일정 앨범 - ScheduleId만 있음
     */
    @Transactional
    public void createAlbum(AlbumRequest albumRequest, Long userId) {
        Long familyId = familyServiceFeignClient.getFamilyInfo(userId).getData().getFamilyId();

        AlbumBuilder albumBuilder = Album.builder().familyId(familyId).albumName(albumRequest.getAlbumName())
                .albumType(albumRequest.getAlbumType());

        if (albumRequest.getAlbumType() == PERSON) {
            log.error(albumRequest.getScheduleId());
            if (userId == null || albumRequest.getScheduleId() != null) {
                throw new InvalidAlbumParameterException();
            }
            albumBuilder.userId(userId);
        } else if (albumRequest.getAlbumType() == SCHEDULE) {
            if (albumRequest.getScheduleId() == null) {
                throw new InvalidAlbumParameterException();
            }
            albumBuilder.scheduleId(albumRequest.getScheduleId());
        } else if (albumRequest.getAlbumType() == NORMAL) {
            if (albumRequest.getScheduleId() != null) {
                throw new InvalidAlbumParameterException();
            }
        }

        Album album = albumBuilder.build();
        albumRepository.save(album);
    }

    /**
     * 인물 앨범의 경우 회원 생성시 자동으로 추가되는데
     * 가족을 처음 생성하는 경우면 가족 생성 작업이 커밋이 아직 안되어서
     * userId로 familyId가 조회 안되기 때문에
     * 앨범을 생성하는데 필요한 familyId를 따로 받는 메서드 추가
     */
    @Transactional
    public void createPersonAlbum(PersonAlbumCreateRequest request) {
        UserInfoResponse user = userServiceFeignClient.getUser(request.getUserId()).getData();

        Album album = Album.builder().familyId(request.getFamilyId()).userId(request.getUserId())
                .albumName(user.getUserNickname() + "의 앨범").albumType(PERSON).build();
        albumRepository.save(album);
    }

    @Transactional
    public void updateAlbum(AlbumUpdateRequest albumUpdateRequest, Long albumId, Long userId) {
        Long familyId = familyServiceFeignClient.getFamilyInfo(userId).getData().getFamilyId();
        Album album = albumRepository.findById(albumId).orElseThrow(AlbumNotFoundException::new);

        if (!album.getFamilyId().equals(familyId)) {
            throw new InvalidAlbumRequestException();
        }

        album.updateAlbumName(albumUpdateRequest.getAlbumName());
    }

    @Transactional
    public void updatePersonAlbum(PersonAlbumUpdateRequest albumUpdateRequest) {
        Album album = albumRepository.findByUserIdAndAlbumType(albumUpdateRequest.getUserId(), PERSON).orElseThrow(AlbumNotFoundException::new);
        album.updateAlbumName(albumUpdateRequest.getUserNickname() + "의 앨범");
    }

    @Transactional
    public void deleteAlbum(Long albumId, Long userId) {
        Album album = albumRepository.findById(albumId).orElseThrow(AlbumNotFoundException::new);
        Long familyId = familyServiceFeignClient.getFamilyInfo(userId).getData().getFamilyId();

        if (!album.getFamilyId().equals(familyId)) {
            throw new InvalidAlbumRequestException();
        }

        albumRepository.delete(album);
        fileServiceFeignClient.deleteFiles(album.getPhotos().stream().map(Photo::getPhotoUrl).toList());
    }

    public Map<AlbumType, List<AlbumInfoResponse>> getAlbums(List<AlbumType> albumTypes, Long userId) {
        Long familyId = familyServiceFeignClient.getFamilyInfo(userId).getData().getFamilyId();
        List<Album> albums = albumQueryRepository.findByAlbumType(albumTypes, familyId);
        Map<AlbumType, List<AlbumInfoResponse>> classifiedAlbums = new HashMap<>();

        albums.forEach(album -> {
            List<AlbumInfoResponse> responseList = classifiedAlbums.computeIfAbsent(album.getAlbumType(), key -> new ArrayList<>());
            responseList.add(AlbumInfoResponse.builder()
                    .id(album.getId())
                    .albumName(album.getAlbumName())
                    .thumbnailUrl(album.getPhotos().isEmpty() ?
                            null : album.getPhotos().get(0).getPhotoUrl())
                    .photoCount(album.getPhotos().size())
                    .build());
        });

        return classifiedAlbums;
    }

    public AlbumResponse getPhotos(Long albumId, Long userId) {
        Album album = albumRepository.findById(albumId).orElseThrow(AlbumNotFoundException::new);
        Long familyId = familyServiceFeignClient.getFamilyInfo(userId).getData().getFamilyId();

        if (!album.getFamilyId().equals(familyId)) {
            throw new InvalidAlbumRequestException();
        }

        return AlbumResponse.builder().albumName(album.getAlbumName()).photos(album.getPhotos().stream().map(
                photo -> PhotoItem.builder().id(photo.getId()).photoUrl(photo.getPhotoUrl()).build()).toList()).build();
    }

    @Transactional
    public void addPhotos(Long albumId, List<MultipartFile> photos, Long userId) {
        Album album = albumRepository.findById(albumId).orElseThrow(AlbumNotFoundException::new);
        Long familyId = familyServiceFeignClient.getFamilyInfo(userId).getData().getFamilyId();

        if (!album.getFamilyId().equals(familyId)) {
            throw new InvalidAlbumRequestException();
        }

        // S3에 사진 업로드
        List<String> photoUrls = fileServiceFeignClient.uploadFiles(photos, getAlbumPhotoPath(familyId)).getData();

        // Photo Entity 변환
        List<Photo> newPhotos = photoUrls.stream().map(url -> Photo.builder().photoUrl(url).build()).toList();

        /****************** 얼굴 사진 분류 시작 ******************/

        // 가족 얼굴 사진 가져오기
        List<UserInfoResponse> familyMembers = familyServiceFeignClient.getFamilyMemberList(userId).getData();

        // 유사도 분석 (userId, similarity)
        FaceSimilarityRequest faceSimilarityRequest = FaceSimilarityRequest.builder()
                .targetImages(photoUrls).people(
                        familyMembers.stream().map(m -> new Person(m.getUserId(), m.getUserFace())).toList()
                ).build();
        List<FaceSimilarityResponse> faceSimilarityResponses = classificationServiceFeignClient.calculateSimilarity(faceSimilarityRequest).getData();

        // 가족 구성원의 앨범 가져오기 (userId, album)
        Map<Long, Album> albumMap = albumRepository.findByUserIdIn(familyMembers.stream().map(UserInfoResponse::getUserId).toList())
                .stream().collect(Collectors.toMap(Album::getUserId, a -> a));

        float threshold = 0.38f;

        // 유사도가 일정 이상 넘으면 앨범에 추가
        for (int i = 0; i < newPhotos.size(); i++) {
            Photo newPhoto = newPhotos.get(i);
            Map<Long, Double> similarities = faceSimilarityResponses.get(i).getSimilarities();

            similarities.forEach((id, score) -> {
                if (score > threshold) {
                    // 새로운 Photo 엔티티 생성
                    Photo copyPhoto = Photo.builder()
                            .photoUrl(newPhoto.getPhotoUrl())
                            .parentPhoto(newPhoto)
                            .build();

                    albumMap.get(id).addPhoto(copyPhoto);
                }
            });
        }

        /****************** 얼굴 사진 분류 끝 ******************/

        // DB에 저장
        album.addPhotos(newPhotos);
    }

    @Transactional
    public void deletePhotos(List<Long> photoIds, Long albumId, Long userId) {
        List<Photo> photos = photoRepository.findAllByIdWithAlbum(photoIds);
        Long familyId = familyServiceFeignClient.getFamilyInfo(userId).getData().getFamilyId();
        photos.forEach(photo -> {
            if (!photo.getAlbum().getFamilyId().equals(familyId) || !photo.getAlbum().getId().equals(albumId)) {
                throw new InvalidAlbumRequestException();
            }
        });
        // DB에서 삭제
        photoRepository.deleteAll(photos);
        // AWS S3에서 삭제
        fileServiceFeignClient.deleteFiles(photos.stream().map(Photo::getPhotoUrl).toList());
    }

    private String getAlbumPhotoPath(Long familyId) {
        return albumPhotoPath + "/" + familyId;
    }

    public Album getAlbumByScheduleId(Long scheduleId) {
        return albumRepository.findByScheduleId(scheduleId).orElse(null);
    }
}