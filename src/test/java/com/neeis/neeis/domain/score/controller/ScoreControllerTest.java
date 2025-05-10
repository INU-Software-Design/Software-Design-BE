package com.neeis.neeis.domain.score.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.score.dto.req.ScoreRequestDto;
import com.neeis.neeis.domain.score.dto.res.ScoreSummaryBySubjectDto;
import com.neeis.neeis.domain.score.service.ScoreService;
import com.neeis.neeis.global.config.SecurityConfig;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_SCORE;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_POST_SCORE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScoreController.class)
@Import(SecurityConfig.class)
class ScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScoreService scoreService;

    @MockBean private JwtProvider jwtProvider;
    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
            // JwtProvider는 위에서 MockBean으로 주입됩니다.
            return new JwtAuthenticationFilter(jwtProvider);
        }
    }


    @Nested
    @DisplayName("성적 등록/수정 테스트")
    class RegisterScoresTest {

        private List<ScoreRequestDto> scoreRequestDtoList;

        @BeforeEach
        void setUp() {
            // 테스트용 ScoreRequestDto 객체 생성
            ScoreRequestDto.StudentScoreDto student1 = ScoreRequestDto.StudentScoreDto.builder().number(1).rawScore(93.0).build();
            ScoreRequestDto.StudentScoreDto student2 = ScoreRequestDto.StudentScoreDto.builder().number(2).rawScore(82.0).build();

            List<ScoreRequestDto.StudentScoreDto> students = Arrays.asList(student1, student2);

            ScoreRequestDto scoreRequestDto = ScoreRequestDto.builder()
                    .classNum(2)
                    .evaluationId(1L)
                    .students(students)
                    .build();

            scoreRequestDtoList = Collections.singletonList(scoreRequestDto);
        }

        @Test
        @WithMockUser(username = "teacher", roles = {"TEACHER"})
        @DisplayName("성적 등록 요청 성공 시 200 OK 응답")
        void registerScores_Success() throws Exception {
            // Given
            doNothing().when(scoreService).saveOrUpdateScores(anyString(), any());

            // When
            ResultActions result = mockMvc.perform(post("/scores")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(scoreRequestDtoList)));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(SUCCESS_POST_SCORE.getMessage()));

            verify(scoreService, times(1)).saveOrUpdateScores(eq("teacher"), anyList());
        }

        @Test
        @WithMockUser(username = "teacher", roles = {"TEACHER"})
        @DisplayName("잘못된 요청 데이터로 성적 등록 시 400 Bad Request 응답")
        void registerScores_BadRequest() throws Exception {
            // Given
            // 유효하지 않은 데이터 생성 (students가 비어있음)
            ScoreRequestDto invalidDto = ScoreRequestDto.builder()
                    .classNum(2)
                    .evaluationId(1L)
                    .students(new ArrayList<>())
                    .build();

            List<ScoreRequestDto> invalidDtoList = Collections.singletonList(invalidDto);

            // When
            ResultActions result = mockMvc.perform(post("/scores")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDtoList)));

            // Then
            result.andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 성적 등록 요청 시 401 Unauthorized 응답")
        void registerScores_Unauthorized() throws Exception {
            // When
            ResultActions result = mockMvc.perform(post("/scores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(scoreRequestDtoList)));

            // Then
            result.andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("성적 조회 테스트")
    class GetScoreSummaryTest {

        private List<ScoreSummaryBySubjectDto> mockResponse;

        @BeforeEach
        void setUp() {
            // 테스트용 응답 데이터 생성
            List<ScoreSummaryBySubjectDto.EvaluationDto> evaluations = Arrays.asList(
                    ScoreSummaryBySubjectDto.EvaluationDto.builder()
                            .evaluationId(1L)
                            .title("기말고사")
                            .build(),
                    ScoreSummaryBySubjectDto.EvaluationDto.builder()
                            .evaluationId(2L)
                            .title("중간고사")
                            .build()
            );

            List<ScoreSummaryBySubjectDto.ScoreItemDto> scores = Arrays.asList(
                    ScoreSummaryBySubjectDto.ScoreItemDto.builder()
                            .evaluationId(1L)
                            .rawScore(85.0)
                            .weightedScore(42.5)
                            .build(),
                    ScoreSummaryBySubjectDto.ScoreItemDto.builder()
                            .evaluationId(2L)
                            .rawScore(90.0)
                            .weightedScore(45.0)
                            .build()
            );

            List<ScoreSummaryBySubjectDto.StudentScoreDto> students = Collections.singletonList(
                    ScoreSummaryBySubjectDto.StudentScoreDto.builder()
                            .studentName("홍길동")
                            .number(1)
                            .scores(scores)
                            .rawTotal(175.0)
                            .weightedTotal(87.5)
                            .average(85.0)
                            .stdDev(5.0)
                            .rank(3)
                            .grade(2)
                            .achievementLevel("B")
                            .build()
            );

            ScoreSummaryBySubjectDto summaryDto = ScoreSummaryBySubjectDto.builder()
                    .subjectName("독서와 문법")
                    .evaluations(evaluations)
                    .students(students)
                    .build();

            mockResponse = Collections.singletonList(summaryDto);
        }

        @Test
        @WithMockUser(username = "teacher", roles = {"TEACHER"})
        @DisplayName("과목이 지정된 성적 조회 요청 성공 시 200 OK 응답")
        void getScoreSummaryBySubject_WithSubject_Success() throws Exception {
            // Given
            when(scoreService.getScoreSummaryBySubject(anyString(), eq(2023), eq(1), eq(1), eq(2), eq("국어")))
                    .thenReturn(mockResponse);

            // When
            ResultActions result = mockMvc.perform(get("/scores/summary")
                    .param("year", "2023")
                    .param("semester", "1")
                    .param("grade", "1")
                    .param("classNum", "2")
                    .param("subject", "국어"));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(SUCCESS_GET_SCORE.getMessage()))
                    .andExpect(jsonPath("$.response[0].subjectName").value("독서와 문법"))
                    .andExpect(jsonPath("$.response[0].evaluations[0].title").value("기말고사"))
                    .andExpect(jsonPath("$.response[0].students[0].studentName").value("홍길동"));

            verify(scoreService).getScoreSummaryBySubject(
                    eq("teacher"), eq(2023), eq(1), eq(1), eq(2), eq("국어"));
        }

        @Test
        @WithMockUser(username = "teacher", roles = {"TEACHER"})
        @DisplayName("과목이 지정되지 않은 성적 조회 요청 성공 시 200 OK 응답")
        void getScoreSummaryBySubject_WithoutSubject_Success() throws Exception {
            // Given
            when(scoreService.getScoreSummaryBySubject(anyString(), eq(2023), eq(1), eq(1), eq(2), eq(null)))
                    .thenReturn(mockResponse);

            // When
            ResultActions result = mockMvc.perform(get("/scores/summary")
                    .param("year", "2023")
                    .param("semester", "1")
                    .param("grade", "1")
                    .param("classNum", "2"));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(SUCCESS_GET_SCORE.getMessage()))
                    .andExpect(jsonPath("$.response[0].subjectName").value("독서와 문법"));

            verify(scoreService).getScoreSummaryBySubject(
                    eq("teacher"), eq(2023), eq(1), eq(1), eq(2), eq(null));
        }

        @Test
        @WithMockUser(username = "teacher", roles = {"TEACHER"})
        @DisplayName("필수 파라미터 누락 시 400 Bad Request 응답")
        void getScoreSummaryBySubject_MissingRequiredParam() throws Exception {
            // When (연도 파라미터 누락)
            ResultActions result = mockMvc.perform(get("/scores/summary")
                    .param("semester", "1")
                    .param("grade", "1")
                    .param("classNum", "2"));

            // Then
            result.andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 성적 조회 요청 시 401 Unauthorized 응답")
        void getScoreSummaryBySubject_Unauthorized() throws Exception {
            // When
            ResultActions result = mockMvc.perform(get("/scores/summary")
                    .param("year", "2023")
                    .param("semester", "1")
                    .param("grade", "1")
                    .param("classNum", "2"));

            // Then
            result.andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}