package com.neeis.neeis.domain.teacher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.teacher.dto.ClassroomStudentDto;
import com.neeis.neeis.domain.teacher.dto.StudentResponseDto;
import com.neeis.neeis.domain.teacher.dto.TeacherResponseDto;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.common.CommonResponse;
import com.neeis.neeis.global.config.SecurityConfig;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherController.class)
@Import(SecurityConfig.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeacherService teacherService;

    private final String USERNAME = "teacher123";

    @MockBean private JwtProvider jwtProvider;
    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
            // JwtProvider는 위에서 MockBean으로 주입됩니다.
            return new JwtAuthenticationFilter(jwtProvider);
        }
    }


    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("교사의 담당 학생 목록 조회 - 성공")
    @WithMockUser(username = USERNAME, roles = "TEACHER")
    void getStudents_Success() throws Exception {
        // given
        ClassroomStudentDto expectedResponse = createMockClassroomStudentDto();
        given(teacherService.getStudentsFlexible(anyString(), anyInt(), any(), any()))
                .willReturn(expectedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/teachers/students")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("학생 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.response.year").value(2025))
                .andExpect(jsonPath("$.response.grade").value(3))
                .andExpect(jsonPath("$.response.classNum").value(2))
                .andExpect(jsonPath("$.response.students").isArray())
                .andExpect(jsonPath("$.response.students[0].studentId").value(1))
                .andExpect(jsonPath("$.response.students[0].number").value(1))
                .andExpect(jsonPath("$.response.students[0].name").value("홍길동"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        // $.response 방식으로 추출
        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println(jsonResponse);

        verify(teacherService, times(1)).getStudentsFlexible(anyString(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("교사의 담당 학생 목록 조회 - 학년/반 지정")
    @WithMockUser(username = USERNAME, roles = "TEACHER")
    void getStudents_WithGradeAndClass_Success() throws Exception {
        // given
        ClassroomStudentDto expectedResponse = createMockClassroomStudentDto();
        given(teacherService.getStudentsFlexible(anyString(), anyInt(), any(), any()))
                .willReturn(expectedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/teachers/students")
                .param("year", "2025")
                .param("grade", "3")
                .param("classNum", "2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("학생 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.response.year").value(2025))
                .andExpect(jsonPath("$.response.grade").value(3))
                .andExpect(jsonPath("$.response.classNum").value(2))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        // $.response 방식으로 추출
        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println(jsonResponse);

        verify(teacherService, times(1)).getStudentsFlexible(anyString(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("학생 개별 조회 - 성공")
    @WithMockUser(username = USERNAME, roles = "TEACHER")
    void getStudentDetails_Success() throws Exception {
        // given
        Long studentId = 1L;
        StudentDetailResDto expectedResponse = createMockStudentDetailResDto();
        given(teacherService.getStudentDetail(anyString(), anyLong(), anyInt()))
                .willReturn(expectedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/teachers/students/{studentId}", studentId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("학생 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.response.id").value(1))
                .andExpect(jsonPath("$.response.name").value("홍길동"))
                .andExpect(jsonPath("$.response.grade").value(3))
                .andExpect(jsonPath("$.response.classroom").value(2))
                .andExpect(jsonPath("$.response.number").value(1))
                .andExpect(jsonPath("$.response.teacherName").value("김선생"))
                .andExpect(jsonPath("$.response.gender").value("남"))
                .andExpect(jsonPath("$.response.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.response.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.response.fatherName").value("홍아버지"))
                .andExpect(jsonPath("$.response.fatherNum").value("010-9876-5432"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        // $.response 방식으로 추출
        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println(jsonResponse);
    }

    @Test
    @DisplayName("교사 개인 정보 조회 - 성공")
    @WithMockUser(username = USERNAME, roles = "TEACHER")
    void getMyProfile_Success() throws Exception {
        // given
        TeacherResponseDto expectedResponse = createMockTeacherResponseDto();
        given(teacherService.getProfile(anyString()))
                .willReturn(expectedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/teachers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("교사 정보 조회 성공"))
                .andExpect(jsonPath("$.response.teacherId").value(1))
                .andExpect(jsonPath("$.response.name").value("김선생"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        // $.response 방식으로 추출
        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println(jsonResponse);
    }

    // 테스트용 Mock 데이터 생성 메서드
    private ClassroomStudentDto createMockClassroomStudentDto() {
        StudentResponseDto student1 = StudentResponseDto.builder().studentId(1L).number(1).name("홍길동").build();
        StudentResponseDto student2 = StudentResponseDto.builder().studentId(2L).number(2).name("김철수").build();
        List<StudentResponseDto> students = Arrays.asList(student1, student2);

        return ClassroomStudentDto.builder()
                .year(2025)
                .grade(3)
                .classNum(2)
                .students(students)
                .build();
    }

    private StudentDetailResDto createMockStudentDetailResDto() {
        return StudentDetailResDto.builder()
                .id(1L)
                .teacherName("김선생")
                .image("student1.jpg")
                .grade(3)
                .classroom(2)
                .number(1)
                .gender("남")
                .name("홍길동")
                .ssn("123456-1234567")
                .address("서울시 강남구")
                .phone("010-1234-5678")
                .admissionDate(LocalDate.of(2020, 3, 2))
                .fatherName("홍아버지")
                .fatherNum("010-9876-5432")
                .motherName("홍어머니")
                .motherNum("010-5678-1234")
                .build();
    }

    private TeacherResponseDto createMockTeacherResponseDto() {
        TeacherResponseDto dto = TeacherResponseDto.builder()
                .teacherId(1L)
                .name("김선생")
                .phone("010-1111-2222")
                .email("teacher@school.com")
                .build();

        return dto;
    }
}