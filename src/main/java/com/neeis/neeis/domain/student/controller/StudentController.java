package com.neeis.neeis.domain.student.controller;

import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.dto.res.PasswordResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.student.dto.res.TokenResponseDto;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_USERNAME;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_LOGIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/id")
    @Operation(summary = "아이디 찾기")
    public ResponseEntity<CommonResponse<StudentResponseDto>> findUsername(@RequestBody FindIdRequestDto findIdRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_USERNAME.getMessage(), studentService.findUsername(findIdRequestDto)));
    }

    @PostMapping("/password")
    @Operation(summary = "비밀번호 찾기")
    public ResponseEntity<CommonResponse<PasswordResponseDto>> findPassword(@RequestBody PasswordRequestDto passwordRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_USERNAME.getMessage(), studentService.findPassword(passwordRequestDto)));
    }

}
