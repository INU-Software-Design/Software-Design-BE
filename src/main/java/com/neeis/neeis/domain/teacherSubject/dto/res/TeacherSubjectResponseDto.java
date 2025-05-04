package com.neeis.neeis.domain.teacherSubject.dto.res;

import com.neeis.neeis.domain.teacherSubject.TeacherSubject;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TeacherSubjectResponseDto {
    private final Long id;
    private final String subjectName;
    private final String teacherName;

    @Builder
    private TeacherSubjectResponseDto(Long id, String subjectName, String teacherName) {
        this.id = id;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
    }

    public static TeacherSubjectResponseDto toDto(TeacherSubject teacherSubject) {
        return TeacherSubjectResponseDto.builder()
                .id(teacherSubject.getId())
                .subjectName(teacherSubject.getSubject().getName())
                .teacherName(teacherSubject.getTeacher().getName())
                .build();
    }
}
