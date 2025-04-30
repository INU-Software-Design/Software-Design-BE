package com.neeis.neeis.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePasswordRequestDto {

    @Schema(description = "로그인 ID", example = "2025001")
    @NotBlank(message = "로그인 ID는 필수입니다.")
    private String loginId;

    @Schema(description = "현재 비밀번호", example = "Oldpass123!")
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String oldPassword;

    @Schema(description = "새 비밀번호 (영문 대소문자, 숫자, 특수문자 포함 8자 이상)", example = "NewPass!123")
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
            message = "새 비밀번호는 영문 대소문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."
    )
    private String newPassword;

    @Builder
    private UpdatePasswordRequestDto (String loginId, String oldPassword, String newPassword) {
        this.loginId = loginId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
