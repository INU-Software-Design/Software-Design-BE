package com.neeis.neeis.domain.counsel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselResponseDto;
import com.neeis.neeis.domain.counsel.service.CounselService;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_COUNSEL;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_POST_COUNSEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CounselController.class)
@Import(SecurityConfig.class)
public class CounselControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CounselService counselService;

    private CounselRequestDto counselRequestDto;
    private CounselDetailDto counselDetailDto;
    private List<CounselDetailDto> counselDetailDtoList;
    private Long counselId = 1L;
    private Long studentId = 100L;
    private String username = "teacher123";

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
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트용 요청 DTO 생성
        counselRequestDto = CounselRequestDto.builder()
                .category("ACADEMIC")
                .content("학업 상담 내용입니다.")
                .nextPlan("다음 상담은 월말에 진행하겠습니다.")
                .dateTime(LocalDate.of(2025, 4, 20))
                .isPublic(true)
                .build();

        // 테스트용 응답 DTO 생성
        counselDetailDto = CounselDetailDto.builder()
                .id(counselId)
                .category(CounselCategory.ACADEMIC)
                .content("학업 상담 내용입니다.")
                .nextPlan("다음 상담은 월말에 진행하겠습니다.")
                .dateTime(LocalDate.of(2025, 4, 20))
                .teacher("김선생")
                .isPublic(true)
                .build();

        // 테스트용 리스트 DTO 생성
        counselDetailDtoList = Arrays.asList(
                counselDetailDto,
                CounselDetailDto.builder()
                        .id(2L)
                        .category(CounselCategory.PERSONAL)
                        .content("개인 상담 내용입니다.")
                        .nextPlan("다음 상담은 필요시 진행하겠습니다.")
                        .dateTime(LocalDate.of(2025, 4, 25))
                        .teacher("김선생")
                        .isPublic(false)
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "teacher123", roles = {"TEACHER"})
    @DisplayName("상담 생성 테스트")
    public void testPostCounsel() throws Exception {
        // given
        Long createdCounselId = 1L;
        when(counselService.createCounsel(eq(username), eq(studentId), any(CounselRequestDto.class)))
                .thenReturn(CounselResponseDto.builder()
                        .counselId(createdCounselId).build());

        // when & then
        mockMvc.perform(post("/counsel")
                        .with(csrf())
                        .param("studentId", String.valueOf(studentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(counselRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_POST_COUNSEL.getMessage()))
                .andExpect(jsonPath("$.response.counselId").value(createdCounselId));
    }

    @Test
    @WithMockUser(username = "teacher123", roles = {"TEACHER"})
    @DisplayName("상담 개별 조회 테스트")
    public void testGetCounsel() throws Exception {
        // given
        when(counselService.getCounsel(eq(username), eq(counselId)))
                .thenReturn(counselDetailDto);

        // when & then
        mockMvc.perform(get("/counsel/{counselId}", counselId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_COUNSEL.getMessage()))
                .andExpect(jsonPath("$.response.id").value(counselId))
                .andExpect(jsonPath("$.response.category").value("ACADEMIC"))
                .andExpect(jsonPath("$.response.content").value("학업 상담 내용입니다."))
                .andExpect(jsonPath("$.response.nextPlan").value("다음 상담은 월말에 진행하겠습니다."))
                .andExpect(jsonPath("$.response.dateTime").value("2025-04-20"))
                .andExpect(jsonPath("$.response.teacher").value("김선생"))
                .andExpect(jsonPath("$.response.isPublic").value(true));
    }

    @Test
    @WithMockUser(username = "teacher123", roles = {"TEACHER"})
    @DisplayName("학생별 상담 기록 조회 테스트")
    public void testGetCounsels() throws Exception {
        // given
        when(counselService.getCounsels(eq(username), eq(studentId)))
                .thenReturn(counselDetailDtoList);

        // when & then
        mockMvc.perform(get("/counsel")
                        .with(csrf())
                        .param("studentId", String.valueOf(studentId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_COUNSEL.getMessage()))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response.length()").value(2))
                .andExpect(jsonPath("$.response[0].id").value(1))
                .andExpect(jsonPath("$.response[0].category").value("ACADEMIC"))
                .andExpect(jsonPath("$.response[0].nextPlan").value("다음 상담은 월말에 진행하겠습니다."))
                .andExpect(jsonPath("$.response[1].id").value(2))
                .andExpect(jsonPath("$.response[1].category").value("PERSONAL"))
                .andExpect(jsonPath("$.response[1].isPublic").value(false));
    }

    @Test
    @WithMockUser(username = "teacher123", roles = {"TEACHER"})
    @DisplayName("상담 수정 테스트")
    public void testUpdateCounsel() throws Exception {
        // given
        CounselRequestDto updateRequest = CounselRequestDto.builder()
                .category("PERSONAL")
                .content("수정된 상담 내용입니다.")
                .nextPlan("필요시 추가 상담 진행")
                .dateTime(LocalDate.of(2025, 4, 22))
                .isPublic(false)
                .build();

        CounselDetailDto updatedDto = CounselDetailDto.builder()
                .id(counselId)
                .category(CounselCategory.PERSONAL)
                .content("수정된 상담 내용입니다.")
                .nextPlan("필요시 추가 상담 진행")
                .dateTime(LocalDate.of(2025, 4, 22))
                .teacher("김선생")
                .isPublic(false)
                .build();

        when(counselService.updateCounsel(eq(username), eq(counselId), any(CounselRequestDto.class)))
                .thenReturn(updatedDto);

        // when & then
        mockMvc.perform(put("/counsel/{counselId}", counselId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_COUNSEL.getMessage()))
                .andExpect(jsonPath("$.response.id").value(counselId))
                .andExpect(jsonPath("$.response.category").value("PERSONAL"))
                .andExpect(jsonPath("$.response.content").value("수정된 상담 내용입니다."))
                .andExpect(jsonPath("$.response.nextPlan").value("필요시 추가 상담 진행"))
                .andExpect(jsonPath("$.response.dateTime").value("2025-04-22"))
                .andExpect(jsonPath("$.response.isPublic").value(false));
    }

    @Test
    @WithMockUser(username = "teacher123", roles = {"TEACHER"})
    @DisplayName("상담 생성 시 유효성 검사 실패 테스트")
    public void testPostCounselValidationFail() throws Exception {
        // given
        CounselRequestDto invalidRequest = CounselRequestDto.builder()
                .category("") // 빈 문자열로 유효성 검사 실패 유도
                .content("상담 내용")
                .nextPlan("다음 상담 계획")
                .dateTime(LocalDate.of(2025, 4, 22))
                .isPublic(true)
                .build();

        // when & then
        mockMvc.perform(post("/counsel")
                        .with(csrf())
                        .param("studentId", String.valueOf(studentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 400 Bad Request 기대
    }

    @Test
    @WithMockUser(username = "teacher123", roles = {"TEACHER"})
    @DisplayName("상담 생성 시 날짜 유효성 검사 실패 테스트")
    public void testPostCounselDateValidationFail() throws Exception {
        // 날짜 null로 유효성 검사 실패 유도
        String invalidJsonRequest = "{\n" +
                "  \"category\": \"ACADEMIC\",\n" +
                "  \"content\": \"학업 상담 내용입니다.\",\n" +
                "  \"nextPlan\": \"다음 상담은 월말에 진행하겠습니다.\",\n" +
                "  \"isPublic\": true\n" +
                "}";

        // when & then
        mockMvc.perform(post("/counsel")
                        .with(csrf())
                        .param("studentId", String.valueOf(studentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonRequest))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 400 Bad Request 기대
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 상담 조회 테스트")
    public void testGetCounselUnauthenticated() throws Exception {
        // 인증되지 않은 사용자의 요청은 실패해야 함
        mockMvc.perform(get("/counsel/{counselId}", counselId))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // 401 Unauthorized 기대
    }
}