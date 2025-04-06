package com.neeis.neeis.domain.student.controller;

import com.neeis.neeis.domain.student.dto.LoginRequestDto;
import com.neeis.neeis.domain.student.dto.StudentResponseDto;
import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.LoginRequestDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_FIND_USERNAME;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_LOGIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/login")
    @Operation(summary = "학생 로그인", description = "학생아이디는 학생에게 주어진 고유 ID입니다. \n" +
            "초기 비밀번호는 생년월일이며, 추후 변경 가능합니다. ")
    public ResponseEntity<StudentResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(studentService.login(loginRequestDto));
    public ResponseEntity<CommonResponse<StudentResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_LOGIN.getMessage(),studentService.login(loginRequestDto)));
    }

    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기")
    public ResponseEntity<CommonResponse<StudentResponseDto>> findUsername(@RequestBody FindIdRequestDto findIdRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_FIND_USERNAME.getMessage(), studentService.findUsername(findIdRequestDto)));
    }
}
