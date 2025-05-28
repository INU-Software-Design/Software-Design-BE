package com.neeis.neeis.domain.scoreSummary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackRequestDto;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackUpdateDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.EvaluationMethodScoreDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.ScoreFeedbackDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScoreSummaryController.class)
@Import(SecurityConfig.class)
class ScoreSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScoreSummaryService scoreSummaryService;

    // 테스트용 더미 데이터
    private StudentScoreSummaryDto studentScoreSummaryDto;
    private ScoreFeedbackRequestDto scoreFeedbackRequestDto;
    private ScoreFeedbackUpdateDto scoreFeedbackUpdateDto;
    private ScoreFeedbackDto scoreFeedbackDto;

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
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 학생 성적 요약 더미 데이터 생성
        studentScoreSummaryDto = createDummyStudentScoreSummaryDto();

        // 피드백 요청 더미 데이터 생성
        scoreFeedbackRequestDto = ScoreFeedbackRequestDto.builder()
                .scoreSummaryId(1L)
                .feedback("테스트 피드백 내용입니다.")
                .build();
        // 피드백 수정 요청 더미 데이터 생성
        scoreFeedbackUpdateDto = ScoreFeedbackUpdateDto.builder()
                .feedback("수정된 피드백 내용입니다.")
                .build();


        // 피드백 조회 결과 더미 데이터 생성
        scoreFeedbackDto = ScoreFeedbackDto.builder()
                .feedback("피드백 내용")
                .build();
    }

    private StudentScoreSummaryDto createDummyStudentScoreSummaryDto() {
        // 국어 과목 평가 방식 생성
        List<EvaluationMethodScoreDto> koreanEvaluations = Arrays.asList(
                EvaluationMethodScoreDto.builder()
                        .examType("WRITTEN")
                        .title("중간고사")
                        .weight(40.0)
                        .fullScore(100.0)
                        .rawScore(85.0)
                        .weightedScore(34.0)
                        .build(),
                EvaluationMethodScoreDto.builder()
                        .examType("WRITTEN")
                        .title("기말고사")
                        .weight(40.0)
                        .fullScore(100.0)
                        .rawScore(90.0)
                        .weightedScore(36.0)
                        .build(),
                EvaluationMethodScoreDto.builder()
                        .examType("PRACTICAL")
                        .title("수행평가1")
                        .weight(10.0)
                        .fullScore(100.0)
                        .rawScore(95.0)
                        .weightedScore(9.5)
                        .build(),
                EvaluationMethodScoreDto.builder()
                        .examType("PRACTICAL")
                        .title("수행평가2")
                        .weight(10.0)
                        .fullScore(100.0)
                        .rawScore(80.0)
                        .weightedScore(8.0)
                        .build()
        );

        // 수학 과목 평가 방식 생성
        List<EvaluationMethodScoreDto> mathEvaluations = Arrays.asList(
                EvaluationMethodScoreDto.builder()
                        .examType("WRITTEN")
                        .title("중간고사")
                        .weight(40.0)
                        .fullScore(100.0)
                        .rawScore(75.0)
                        .weightedScore(30.0)
                        .build(),
                EvaluationMethodScoreDto.builder()
                        .examType("WRITTEN")
                        .title("기말고사")
                        .weight(40.0)
                        .fullScore(100.0)
                        .rawScore(80.0)
                        .weightedScore(32.0)
                        .build(),
                EvaluationMethodScoreDto.builder()
                        .examType("PRACTICAL")
                        .title("수행평가")
                        .weight(20.0)
                        .fullScore(100.0)
                        .rawScore(90.0)
                        .weightedScore(18.0)
                        .build()
        );

        // 과목별 성적 정보 생성
        List<SubjectScoreDto> subjects = Arrays.asList(
                SubjectScoreDto.builder()
                        .scoreSummaryId(1L)
                        .subjectName("국어")
                        .feedback("독해력이 좋으며 글쓰기 능력이 우수합니다.")
                        .evaluationMethods(koreanEvaluations)
                        .rawTotal(87.5)
                        .weightedTotal(88.0)
                        .rank(5)
                        .grade(2)
                        .achievementLevel("A")
                        .average(79.4)
                        .stdDev(5.3)
                        .totalStudentCount(28)
                        .build(),
                SubjectScoreDto.builder()
                        .scoreSummaryId(2L)
                        .subjectName("수학")
                        .feedback("기본 개념은 이해하고 있으나 응용문제에 어려움이 있습니다.")
                        .evaluationMethods(mathEvaluations)
                        .rawTotal(80.0)
                        .weightedTotal(80.0)
                        .rank(10)
                        .grade(3)
                        .achievementLevel("B")
                        .average(75.2)
                        .stdDev(8.1)
                        .totalStudentCount(28)
                        .build()
        );

        // 학생 성적 요약 DTO 생성
        return StudentScoreSummaryDto.builder()
                .number(6)
                .studentName("홍길동")
                .subjects(subjects)
                .build();
    }

    @Test
    @DisplayName("학생 성적 학적 조회 테스트")
    @WithMockUser(username = "testTeacher", roles = "TEACHER")
    void getStudentScoreSummaryTest() throws Exception {
        // given
        given(scoreSummaryService.getStudentSummary(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
                .willReturn(studentScoreSummaryDto);

        // when & then
        mockMvc.perform(get("/score-summary")
                        .param("year", "2025")
                        .param("semester", "1")
                        .param("grade", "1")
                        .param("classNum", "1")
                        .param("number", "6")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_SCORE.getMessage()))
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.response.number").value(6))
                .andExpect(jsonPath("$.response.studentName").value("홍길동"))
                .andExpect(jsonPath("$.response.subjects").isArray())
                .andExpect(jsonPath("$.response.subjects.length()").value(2))
                .andExpect(jsonPath("$.response.subjects[0].subjectName").value("국어"))
                .andExpect(jsonPath("$.response.subjects[0].evaluationMethods").isArray())
                .andExpect(jsonPath("$.response.subjects[0].evaluationMethods.length()").value(4))
                .andExpect(jsonPath("$.response.subjects[0].weightedTotal").value(88.0))
                .andExpect(jsonPath("$.response.subjects[1].subjectName").value("수학"));

        verify(scoreSummaryService).getStudentSummary(anyString(), eq(2025), eq(1), eq(1), eq(1), eq(6));
    }

    @Test
    @DisplayName("성적 피드백 등록 테스트")
    @WithMockUser(username = "testTeacher", roles = "TEACHER")
    void saveFeedbackTest() throws Exception {
        // given
        doNothing().when(scoreSummaryService).saveFeedback(anyString(), any(ScoreFeedbackRequestDto.class));

        // when & then
        mockMvc.perform(post("/score-summary/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreFeedbackRequestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_FEEDBACK.getMessage()));

        verify(scoreSummaryService).saveFeedback(eq("testTeacher"), any(ScoreFeedbackRequestDto.class));
    }

    @Test
    @DisplayName("성적 피드백 수정 테스트")
    @WithMockUser(username = "testTeacher", roles = "TEACHER")
    void updateFeedbackTest() throws Exception {
        // given
        Long scoreSummaryId = 1L;
        doNothing().when(scoreSummaryService).updateFeedback(anyString(), anyLong(), any(ScoreFeedbackUpdateDto.class));

        // when & then
        mockMvc.perform(post("/score-summary/feedback/{score-summary-id}", scoreSummaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreFeedbackUpdateDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_FEEDBACK.getMessage()));

        verify(scoreSummaryService).updateFeedback(eq("testTeacher"), eq(scoreSummaryId), any(ScoreFeedbackUpdateDto.class));
    }

    @Test
    @DisplayName("성적 피드백 조회 테스트")
    @WithMockUser(username = "testTeacher", roles = "TEACHER")
    void getFeedbackTest() throws Exception {
        // given
        Long scoreSummaryId = 1L;
        given(scoreSummaryService.getFeedback(anyString(), anyLong())).willReturn(scoreFeedbackDto);

        // when & then
        mockMvc.perform(get("/score-summary/feedback/{score-summary-id}", scoreSummaryId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_FEEDBACK.getMessage()))
                 .andExpect(jsonPath("$.response.feedback").value(scoreFeedbackDto.getFeedback()));

        verify(scoreSummaryService).getFeedback("testTeacher",eq(scoreSummaryId));
    }

    @Test
    @DisplayName("권한 없는 사용자의 성적 조회 실패 테스트")
    @WithMockUser(username = "testUser", roles = "USER")
    void getStudentScoreSummaryUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/score-summary")
                        .param("year", "2025")
                        .param("semester", "1")
                        .param("grade", "1")
                        .param("classNum", "1")
                        .param("number", "6")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}