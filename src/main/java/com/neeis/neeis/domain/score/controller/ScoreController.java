package com.neeis.neeis.domain.score.controller;

import com.neeis.neeis.domain.score.dto.req.ScoreRequestDto;
import com.neeis.neeis.domain.score.dto.res.ScoreSummaryBySubjectDto;
import com.neeis.neeis.domain.score.service.ScoreService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_SCORE;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_POST_SCORE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scores")
public class ScoreController {
    private final ScoreService scoreService;

    @PostMapping
    @Operation(
            summary = "[교사 전용] 학생 성적 저장/수정",
            description = """
            반 전체 학생의 특정 평가 점수를 한 번에 저장하거나 수정합니다. <br><br>
            - 이미 점수가 입력되어 있으면 → 수정 <br>
            - 없으면 → 새로 저장 <br><br>
            <b>요청 예시:</b><br>
            [
              {
                "classNum": 2,
                "evaluationId": 1,
                "students": [
                  { "number": 1, "rawScore": 93.0 },
                  { "number": 2, "rawScore": 82.0 }
                ]
              }
            ]
            """
    )public ResponseEntity<CommonResponse<Object>> registerScores(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody List<ScoreRequestDto> dtoList) {

        scoreService.saveOrUpdateScores(userDetails.getUsername(), dtoList);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_SCORE.getMessage()));
    }

    @GetMapping("/summary")
    @Operation(
            summary = "특정 반의 과목별 평가 방식 및 점수 조회",
            description = """
        입력한 연도/학기/학년/반 정보에 따라, 각 과목의 평가 방식과 학생들의 점수를 반환합니다. <br><br>
        <b>선택적 필터링:</b><br>
        <code>subject</code> 파라미터를 입력하면, 해당 과목만 조회됩니다. <br>
        입력하지 않으면 해당 반에서 평가 방식이 등록된 모든 과목이 조회됩니다.
        """)
    public ResponseEntity<CommonResponse<List<ScoreSummaryBySubjectDto>>> getScoreSummaryBySubject(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int semester,
            @RequestParam int grade,
            @RequestParam int classNum,
            @RequestParam(required = false) String subject
    ) {
        List<ScoreSummaryBySubjectDto> result = scoreService.getScoreSummaryBySubject(userDetails.getUsername(),year, semester, grade, classNum, subject);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_SCORE.getMessage(), result));
    }
}
