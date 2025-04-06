package com.neeis.neeis.domain.student.dto;

import com.neeis.neeis.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudentResponseDto {
    private final Long studentId;

    @Builder
    public StudentResponseDto(Long studentId) {
        this.studentId = studentId;
    }


    public static StudentResponseDto of(Student student){
        return StudentResponseDto.builder()
                .studentId(student.getId())
                .build();
    }
}
