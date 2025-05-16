package com.neeis.neeis.domain.evaluationMethod.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.evaluationMethod.dto.req.CreateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.req.UpdateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.res.EvaluationMethodResponseDto;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
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
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EvaluationMethodController.class)
@Import(SecurityConfig.class)
class EvaluationMethodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EvaluationMethodService evaluationMethodService;

    private CreateEvaluationMethodDto createDto;
    private UpdateEvaluationMethodDto updateDto;
    private EvaluationMethodResponseDto responseDto;

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
        // 테스트용 DTO 객체 생성
        createDto = CreateEvaluationMethodDto.builder()
                .subject("국어")
                .year(2025)
                .semester(1)
                .grade(1)
                .examType("WRITTEN")
                .title("기말고사")
                .weight(20.0)
                .fullScore(100.0)
                .build();

        updateDto = UpdateEvaluationMethodDto.builder()
                .examType("WRITTEN")
                .title("기말고사")
                .weight(25.0)
                .fullScore(100.0)
                .build();

        responseDto = EvaluationMethodResponseDto.builder()
                .id(1L)
                .examType("WRITTEN")
                .title("기말고사")
                .weight(20.0)
                .fullScore(100.0)
                .build();
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("평가 방식 생성 성공 테스트")
    void createEvaluationMethod_Success() throws Exception {
        // given
        doNothing().when(evaluationMethodService).save(anyString(), any(CreateEvaluationMethodDto.class));

        // when
        ResultActions result = mockMvc.perform(post("/evaluation-methods")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_EVALUATION_METHOD.getMessage()));

        verify(evaluationMethodService).save(eq("teacher"), any(CreateEvaluationMethodDto.class));
    }

    @Test
    @DisplayName("평가 방식 조회 성공 테스트")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void getEvaluationMethods_Success() throws Exception {
        // given
        List<EvaluationMethodResponseDto> responseDtoList = Arrays.asList(responseDto);
        given(evaluationMethodService.getEvaluationMethods("국어", 2025, 1, 1))
                .willReturn(responseDtoList);

        // when
        ResultActions result = mockMvc.perform(get("/evaluation-methods")
                .param("subject", "국어")
                .param("year", "2025")
                .param("semester", "1")
                .param("grade", "1"));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_GET_EVALUATION_METHOD.getMessage()))
                .andExpect(jsonPath("$.response[0].id").value(1L))
                .andExpect(jsonPath("$.response[0].examType").value("WRITTEN"))
                .andExpect(jsonPath("$.response[0].title").value("기말고사"))
                .andExpect(jsonPath("$.response[0].weight").value(20.0))
                .andExpect(jsonPath("$.response[0].fullScore").value(100.0));
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("평가 방식 수정 성공 테스트")
    void updateEvaluationMethod_Success() throws Exception {
        // given
        Long id = 1L;
        doNothing().when(evaluationMethodService).update(anyString(), anyLong(), any(UpdateEvaluationMethodDto.class));

        // when
        ResultActions result = mockMvc.perform(put("/evaluation-methods/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_UPDATE_EVALUATION_METHOD.getMessage()));

        verify(evaluationMethodService).update(eq("teacher"), eq(id), any(UpdateEvaluationMethodDto.class));
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("평가 방식 삭제 성공 테스트")
    void deleteEvaluationMethod_Success() throws Exception {
        // given
        Long id = 1L;
        doNothing().when(evaluationMethodService).delete(anyString(), anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/evaluation-methods/{id}", id)
                .with(csrf()));

        // then
        result.andDo(print())
                .andExpect(status().isNoContent());

        verify(evaluationMethodService).delete(eq("teacher"), eq(id));
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    @DisplayName("권한 없는 사용자의 평가 방식 생성 실패 테스트")
    void createEvaluationMethod_Unauthorized() throws Exception {
        // when
        ResultActions result = mockMvc.perform(post("/evaluation-methods")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)));

        // then
        result.andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유효하지 않은 입력값으로 평가 방식 생성 실패 테스트")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void createEvaluationMethod_InvalidInput() throws Exception {
        // given
        CreateEvaluationMethodDto invalidDto = CreateEvaluationMethodDto.builder()
                .subject("")  // 빈 값으로 설정
                .year(2025)
                .semester(1)
                .grade(1)
                .examType("INVALID_TYPE")
                .title("기말고사")
                .weight(-10.0)  // 음수 값
                .fullScore(100.0)
                .build();

        // when
        ResultActions result = mockMvc.perform(post("/evaluation-methods")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)));

        // then
        result.andDo(print())
                .andExpect(status().isBadRequest());
    }
}