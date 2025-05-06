package com.neeis.neeis.domain.scoreSummary.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class StudentScoreSummaryDto {
    private final int number; // 출석번호
    private final String studentName;
    private final List<SubjectScoreDto> subjects;

    @Builder
    private StudentScoreSummaryDto(int number, String studentName, List<SubjectScoreDto> subjects) {
        this.number = number;
        this.studentName = studentName;
        this.subjects = subjects;
    }
}
