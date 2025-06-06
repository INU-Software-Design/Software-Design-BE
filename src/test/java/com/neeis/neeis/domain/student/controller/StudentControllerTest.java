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
import com.neeis.neeis.global.common.StatusCode;
import com.neeis.neeis.global.config.SecurityConfig;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

    @MockBean
    private StudentService studentService;

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

    @Test
    @DisplayName("아이디 찾기 성공 테스트")
    public void findUsername_success() throws Exception {
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
    @DisplayName("아이디 찾기 - 유효성 검사 실패")
    public void findUsername_validation_fail() throws Exception {
        // given - 빈 이름으로 요청
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .name("") // 빈 이름
                .phone("")
                .school("인천중학교")
                .build();

        // when & then
        mockMvc.perform(post("/students/id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 찾기 성공 테스트")
    public void findPassword_success() throws Exception {
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
                .andExpect(jsonPath("$.response.password").value("temp1234"))
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_PASSWORD.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 찾기 - 유효성 검사 실패")
    public void findPassword_validation_fail() throws Exception {
        // given - SSN 누락
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .name("김마리")
                .phone("010-2222-2222")
                .school("인천중학교")
                // ssn 누락
                .build();

        // when & then
        mockMvc.perform(post("/students/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("학생 등록 성공 테스트")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    public void registerStudent_success() throws Exception {
        // given
        StudentRequestDto requestDto = StudentRequestDto.builder()
                .role("STUDENT")
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

        when(studentService.saveStudent(eq("teacher"), any(StudentRequestDto.class), any(MultipartFile.class)))
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
    @DisplayName("학생 등록 - 이미지 없이 성공")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void registerStudent_success_without_image() throws Exception {
        // given
        StudentRequestDto requestDto = StudentRequestDto.builder()
                .role("STUDENT")
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

        // 빈 이미지 파일
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        StudentSaveResponseDto responseDto = StudentSaveResponseDto.builder()
                .loginId("hong2023")
                .studentName("홍길동")
                .password("initial123")
                .build();

        when(studentService.saveStudent(eq("admin"), any(StudentRequestDto.class), any(MultipartFile.class)))
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
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_STUDENTS.getMessage()));
    }

    @Test
    @DisplayName("학생 등록 - 인증되지 않은 사용자")
    public void registerStudent_unauthenticated() throws Exception {
        // given
        StudentRequestDto requestDto = StudentRequestDto.builder()
                .role("STUDENT")
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

        // when & then - 인증 없이 요청
        mockMvc.perform(multipart("/students/register")
                        .file(jsonFile)
                        .file(imageFile)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("학생 정보 수정 성공 테스트")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    public void updateStudent_success() throws Exception {
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

        doNothing().when(studentService).updateStudent(eq("teacher"), eq(studentId), any(StudentUpdateRequestDto.class), any(MultipartFile.class));

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

    @Test
    @DisplayName("학생 정보 수정 - 이미지 없이 성공")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateStudent_success_without_image() throws Exception {
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

        doNothing().when(studentService).updateStudent(eq("admin"), eq(studentId), any(StudentUpdateRequestDto.class), any());

        // when & then - 이미지 파일 없이 요청
        mockMvc.perform(multipart("/students/{studentId}", studentId)
                        .file(jsonFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_UPDATE_STUDENTS.getMessage()));
    }

    @Test
    @DisplayName("학생 정보 수정 - 인증되지 않은 사용자")
    public void updateStudent_unauthenticated() throws Exception {
        // given
        Long studentId = 1L;
        StudentUpdateRequestDto requestDto = StudentUpdateRequestDto.builder()
                .name("홍길동")
                .address("서울특별시 강남구 테헤란로 123")
                .phone("010-1234-5678")
                .build();

        MockMultipartFile jsonFile = new MockMultipartFile(
                "info",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes()
        );

        // when & then - 인증 없이 요청
        mockMvc.perform(multipart("/students/{studentId}", studentId)
                        .file(jsonFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("학생 정보 수정 - 유효성 검사 실패")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    public void updateStudent_validation_fail() throws Exception {
        // given - 잘못된 전화번호 형식
        Long studentId = 1L;
        StudentUpdateRequestDto requestDto = StudentUpdateRequestDto.builder()
                .name("홍길동")
                .address("서울특별시 강남구 테헤란로 123")
                .phone("invalid-phone") // 잘못된 전화번호 형식
                .build();

        MockMultipartFile jsonFile = new MockMultipartFile(
                "info",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/students/{studentId}", studentId)
                        .file(jsonFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}