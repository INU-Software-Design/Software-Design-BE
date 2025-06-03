package com.neeis.neeis.domain.student.dto.report;

import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectGradeDto {
    private String subjectName;
    private double score;           // 원점수 (weightedTotal)
    private int credits;            // 학점 (임시로 3학점)
    private String grade;           // 등급 (1~9)
    private int rank;              // 석차
    private int totalStudents;     // 전체 학생 수
    private String achievementLevel; // 성취도 (A, B, C...)

    // 기존 SubjectScoreDto에서 변환
    public static SubjectGradeDto from(SubjectScoreDto subjectScore) {
        return SubjectGradeDto.builder()
                .subjectName(subjectScore.getSubjectName())
                .score(subjectScore.getWeightedTotal())
                .credits(3) // 임시: 모든 과목 3학점
                .grade(String.valueOf(subjectScore.getGrade()))
                .rank(subjectScore.getRank())
                .totalStudents(subjectScore.getTotalStudentCount())
                .achievementLevel(subjectScore.getAchievementLevel())
                .build();
    }
}