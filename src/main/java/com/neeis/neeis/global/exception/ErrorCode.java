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
    INVALID_JSON(HttpStatus.BAD_REQUEST, "COMMON-004", "요청 JSON이 유효하지 않습니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Common-005", "접근이 제한됩니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Common-006", "Token Expired"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "DB-002", "데이터가 존재하지 않습니다."),


    //Member Validation
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST,"Member-001", "회원정보를 찾을 수 없습니다."),
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "Member-002", "회원정보가 일치하지 않습니다."),
    PASSWORD_EQUALS(HttpStatus.BAD_REQUEST, "Member-003", "이전 비밀번호와 일치합니다."),

    CLASSROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Teacher-001", "담임 학급과 학생을 찾을 수 없습니다."),
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "Teacher-002", "해당 교사가 존재하지 않습니다."),

    COUNSEL_NOT_FOUND(HttpStatus.NOT_FOUND, "Counsel-001", "상담이 존재하지 않습니다."),
    COUNSEL_CATEGORY_NOT_EXIST(HttpStatus.BAD_REQUEST,"Counsel-002", "상담 종류가 존재하지 않습니다."),

    BEHAVIOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Behavior-001", "행동 특성 데이터가 존재하지 않습니다."),

    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "Attendance-001","조회 날짜가 학기 시작일 이전입니다."),

    IMAGE_SAVE_ERROR(HttpStatus.BAD_REQUEST, "Common-007", "이미지 저장에 실패했습니다."),

    SUBJECT_DUPLICATE(HttpStatus.BAD_REQUEST, "Subject-001", "과목명이 이미 존재합니다."),
    SUBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Subject-002", "해당 과목이 존재하지 않습니다."),

    TEACHER_SUBJECT_DUPLICATE(HttpStatus.BAD_REQUEST, "TeacherSubject-001", "해당 과목에 대한 교사가 이미 배정되어 있습니다."),
    TEACHER_SUBJECT_NOT_FOUND(HttpStatus.BAD_REQUEST, "TeacherSubject-002", "해당 과목에 대한 교사가 존재하지 않습니다."),

    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "Feedback-001", "해당 과목의 피드백이 존재하지 않습니다."),

    EVALUATION_METHOD_DUPLICATE(HttpStatus.BAD_REQUEST, "EvaluationMethod-001", "해당 평가 방식이 이미 존재합니다."),
    EVALUATION_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "EvaluationMethod-002", "해당 평가 방식이 존재하지 않습니다."),
    EXCESS_TOTAL_100(HttpStatus.BAD_REQUEST,"EvaluationMethod-003", "해당 과목의 반영 비율 총합이 100을 초과합니다."),

    SCORE_OVER_FULL(HttpStatus.BAD_REQUEST, "SCORE_001", "점수가 만점을 초과했습니다."),
    SCORE_NEGATIVE(HttpStatus.BAD_REQUEST, "SCORE_002", "점수가 0보다 작을 수 없습니다."),

    SCORE_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "Score-Summary-001", "점수 통계가 존재하지 않습니다."),

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification-001", "알림이 존재하지 않습니다."),

    PDF_GENERATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PDF_GENERATION_ERROR", "PDF 생성에 실패하였습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;


}
