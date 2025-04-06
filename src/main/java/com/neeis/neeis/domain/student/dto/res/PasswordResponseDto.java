package com.neeis.neeis.domain.student.dto.res;

import com.neeis.neeis.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PasswordResponseDto {

    private final String password;

    @Builder
    private PasswordResponseDto(String password) {
        this.password = password;
    }

    public static PasswordResponseDto of(Student student) {
        return PasswordResponseDto.builder()
                .password(student.getUser().getPassword())
                .build();
    }
}
