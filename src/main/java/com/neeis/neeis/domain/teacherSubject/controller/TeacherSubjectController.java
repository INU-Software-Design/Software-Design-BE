package com.neeis.neeis.domain.teacherSubject.controller;

import com.neeis.neeis.domain.teacherSubject.dto.req.CreateTeacherSubjectDto;
import com.neeis.neeis.domain.teacherSubject.dto.res.TeacherSubjectResponseDto;
import com.neeis.neeis.domain.teacherSubject.service.TeacherSubjectService;
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
@RequestMapping("/teacherSubjects")
public class TeacherSubjectController {
    private final TeacherSubjectService teacherSubjectService;

    @PostMapping
    @Operation(
            summary = "[관리자 및 교사 전용] 교사-과목 배정 추가",
            description = "특정 교사와 과목 간의 연결을 생성합니다. <br>" +
                    "동일한 교사-과목 연결은 중복 등록이 불가합니다.")
    public ResponseEntity<CommonResponse<Object>> save(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTeacherSubjectDto createTeacherSubjectDto ){
        teacherSubjectService.save(userDetails.getUsername(), createTeacherSubjectDto);

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_TEACHER_SUBJECT.getMessage()));
    }

    @GetMapping
    @Operation(
            summary = "교사-과목 연결 전체 조회",
            description = "등록된 모든 교사-과목 배정 목록을 조회합니다. <br>" +
                    "해당 API는 인증 없이 접근 가능합니다.")
    public ResponseEntity<CommonResponse<List<TeacherSubjectResponseDto>>> getAlls(){
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_TEACHER_SUBJECT.getMessage(), teacherSubjectService.getTeacherSubjects()));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "[관리자 및 교사 전용] 교사-과목 연결 수정",
            description = "ID에 해당하는 교사-과목 연결 정보를 수정합니다. <br>" +
                    "교사 이름 및 과목 이름을 모두 변경할 수 있습니다.")
    public ResponseEntity<CommonResponse<Object>> update(@AuthenticationPrincipal UserDetails userDetails,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody CreateTeacherSubjectDto createTeacherSubjectDto ){
        teacherSubjectService.update(userDetails.getUsername(), id, createTeacherSubjectDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_UPDATE_TEACHER_SUBJECT.getMessage()));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "[관리자 및 교사 전용] 교사-과목 연결 삭제",
            description = "ID에 해당하는 교사-과목 연결 정보를 삭제합니다."
    )
    public ResponseEntity<CommonResponse<Object>> delete(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id ){
        teacherSubjectService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

}
