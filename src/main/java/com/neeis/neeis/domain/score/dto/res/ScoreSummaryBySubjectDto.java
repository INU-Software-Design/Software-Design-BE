package com.neeis.neeis.domain.score.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ScoreSummaryBySubjectDto {

    @Schema(description = "과목명", example = "독서와 문법")
    private final String subjectName;

    @Schema(description = "평가 항목 정보 (ID와 제목)")
    private final List<EvaluationDto> evaluations;

    @Schema(description = "학생별 성적 정보")
    private final List<StudentScoreDto> students;

    @Builder
    private ScoreSummaryBySubjectDto(String subjectName, List<EvaluationDto> evaluations, List<StudentScoreDto> students) {
        this.subjectName = subjectName;
        this.evaluations = evaluations;
        this.students = students;
    }

    @Getter
    public static class EvaluationDto {

        @Schema(description = "평가 항목 ID", example = "1")
        private final Long evaluationId;

        @Schema(description = "항목 이름", example = "기말고사")
        private final String title;

        @Builder
        private EvaluationDto(Long evaluationId, String title) {
            this.evaluationId = evaluationId;
            this.title = title;
        }
    }

    @Getter
    public static class StudentScoreDto {

        @Schema(description = "학생 이름", example = "홍길동")
        private final String studentName;

        @Schema(description = "출석번호", example = "1")
        private final int number;

        @Schema(description = "항목별 점수")
        private final List<ScoreItemDto> scores;

        @Schema(description = "총 점수", example = "96.0")
        private final double rawTotal;

        @Schema(description = "총 환산점수", example = "78.2")
        private final double weightedTotal;

        @Schema(description = "전체 평균 점수", example = "79.4")
        private final double average;

        @Schema(description = "표준편차", example = "5.3")
        private final double stdDev;

        @Schema(description = "석차", example = "3")
        private final int rank;

        @Schema(description = "석차등급", example = "2")
        private final int grade;

        @Schema(description = "성취도", example = "B")
        private final String achievementLevel;


        @Builder
        private StudentScoreDto(String studentName, int number, List<ScoreItemDto> scores,
                               double rawTotal, double weightedTotal, double average, double stdDev, int rank, int grade, String achievementLevel) {
            this.studentName = studentName;
            this.number = number;
            this.scores = scores;
            this.rawTotal = rawTotal;
            this.weightedTotal = weightedTotal;
            this.average = average;
            this.stdDev = stdDev;
            this.rank = rank;
            this.grade = grade;
            this.achievementLevel = achievementLevel;
        }
    }

    @Getter
    public static class ScoreItemDto {

        @Schema(description = "평가 항목 ID", example = "1")
        private final Long evaluationId;

        @Schema(description = "원점수", example = "74.0")
        private final Double rawScore;

        @Schema(description = "환산점수", example = "51.8")
        private final Double weightedScore;

        @Builder
        public ScoreItemDto(Long evaluationId, Double rawScore, Double weightedScore) {
            this.evaluationId = evaluationId;
            this.rawScore = rawScore;
            this.weightedScore = weightedScore;
        }
    }
}
