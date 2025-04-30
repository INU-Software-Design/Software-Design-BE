package com.neeis.neeis.domain.student.dto.req;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class StudentRequestDto {

    @Schema(
            example = "STUDENT",
            description = "회원 역할 (STUDENT만 허용)"
    )
    @NotBlank(message = "역할은 필수입니다.")
    @Pattern(regexp = "STUDENT", message = "역할은 STUDENT만 가능합니다.")
    private String role;

    @Schema(
            example = "인천중학교",
            description = "학생이 소속된 학교 이름"
    )
    @NotBlank(message = "학교는 필수입니다.")
    private String school;

    @Schema(
            example = "김인천",
            description = "학생 이름"
    )
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(
            example = "남",
            allowableValues = {"남", "여"},
            description = "성별 (남 또는 여)"
    )
    @NotBlank(message = "성별은 필수입니다.")
    @Pattern(regexp = "남|여", message = "성별은 '남' 또는 '여'만 입력 가능합니다.")
    private String gender;

    @Schema(
            example = "100405-1234811",
            description = "주민등록번호 앞 6자리+뒷자리 7자리 형식"
    )
    @NotBlank(message = "주민등록번호는 필수입니다.")
    @Pattern(regexp = "^\\d{6}-\\d{7}$", message = "주민등록번호는 '6자리-7자리' 형식이어야 합니다.")
    private String ssn;

    @Schema(
            example = "인천광역시 연수구 송도동",
            description = "학생의 주소"
    )
    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @Schema(
            example = "010-1234-2345",
            description = "학생 연락처"
    )
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-0000-0000 이어야 합니다.")
    private String phone;

    @Schema(
            example = "2025-03-02",
            description = "입학일 (yyyy-MM-dd 형식)"
    )
    @NotNull(message = "입학일은 필수입니다.")
    private LocalDate admissionDate;

    @Builder
    private StudentRequestDto(String role, String school, String name, String gender, String ssn, String address, String phone, LocalDate admissionDate) {
        this.role = role;
        this.school = school;
        this.name = name;
        this.gender = gender;
        this.ssn = ssn;
        this.address = address;
        this.phone = phone;
        this.admissionDate = admissionDate;
    }

    public static Student of(StudentRequestDto studentRequestDto, String image, User user) {
        return Student.builder()
                .name(studentRequestDto.getName())
                .image(image)
                .gender(studentRequestDto.getGender())
                .ssn(studentRequestDto.getSsn())
                .address(studentRequestDto.getAddress())
                .phone(studentRequestDto.getPhone())
                .admissionDate(studentRequestDto.getAdmissionDate())
                .user(user)
                .build();
    }
}
