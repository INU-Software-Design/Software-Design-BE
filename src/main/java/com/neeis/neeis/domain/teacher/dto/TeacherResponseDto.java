package com.neeis.neeis.domain.teacher.dto;

import com.neeis.neeis.domain.teacher.Teacher;
import lombok.Builder;
import lombok.Getter;


@Getter
public class TeacherResponseDto {
    private final Long teacherId;
    private final String name;
    private final String phone;
    private final String email;

    @Builder
    private TeacherResponseDto(Long teacherId, String name, String phone, String email) {
        this.teacherId = teacherId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public static TeacherResponseDto toDto(Teacher teacher) {
        return TeacherResponseDto.builder()
                .teacherId(teacher.getId())
                .name(teacher.getName())
                .phone(teacher.getPhone())
                .email(teacher.getEmail())
                .build();
    }

}
