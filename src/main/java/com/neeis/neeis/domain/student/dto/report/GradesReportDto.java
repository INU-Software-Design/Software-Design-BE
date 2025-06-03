package com.neeis.neeis.domain.student.dto.report;

import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class GradesReportDto {
    private double gpa;                           // 전체 평균 성적
    private int totalCredits;                     // 총 이수 학점
    private List<SubjectGradeDto> subjects;       // 과목별 성적 (PDF용 간소화)
    private int classRank;                        // 반 석차
    private int gradeRank;                        // 학년 석차 (계산 필요)

    // 기존 StudentScoreSummaryDto에서 변환
    public static GradesReportDto from(StudentScoreSummaryDto summary) {
        // GPA 계산 (가중평균)
        double gpa = summary.getSubjects().stream()
                .mapToDouble(SubjectScoreDto::getWeightedTotal)
                .average()
                .orElse(0.0);

        // 과목별 성적을 PDF용으로 간소화
        List<SubjectGradeDto> subjects = summary.getSubjects().stream()
                .map(SubjectGradeDto::from)
                .toList();

        return GradesReportDto.builder()
                .gpa(Math.round(gpa * 100) / 100.0) // 소수점 2자리
                .totalCredits(summary.getSubjects().size() * 3) // 임시: 과목당 3학점
                .subjects(subjects)
                .classRank(calculateClassRank(summary.getSubjects()))
                .gradeRank(0) // 별도 계산 필요
                .build();
    }

    // 반 석차 계산 (가장 높은 석차 기준)
    private static int calculateClassRank(List<SubjectScoreDto> subjects) {
        return subjects.stream()
                .mapToInt(SubjectScoreDto::getRank)
                .min()
                .orElse(0);
    }
}