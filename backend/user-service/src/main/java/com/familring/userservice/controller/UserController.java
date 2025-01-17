package com.familring.userservice.controller;

import com.familring.common_module.dto.BaseResponse;
import com.familring.userservice.model.dto.request.FileUploadRequest;
import com.familring.userservice.model.dto.request.UserEmotionRequest;
import com.familring.userservice.model.dto.request.UserJoinRequest;
import com.familring.userservice.model.dto.request.UserLoginRequest;
import com.familring.userservice.model.dto.response.JwtTokenResponse;
import com.familring.userservice.model.dto.response.UserInfoResponse;
import com.familring.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "회원 컨트롤러", description = "회원관련 기능 수행")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "회원 정보 조회", description = "Header의 토큰을 사용해 요청 회원의 정보만을 조회")
    public ResponseEntity<BaseResponse<UserInfoResponse>> getUser(@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId) {
        log.info("userId: {}", userId);
        UserInfoResponse response = userService.getUser(userId);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "회원 정보를 성공적으로 조회했습니다.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "카카오톡 소셜 로그인", description = "로그인 시 회원가입 여부 확인 후 회원가입")
    public ResponseEntity<BaseResponse<JwtTokenResponse>> login(@RequestBody UserLoginRequest userLogInRequest) {
        JwtTokenResponse tokens = userService.login(userLogInRequest);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "정상적으로 로그인되었습니다.", tokens));
    }

    @PostMapping(value = "/join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입", description = "회원가입이 되어 있지 않은 사용자 회원가입 처리 후 JWT 발급")
    public ResponseEntity<BaseResponse<JwtTokenResponse>> join
            (@RequestPart("userJoinRequest") UserJoinRequest userJoinRequest,
             @RequestPart(value = "image", required = false) MultipartFile image) {
        JwtTokenResponse tokens = userService.join(userJoinRequest, image);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "정상적으로 회원가입 되었습니다.", tokens));
    }

    @PostMapping("/jwt")
    @Operation(summary = "JWT 재발급", description = "유효기간 만료로 인한 JWT 토큰 재발급")
    public ResponseEntity<BaseResponse<JwtTokenResponse>> updateJWT(@RequestParam("refreshToken") String refreshToken) {
        JwtTokenResponse tokens = userService.updateJWT(refreshToken);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "토큰이 정상적으로 재발급되었습니다.", tokens));
    }

    @PostMapping("/fcm")
    @Operation(summary = "발급된 FCM 토큰 저장", description = "Android에서 발급한 FCM 토큰 저장")
    public ResponseEntity<BaseResponse<Void>> updateFcmToken
            (@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId,
             @RequestParam String fcmToken) {
        userService.updateFcmToken(userId, fcmToken);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "FCM 토큰이 정상적으로 저장되었습니다."));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Header의 토큰을 사용해 요청 회원을 로그아웃 처리")
    public ResponseEntity<BaseResponse<Void>> logout(@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId) {
        userService.logout(userId);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "회원이 성공적으로 로그아웃되었습니다."));
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "음성 파일 업로드", description = "음성 파일을 S3에 저장한 후 url 반환")
    public ResponseEntity<BaseResponse<String>> uploadVoiceFile
            (@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId,
             @RequestPart("fileUploadRequest") FileUploadRequest fileUploadRequest,
             @RequestPart(value = "voice", required = false) MultipartFile voice) {
        String responseUrl = userService.uploadVoiceFile(userId, fileUploadRequest, voice);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "음성 파일을 S3에 성공적으로 업로드했습니다.", responseUrl));
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사진 파일 업로드", description = "음성 파일을 S3에 저장한 후 url 반환")
    public ResponseEntity<BaseResponse<String>> uploadPhotoFile
            (@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId,
             @RequestPart("fileUploadRequest") FileUploadRequest fileUploadRequest,
             @RequestPart(value = "photo", required = false) MultipartFile photo) {
        String responseUrl = userService.uploadPhotoFile(userId, fileUploadRequest, photo);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "음성 파일을 S3에 성공적으로 업로드했습니다.", responseUrl));
    }

    @PatchMapping("/emotion")
    @Operation(summary = "회원 기분 수정", description = "userId에 해당하는 회원 기분 수정")
    public ResponseEntity<BaseResponse<Void>> updateUserEmotion
            (@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId,
             @RequestBody UserEmotionRequest userEmotionRequest) {
        userService.updateUserEmotion(userId, userEmotionRequest);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "회원 기분 변경에 성공했습니다."));
    }

    @PatchMapping("/nickname")
    @Operation(summary = "회원 닉네임 수정", description = "userId에 해당하는 회원 닉네임 수정")
    public ResponseEntity<BaseResponse<Void>> updateUserEmotion
            (@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId,
            @RequestParam("userNickname")String userNickname) {
        userService.updateUserNickname(userId, userNickname);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "회원 닉네임 변경에 성공했습니다."));
    }

    @PatchMapping("/color")
    @Operation(summary = "회원 프로필 배경색 수정", description = "userId에 해당하는 회원 프로필 배경색 수정")
    public ResponseEntity<BaseResponse<Void>> updateUserColor
            (@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId,
             @RequestParam("userColor")String userColor) {
        userService.updateUserColor(userId, userColor);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "회원 프로필 배경색 변경에 성공했습니다."));
    }

    @PatchMapping(value="/face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원 얼굴 수정", description = "userId에 해당하는 얼굴 사진 수정")
    public ResponseEntity<BaseResponse<Void>> updateUserFace
            (@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId,
             @RequestPart(value = "image", required = false) MultipartFile image) {
        userService.updateFace(userId, image);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "회원 얼굴 변경에 성공했습니다."));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "Header의 토큰을 사용해 회원을 탈퇴 처리")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId) {
        userService.deleteUser(userId);

        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "정상적으로 회원 탈퇴되었습니다."));
    }
}
