package com.neeis.neeis.domain.user.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenResponseDto {
    private final String accessToken;
    private final String name;
    private final String role;

    private final Integer year;
    private final Integer grade;
    private final Integer classNum;
    private final Integer number;
    private final Long studentId;

    @Builder
    public TokenResponseDto(String accessToken,String name,String role,
                            Integer year, Integer grade, Integer classNum,Integer number, Long studentId) {
        this.accessToken = accessToken;
        this.name = name;
        this.role = role;
        this.year = year;
        this.grade = grade;
        this.classNum = classNum;
        this.number = number;
        this.studentId = studentId;
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

    public static TokenResponseDto ofCommon(String accessToken, String name, String role) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .name(name)
                .role(role)
                .build();
    }
}
