package com.neeis.neeis.domain.student.dto.res;

import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.student.dto.report.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StudentReportResponseDto {
    private StudentDetailResDto studentInfo;           // 기존 DTO 그대로 사용
    private AttendanceReportDto attendance;            // 새로 생성
    private GradesReportDto grades;                    // 새로 생성
    private CounselingReportDto counseling;            // 새로 생성
    private BehaviorReportDto behavior;                // 새로 생성
    private ParentReportDto parents;
    private List<SubjectFeedbackDto> scoreFeedbacks;           // 과목별 성적 피드백 목록
    private AttendanceFeedbackResDto attendanceFeedback;       // 전체 출결 피드백 (교사가 작성한 한 줄 피드백)
    private LocalDateTime generatedAt;
}
