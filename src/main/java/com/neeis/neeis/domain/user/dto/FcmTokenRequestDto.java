package com.neeis.neeis.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmTokenRequestDto {
    private String token;
}
