package com.familring.userservice.service.client;

import com.familring.common_module.dto.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "family-service")
public interface FamilyServiceFeignClient {
    @PutMapping("/client/family/member")
    BaseResponse<String> deleteFamilyMember(@RequestParam Long userId);
}
