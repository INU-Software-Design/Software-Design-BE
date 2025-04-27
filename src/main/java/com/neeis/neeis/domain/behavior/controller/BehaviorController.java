package com.neeis.neeis.domain.behavior.controller;

import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.behavior.service.BehaviorService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
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
    @Operation(summary = "행동특성 / 종합의견 작성", description = "학생 개인의 행동특성 및 종합의견을 작성합니다." +
            "studentId는 학생 고유 번호입니다. ")
    public ResponseEntity<CommonResponse<BehaviorResponseDto>> postBehavior( @AuthenticationPrincipal UserDetails userDetails,
                                                                             @RequestParam(value = "year", defaultValue = "2025") @Parameter(description = "연도") Integer year,
                                                                             @RequestParam("grade") @Parameter(description = "학년") Integer grade,
                                                                             @RequestParam("classNum") @Parameter(description = "반") Integer classNum,
                                                                             @RequestParam("studentId") @Parameter(description = "학생 ID") Long studentId,
                                                                             @Valid @RequestBody BehaviorRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_BEHAVIOR.getMessage(), behaviorService.createBehavior(userDetails.getUsername(), year,grade,classNum, studentId, requestDto)));
    }

    @GetMapping
    @Operation(summary = "행동 / 태도 조회", description = "학생 개인의 행동특성 및 종합의견을 조회합니다. ")
    public ResponseEntity<CommonResponse<BehaviorDetailResponseDto>> getBehavior( @AuthenticationPrincipal UserDetails userDetails,
                                                                                  @RequestParam(value = "year", defaultValue = "2025") @Parameter(description = "연도") Integer year,
                                                                                  @RequestParam("grade") @Parameter(description = "학년") Integer grade,
                                                                                  @RequestParam("classNum") @Parameter(description = "반") Integer classNum,
                                                                                  @RequestParam("studentId") @Parameter(description = "학생 ID") Long studentId) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_BEHAVIOR.getMessage(), behaviorService.getBehavior(userDetails.getUsername(), year, grade, classNum, studentId)));
    }

    @PutMapping("/{behaviorId}")
    @Operation(summary = "행동 / 태도 정보 수정", description = "조회를 통해 응답받은 behaviorId 를 이용하세요. " +
            "빈 값이면 안됩니다. ")
    public ResponseEntity<CommonResponse<BehaviorDetailResponseDto>> updateBehavior (@AuthenticationPrincipal UserDetails userDetails,
                                                                                     @PathVariable Long behaviorId,
                                                                                     @Valid  @RequestBody BehaviorRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_BEHAVIOR.getMessage(), behaviorService.updateBehavior(userDetails.getUsername(), behaviorId, requestDto)));
    }


}
