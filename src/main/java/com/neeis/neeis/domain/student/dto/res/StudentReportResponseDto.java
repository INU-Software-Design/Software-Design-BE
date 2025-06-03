package com.neeis.neeis.domain.student.dto.res;

import com.neeis.neeis.domain.student.dto.report.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StudentReportResponseDto {
    private StudentDetailResDto studentInfo;           // 기존 DTO 그대로 사용
    private AttendanceReportDto attendance;            // 새로 생성
    private GradesReportDto grades;                    // 새로 생성
    private CounselingReportDto counseling;            // 새로 생성
    private BehaviorReportDto behavior;                // 새로 생성
    private LocalDateTime generatedAt;
    private ParentReportDto parents;
}
