package com.neeis.neeis.domain.subject.dto.res;


import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StudentReportResponseDto {
    private StudentDetailResDto studentInfo;
    private StudentAttendanceSummaryDto attendance;
    private StudentScoreSummaryDto grades;  // ScoreSummary 기반으로 변경
    private CounselDetailDto counseling;
    private BehaviorDetailResponseDto behavior;
    private LocalDateTime generatedAt;
}