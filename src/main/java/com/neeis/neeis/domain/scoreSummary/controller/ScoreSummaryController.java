package com.neeis.neeis.domain.scoreSummary.controller;

import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackUpdateDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.ScoreFeedbackDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackRequestDto;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.neeis.neeis.global.common.StatusCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/score-summary")
public class ScoreSummaryController {
    private final ScoreSummaryService scoreSummaryService;

    @Operation(summary = "[교사 및 학생 전용] 학생 성적 학적 조회",
            description = """
            출석번호 기준 단일 학생의 과목별 성적 요약을 조회합니다.
            
            - 학생은 자신의 성적 조회만 가능합니다.
            - 교사는 담임 및 타 반 학생 성적 조회도 가능합니다.
            """)
    @GetMapping
    public ResponseEntity<CommonResponse<StudentScoreSummaryDto>> getStudentScoreSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "조회 연도", example = "2025") @RequestParam int year,
            @Parameter(description = "학기 (1 또는 2)", example = "1") @RequestParam int semester,
            @Parameter(description = "학년", example = "1") @RequestParam int grade,
            @Parameter(description = "반", example = "1") @RequestParam int classNum,
            @Parameter(description = "출석번호", example = "6") @RequestParam int number
    ) {

        StudentScoreSummaryDto dto = scoreSummaryService.getStudentSummary(userDetails.getUsername(), year, semester, grade, classNum, number);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_SCORE.getMessage(), dto));
    }

    @PostMapping("/feedback")
    @Operation(summary = "[교사 전용] 성적 피드백 등록", description =
            """
            특정 성적 요약(ScoreSummary)에 대해 피드백을 작성합니다.
            - `scoreSummaryId`는 성적 통계 ID입니다.
            """)
    public ResponseEntity<CommonResponse<Object>> saveFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ScoreFeedbackRequestDto requestDto) {
        scoreSummaryService.saveFeedback(userDetails.getUsername(), requestDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_FEEDBACK.getMessage()));
    }

    @PostMapping("/feedback/{score-summary-id}")
    @Operation(summary = "[교사 전용] 성적 피드백 수정", description =
            """
            특정 성적 요약에 대해 기존 피드백 내용을 수정합니다.
            - `score-summary-id`는 피드백이 연결된 성적 요약의 ID입니다.
            - 요청 본문에는 수정할 피드백 내용이 포함됩니다.
            """)
    public ResponseEntity<CommonResponse<Object>> updateFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("score-summary-id") Long scoreSummaryId,
            @Valid @RequestBody ScoreFeedbackUpdateDto requestDto) {
        scoreSummaryService.updateFeedback(userDetails.getUsername(), scoreSummaryId, requestDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_FEEDBACK.getMessage()));
    }

    @GetMapping("/feedback/{score-summary-id}")
    @Operation(summary = "[교사 및 학생 전용] 성적 피드백 조회", description =
            """
            특정 성적 요약에 작성된 피드백을 조회합니다.
            - `score-summary-id`는 성적 요약 ID입니다.
            """)
    public ResponseEntity<CommonResponse<ScoreFeedbackDto>> getFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("score-summary-id") Long scoreSummaryId){
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_FEEDBACK.getMessage(), scoreSummaryService.getFeedback(userDetails.getUsername(), scoreSummaryId)));
    }


}
