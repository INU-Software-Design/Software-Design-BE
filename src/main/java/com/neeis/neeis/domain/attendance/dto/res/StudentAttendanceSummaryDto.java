package com.neeis.neeis.domain.attendance.dto.res;

import lombok.Builder;
import lombok.Getter;


@Getter
public class StudentAttendanceSummaryDto {
    private final Long studentId;
    private final String studentName;
    private final int totalSchoolDays;
    private final int presentDays; // 출석
    private final int absentDays; // 결석
    private final int lateDays; // 지각
    private final int leaveEarlyDays; // 조퇴

    @Builder
    private StudentAttendanceSummaryDto(Long studentId, String studentName, int totalSchoolDays, int presentDays, int absentDays, int lateDays, int leaveEarlyDays
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalSchoolDays = totalSchoolDays;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.lateDays = lateDays;
        this.leaveEarlyDays = leaveEarlyDays;
    }
}