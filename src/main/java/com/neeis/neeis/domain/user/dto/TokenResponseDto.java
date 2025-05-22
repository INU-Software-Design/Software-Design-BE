package com.neeis.neeis.domain.user.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenResponseDto {
    private final String accessToken;
    private final String name;
    private final String role;

    @Builder
    public TokenResponseDto(String accessToken,String name,String role) {
        this.accessToken = accessToken;
        this.name = name;
        this.role = role;
    }

    public static TokenResponseDto of(String accessToken, String name, String role) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .name(name)
                .role(role)
                .build();
    }
}
