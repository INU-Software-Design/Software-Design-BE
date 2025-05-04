package com.neeis.neeis.domain.evaluationMethod.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateEvaluationMethodDto {

    @NotNull(message = "시험 유형은 필수 입력입니다.")
    @Pattern(regexp = "WRITTEN|PRACTICAL", message = "시험 유형은 WRITTEN (자필) 또는 PRACTICAL (수행) 중 하나여야 합니다.")
    @Schema(description = "시험 유형", example = "WRITTEN", allowableValues = {"WRITTEN", "PRACTICAL"})
    private String examType;

    @NotBlank(message = "고사명을 입력해주세요.")
    @Schema(description = "고사명", example = "기말고사")
    private String title;

    @Min(value = 0, message = "반영 비율은 0 이상이어야 합니다.")
    @Max(value = 100, message = "반영 비율은 100 이하여야 합니다.")
    @Schema(description = "반영 비율 (%)", example = "20")
    private Double weight;

    @Positive(message = "만점 점수는 0보다 커야 합니다.")
    @Schema(description = "만점 점수", example = "100.0")
    private Double fullScore;

    @Builder
    private UpdateEvaluationMethodDto(String examType, String title, Double weight, Double fullScore) {
        this.examType = examType;
        this.title = title;
        this.weight = weight;
        this.fullScore = fullScore;
    }
}