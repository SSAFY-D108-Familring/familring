package com.familring.userservice.controller;

import com.familring.userservice.model.dto.request.UserJoinRequest;
import com.familring.userservice.model.dto.request.UserLoginRequest;
import com.familring.userservice.model.dto.response.JwtTokenResponse;
import com.familring.userservice.model.dto.response.UserInfoResponse;
import com.familring.userservice.service.CustomUserDetailsService;
import com.familring.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    @Operation(summary = "회원 정보 조회", description = "Header의 토큰을 사용해 회원의 정보를 조회")
    public ResponseEntity getUser(Authentication authentication) {
        UserInfoResponse response = userService.getUser(authentication.getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "카카오톡 소셜 로그인", description = "로그인 시 회원가입 여부 확인 후 회원가입")
    public ResponseEntity login(@RequestBody UserLoginRequest userLogInRequest) {
        JwtTokenResponse tokens = userService.login(userLogInRequest);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping(value = "/join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입", description = "회원가입이 되어 있지 않은 사용자 회원가입 처리 후 JWT 발급")
    public ResponseEntity join
            (@RequestPart("userJoinRequest") UserJoinRequest userLogInRequest,
             @RequestPart(value = "image", required = false) MultipartFile image) {
        JwtTokenResponse tokens = userService.join(userLogInRequest, image);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/jwt")
    @Operation(summary = "JWT 재발급", description = "유효기간 만료로 인한 JWT 토큰 재발급")
    public ResponseEntity<JwtTokenResponse> updateJWT(@RequestHeader("Authorization") String authorizationHeader) {
        // "Bearer " 문자열을 제거하고 refreshToken만 추출
        String refreshToken = authorizationHeader.replace("Bearer ", "");

        JwtTokenResponse tokens = userService.updateJWT(refreshToken);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/fcm")
    @Operation(summary = "발급된 FCM 토큰 저장", description = "Android에서 발급한 FCM 토큰 저장")
    public ResponseEntity updateFcmToken
            (Authentication authentication, @RequestParam String fcmToken) {
        String response = userService.updateFcmToken(authentication.getName(), fcmToken);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "Header의 토큰을 사용해 회원을 탈퇴 처리")
    public ResponseEntity deleteUser(Authentication authentication) {
        String response = userService.deleteUser(authentication.getName());

        return ResponseEntity.ok(response);
    }
}