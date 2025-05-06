package com.neeis.neeis.domain.teacher.controller;

import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.teacher.dto.ClassroomStudentDto;
import com.neeis.neeis.domain.teacher.dto.TeacherResponseDto;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_STUDENTS;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;


    @GetMapping("/students")
    @Operation(summary = "교사 학생 조회", description = """
        담임 교사의 담당 반 또는 특정 학급의 학생 출석번호와 이름을 조회합니다. <br>
        year는 생략 시 올해 기준입니다. <br>
        grade와 classNum이 모두 있으면 해당 반 조회, 없으면 담임 반 기준으로 조회됩니다.
        """)
    public ResponseEntity<CommonResponse<ClassroomStudentDto>> getStudents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "year", required = false) @Parameter(description = "연도") Integer year,
            @RequestParam(value = "grade", required = false) @Parameter(description = "학년") Integer grade,
            @RequestParam(value = "classNum", required = false) @Parameter(description = "반") Integer classNum){
        int resolvedYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_STUDENTS.getMessage(), teacherService.getStudentsFlexible(userDetails.getUsername(), resolvedYear, grade, classNum)));
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "학생 개별 조회", description = "학생의 학적 조회를 합니다.<br> " +
            "메인페이지에 나와있는 학생의 정보를 제공합니다. <br>" +
            "학적 이미지 조회시 `기본도메인(BASE_URL)/images/{파일명}`으로 조회가능합니다.")
    public ResponseEntity<CommonResponse<StudentDetailResDto>> getStudentDetails(@AuthenticationPrincipal UserDetails userDetails,
                                                                                 @PathVariable Long studentId,
                                                                                 @RequestParam(value = "year", required = false) @Parameter(description = "연도") Integer year) {
        int resolvedYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_STUDENTS.getMessage(),teacherService.getStudentDetail(userDetails.getUsername(),studentId, resolvedYear)));
    }

    @GetMapping
    @Operation(summary = "교사 개인 정보 조회", description = "로그인한 교사의 정보를 반환합니다.")
    public ResponseEntity<CommonResponse<TeacherResponseDto>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(CommonResponse.from("교사 정보 조회 성공", teacherService.getProfile(userDetails.getUsername())));
    }

}
