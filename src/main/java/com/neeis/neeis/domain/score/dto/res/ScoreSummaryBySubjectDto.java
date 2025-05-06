package com.neeis.neeis.domain.score.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ScoreSummaryBySubjectDto {
    private final String subjectName;
    private final List<EvaluationMethodScoreDto> evaluations;

    @Builder
    private ScoreSummaryBySubjectDto(String subjectName, List<EvaluationMethodScoreDto> evaluations) {
        this.subjectName = subjectName;
        this.evaluations = evaluations;
    }

    @Getter
    public static class EvaluationMethodScoreDto {
        private final Long evaluationId;
        private final String title;
        private final String examType;
        private final double weight;
        private final double fullScore;

        private List<StudentScoreDto> scores;

        @Builder
        private EvaluationMethodScoreDto( Long evaluationId,String title, String examType, double weight, double fullScore, List<StudentScoreDto> scores) {
            this.evaluationId = evaluationId;
            this.title = title;
            this.examType = examType;
            this.weight = weight;
            this.fullScore = fullScore;
            this.scores = scores;
        }

        @Getter
        @Builder
        public static class StudentScoreDto {
            private final String studentName;
            private final int number;
            private final Double rawScore;
            private final Double weightedScore;
        }
    }
}
