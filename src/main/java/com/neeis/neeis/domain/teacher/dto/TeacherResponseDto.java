package com.neeis.neeis.domain.teacher.dto;

import com.neeis.neeis.domain.teacher.Teacher;
import lombok.Builder;
import lombok.Getter;


@Getter
public class TeacherResponseDto {
    private final Long teacherId;

    @Builder
    private TeacherResponseDto(Long teacherId) {
        this.teacherId = teacherId;
    }

    public static TeacherResponseDto of(Teacher teacher) {
        return TeacherResponseDto.builder()
                .teacherId(teacher.getId())
                .build();
    }

}
