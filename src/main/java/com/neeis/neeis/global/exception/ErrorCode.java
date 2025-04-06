package com.neeis.neeis.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //Common -> http 요청시 발생할만한 예외
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Common-001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Common-002", " Invalid Http Method"),
    ENTITY_NOT_FOUND(HttpStatus.BAD_REQUEST,"Common-003", " Entity Not Found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Common-004", "Server Error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,"Common-005", " Invalid Type Value"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Common-006", "Access is Denied"),

    //Member Validation
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "Member-001", "Login input is invalid")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;


}
