package com.neeis.neeis.domain.user.dto;

import com.neeis.neeis.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final Long userId;
    private final String username;

    @Builder
    public UserResponseDto(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}
