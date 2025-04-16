package com.neeis.neeis.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StatusCode {

    SUCCESS_LOGIN(HttpStatus.OK, "Common-201", "로그인에 성공하였습니다."),
    SUCCESS_GET_USERNAME(HttpStatus.OK, "Common-201", "아이디 찾기에 성공하였습니다."),

    SUCCESS_GET_STUDENTS(HttpStatus.OK, "Common-202", "학생 조회에 성공하였습니다."),

    SUCCESS_POST_BEHAVIOR(HttpStatus.OK, "Common-203", "행동 특성 저장에 성공하였습니다."),
    SUCCESS_GET_BEHAVIOR(HttpStatus.OK, "Common-203", "행동 특성 조회에 성공하였습니다."),

    SUCCESS_POST_COUNSEL(HttpStatus.OK, "Common-204", "상담 내용 저장에 성공하였습니다."),
    SUCCESS_GET_COUNSEL(HttpStatus.OK, "Common-205", "상담 조회에 성공하였습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
