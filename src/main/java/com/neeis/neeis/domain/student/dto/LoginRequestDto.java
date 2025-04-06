package com.neeis.neeis.domain.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "학생 로그인 요청 DTO")
public class LoginRequestDto {

    @Schema(description = "로그인 아이디", example = "학생아이디")
    private String loginId;

    @Schema(description = "비밀번호", example = "비밀번호")
    private String password;

    @Builder
    public LoginRequestDto(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }



}
