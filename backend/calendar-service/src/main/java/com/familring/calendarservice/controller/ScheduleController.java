package com.familring.calendarservice.controller;

import com.familring.calendarservice.dto.request.ScheduleRequest;
import com.familring.calendarservice.dto.request.ScheduleUpdateRequest;
import com.familring.calendarservice.dto.response.DailyDateResponse;
import com.familring.calendarservice.dto.response.MonthDailyScheduleResponse;
import com.familring.calendarservice.dto.response.ScheduleDateResponse;
import com.familring.calendarservice.dto.response.ScheduleResponse;
import com.familring.calendarservice.service.ScheduleService;
import com.familring.common_module.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calendars/schedules")
@RequiredArgsConstructor
@Tag(name = "일정 컨트롤러", description = "일정 관리")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping()
    @Operation(summary = "다중 일정 조회", description = "일정들의 세부 정보를 조회합니다.")
    public ResponseEntity<BaseResponse<List<ScheduleResponse>>> getSchedules(
            @RequestParam("schedule_id") List<Long> scheduleIds,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId
    ) {
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(),
                "일정 정보를 조회했습니다.", scheduleService.getSchedules(scheduleIds, userId)));
    }

    @PostMapping()
    @Operation(summary = "일정 생성", description = "새로운 일정을 추가합니다.")
    public ResponseEntity<BaseResponse<Void>> createSchedule(
            @RequestBody ScheduleRequest scheduleRequest,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId) {
        scheduleService.createSchedule(scheduleRequest, userId);
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "일정을 성공적으로 생성했습니다."));
    }

    @DeleteMapping("/{schedule_id}")
    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다.")
    public ResponseEntity<BaseResponse<Void>> deleteSchedule(
            @PathVariable("schedule_id") Long scheduleId,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId
    ) {
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "일정을 성공적으로 삭제했습니다."));
    }

    @PatchMapping("/{schedule_id}")
    @Operation(summary = "일정 수정", description = "일정을 수정합니다.")
    public ResponseEntity<BaseResponse<Void>> updateSchedule(
            @RequestBody ScheduleUpdateRequest scheduleUpdateRequest,
            @PathVariable("schedule_id") Long scheduleId,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId) {
        scheduleService.updateSchedule(scheduleId, scheduleUpdateRequest, userId);
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "일정을 성공적으로 수정했습니다."));
    }

    @GetMapping("/date")
    @Operation(summary = "날짜별 일정 조회", description = "입력받은 년, 월, 일에 해당하는 일정을 반환합니다.")
    public ResponseEntity<BaseResponse<List<ScheduleResponse>>>  getSchedulesByDate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId
    ) {
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(),
                year + "년 " + month + "월 " + day + "일의 일정을 모두 조회했습니다.", scheduleService.getSchedulesByDate(year, month, day, userId)));
    }
}
