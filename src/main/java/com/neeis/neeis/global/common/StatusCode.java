package com.neeis.neeis.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StatusCode {

    SUCCESS_LOGIN(HttpStatus.OK, "Common-201", "로그인에 성공하였습니다."),
    SUCCESS_FIND_USERNAME(HttpStatus.OK, "Common-201", "아이디 찾기에 성공하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
