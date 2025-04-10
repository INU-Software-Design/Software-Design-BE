package com.neeis.neeis.domain.user.controller;

import com.neeis.neeis.domain.student.dto.res.TokenResponseDto;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_LOGIN;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "통합 로그인", description = "아이디는 유저에게 주어진 고유 ID입니다. \n" +
            "초기 비밀번호는 생년월일이며, 추후 변경 가능합니다." +
            "권한에 따라, 접근이 제한되게 해놨으니 로그인 통일시켰습니다. ")
    public ResponseEntity<CommonResponse<TokenResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_LOGIN.getMessage(),userService.login(loginRequestDto)));
    }
}
