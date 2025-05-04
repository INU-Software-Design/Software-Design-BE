package com.neeis.neeis.domain.subject.controller;

import com.neeis.neeis.domain.subject.dto.req.CreateSubjectRequestDto;
import com.neeis.neeis.domain.subject.dto.res.SubjectResponseDto;
import com.neeis.neeis.domain.subject.service.SubjectService;
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
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping
    @Operation(
            summary = "[관리자 및 교사 전용] 과목 생성",
            description = "새로운 과목을 생성합니다. <br>" +
                    "과목명 중복은 허용되지 않습니다."
    )
    public ResponseEntity<CommonResponse<Object>> createSubject(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateSubjectRequestDto createSubjectRequestDto) {
        subjectService.createSubject(userDetails.getUsername(), createSubjectRequestDto);

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_SUBJECT.getMessage()));
    }

    @GetMapping
    @Operation(
            summary = "과목 전체 조회",
            description = "등록된 모든 과목을 이름 오름차순으로 조회합니다."
    )
    public ResponseEntity<CommonResponse<List<SubjectResponseDto>>> getSubjects() {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_SUBJECT.getMessage(), subjectService.getSubjects()));
    }

    @PutMapping("/{subjectId}")
    @Operation(
            summary = "[관리자 및 교사 전용] 과목 수정",
            description = "과목 ID에 해당하는 과목의 이름을 수정합니다."
    )
    public ResponseEntity<CommonResponse<Object>> updateSubject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateSubjectRequestDto dto) {
        subjectService.updateSubject(userDetails.getUsername(), subjectId, dto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_UPDATE_SUBJECT.getMessage()));
    }

    @DeleteMapping("/{subjectId}")
    @Operation(
            summary = "[관리자 및 교사 전용] 과목 삭제",
            description = "과목 ID에 해당하는 과목을 삭제합니다."
    )
    public ResponseEntity<CommonResponse<Object>> deleteSubject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long subjectId) {
        subjectService.deleteSubject(userDetails.getUsername(), subjectId);
        return ResponseEntity.noContent().build();
    }

}
