package com.neeis.neeis.domain.scoreSummary.dto;

import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class SubjectScoreDto {

    @Schema(description = "과목명", example = "국어")
    private final String subjectName;

    @Schema(description = "해당 과목의 평가 방식별 점수 정보")
    private final List<EvaluationMethodScoreDto> evaluationMethods;

    @Schema(description = "반영비율이 적용된 총점 (지필/수행 포함)", example = "87.5")
    private final double rawTotal;

    @Schema(description = "원점수 (환산 점수 합, 정수 반올림)", example = "88")
    private final double weightedTotal;

    @Schema(description = "석차", example = "5")
    private final int rank;

    @Schema(description = "석차 등급 (1~9)", example = "2")
    private final int grade;

    @Schema(description = "성취도 등급", example = "B")
    private final String achievementLevel;

    @Schema(description = "전체 평균 점수", example = "79.4")
    private final double average;

    @Schema(description = "표준편차", example = "5.3")
    private final double stdDev;

    @Schema(description = "해당 과목 수강자 수", example = "28")
    private final int totalStudentCount;

    @Builder
    private SubjectScoreDto(String subjectName, List<EvaluationMethodScoreDto> evaluationMethods, double rawTotal,
                            double weightedTotal, int rank, int grade, String achievementLevel, double average, double stdDev, int totalStudentCount) {
        this.subjectName = subjectName;
        this.evaluationMethods = evaluationMethods;
        this.rawTotal = rawTotal;
        this.weightedTotal = weightedTotal;
        this.rank = rank;
        this.grade = grade;
        this.achievementLevel = achievementLevel;
        this.average = average;
        this.stdDev = stdDev;
        this.totalStudentCount = totalStudentCount;
    }

    public static SubjectScoreDto toDto(ScoreSummary summary, List<Score> scores) {
        return SubjectScoreDto.builder()
                .subjectName(summary.getSubject().getName())
                .rawTotal(summary.getSumScore())
                .weightedTotal(summary.getOriginalScore())
                .rank(summary.getRank())
                .grade(summary.getGrade())
                .achievementLevel(summary.getAchievementLevel())
                .average(summary.getAverage())
                .stdDev(summary.getStdDeviation())
                .totalStudentCount(summary.getTotalStudentCount())
                .evaluationMethods(
                        scores.stream().map(score -> {
                            var method = score.getEvaluationMethod();
                            return EvaluationMethodScoreDto.builder()
                                    .examType(method.getExamType().name())
                                    .title(method.getTitle())
                                    .weight(method.getWeight())
                                    .fullScore(method.getFullScore())
                                    .rawScore(score.getRawScore())
                                    .weightedScore(score.getWeightedScore())
                                    .build();
                        }).toList()
                )
                .build();
    }
}
