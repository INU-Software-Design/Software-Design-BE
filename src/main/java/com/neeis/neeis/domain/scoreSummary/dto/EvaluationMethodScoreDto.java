package com.neeis.neeis.domain.scoreSummary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EvaluationMethodScoreDto {

    @Schema(description = "시험 유형 (예: WRITTEN, PERFORMANCE)", example = "WRITTEN")
    private final String examType;

    @Schema(description = "시험 제목", example = "기말고사")
    private final String title;

    @Schema(description = "해당 평가의 반영 비율", example = "70.0")
    private final double weight;

    @Schema(description = "만점 점수", example = "100.0")
    private final double fullScore;

    @Schema(description = "실제 점수", example = "85.0")
    private final double rawScore;

    @Schema(description = "반영된 점수", example = "59.5")
    private final double weightedScore;


    @Builder
    private EvaluationMethodScoreDto(String title, String examType, double weight, double fullScore, double rawScore, double weightedScore) {
        this.examType = examType;
        this.title = title;
        this.weight = weight;
        this.fullScore = fullScore;
        this.rawScore = rawScore;
        this.weightedScore = weightedScore;
    }


}
