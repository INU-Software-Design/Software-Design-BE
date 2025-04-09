package com.neeis.neeis.domain.teacher.controller;

import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.teacher.dto.StudentResponseDto;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_STUDENTS;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/students")
    public ResponseEntity<CommonResponse<List<StudentResponseDto>>> getStudents(@AuthenticationPrincipal UserDetails userDetails) {
        List<StudentResponseDto> studentResponseDtoList = teacherService.getStudents(userDetails.getUsername());

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_STUDENTS.getMessage(), studentResponseDtoList));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<CommonResponse<StudentDetailResDto>> getStudentDetails(@AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long studentId){
            return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_STUDENTS.getMessage(),teacherService.getStudentDetail(studentId)));
    }

}
