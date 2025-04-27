package com.neeis.neeis.domain.attendance.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StudentAttendanceDto {
    @Schema(example = "1")
    private Long studentId;

    private List<DailyAttendanceDto> attendances;

    @Builder
    private StudentAttendanceDto(Long studentId, List<DailyAttendanceDto> attendances) {
        this.studentId = studentId;
        this.attendances = attendances;
    }
}
