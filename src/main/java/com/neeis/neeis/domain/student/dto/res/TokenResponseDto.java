package com.neeis.neeis.domain.student.dto.res;


import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenResponseDto {
    private final String accessToken;

    @Builder
    public TokenResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public static TokenResponseDto of(String accessToken) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .build();
    }
}
