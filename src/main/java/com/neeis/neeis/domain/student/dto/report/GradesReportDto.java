package com.neeis.neeis.domain.student.dto.report;

import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class GradesReportDto {
    private double gpa;                    // 전체 평균
    private int totalSubjects;            // 총 이수과목 수
    private int classRank;                // 학급 순위
    private int totalStudents;            // 전체 학생 수
    private List<SubjectScoreDto> subjects; // 과목별 상세 성적

    // StudentScoreSummaryDto에서 GradesReportDto로 변환하는 메서드
    public static GradesReportDto from(StudentScoreSummaryDto summary) {
        List<SubjectScoreDto> subjects = summary.getSubjects();

        // 전체 평균 계산
        double totalWeighted = subjects.stream()
                .mapToDouble(s -> s.getWeightedTotal())
                .average()
                .orElse(0.0);

        // 학급 순위 계산 (평균 순위로 근사)
        int avgRank = (int) subjects.stream()
                .mapToInt(s -> s.getRank())
                .average()
                .orElse(0);

        // 전체 학생 수 (첫 번째 과목 기준)
        int totalStudents = subjects.isEmpty() ? 0 : subjects.get(0).getTotalStudentCount();

        return GradesReportDto.builder()
                .gpa(totalWeighted)
                .totalSubjects(subjects.size())
                .classRank(avgRank)
                .totalStudents(totalStudents)
                .subjects(subjects)
                .build();
    }
}