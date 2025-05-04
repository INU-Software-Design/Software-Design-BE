package com.neeis.neeis.domain.evaluationMethod.controller;

import com.neeis.neeis.domain.evaluationMethod.dto.req.CreateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.req.UpdateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.res.EvaluationMethodResponseDto;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.dto.res.SubjectResponseDto;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/evaluation-methods")
public class EvaluationMethodController {

    private final EvaluationMethodService evaluationMethodService;

    @PostMapping
    @Operation(
            summary = "[교사 전용] 평가 방식 생성",
            description = """
                    특정 과목의 평가 방식을 등록합니다. <br>
                    동일 과목/연도/학기/학년/시험유형/고사명에 대해 중복 등록은 불가합니다. <br><br>
                    <b>ExamType (시험 유형)은 다음 중 하나를 입력해야 합니다:</b><br>
                    - <code>WRITTEN</code> : 자필 <br>
                    - <code>PRACTICAL</code> : 수행 <br><br>
                    예시: 2025년 1학기 1학년 국어 자필 기말고사, 20, 만점 100.00 <br><br>
                    <b>※ 반영 비율은 숫자로 입력해주세요 (예: 20% → 20)</b>
                    """)
    public ResponseEntity<CommonResponse<Object>> createEvaluationMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateEvaluationMethodDto dto) {

        evaluationMethodService.save(userDetails.getUsername(), dto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_EVALUATION_METHOD.getMessage()));
    }

    @GetMapping
    @Operation(
            summary = "평가 방식 조회",
            description = "특정 연도/학기/학년/과목의 평가 방식 목록을 조회합니다." )
    public ResponseEntity<CommonResponse<List<EvaluationMethodResponseDto>>> getEvaluationMethods(
            @RequestParam int year, @RequestParam int semester,
            @RequestParam int grade, @RequestParam String subject) {

        List<EvaluationMethodResponseDto> result =
                evaluationMethodService.getEvaluationMethods(subject, year, semester, grade);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_EVALUATION_METHOD.getMessage(), result));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "[교사 전용] 평가 방식 수정",
            description = """
                평가 방식 ID에 해당하는 데이터를 수정합니다. <br>
                시험 유형, 고사명, 반영 비율, 만점 등을 변경할 수 있습니다. <br><br>
                <b>ExamType (시험 유형)은 다음 중 하나를 입력해야 합니다:</b><br>
                - <code>WRITTEN</code> : 자필 <br>
                - <code>PRACTICAL</code> : 수행 <br><br>
                <b> ※ 반영 비율은 숫자로 입력해주세요 (예: 20% → 20)</b>
                """ )
    public ResponseEntity<CommonResponse<Object>> updateEvaluationMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationMethodDto dto) {
        evaluationMethodService.update(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_UPDATE_EVALUATION_METHOD.getMessage()));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "[교사 전용] 평가 방식 삭제",
            description = "평가 방식 ID에 해당하는 데이터를 삭제합니다."
    )
    public ResponseEntity<CommonResponse<Object>> deleteEvaluationMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        evaluationMethodService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
