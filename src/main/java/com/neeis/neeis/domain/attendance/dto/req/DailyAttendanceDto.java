package com.neeis.neeis.domain.attendance.dto.req;

import com.neeis.neeis.domain.attendance.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DailyAttendanceDto {

    @Schema(example = "2025-04-01")
    private LocalDate date;

    @Schema(example = "ABSENT")
    private AttendanceStatus status;

    @Builder
    private DailyAttendanceDto(LocalDate date, AttendanceStatus status) {
        this.date = date;
        this.status = status != null ? status : AttendanceStatus.PRESENT;
    }
}