package com.neeis.neeis.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    private String message;
    private int status;
    private String code;
    private List<FieldError> fieldErrors;

    private ErrorResponse(ErrorCode errorCode) {
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus().value();
        this.code = errorCode.getCode();
        this.fieldErrors = new ArrayList<>();
    }

    private ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus().value();
        this.code = errorCode.getCode();
        this.fieldErrors = fieldErrors;
    }

    private ErrorResponse(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(FieldError.of(bindingResult));
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(errorCode, FieldError.of(bindingResult));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String reason;

        private FieldError(String field, Object rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<FieldError> of(final BindingResult bindingResult) {

            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();

            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ?
                                    "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }

    }
}
