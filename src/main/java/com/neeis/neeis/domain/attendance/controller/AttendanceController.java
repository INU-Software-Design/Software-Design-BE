package com.neeis.neeis.domain.attendance.controller;

import com.neeis.neeis.domain.attendance.dto.req.AttendanceBulkRequestDto;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceFeedbackReqDto;
import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.attendance.service.AttendanceService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "담임 학생 월별 출결 저장", description = "로그인한 교사의 월별 출결을 저장합니다. <br>" +
            "출결은 당월 저장만 가능합니다. <br>" +
            "조회하려는 학급의 년도, 학년, 반이 필수로 입력해야합니다. <br>" +
            "status : PRESENT(출석), ABSENT(결석), LATE(지각), EARLY(조퇴)  ")
    public ResponseEntity<CommonResponse<Object>> saveAttendance(@AuthenticationPrincipal UserDetails userDetails,
                                                           @Valid @RequestBody AttendanceBulkRequestDto attendanceBulkRequestDto){
        attendanceService.saveAttendance(userDetails.getUsername(), attendanceBulkRequestDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_ATTENDANCE.getMessage()));
    }

    @GetMapping
    @Operation(summary = "담임 학생 월별 출결 조회", description = "로그인한 교사의 월별 출결을 조회합니다. <br>" +
            "조회하려는 학급의 년도, 학년, 반이 필수로 입력해야합니다. <br>" +
            "월(month)은 선택사항이며, 입력하지 않으면 당월을 기본값으로 조회합니다. <br>" +
            "(담당 학급이 아닌 경우 접근이 제한됩니다.)" )
    public ResponseEntity<CommonResponse<List<StudentAttendanceResDto>>> getAttendances(@AuthenticationPrincipal UserDetails userDetails,
                                                                                        @RequestParam("year") @Parameter(description = "연도") int year,
                                                                                        @RequestParam("grade") @Parameter(description = "학년") int grade,
                                                                                        @RequestParam("classNum") @Parameter(description = "반") int classNum,
                                                                                        @RequestParam(value = "month", required = false) @Parameter(description = "월(선택)") Integer month){
        if (month == null) {
            month = LocalDate.now().getMonthValue(); // 현재 달로 기본 설정
        }
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_ATTENDANCE.getMessage(),
                attendanceService.getAttendances(userDetails.getUsername(), year, grade, classNum, month)));
    }

    @GetMapping("/student")
    @Operation(summary = "담임 학생 월별 출결 개별 조회", description = "로그인한 교사의 학급 월별 출결을 개별 조회합니다. <br>" +
            "조회하려는 학급의 년도, 학년, 반이 필수로 입력해야합니다. <br>" +
            "월(month)은 선택사항이며, 입력하지 않으면 당월을 기본값으로 조회합니다. <br>" +
            "(담당 학급이 아닌 경우 접근이 제한됩니다.)" )
    public ResponseEntity<CommonResponse<StudentAttendanceResDto>> getOneAttendance(@AuthenticationPrincipal UserDetails userDetails,
                                                                                    @RequestParam("year") @Parameter(description = "연도") int year,
                                                                                    @RequestParam("grade") @Parameter(description = "학년") int grade,
                                                                                    @RequestParam("classNum") @Parameter(description = "반") int classNum,
                                                                                    @RequestParam("number") @Parameter(description = "번호") int number,
                                                                                    @RequestParam(value = "month", required = false) @Parameter(description = "월(선택)") Integer month){
        if (month == null) {
            month = LocalDate.now().getMonthValue(); // 현재 달로 기본 설정
        }
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_ATTENDANCE.getMessage(),
                attendanceService. getStudentMonthlyAttendance(userDetails.getUsername(), year, grade, classNum, number, month)));
    }

    @GetMapping("/summary")
    @Operation(summary = "담임 학생 출결 통계 조회", description = "로그인한 교사의 학급 학생의 출결을 통계냅니다. <br>" +
            "조회하려는 학급과 학생의 년도, 학년, 반, 번호이 필수로 입력해야합니다. <br>" +
            "추가로 출석 통계는 '학기당 수업 일수'로 계산되므로, 반드시 '학기'를 입력해야 합니다.")
    public ResponseEntity<CommonResponse<StudentAttendanceSummaryDto>> getStudentAttendanceSummary( @AuthenticationPrincipal UserDetails userDetails,
                                                                                                    @RequestParam("year") @Parameter(description = "연도") int year,
                                                                                                    @RequestParam("semester") @Parameter(description = "학기") int semester,
                                                                                                    @RequestParam("grade") @Parameter(description = "학년") int grade,
                                                                                                    @RequestParam("classNum") @Parameter(description = "반") int classNum,
                                                                                                    @RequestParam("number") @Parameter(description = "번호") int number) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_ATTENDANCE.getMessage(),
                attendanceService.getStudentAttendanceSummary(userDetails.getUsername(), year, semester, grade, classNum, number)));
    }

    @PostMapping("/feedback")
    @Operation(summary = "담임 학생 출결 피드백 작성", description = "로그인한 교사가 담당 학급 학생에 대해 출결 피드백을 작성합니다. <br>" +
                    "학급의 연도, 학년, 반, 번호 정보를 입력해야 하며, 피드백은 본인 학급 학생에 대해서만 작성할 수 있습니다.")
    public ResponseEntity<CommonResponse<AttendanceFeedbackResDto>> saveFeedback(@AuthenticationPrincipal UserDetails userdetails,
                                                               @RequestParam("year") @Parameter(description = "연도") int year,
                                                               @RequestParam("grade") @Parameter(description = "학년") int grade,
                                                               @RequestParam("classNum") @Parameter(description = "반") int classNum,
                                                               @RequestParam("number") @Parameter(description = "번호") int number,
                                                               @Valid @RequestBody AttendanceFeedbackReqDto attendanceFeedbackDto) {
        AttendanceFeedbackResDto dto =  attendanceService.saveFeedback(userdetails.getUsername(),  year, grade, classNum, number, attendanceFeedbackDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_FEEDBACK.getMessage(), dto));
    }

    @PutMapping("/feedback/{feedbackId}")
    @Operation(summary = "담임 학생 출결 피드백 수정", description = "로그인한 교사가 작성한 학생 출결 피드백을 수정합니다. <br>" +
                    "피드백 ID를 경로로 입력하고, 수정할 피드백 내용을 요청 본문에 작성합니다. <br>" +
                    "본인이 작성한 피드백만 수정할 수 있습니다.")
    public ResponseEntity<CommonResponse<AttendanceFeedbackResDto>> updateFeedback(@AuthenticationPrincipal UserDetails userdetails,
                                                                 @PathVariable Long feedbackId,
                                                                 @Valid @RequestBody AttendanceFeedbackReqDto attendanceFeedbackDto) {

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_FEEDBACK.getMessage(),attendanceService.updateFeedback(userdetails.getUsername(), feedbackId, attendanceFeedbackDto)));
    }

    @GetMapping("/feedback")
    @Operation(summary = "담임 학생 출결 피드백 조회", description =
            "로그인한 교사가 담당 학생의 출결 피드백을 조회합니다. <br>" +
                    "학급의 연도, 학년, 반, 번호 정보를 입력하여 해당 학생의 피드백을 가져옵니다.")
    public ResponseEntity<CommonResponse<AttendanceFeedbackResDto>> getFeedback(@AuthenticationPrincipal UserDetails userDetails,
                                                                                @RequestParam("year") @Parameter(description = "연도") int year,
                                                                                @RequestParam("grade") @Parameter(description = "학년") int grade,
                                                                                @RequestParam("classNum") @Parameter(description = "반") int classNum,
                                                                                @RequestParam("number") @Parameter(description = "번호") int number) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_FEEDBACK.getMessage(), attendanceService.getFeedback(userDetails.getUsername(),year, grade, classNum, number)));
    }
}
