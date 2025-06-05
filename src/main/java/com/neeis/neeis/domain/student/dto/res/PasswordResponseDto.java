package com.neeis.neeis.domain.student.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PasswordResponseDto {

    private final String password;

    @Builder
    private PasswordResponseDto(String password) {
        this.password = password;
    }

    public static PasswordResponseDto of(String password) {
        return PasswordResponseDto.builder()
                .password(password)
                .build();
    }


}
