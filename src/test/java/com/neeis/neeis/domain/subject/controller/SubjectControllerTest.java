package com.neeis.neeis.domain.subject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.subject.dto.req.CreateSubjectRequestDto;
import com.neeis.neeis.domain.subject.dto.res.SubjectResponseDto;
import com.neeis.neeis.domain.subject.service.SubjectService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubjectController.class)
@Import(SecurityConfig.class)
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private EvaluationMethodService evaluationMethodService;

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
    @DisplayName("과목 생성 성공 테스트")
    @WithMockUser(roles = "TEACHER") // 교사 권한으로 테스트
    void createSubject_Success() throws Exception {
        // given
        CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                .name("수학")
                .build();
        String content = objectMapper.writeValueAsString(requestDto);

        doNothing().when(subjectService).createSubject(anyString(), any(CreateSubjectRequestDto.class));

        // when & then
        mockMvc.perform(post("/subjects")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_SUBJECT.getMessage()));

        verify(subjectService, times(1)).createSubject(anyString(), any(CreateSubjectRequestDto.class));
    }

    @Test
    @DisplayName("전체 과목 조회 성공 테스트")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void getSubjects_Success() throws Exception {
        // given
        List<SubjectResponseDto> subjects = Arrays.asList(
                SubjectResponseDto.builder().id(1L).name("수학").build(),
                SubjectResponseDto.builder().id(2L).name("영어").build(),
                SubjectResponseDto.builder().id(3L).name("국어").build()
        );

        when(subjectService.getSubjects()).thenReturn(subjects);

        // when & then
        mockMvc.perform(get("/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_SUBJECT.getMessage()))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response.length()").value(3))
                .andExpect(jsonPath("$.response[0].id").value(1))
                .andExpect(jsonPath("$.response[0].name").value("수학"))
                .andExpect(jsonPath("$.response[1].id").value(2))
                .andExpect(jsonPath("$.response[1].name").value("영어"))
                .andExpect(jsonPath("$.response[2].id").value(3))
                .andExpect(jsonPath("$.response[2].name").value("국어"));

        verify(subjectService, times(1)).getSubjects();
    }

    @Test
    @DisplayName("특정 조건의 과목 목록 조회 성공 테스트")
    @WithMockUser(username = "TEACHER", roles = {"TEACHER"})
    void getSubjectsByEvaluation_Success() throws Exception {
        // given
        int year = 2025;
        int semester = 1;
        int grade = 1;

        List<SubjectResponseDto> subjects = Arrays.asList(
                SubjectResponseDto.builder().id(1L).name("수학").build(),
                SubjectResponseDto.builder().id(2L).name("영어").build()
        );

        when(evaluationMethodService.findSubjectList(year, semester, grade)).thenReturn(subjects);

        // when & then
        mockMvc.perform(get("/subjects/subjects")
                        .param("year", String.valueOf(year))
                        .param("semester", String.valueOf(semester))
                        .param("grade", String.valueOf(grade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_SUBJECT.getMessage()))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response.length()").value(2))
                .andExpect(jsonPath("$.response[0].id").value(1))
                .andExpect(jsonPath("$.response[0].name").value("수학"))
                .andExpect(jsonPath("$.response[1].id").value(2))
                .andExpect(jsonPath("$.response[1].name").value("영어"));

        verify(evaluationMethodService, times(1)).findSubjectList(year, semester, grade);
    }

    @Test
    @DisplayName("과목 수정 성공 테스트")
    @WithMockUser(roles = "TEACHER") // 교사 권한으로 테스트
    void updateSubject_Success() throws Exception {
        // given
        Long subjectId = 1L;
        CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                .name("수학(수정)")
                .build();
        String content = objectMapper.writeValueAsString(requestDto);

        doNothing().when(subjectService).updateSubject(anyString(), eq(subjectId), any(CreateSubjectRequestDto.class));

        // when & then
        mockMvc.perform(put("/subjects/{subjectId}", subjectId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_UPDATE_SUBJECT.getMessage()));

        verify(subjectService, times(1)).updateSubject(anyString(), eq(subjectId), any(CreateSubjectRequestDto.class));
    }

    @Test
    @DisplayName("과목 삭제 성공 테스트")
    @WithMockUser(roles = "TEACHER") // 교사 권한으로 테스트
    void deleteSubject_Success() throws Exception {
        // given
        Long subjectId = 1L;

        doNothing().when(subjectService).deleteSubject(anyString(), eq(subjectId));

        // when & then
        mockMvc.perform(delete("/subjects/{subjectId}", subjectId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(subjectService, times(1)).deleteSubject(anyString(), eq(subjectId));
    }

    @Test
    @DisplayName("권한 없는 사용자의 과목 생성 실패 테스트")
    @WithMockUser(roles = "STUDENT") // 학생 권한으로 테스트 (권한 없음)
    void createSubject_Fail_Unauthorized() throws Exception {
        // given
        CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                .name("수학")
                .build();
        String content = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/subjects")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isForbidden()); // 403 Forbidden 예상

        verify(subjectService, never()).createSubject(anyString(), any(CreateSubjectRequestDto.class));
    }
}