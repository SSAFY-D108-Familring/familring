package com.familring.questionservice.controller;

import com.familring.common_module.dto.BaseResponse;
import com.familring.questionservice.dto.request.QuestionAnswerCreateRequest;
import com.familring.questionservice.dto.request.QuestionAnswerUpdateRequest;
import com.familring.questionservice.dto.response.QuestionTodayResponse;
import com.familring.questionservice.dto.response.QuestionListResponse;
import com.familring.questionservice.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/answers")
    @Operation(summary = "랜덤 질문 답변 작성", description = "랜덤 질문 답변 작성 (내용만 입력)")
    public ResponseEntity<BaseResponse<Void>> createQuestionAnswer(@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId, @RequestBody QuestionAnswerCreateRequest questionAnswerCreateRequest) {
        questionService.createQuestionAnswer(userId, questionAnswerCreateRequest);
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "랜덤 질문 답변 작성에 성공했습니다."));
    }

    @PatchMapping("/answers/{answer-id}")
    @Operation(summary = "랜덤 질문 답변 수정", description = "랜덤 질문 답변 수정 (질문 ID 값 전달)")
    public ResponseEntity<BaseResponse<Void>> createTimeCapsule(@PathVariable("answer-id") Long answerId, @RequestBody QuestionAnswerUpdateRequest questionAnswerUpdateRequest) {
        questionService.updateQuestionAnswer(answerId, questionAnswerUpdateRequest);
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "랜덤 질문 답변 수정에 성공했습니다."));
    }

    @GetMapping()
    @Operation(summary = "오늘의 랜덤 질문 조회", description = "랜덤 질문 작성자 목록까지 같이 가요")
    public ResponseEntity<BaseResponse<QuestionTodayResponse>> getQuestionAnswers(@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId) {
        QuestionTodayResponse response = questionService.getQuestionToday(userId);
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "오늘의 랜덤 질문 조회에 성공했습니다.", response));
    }

    @GetMapping("/all")
    @Operation(summary = "랜덤 질문 전체 목록 조회", description = "지금까지 작성했던 모든 랜덤 질문 조회")
    public ResponseEntity<BaseResponse<QuestionListResponse>> getAllQuestions(@Parameter(hidden = true) @RequestHeader("X-User-ID") Long userId, @RequestParam int pageNo, @RequestParam String order) {
        QuestionListResponse response = questionService.getAllQuestions(userId, pageNo, order);
        return ResponseEntity.ok(BaseResponse.create(HttpStatus.OK.value(), "작성했던 모든 랜덤 질문 조회에 성공했습니다.", response));
    }

}
