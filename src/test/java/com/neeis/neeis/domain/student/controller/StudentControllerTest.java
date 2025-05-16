package com.neeis.neeis.domain.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentUpdateRequestDto;
import com.neeis.neeis.domain.student.dto.res.PasswordResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentSaveResponseDto;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.global.common.StatusCode;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @MockBean private JwtProvider jwtProvider;
    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
            // JwtProvider는 위에서 MockBean으로 주입됩니다.
            return new JwtAuthenticationFilter(jwtProvider);
        }
    }


    @Test
    @DisplayName("아이디 찾기 테스트")
    public void findUsernameTest() throws Exception {
        // given
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .name("김마리")
                .phone("010-2222-2222")
                .school("인천중학교")
                .build();

        StudentResponseDto responseDto = StudentResponseDto.builder()
                .loginId("mari2022")
                .build();

        when(studentService.findUsername(any(FindIdRequestDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/students/id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.response.loginId").value("mari2022"))
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_USERNAME.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 찾기 테스트")
    public void findPasswordTest() throws Exception {
        // given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .name("김마리")
                .phone("010-2222-2222")
                .ssn("100404-4011111")
                .school("인천중학교")
                .build();

        PasswordResponseDto responseDto = PasswordResponseDto.builder()
                .password("temp1234")
                .build();

        when(studentService.findPassword(any(PasswordRequestDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/students/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_USERNAME.getMessage()));
    }

    @Test
    @DisplayName("학생 등록 테스트")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    public void registerStudentTest() throws Exception {
        // given
        StudentRequestDto requestDto = StudentRequestDto.builder()
                .role(Role.STUDENT.toString())
                .name("홍길동")
                .school("서울중학교")
                .gender("남")
                .ssn("060101-3123456")
                .address("주소")
                .phone("010-1234-5678")
                .admissionDate(LocalDate.of(2023, 3, 2))
                .build();

        MockMultipartFile jsonFile = new MockMultipartFile(
                "info",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        StudentSaveResponseDto responseDto = StudentSaveResponseDto.builder()
                .loginId("hong2023")
                .studentName("홍길동")
                .password("initial123")
                .build();

        when(studentService.saveStudent(eq("teacher"), any(StudentRequestDto.class), any(MockMultipartFile.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(multipart("/students/register")
                        .file(jsonFile)
                        .file(imageFile)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.response.loginId").value("hong2023"))
                .andExpect(jsonPath("$.response.studentName").value("홍길동"))
                .andExpect(jsonPath("$.response.password").value("initial123"))
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_STUDENTS.getMessage()));
    }

    @Test
    @DisplayName("학생 정보 수정 테스트")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    public void updateStudentTest() throws Exception {
        // given
        Long studentId = 1L;
        StudentUpdateRequestDto requestDto = StudentUpdateRequestDto.builder()
                .name("홍길동")
                .address("서울특별시 강남구 테헤란로 123")
                .phone("010-1234-5678")
                .fatherName("홍아버지")
                .motherName("홍어머니")
                .fatherPhone("010-1111-2222")
                .motherPhone("010-3333-4444")
                .build();

        MockMultipartFile jsonFile = new MockMultipartFile(
                "info",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/students/{studentId}", studentId)
                        .file(jsonFile)
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_UPDATE_STUDENTS.getMessage()));
    }
}