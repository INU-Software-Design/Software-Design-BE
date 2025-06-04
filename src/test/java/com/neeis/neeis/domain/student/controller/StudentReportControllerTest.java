package com.neeis.neeis.domain.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.student.dto.req.StudentReportRequestDto;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.student.dto.res.StudentReportResponseDto;
import com.neeis.neeis.domain.student.service.StudentReportService;
import com.neeis.neeis.global.common.StatusCode;
import com.neeis.neeis.global.config.SecurityConfig;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import com.neeis.neeis.global.report.service.PdfGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentReportController.class)
@Import(SecurityConfig.class)
class StudentReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentReportService studentReportService;

    @MockBean
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
            return new JwtAuthenticationFilter(jwtProvider);
        }
    }

    @Nested
    @DisplayName("본인 학생 보고서 조회 테스트")
    class MyStudentReportDataTest {

        @Test
        @DisplayName("학생이 본인 보고서 데이터 조회 성공")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void getMyStudentReportData_success() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .includeAttendance(true)
                    .includeCounseling(false)
                    .includeBehavior(true)
                    .build();

            // StudentDetailResDto 사용 (실제 DTO 구조)
            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .id(1L)
                    .name("김학생")
                    .teacherName("김선생")
                    .grade(2)
                    .classroom(3)
                    .number(15)
                    .gender("M")
                    .ssn("050101-3123456")
                    .address("서울시 강남구")
                    .phone("010-1234-5678")
                    .admissionDate(LocalDate.of(2023, 3, 1))
                    .fatherName("김아버지")
                    .fatherNum("010-1111-2222")
                    .motherName("김어머니")
                    .motherNum("010-3333-4444")
                    .image("student_image.jpg")
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            when(studentReportService.generateMyStudentReport(eq("student123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);

            // when & then
            mockMvc.perform(post("/reports/my/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_PDF.getMessage()))
                    .andExpect(jsonPath("$.response.studentInfo.name").value("김학생"))
                    .andExpect(jsonPath("$.response.studentInfo.teacherName").value("김선생"))
                    .andExpect(jsonPath("$.response.studentInfo.grade").value(2))
                    .andExpect(jsonPath("$.response.studentInfo.classroom").value(3))
                    .andExpect(jsonPath("$.response.studentInfo.number").value(15))
                    .andExpect(jsonPath("$.response.generatedAt").exists());
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 본인 보고서 조회 요청 실패")
        void getMyStudentReportData_unauthenticated() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .build();

            // when & then
            mockMvc.perform(post("/reports/my/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 본인 보고서 조회")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void getMyStudentReportData_invalid_request() throws Exception {
            // given - 빈 JSON 객체
            String invalidJson = "{}";

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .name("김학생")
                    .grade(2)
                    .classroom(3)
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            when(studentReportService.generateMyStudentReport(eq("student123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);

            // when & then
            mockMvc.perform(post("/reports/my/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson)
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response.studentInfo.name").value("김학생"))
                    .andExpect(jsonPath("$.response.studentInfo.grade").value(2))
                    .andExpect(jsonPath("$.response.studentInfo.classroom").value(3))
                    .andExpect(jsonPath("$.response.studentInfo.number").value(0))
                    .andExpect(jsonPath("$.response.studentInfo.fatherName").value("김아버지"))
                    .andExpect(jsonPath("$.response.studentInfo.motherName").value("김어머니"));
        }
    }

    @Nested
    @DisplayName("본인 학생 보고서 PDF 다운로드 테스트")
    class MyStudentReportPdfTest {

        @Test
        @DisplayName("학생이 본인 보고서 PDF 다운로드 성공")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void downloadMyStudentReportPdf_success() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .includeAttendance(true)
                    .build();

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .name("김학생")
                    .teacherName("김선생")
                    .grade(2)
                    .classroom(3)
                    .number(10)
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            byte[] pdfBytes = "PDF 내용".getBytes();

            when(studentReportService.generateMyStudentReport(eq("student123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);
            when(pdfGeneratorService.generateStudentReportPdf(any(StudentReportResponseDto.class), eq("student123")))
                    .thenReturn(pdfBytes);

            // when & then
            mockMvc.perform(post("/reports/my/pdf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().exists("Content-Disposition"))
                    .andExpect(content().bytes(pdfBytes));
        }

        @Test
        @DisplayName("PDF 생성 중 오류 발생 시 예외 처리")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void downloadMyStudentReportPdf_error() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .build();

            when(studentReportService.generateMyStudentReport(eq("student123"), any(StudentReportRequestDto.class)))
                    .thenThrow(new RuntimeException("PDF 생성 오류"));

            // when & then
            mockMvc.perform(post("/reports/my/pdf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("교사용 학생 보고서 조회 테스트")
    class TeacherStudentReportDataTest {

        @Test
        @DisplayName("교사가 학생 보고서 데이터 조회 성공")
        @WithMockUser(username = "teacher123", roles = {"TEACHER"})
        void getStudentReportDataByTeacher_success() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .studentId(1L)
                    .includeGrades(true)
                    .includeAttendance(true)
                    .includeCounseling(true)
                    .includeBehavior(true)
                    .build();

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .id(1L)
                    .name("김학생")
                    .teacherName("김선생")
                    .grade(2)
                    .classroom(3)
                    .number(12)
                    .gender("M")
                    .phone("010-1234-5678")
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            when(studentReportService.generateStudentReportByTeacher(eq("teacher123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);

            // when & then
            mockMvc.perform(post("/reports/teacher/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_PDF.getMessage()))
                    .andExpect(jsonPath("$.response.studentInfo.name").value("김학생"))
                    .andExpect(jsonPath("$.response.studentInfo.teacherName").value("김선생"))
                    .andExpect(jsonPath("$.response.studentInfo.grade").value(2))
                    .andExpect(jsonPath("$.response.studentInfo.classroom").value(3))
                    .andExpect(jsonPath("$.response.generatedAt").exists());
        }

        @Test
        @DisplayName("학생 권한으로 교사용 보고서 조회 시도 시 실패")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void getStudentReportDataByTeacher_forbidden_student() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .studentId(1L)
                    .includeGrades(true)
                    .build();

            // when & then
            mockMvc.perform(post("/reports/teacher/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("studentId 없이 교사용 보고서 조회 요청")
        @WithMockUser(username = "teacher123", roles = {"TEACHER"})
        void getStudentReportDataByTeacher_missing_studentId() throws Exception {
            // given - studentId 없는 요청
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .build();

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .name("기본학생")
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            when(studentReportService.generateStudentReportByTeacher(eq("teacher123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);

            // when & then
            mockMvc.perform(post("/reports/teacher/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response.studentInfo.name").value("기본학생"));
        }
    }

    @Nested
    @DisplayName("교사용 학생 보고서 PDF 다운로드 테스트")
    class TeacherStudentReportPdfTest {

        @Test
        @DisplayName("교사가 학생 보고서 PDF 다운로드 성공")
        @WithMockUser(username = "teacher123", roles = {"TEACHER"})
        void downloadStudentReportPdfByTeacher_success() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .studentId(1L)
                    .includeGrades(true)
                    .includeAttendance(true)
                    .build();

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .name("김학생")
                    .teacherName("김선생")
                    .grade(2)
                    .classroom(3)
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            byte[] pdfBytes = "교사용 PDF 내용".getBytes();

            when(studentReportService.generateStudentReportByTeacher(eq("teacher123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);
            when(pdfGeneratorService.generateStudentReportPdf(any(StudentReportResponseDto.class), eq("teacher123")))
                    .thenReturn(pdfBytes);

            // when & then
            mockMvc.perform(post("/reports/teacher/pdf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().exists("Content-Disposition"))
                    .andExpect(content().bytes(pdfBytes));
        }

        @Test
        @DisplayName("관리자가 학생 보고서 PDF 다운로드 성공")
        @WithMockUser(username = "admin123", roles = {"ADMIN"})
        void downloadStudentReportPdfByTeacher_admin_success() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .studentId(1L)
                    .includeGrades(true)
                    .build();

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .name("김학생")
                    .teacherName("김선생")
                    .grade(2)
                    .classroom(3)
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            byte[] pdfBytes = "관리자용 PDF 내용".getBytes();

            when(studentReportService.generateStudentReportByTeacher(eq("admin123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);
            when(pdfGeneratorService.generateStudentReportPdf(any(StudentReportResponseDto.class), eq("admin123")))
                    .thenReturn(pdfBytes);

            // when & then
            mockMvc.perform(post("/reports/teacher/pdf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        }
    }

    @Nested
    @DisplayName("패스워드 정보 조회 테스트")
    class PasswordInfoTest {

        @Test
        @DisplayName("학생 사용자의 패스워드 정보 조회")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void getPasswordInfo_student() throws Exception {
            // given
            when(pdfGeneratorService.getUserRoleByUsername("student123")).thenReturn("STUDENT");

            // when & then
            mockMvc.perform(get("/reports/password-info")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("PDF 문서의 암호는 본인의 생년월일 6자리입니다."))
                    .andExpect(jsonPath("$.format").value("YYMMDD (예: 051225)"))
                    .andExpect(jsonPath("$.example").value("2005년 12월 25일생 → 051225"))
                    .andExpect(jsonPath("$.role").value("STUDENT"))
                    .andExpect(jsonPath("$.username").value("student123"));
        }

        @Test
        @DisplayName("교사 사용자의 패스워드 정보 조회")
        @WithMockUser(username = "teacher123", roles = {"TEACHER"})
        void getPasswordInfo_teacher() throws Exception {
            // given
            when(pdfGeneratorService.getUserRoleByUsername("teacher123")).thenReturn("TEACHER");

            // when & then
            mockMvc.perform(get("/reports/password-info")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("PDF 문서의 암호는 선생님의 핸드폰 번호 뒷자리 4자리입니다."))
                    .andExpect(jsonPath("$.format").value("핸드폰 뒷자리 4자리 (예: 1234)"))
                    .andExpect(jsonPath("$.example").value("010-1234-5678 → 5678"))
                    .andExpect(jsonPath("$.role").value("TEACHER"))
                    .andExpect(jsonPath("$.username").value("teacher123"));
        }

        @Test
        @DisplayName("부모 사용자의 패스워드 정보 조회")
        @WithMockUser(username = "parent123", roles = {"PARENT"})
        void getPasswordInfo_parent() throws Exception {
            // given
            when(pdfGeneratorService.getUserRoleByUsername("parent123")).thenReturn("PARENT");

            // when & then
            mockMvc.perform(get("/reports/password-info")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("PDF 문서의 암호는 자녀의 생년월일 6자리입니다."))
                    .andExpect(jsonPath("$.format").value("YYMMDD (예: 051225)"))
                    .andExpect(jsonPath("$.example").value("자녀가 2005년 12월 25일생 → 051225"))
                    .andExpect(jsonPath("$.role").value("PARENT"))
                    .andExpect(jsonPath("$.username").value("parent123"));
        }

        @Test
        @DisplayName("알 수 없는 역할의 패스워드 정보 조회")
        @WithMockUser(username = "unknown123", roles = {"UNKNOWN"})
        void getPasswordInfo_unknown_role() throws Exception {
            // given
            when(pdfGeneratorService.getUserRoleByUsername("unknown123")).thenReturn("UNKNOWN");

            // when & then
            mockMvc.perform(get("/reports/password-info")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("패스워드 정보를 확인할 수 없습니다."))
                    .andExpect(jsonPath("$.format").value("관리자에게 문의하세요."))
                    .andExpect(jsonPath("$.role").value("UNKNOWN"))
                    .andExpect(jsonPath("$.username").value("unknown123"));
        }

        @Test
        @DisplayName("패스워드 정보 조회 중 예외 발생")
        @WithMockUser(username = "error123", roles = {"STUDENT"})
        void getPasswordInfo_exception() throws Exception {
            // given
            when(pdfGeneratorService.getUserRoleByUsername("error123"))
                    .thenThrow(new RuntimeException("서비스 오류"));

            // when & then
            mockMvc.perform(get("/reports/password-info")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("패스워드 정보를 조회할 수 없습니다."));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 패스워드 정보 조회 실패")
        void getPasswordInfo_unauthenticated() throws Exception {
            // when & then
            mockMvc.perform(get("/reports/password-info")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("보고서 데이터 구조 검증 테스트")
    class ReportDataStructureTest {

        @Test
        @DisplayName("전체 보고서 옵션 포함한 데이터 구조 검증")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void getStudentReportData_full_structure() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .includeAttendance(true)
                    .includeCounseling(true)
                    .includeBehavior(true)
                    .build();

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .name("김학생")
                    .grade(2)
                    .classroom(3)
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .attendance(null) // 실제로는 AttendanceReportDto 객체
                    .grades(null)     // 실제로는 GradesReportDto 객체
                    .counseling(null) // 실제로는 CounselingReportDto 객체
                    .behavior(null)   // 실제로는 BehaviorReportDto 객체
                    .parents(null)    // 실제로는 ParentReportDto 객체
                    .scoreFeedbacks(null) // 실제로는 List<SubjectFeedbackDto>
                    .attendanceFeedback(null) // 실제로는 AttendanceFeedbackResDto
                    .generatedAt(LocalDateTime.now())
                    .build();

            when(studentReportService.generateMyStudentReport(eq("student123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);

            // when & then
            mockMvc.perform(post("/reports/my/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response.studentInfo").exists())
                    .andExpect(jsonPath("$.response.attendance").exists())
                    .andExpect(jsonPath("$.response.grades").exists())
                    .andExpect(jsonPath("$.response.counseling").exists())
                    .andExpect(jsonPath("$.response.behavior").exists())
                    .andExpect(jsonPath("$.response.parents").exists())
                    .andExpect(jsonPath("$.response.scoreFeedbacks").exists())
                    .andExpect(jsonPath("$.response.attendanceFeedback").exists())
                    .andExpect(jsonPath("$.response.generatedAt").exists());
        }

        @Test
        @DisplayName("선택적 보고서 옵션에 따른 데이터 검증")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void getStudentReportData_selective_options() throws Exception {
            // given - 성적과 출석만 포함
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .includeAttendance(true)
                    .includeCounseling(false)
                    .includeBehavior(false)
                    .build();

            StudentDetailResDto studentInfo = StudentDetailResDto.builder()
                    .id(1L)
                    .name("김학생")
                    .teacherName("김선생")
                    .grade(2)
                    .classroom(3)
                    .number(8)
                    .gender("F")
                    .phone("010-9876-5432")
                    .fatherName("김아버지")
                    .motherName("김어머니")
                    .build();

            StudentReportResponseDto responseDto = StudentReportResponseDto.builder()
                    .studentInfo(studentInfo)
                    .generatedAt(LocalDateTime.now())
                    .build();

            when(studentReportService.generateMyStudentReport(eq("student123"), any(StudentReportRequestDto.class)))
                    .thenReturn(responseDto);

            // when & then
            mockMvc.perform(post("/reports/my/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response.studentInfo.name").value("김학생"))
                    .andExpect(jsonPath("$.response.generatedAt").exists());
        }
    }

    @Nested
    @DisplayName("CSRF 및 보안 테스트")
    class SecurityTest {

        @Test
        @DisplayName("CSRF 토큰 없이 POST 요청 시 실패")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void post_request_without_csrf_token() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .build();

            // when & then - CSRF 토큰 없이 요청
            mockMvc.perform(post("/reports/my/data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("잘못된 Content-Type으로 요청 시 실패")
        @WithMockUser(username = "student123", roles = {"STUDENT"})
        void post_request_wrong_content_type() throws Exception {
            // given
            StudentReportRequestDto requestDto = StudentReportRequestDto.builder()
                    .includeGrades(true)
                    .build();

            // when & then - 잘못된 Content-Type
            mockMvc.perform(post("/reports/my/data")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());
        }
    }
}