package com.neeis.neeis.domain.score.dto.req;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ScoreRequestDto {

    @NotNull
    private int classNum;

    @NotNull
    private Long evaluationId;

    @Size(min = 1)
    private List<StudentScoreDto> students;

    @Builder
    private ScoreRequestDto(int classNum,Long evaluationId, List<StudentScoreDto> students) {
        this.classNum = classNum;
        this.evaluationId = evaluationId;
        this.students = students;
    }

    @Getter
    @NoArgsConstructor
    public static class StudentScoreDto {

        @NotNull
        private int number;

        @NotNull
        @DecimalMin("0.0")
        private Double rawScore;

        @Builder
        private StudentScoreDto(int number, Double rawScore) {
            this.number = number;
            this.rawScore = rawScore;
        }
    }
}
