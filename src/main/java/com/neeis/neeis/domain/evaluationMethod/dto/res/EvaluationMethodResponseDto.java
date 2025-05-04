package com.neeis.neeis.domain.evaluationMethod.dto.res;

import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EvaluationMethodResponseDto {

    private final Long id;
    private final String examType;
    private final String title;
    private final double weight;
    private final double fullScore;

    @Builder
    private EvaluationMethodResponseDto(Long id,String examType, String title, double weight, double fullScore) {
        this.id = id;
        this.examType = examType;
        this.title = title;
        this.weight = weight;
        this.fullScore = fullScore;
    }

    public static EvaluationMethodResponseDto toDto(EvaluationMethod method) {
        return EvaluationMethodResponseDto.builder()
                .id(method.getId())
                .examType(method.getExamType().name())
                .title(method.getTitle())
                .weight(method.getWeight())
                .fullScore(method.getFullScore())
                .build();
    }
}
