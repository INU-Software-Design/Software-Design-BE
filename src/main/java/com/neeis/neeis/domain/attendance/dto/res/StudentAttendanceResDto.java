package com.neeis.neeis.domain.attendance.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.neeis.neeis.domain.attendance.Attendance;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudentAttendanceResDto {
    private final Long studentId;
    private final String studentName;
    private final List<DailyAttendanceDto> attendances;

    @Builder
    private StudentAttendanceResDto(Long studentId, String studentName, List<DailyAttendanceDto> attendances) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.attendances = attendances;
    }

    public static StudentAttendanceResDto toDto(Attendance attendance, List<DailyAttendanceDto> dailyAttendanceDtos) {
        return StudentAttendanceResDto.builder()
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getName())
                .attendances(dailyAttendanceDtos)
                .build();
    }
}
