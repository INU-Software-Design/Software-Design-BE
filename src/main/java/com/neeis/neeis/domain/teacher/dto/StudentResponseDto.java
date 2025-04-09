package com.neeis.neeis.domain.teacher.dto;

import com.neeis.neeis.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudentResponseDto {
    private final Long studentId;
    private final String name;

    @Builder
    private StudentResponseDto(Long studentId, String name) {
        this.studentId = studentId;
        this.name = name;
    }

    public static StudentResponseDto of(Student student) {
        return StudentResponseDto.builder()
                .studentId(student.getId())
                .name(student.getName())
                .build();
    }
}
