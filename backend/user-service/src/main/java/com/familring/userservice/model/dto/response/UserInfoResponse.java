package com.familring.userservice.model.dto.response;

import com.familring.userservice.model.dto.FamilyRole;
import lombok.Data;

import java.util.Date;

@Data
public class UserInfoResponse {
    private Long userId;
    private String userKakaoId;
    private String userNickname;
    private Date userBirthDate;
    private String userZodiacSign;
    private FamilyRole userRole;
    private String userFace;
    private String userColor;
    private String userEmotion;
}