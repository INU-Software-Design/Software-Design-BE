package com.neeis.neeis.domain.student.dto.res;

import com.neeis.neeis.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudentSaveResponseDto {
    private final String loginId;
    private final String studentName;
    private final String password;

    @Builder
    public StudentSaveResponseDto(String loginId, String studentName, String password) {
        this.loginId = loginId;
        this.studentName = studentName;
        this.password = password;
    }

    public static StudentSaveResponseDto toDto(Student student, String password) {
        return StudentSaveResponseDto.builder()
                .loginId(student.getUser().getUsername())
                .studentName(student.getName())
                .password(password)
                .build();
    }
}
