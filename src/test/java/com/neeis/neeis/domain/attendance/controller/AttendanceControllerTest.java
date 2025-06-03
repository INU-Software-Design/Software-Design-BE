package com.neeis.neeis.domain.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceBulkRequestDto;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceFeedbackReqDto;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.req.StudentAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.attendance.service.AttendanceService;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AttendanceService attendanceService;
    @MockBean JwtProvider jwtProvider;
    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
            // JwtProvider는 위에서 MockBean으로 주입됩니다.
            return new JwtAuthenticationFilter(jwtProvider);
        }
    }

    @Test
    @DisplayName("POST /attendances → 출결 저장 호출")
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void saveAttendance() throws Exception {
        var dto = AttendanceBulkRequestDto.builder()
                .year(2025).month(4).grade(2).classNumber(1)
                .students(List.of(
                        StudentAttendanceDto.builder()
                                .studentId(123L)
                                .attendances(List.of(
                                        DailyAttendanceDto.builder()
                                                .date(LocalDate.of(2025,4,1))
                                                .status(com.neeis.neeis.domain.attendance.AttendanceStatus.ABSENT)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        mockMvc.perform(post("/attendances")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_POST_ATTENDANCE.getMessage()))
                .andExpect(jsonPath("$.response").doesNotExist());

        then(attendanceService).should()
                .saveOrUpdateAttendance(eq("teacher1"), any(AttendanceBulkRequestDto.class));
    }

    @Test
    @DisplayName("GET /attendances → 학급 출결 리스트 조회")
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void getAttendances() throws Exception {
        var sample = StudentAttendanceResDto.builder()
                .studentId(123L)
                .studentName("철수")
                .attendances(List.of())
                .build();
        given(attendanceService.getAttendances("teacher1", 2025,2,1,4))
                .willReturn(List.of(sample));

        mockMvc.perform(get("/attendances")
                        .param("year","2025")
                        .param("grade","2")
                        .param("classNum","1")
                        .param("month","4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_ATTENDANCE.getMessage()))
                .andExpect(jsonPath("$.response[0].studentName").value("철수"));

        then(attendanceService).should()
                .getAttendances("teacher1", 2025,2,1,4);
    }

    @Test
    @DisplayName("GET /attendances/student → 개별 학생 출결 조회")
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void getOneAttendance() throws Exception {
        var sample = StudentAttendanceResDto.builder()
                .studentId(123L)
                .studentName("영희")
                .attendances(List.of())
                .build();
        given(attendanceService.getStudentMonthlyAttendance("teacher1",2025,2,1,5,4))
                .willReturn(sample);

        mockMvc.perform(get("/attendances/student")
                        .param("year","2025")
                        .param("grade","2")
                        .param("classNum","1")
                        .param("number","5")
                        .param("month","4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_ATTENDANCE.getMessage()))
                .andExpect(jsonPath("$.response.studentName").value("영희"));

        then(attendanceService).should()
                .getStudentMonthlyAttendance("teacher1",2025,2,1,5,4);
    }

    @Test
    @DisplayName("GET /attendances/summary → 학생 출결 통계 조회")
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void getStudentAttendanceSummary() throws Exception {
        var summary = StudentAttendanceSummaryDto.builder()
                .studentId(123L)
                .studentName("민수")
                .totalSchoolDays(20)
                .presentDays(18)
                .absentDays(1)
                .lateDays(1)
                .leaveEarlyDays(0)
                .build();
        given(attendanceService.getStudentAttendanceSummary("teacher1",2025,1,2,1,5))
                .willReturn(summary);

        mockMvc.perform(get("/attendances/summary")
                        .param("year","2025")
                        .param("semester","1")
                        .param("grade","2")
                        .param("classNum","1")
                        .param("number","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_ATTENDANCE.getMessage()))
                .andExpect(jsonPath("$.response.absentDays").value(1));

        then(attendanceService).should()
                .getStudentAttendanceSummary("teacher1",2025,1,2,1,5);
    }

    @Test
    @DisplayName("POST /attendances/feedback → 피드백 작성")
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void saveFeedback() throws Exception {
        var req = AttendanceFeedbackReqDto.builder()
                .feedback("잘했어요")
                .build();
        var resDto = AttendanceFeedbackResDto.builder()
                .feedbackId(999L)
                .feedback("잘했어요")
                .build();
        given(attendanceService.saveFeedback(
                        eq("teacher1"),
                        eq(2025),
                        eq(2),
                        eq(1),
                        eq(5),
                        any(AttendanceFeedbackReqDto.class)))
                .willReturn(resDto);

        mockMvc.perform(post("/attendances/feedback")
                        .param("year","2025")
                        .param("grade","2")
                        .param("classNum","1")
                        .param("number","5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_POST_FEEDBACK.getMessage()))
                .andExpect(jsonPath("$.response.feedbackId").value(999))
                .andExpect(jsonPath("$.response.feedback").value("잘했어요"));

        then(attendanceService).should().saveFeedback(
                eq("teacher1"),
                eq(2025),
                eq(2),
                eq(1),
                eq(5),
                any(AttendanceFeedbackReqDto.class));
    }

    @Test
    @DisplayName("PUT /attendances/feedback → 피드백 수정")
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void updateFeedback() throws Exception {
        AttendanceFeedbackReqDto req = AttendanceFeedbackReqDto.builder()
                .feedback("수정된 피드백")
                .build();
        AttendanceFeedbackResDto resDto = AttendanceFeedbackResDto.builder()
                .feedbackId(999L)
                .feedback("수정된 피드백")
                .build();
        given(attendanceService.updateFeedback(eq("teacher1"),
                eq(2025),
                eq(2),
                eq(1),
                eq(5),
                any(AttendanceFeedbackReqDto.class)))
                .willReturn(resDto);

        mockMvc.perform(put("/attendances/feedback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_POST_FEEDBACK.getMessage()))
                .andExpect(jsonPath("$.response.feedback").value("수정된 피드백"));

        then(attendanceService).should()
                .updateFeedback(eq("teacher1"),
                        eq(2025),
                        eq(2),
                        eq(1),
                        eq(5),
                        any(AttendanceFeedbackReqDto.class));
    }

    @Test
    @DisplayName("GET /attendances/feedback → 피드백 조회")
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void getFeedback() throws Exception {
        var resDto = AttendanceFeedbackResDto.builder()
                .feedbackId(888L)
                .feedback("내용")
                .build();
        given(attendanceService.getFeedback("teacher1",2025,2,1,5))
                .willReturn(resDto);

        mockMvc.perform(get("/attendances/feedback")
                        .param("year","2025")
                        .param("grade","2")
                        .param("classNum","1")
                        .param("number","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_FEEDBACK.getMessage()))
                .andExpect(jsonPath("$.response.feedbackId").value(888));

        then(attendanceService).should()
                .getFeedback("teacher1",2025,2,1,5);
    }
}