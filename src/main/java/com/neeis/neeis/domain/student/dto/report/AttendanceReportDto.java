package com.neeis.neeis.domain.student.dto.report;

import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AttendanceReportDto {
    private int totalDays;
    private int presentDays;
    private int absentDays;
    private int lateDays;
    private int earlyLeaveDays;
    private double attendanceRate;
    private List<AttendanceDetailDto> details;

    // 기존 StudentAttendanceSummaryDto에서 변환
    public static AttendanceReportDto from(StudentAttendanceSummaryDto summary) {
        double attendanceRate = summary.getTotalSchoolDays() > 0 ?
                (double) summary.getPresentDays() / summary.getTotalSchoolDays() * 100 : 0.0;

        return AttendanceReportDto.builder()
                .totalDays(summary.getTotalSchoolDays())
                .presentDays(summary.getPresentDays())
                .absentDays(summary.getAbsentDays())
                .lateDays(summary.getLateDays())
                .earlyLeaveDays(summary.getLeaveEarlyDays())
                .attendanceRate(Math.round(attendanceRate * 10) / 10.0) // 소수점 1자리
                .details(List.of()) // 상세 기록은 별도 조회 필요
                .build();
    }
}