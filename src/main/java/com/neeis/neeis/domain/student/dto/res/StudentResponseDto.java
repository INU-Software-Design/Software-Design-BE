package com.neeis.neeis.domain.student.dto.res;

import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudentResponseDto {
    private final String loginId;

    @Builder
    public StudentResponseDto(String loginId) {
       this.loginId = loginId;
    }

    public static StudentResponseDto of(Student student){
        return StudentResponseDto.builder()
                .loginId(student.getUser().getUsername())
                .build();
    }

    public static StudentResponseDto ofTeacher(Teacher teacher) {
        return StudentResponseDto.builder()
                .loginId(teacher.getUser().getUsername())
                .build();
    }

    public static StudentResponseDto ofParent(Parent parent) {
        return StudentResponseDto.builder()
                .loginId(parent.getUser().getUsername())
                .build();
    }

}
