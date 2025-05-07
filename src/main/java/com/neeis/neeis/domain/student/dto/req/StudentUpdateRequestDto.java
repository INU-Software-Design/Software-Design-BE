package com.neeis.neeis.domain.student.dto.req;

import com.neeis.neeis.domain.student.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudentUpdateRequestDto {

    @Schema(description = "학생 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @Schema(description = "학생 연락처", example = "010-1234-5678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-0000-0000입니다.")
    private String phone;

    @Schema(description = "부(아버지) 이름", example = "홍아버지")
    private String fatherName;

    @Schema(description = "모(어머니) 이름", example = "홍어머니")
    private String motherName;

    @Schema(description = "부(아버지) 연락처", example = "010-1111-2222")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-0000-0000입니다.")
    private String fatherPhone;

    @Schema(description = "모(어머니) 연락처", example = "010-3333-4444")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-0000-0000입니다.")
    private String motherPhone;

    @Builder
    private StudentUpdateRequestDto(String name,String address, String phone, String fatherName, String motherName, String fatherPhone, String motherPhone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.fatherPhone = fatherPhone;
        this.motherPhone = motherPhone;
    }
}
