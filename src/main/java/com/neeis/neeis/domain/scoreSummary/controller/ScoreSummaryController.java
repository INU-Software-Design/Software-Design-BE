package com.neeis.neeis.domain.scoreSummary.controller;

import com.neeis.neeis.domain.scoreSummary.dto.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_SCORE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/score-summary")
public class ScoreSummaryController {
    private final ScoreSummaryService scoreSummaryService;

    @Operation(summary = "학생 성적 학적 조회",
            description = "출석번호 기준 단일 학생의 과목별 성적 요약을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<StudentScoreSummaryDto>> getStudentScoreSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "조회 연도", example = "2025") @RequestParam int year,
            @Parameter(description = "학기 (1 또는 2)", example = "1") @RequestParam int semester,
            @Parameter(description = "학년", example = "3") @RequestParam int grade,
            @Parameter(description = "반", example = "2") @RequestParam int classNum,
            @Parameter(description = "출석번호", example = "12") @RequestParam int number
    ) {

        StudentScoreSummaryDto dto = scoreSummaryService.getStudentSummary(userDetails.getUsername(), year, semester, grade, classNum, number);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_SCORE.getMessage(), dto));
    }
}
