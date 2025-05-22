package com.neeis.neeis.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.user.dto.TokenResponseDto;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.user.dto.UpdatePasswordRequestDto;
import com.neeis.neeis.domain.user.service.UserService;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_LOGIN;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_UPDATE_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private LoginRequestDto loginRequestDto;
    private UpdatePasswordRequestDto updatePasswordRequestDto;
    private TokenResponseDto tokenResponseDto;

    @MockBean
    private JwtProvider jwtProvider;
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
        loginRequestDto = LoginRequestDto.builder()
                .loginId("user1")
                .password("password")
                .build();

        updatePasswordRequestDto = UpdatePasswordRequestDto.builder()
                .loginId("user1")
                .oldPassword("password")
                .newPassword("newPassword123!")
                .build();

        tokenResponseDto = TokenResponseDto.builder()
                .accessToken("accessToken")
                .build();

    }

    @Test
    @DisplayName("통합 로그인 - 성공")
    void login_Success() throws Exception {

        // given
        String content = objectMapper.writeValueAsString(loginRequestDto);
        given(userService.login(any(LoginRequestDto.class))).willReturn(tokenResponseDto);

        // when && then
        mockMvc.perform(post("/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_LOGIN.getMessage()))
                .andExpect(jsonPath("$.response.accessToken").value(tokenResponseDto.getAccessToken()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    @DisplayName("통합 로그인 - 유효성 검사 실패 시")
    void login_ValidationFail() throws Exception {

        LoginRequestDto invalidReq = LoginRequestDto.builder()
                        .loginId("userId")
                        .password("").build();

        String content = objectMapper.writeValueAsString(invalidReq);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    @WithMockUser(username="testUser", roles={"USER"})
    void updatePassword_Success() throws Exception {
        // given
        String content = objectMapper.writeValueAsString(updatePasswordRequestDto);
        doNothing().when(userService).updatePassword(any(UpdatePasswordRequestDto.class));

        // when && then
        mockMvc.perform(put("/users/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_UPDATE_PASSWORD.getMessage()))
                .andExpect(jsonPath("$.response").doesNotExist());
    }

    @Test
    @DisplayName("비밀번호 변경 - 유효성 검사 실패")
    @WithMockUser(username = "user", roles = {"USER"})
    void updatePassword_ValidationFail() throws Exception {
        // 새 비밀번호가 짧아 유효성 실패
        UpdatePasswordRequestDto invalidReq = UpdatePasswordRequestDto.builder()
                .loginId("")
                .oldPassword("")
                .newPassword("short")
                .build();

        String content = objectMapper.writeValueAsString(invalidReq);

        mockMvc.perform(put("/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors.length()").value(3))
                .andExpect(jsonPath("$.fieldErrors[*].field")
                        .value(org.hamcrest.Matchers.hasItems("loginId", "oldPassword", "newPassword")));
    }
}