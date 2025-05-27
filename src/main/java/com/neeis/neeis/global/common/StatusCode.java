package com.neeis.neeis.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StatusCode {

    SUCCESS_LOGIN(HttpStatus.OK, "Common-Login", "로그인에 성공하였습니다."),
    SUCCESS_GET_USERNAME(HttpStatus.OK, "Common-Login", "아이디 찾기에 성공하였습니다."),

    SUCCESS_SAVE_FCM_TOKEN(HttpStatus.OK, "Commone", "FCM 토큰 저장에 성공하였습니다."),

    SUCCESS_POST_STUDENTS(HttpStatus.OK, "Student", "학생 정보 저장에 성공하였습니다."),
    SUCCESS_GET_STUDENTS(HttpStatus.OK, "Student", "학생 조회에 성공하였습니다."),
    SUCCESS_UPDATE_PASSWORD(HttpStatus.OK, "Student", "비밀번호 변경에 성공하였습니다."),
    SUCCESS_UPDATE_STUDENTS(HttpStatus.OK, "Student", "학생 정보 변경에 성공하였습니다."),

    SUCCESS_GET_TEACHERS(HttpStatus.OK, "Teacher", "교사 정보 조회에 성공하였습니다."),

    SUCCESS_POST_BEHAVIOR(HttpStatus.OK, "Common-Behavior", "행동 특성 저장에 성공하였습니다."),
    SUCCESS_GET_BEHAVIOR(HttpStatus.OK, "Common-Behavior", "행동 특성 조회에 성공하였습니다."),

    SUCCESS_POST_COUNSEL(HttpStatus.OK, "Common-Counsel", "상담 내용 저장에 성공하였습니다."),
    SUCCESS_GET_COUNSEL(HttpStatus.OK, "Common-Counsel", "상담 조회에 성공하였습니다."),

    SUCCESS_POST_ATTENDANCE(HttpStatus.OK, "Common-Attendance", "출결 저장에 성공하였습니다."),
    SUCCESS_GET_ATTENDANCE(HttpStatus.OK, "Common-Attendance", "출결 조회에 성공하였습니다."),

    SUCCESS_POST_FEEDBACK(HttpStatus.OK, "Common-Feedback", "피드백 저장에 성공하였습니다."),
    SUCCESS_GET_FEEDBACK(HttpStatus.OK, "Common-Feedback", "피드백 조회에 성공하였습니다."),

    SUCCESS_POST_SUBJECT(HttpStatus.OK, "Common-Subject", "과목 저장에 성공하였습니다."),
    SUCCESS_UPDATE_SUBJECT(HttpStatus.OK, "Common-Subject", "과목명 변경에 성공하였습니다."),
    SUCCESS_GET_SUBJECT(HttpStatus.OK, "Common-Subject", "과목 조회에 성공하였습니다."),

    SUCCESS_POST_TEACHER_SUBJECT(HttpStatus.OK, "Common-TeacherSubject", "담당 교사 배정 저장에 성공하였습니다."),
    SUCCESS_UPDATE_TEACHER_SUBJECT(HttpStatus.OK, "Common-TeacherSubject", "담당 교사 배정 변경에 성공하였습니다."),
    SUCCESS_GET_TEACHER_SUBJECT(HttpStatus.OK, "Common-TeacherSubject", "담당 교사 배정 조회에 성공하였습니다."),

    SUCCESS_POST_EVALUATION_METHOD(HttpStatus.OK, "EvaluationMethod", "평가 방식 저장(추가)에 성공하였습니다."),
    SUCCESS_GET_EVALUATION_METHOD(HttpStatus.OK, "EvaluationMethod", "평가 방식 조회에 성공하였습니다."),
    SUCCESS_UPDATE_EVALUATION_METHOD(HttpStatus.OK, "EvaluationMethod", "평가 방식 수정에 성공하였습니다."),

    SUCCESS_POST_SCORE(HttpStatus.OK, "Score", "성적 저장에 성공하였습니다."),
    SUCCESS_GET_SCORE(HttpStatus.OK, "Score", "성적 조회에 성공하였습니다."),

    SUCCESS_GET_NOTIFICATION(HttpStatus.OK, "Notice", "알림 조회에 성공하였습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
