package com.neeis.neeis.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //Common -> http 요청시 발생할만한 예외
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Common-001", "입력값이 유효하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Common-002", " Invalid Http Method"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Common-003", "Server Error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,"Common-004", " Invalid Type Value"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Common-005", "Access is Denied"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Common-006", "Token Expired"),
    INVALID_DATA(HttpStatus.CONFLICT, "DB-001", "Invalid Data"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "DB-002", "데이터가 존재하지 않습니다."),
    //Member Validation
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST,"Member-001", "회원정보를 찾을 수 없습니다."),
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "Member-002", "회원정보를 찾을 수 없습니다."),

    CLASSROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Teacher-001", "담당학급이 아닙니다."),

    COUNSEL_NOT_FOUND(HttpStatus.NOT_FOUND, "Counsel-001", "상담이 존재하지 않습니다."),
    COUNSEL_CATEGORY_NOT_EXIST(HttpStatus.BAD_REQUEST,"Counsel-002", "상담 종류가 존재하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;


}
