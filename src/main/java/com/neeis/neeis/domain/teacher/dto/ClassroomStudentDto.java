package com.neeis.neeis.domain.teacher.dto;

import com.neeis.neeis.domain.classroom.Classroom;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ClassroomStudentDto {
    private final int year;
    private final int grade;
    private final int classNum;
    private final List<StudentResponseDto> students;

    @Builder
    private ClassroomStudentDto(int year, int grade, int classNum, List<StudentResponseDto> students) {
        this.year = year;
        this.grade = grade;
        this.classNum = classNum;
        this.students = students;
    }

    public static ClassroomStudentDto toDto(Classroom classroom, List<StudentResponseDto> students) {
        return ClassroomStudentDto.builder()
                .year(classroom.getYear())
                .grade(classroom.getGrade())
                .classNum(classroom.getClassNum())
                .students(students)
                .build();
    }

}
