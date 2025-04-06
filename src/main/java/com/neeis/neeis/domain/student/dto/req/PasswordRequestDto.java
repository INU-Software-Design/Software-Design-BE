package com.neeis.neeis.domain.student.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordRequestDto {

    @Schema(description = "이름" , example= "김마리")
    private String name;

    @Schema(description = "전화번호" , example= "010-2222-2222")
    private String phone;

    @Schema(description = "주민번호" , example= "100404-4011111")
    private String ssn;

    @Schema(description = "학교" , example= "인천중학교")
    private String school;

    @Builder
    private PasswordRequestDto(String name, String phone, String ssn, String school) {
        this.name = name;
        this.phone = phone;
        this.ssn = ssn;
        this.school = school;
    }
}
