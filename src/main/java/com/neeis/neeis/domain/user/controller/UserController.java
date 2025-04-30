package com.neeis.neeis.domain.user.controller;

import com.neeis.neeis.domain.student.dto.res.TokenResponseDto;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.user.dto.UpdatePasswordRequestDto;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_LOGIN;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_UPDATE_PASSWORD;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "통합 로그인", description = "아이디는 유저에게 주어진 고유 ID 입니다. (변경불가능) <br>" +
            "초기 비밀번호는 핸드폰 뒷자리 4자리이며, 추후 변경 가능합니다. <br>" +
            "로그인은 통합로그인입니다. 권한에 따라 API 접근이 제한됩니다. <br> ")
    public ResponseEntity<CommonResponse<TokenResponseDto>> login( @Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_LOGIN.getMessage(),userService.login(loginRequestDto)));
    }

    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경", description = """
        사용자의 기존 비밀번호를 확인한 후 새 비밀번호로 변경합니다.
        <br><br>
        비밀번호 정책은 다음과 같습니다:
        <ul>
            <li>8자 이상</li>
            <li>영문 대문자, 소문자 포함</li>
            <li>숫자 또는 특수문자 중 최소 하나 포함</li>
        </ul>
        <br>
        잘못된 기존 비밀번호를 입력하거나, 새 비밀번호가 정책에 맞지 않는 경우 예외가 발생합니다.
        """)
    public ResponseEntity<CommonResponse<Object>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto) {
        userService.updatePassword(updatePasswordRequestDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_UPDATE_PASSWORD.getMessage()));
    }
}
