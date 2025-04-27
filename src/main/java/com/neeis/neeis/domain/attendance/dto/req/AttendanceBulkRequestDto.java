package com.neeis.neeis.domain.attendance.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AttendanceBulkRequestDto {

    @NotNull(message = "년도는 필수 입력 값입니다.")
    @Schema(example = "2025")
    private int year;

    @NotNull(message = "월은 필수 입력 값입니다.")
    @Schema(example = "4")
    private int month;

    @NotNull(message = "학년은 필수 입력 값입니다.")
    @Schema(example = "1")
    private int grade;

    @NotNull(message = "반은 필수 입력 값입니다.")
    @Schema(example = "2")
    private int classNumber;

    private List<StudentAttendanceDto> students;

    @Builder
    private AttendanceBulkRequestDto(int year, int month, int grade, int classNumber, List<StudentAttendanceDto> students) {
        this.year = year;
        this.month = month;
        this.grade = grade;
        this.classNumber = classNumber;
        this.students = students;
    }
}