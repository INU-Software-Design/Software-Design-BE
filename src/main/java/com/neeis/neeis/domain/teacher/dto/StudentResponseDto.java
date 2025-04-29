package com.neeis.neeis.domain.teacher.dto;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudentResponseDto {
    private final Long studentId;
    private final int number;
    private final String name;

    @Builder
    private StudentResponseDto(Long studentId, int number, String name) {
        this.studentId = studentId;
        this.number = number;
        this.name = name;
    }

    public static StudentResponseDto of(Student student, ClassroomStudent classroomStudent) {
        return StudentResponseDto.builder()
                .studentId(student.getId())
                .number(classroomStudent.getNumber())
                .name(student.getName())
                .build();
    }
}
