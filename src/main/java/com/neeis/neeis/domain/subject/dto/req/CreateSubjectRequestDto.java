package com.neeis.neeis.domain.subject.dto.req;

import com.neeis.neeis.domain.subject.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateSubjectRequestDto {

    @NotBlank(message = "과목 명은 필수 입력값입니다.")
    @Schema(description = "과목 명" , example = "영어")
    private String name;

    @Builder
    private CreateSubjectRequestDto(String name) {
        this.name = name;
    }

    public static Subject of(CreateSubjectRequestDto dto) {
        return Subject.builder()
                .name(dto.getName())
                .build();
    }
}