package com.neeis.neeis.domain.user.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenResponseDto {
    private final String accessToken;
    private final String name;
    private final String role;
    private final String subject;

    private final Integer year;
    private final Integer grade;
    private final Integer classNum;
    private final Integer number;
    private final Long studentId;

    private final String studentName;

    @Builder
    public TokenResponseDto(String accessToken,String name,String role, String subject,
                            Integer year, Integer grade, Integer classNum,Integer number, Long studentId, String studentName) {
        this.accessToken = accessToken;
        this.name = name;
        this.role = role;
        this.subject = subject;
        this.year = year;
        this.grade = grade;
        this.classNum = classNum;
        this.number = number;
        this.studentId = studentId;
        this.studentName = studentName;
    }

    public static TokenResponseDto ofStudent(String accessToken, String name, String role,
                                             int year, int grade, int classNum, int number, Long studentId) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .name(name)
                .role(role)
                .year(year)
                .grade(grade)
                .classNum(classNum)
                .number(number)
                .studentId(studentId)
                .build();
    }

    public static TokenResponseDto ofTeacher(String accessToken, String name, String role, String subject) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .name(name)
                .role(role)
                .subject(subject)
                .build();
    }

    public static TokenResponseDto ofParent(String accessToken, String name, String role,
                                             int year, int grade, int classNum, int number, Long studentId, String studentName) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .name(name)
                .role(role)
                .year(year)
                .grade(grade)
                .classNum(classNum)
                .number(number)
                .studentId(studentId)
                .studentName(studentName)
                .build();
    }

}
