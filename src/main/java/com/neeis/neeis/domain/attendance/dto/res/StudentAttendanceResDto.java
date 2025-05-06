package com.neeis.neeis.domain.attendance.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.neeis.neeis.domain.attendance.Attendance;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudentAttendanceResDto {
    private final Long studentId;
    private final String studentName;
    private final int number;
    private final List<DailyAttendanceDto> attendances;

    @Builder
    private StudentAttendanceResDto(Long studentId, String studentName, int number, List<DailyAttendanceDto> attendances) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.number = number;
        this.attendances = attendances;
    }

    public static StudentAttendanceResDto toDto(ClassroomStudent classroomStudent, Attendance attendance, List<DailyAttendanceDto> dailyAttendanceDtos) {
        return StudentAttendanceResDto.builder()
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getName())
                .number(classroomStudent.getNumber())
                .attendances(dailyAttendanceDtos)
                .build();
    }
}
