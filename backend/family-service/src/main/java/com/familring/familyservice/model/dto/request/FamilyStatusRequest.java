package com.familring.familyservice.model.dto.request;

import lombok.Data;

@Data
public class FamilyStatusRequest {
    private Long familyId;
    private int amount;
}