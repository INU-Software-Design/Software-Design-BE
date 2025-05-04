package com.neeis.neeis.domain.evaluationMethod.dto.req;

import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.ExamType;
import com.neeis.neeis.domain.subject.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateEvaluationMethodDto {

    @NotBlank(message = "과목명은 필수 입력입니다.")
    @Schema(description = "과목명", example = "국어")
    private String subject;

    @NotNull(message = "연도는 필수 입력입니다.")
    @Schema(description = "연도", example = "2025")
    private Integer year;

    @NotNull(message = "학기는 필수 입력입니다.")
    @Min(value = 1, message = "학기는 1 또는 2만 입력 가능합니다.")
    @Max(value = 2, message = "학기는 1 또는 2만 입력 가능합니다.")
    @Schema(description = "학기 (1 또는 2)", example = "1", allowableValues = {"1", "2"}, required = true)
    private Integer semester;

    @NotNull(message = "학년은 필수 입력입니다.")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 6, message = "학년은 6 이하여야 합니다.")
    @Schema(description = "학년", example = "1")
    private Integer grade;

    @NotNull(message = "시험 유형은 필수 입력입니다.")
    @Pattern(regexp = "WRITTEN|PRACTICAL", message = "시험 유형은 WRITTEN 또는 PRACTICAL 중 하나여야 합니다.")
    @Schema(description = "시험 유형", example = "WRITTEN", allowableValues = {"WRITTEN", "PRACTICAL"})
    private String examType;

    @NotBlank(message = "고사명은 필수 입력입니다.")
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
    private CreateEvaluationMethodDto(String subject, int year, int semester, int grade,
                                     String examType, String title, double weight, double fullScore) {
        this.subject = subject;
        this.year = year;
        this.semester = semester;
        this.grade = grade;
        this.examType = examType;
        this.title = title;
        this.weight = weight;
        this.fullScore = fullScore;
    }

    public static EvaluationMethod of(Subject subject, ExamType examType, CreateEvaluationMethodDto dto) {
        return EvaluationMethod.builder()
                .examType(examType)
                .subject(subject)
                .year(dto.getYear())
                .semester(dto.getSemester())
                .grade(dto.getGrade())
                .title(dto.getTitle())
                .weight(dto.getWeight())
                .fullScore(dto.getFullScore())
                .build();
    }


}
