package com.neeis.neeis.domain.student.dto.report;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class AttendanceDetailDto {
    private LocalDate date;
    private String status;
    private String reason;
}