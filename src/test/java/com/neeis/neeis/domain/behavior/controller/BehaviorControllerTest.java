package com.neeis.neeis.domain.behavior.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.behavior.service.BehaviorService;
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

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_BEHAVIOR;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_POST_BEHAVIOR;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BehaviorController.class)
@Import(SecurityConfig.class)
class BehaviorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BehaviorService behaviorService;

    private BehaviorRequestDto behaviorRequestDto;
    private BehaviorResponseDto behaviorResponseDto;
    private BehaviorDetailResponseDto behaviorDetailResponseDto;

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
        // 요청 DTO 설정
        behaviorRequestDto = BehaviorRequestDto.builder()
                .behavior("책임감이 강하며 수업시간에 적극적으로 참여함.")
                .generalComment("전반적으로 성실하며, 친구들과의 관계도 원만합니다.")
                .build();

        // 응답 DTO 설정
        behaviorResponseDto = BehaviorResponseDto.builder()
                .behaviorId(1L)
                .build();

        // 상세 응답 DTO 설정
        behaviorDetailResponseDto = BehaviorDetailResponseDto.builder()
                .behaviorId(1L)
                .behavior("책임감이 강하며 수업시간에 적극적으로 참여함.")
                .generalComment("전반적으로 성실하며, 친구들과의 관계도 원만합니다.")
                .build();
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("행동특성 작성 성공 테스트")
    void postBehaviorSuccess() throws Exception {
        // given
        given(behaviorService.createBehavior(anyString(), anyInt(), anyInt(), anyInt(), anyLong(), any(BehaviorRequestDto.class)))
                .willReturn(behaviorResponseDto);

        // when & then
        mockMvc.perform(post("/behavior")
                        .with(csrf())
                        .param("year", "2025")
                        .param("grade", "1")
                        .param("classNum", "3")
                        .param("studentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(behaviorRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_POST_BEHAVIOR.getMessage()))
                .andExpect(jsonPath("$.response.behaviorId").value(1L))
                .andDo(print());

        // verify
        verify(behaviorService).createBehavior(
                eq("teacher"), eq(2025), eq(1), eq(3), eq(1L), any(BehaviorRequestDto.class));
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("행동특성 조회 성공 테스트")
    void getBehaviorSuccess() throws Exception {
        // given
        given(behaviorService.getBehavior(anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
                .willReturn(behaviorDetailResponseDto);

        // when & then
        mockMvc.perform(get("/behavior")
                        .param("year", "2025")
                        .param("grade", "1")
                        .param("classNum", "3")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_BEHAVIOR.getMessage()))
                .andExpect(jsonPath("$.response.behaviorId").value(1L))
                .andExpect(jsonPath("$.response.behavior").value("책임감이 강하며 수업시간에 적극적으로 참여함."))
                .andExpect(jsonPath("$.response.generalComment").value("전반적으로 성실하며, 친구들과의 관계도 원만합니다."))
                .andDo(print());

        // verify
        verify(behaviorService).getBehavior(eq("teacher"), eq(2025), eq(1), eq(3), eq(1));
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("행동특성 수정 성공 테스트")
    void updateBehaviorSuccess() throws Exception {
        // given
        given(behaviorService.updateBehavior(anyString(), anyLong(), any(BehaviorRequestDto.class)))
                .willReturn(behaviorDetailResponseDto);

        // when & then
        mockMvc.perform(put("/behavior/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(behaviorRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_BEHAVIOR.getMessage()))
                .andExpect(jsonPath("$.response.behaviorId").value(1L))
                .andExpect(jsonPath("$.response.behavior").value("책임감이 강하며 수업시간에 적극적으로 참여함."))
                .andExpect(jsonPath("$.response.generalComment").value("전반적으로 성실하며, 친구들과의 관계도 원만합니다."))
                .andDo(print());

        // verify
        verify(behaviorService).updateBehavior(eq("teacher"), eq(1L), any(BehaviorRequestDto.class));
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("행동특성 작성 시 요청 검증 실패 테스트 - behavior 누락")
    void postBehaviorValidationFailureMissingBehavior() throws Exception {
        // given
        BehaviorRequestDto invalidRequest = BehaviorRequestDto.builder()
                .generalComment("전반적으로 성실하며, 친구들과의 관계도 원만합니다.")
                .build();

        // when & then
        mockMvc.perform(post("/behavior")
                        .with(csrf())
                        .param("year", "2025")
                        .param("grade", "1")
                        .param("classNum", "3")
                        .param("studentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("행동특성 작성 시 요청 검증 실패 테스트 - generalComment 누락")
    void postBehaviorValidationFailureMissingGeneralComment() throws Exception {
        // given
        BehaviorRequestDto invalidRequest = BehaviorRequestDto.builder()
                .behavior("책임감이 강하며 수업시간에 적극적으로 참여함.")
                .build();

        // when & then
        mockMvc.perform(post("/behavior")
                        .with(csrf())
                        .param("year", "2025")
                        .param("grade", "1")
                        .param("classNum", "3")
                        .param("studentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    @DisplayName("행동특성 수정 시 요청 검증 실패 테스트")
    void updateBehaviorValidationFailure() throws Exception {
        // given
        BehaviorRequestDto invalidRequest = BehaviorRequestDto.builder()
                .behavior("책임감이 강하며 수업시간에 적극적으로 참여함.")
                // generalComment 누락
                .build();

        // when & then
        mockMvc.perform(put("/behavior/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 거부 테스트")
    void unauthenticatedAccessDenied() throws Exception {
        // when & then
        mockMvc.perform(get("/behavior")
                        .param("year", "2025")
                        .param("grade", "1")
                        .param("classNum", "3")
                        .param("studentId", "1"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}