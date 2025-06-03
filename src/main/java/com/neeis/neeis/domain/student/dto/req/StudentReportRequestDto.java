package com.neeis.neeis.domain.student.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportRequestDto {
    private Long studentId;  // username 대신 studentId 사용
    private int year;
    private int semester;

    // 포함할 섹션 선택
    private boolean includeAttendance = true;
    private boolean includeGrades = true;  // ScoreSummary 기반
    private boolean includeCounseling = true;
    private boolean includeBehavior = true;
    private boolean includeFeedback = true;
}