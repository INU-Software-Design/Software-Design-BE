package com.neeis.neeis.domain.behavior.controller;

import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.behavior.service.BehaviorService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_BEHAVIOR;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_POST_BEHAVIOR;

@RestController
@Getter
@RequiredArgsConstructor
@RequestMapping("/behavior")
public class BehaviorController {
    private final BehaviorService behaviorService;

    @PostMapping
    @Operation(summary = "행동 / 태도 작성", description = "학생 개인의 행동, 행동피드백, 태도, 태도 피드백을 작성합니다. " )
    public ResponseEntity<CommonResponse<BehaviorResponseDto>> postBehavior(@RequestParam Long studentId, @RequestBody BehaviorRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_BEHAVIOR.getMessage(), behaviorService.postBehavior(studentId, requestDto)));
    }
}
