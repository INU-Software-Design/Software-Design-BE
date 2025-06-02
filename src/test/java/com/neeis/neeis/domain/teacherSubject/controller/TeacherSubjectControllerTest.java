package com.neeis.neeis.domain.teacherSubject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.teacherSubject.dto.req.CreateTeacherSubjectDto;
import com.neeis.neeis.domain.teacherSubject.dto.res.TeacherSubjectResponseDto;
import com.neeis.neeis.domain.teacherSubject.service.TeacherSubjectService;
import com.neeis.neeis.global.config.SecurityConfig;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherSubjectController.class)
@Import(SecurityConfig.class)
class TeacherSubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeacherSubjectService teacherSubjectService;

    @MockBean
    private TeacherService teacherService;

    // ArgumentCaptor를 사용하여 서비스 메소드에 전달된 정확한 파라미터 값 검증 가능
    private ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    private ArgumentCaptor<CreateTeacherSubjectDto> dtoCaptor = ArgumentCaptor.forClass(CreateTeacherSubjectDto.class);

    private CreateTeacherSubjectDto createTeacherSubjectDto;
    private List<TeacherSubjectResponseDto> teacherSubjectResponseDtoList;

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
        // 테스트용 데이터 설정
        createTeacherSubjectDto = CreateTeacherSubjectDto.builder()
                .subjectName("수학")
                .teacherName("김철수")
                .build();

        TeacherSubjectResponseDto responseDto1 = TeacherSubjectResponseDto.builder()
                .id(1L)
                .subjectName("수학")
                .teacherName("김철수")
                .build();

        TeacherSubjectResponseDto responseDto2 = TeacherSubjectResponseDto.builder()
                .id(2L)
                .subjectName("영어")
                .teacherName("홍길동")
                .build();

        teacherSubjectResponseDtoList = Arrays.asList(responseDto1, responseDto2);
    }

    @Test
    @DisplayName("교사-과목 배정 추가 테스트")
    @WithMockUser(username = "testUser", roles = {"TEACHER"})
    void saveTest() throws Exception {
        // given
        String username = "testUser";
        doNothing().when(teacherSubjectService).save( any(CreateTeacherSubjectDto.class));

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/teacherSubjects")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTeacherSubjectDto)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_POST_TEACHER_SUBJECT.getMessage()))
                .andExpect(jsonPath("$.response").doesNotExist());

        verify(teacherSubjectService).save( dtoCaptor.capture());
        assertThat(dtoCaptor.getValue().getSubjectName()).isEqualTo("수학");
        assertThat(dtoCaptor.getValue().getTeacherName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("교사-과목 배정 전체 조회 테스트")
    void getAllsTest() throws Exception {
        // given
        given(teacherSubjectService.getTeacherSubjects()).willReturn(teacherSubjectResponseDtoList);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/teacherSubjects")
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SUCCESS_GET_TEACHER_SUBJECT.getMessage()))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response", hasSize(2)))
                .andExpect(jsonPath("$.response[0].id").value(1L))
                .andExpect(jsonPath("$.response[0].subjectName").value("수학"))
                .andExpect(jsonPath("$.response[0].teacherName").value("김철수"))
                .andExpect(jsonPath("$.response[1].id").value(2L))
                .andExpect(jsonPath("$.response[1].subjectName").value("영어"))
                .andExpect(jsonPath("$.response[1].teacherName").value("홍길동"));

        verify(teacherSubjectService).getTeacherSubjects();
    }


    @Test
    @DisplayName("교사-과목 배정 삭제 테스트")
    @WithMockUser(username = "testUser", roles = {"TEACHER"})
    void deleteTest() throws Exception {
        // given
        Long id = 1L;
        String username = "testUser";
        doNothing().when(teacherSubjectService).delete( anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/teacherSubjects/{id}", id)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(teacherSubjectService).delete( id);
    }

    @Test
    @DisplayName("교사-과목 배정 추가 시 유효성 검사 실패 테스트 - 과목명 누락")
    @WithMockUser(username = "testUser", roles = {"TEACHER"})
    void saveValidationFailSubjectNameTest() throws Exception {
        // given
        CreateTeacherSubjectDto invalidDto = CreateTeacherSubjectDto.builder()
                .subjectName("")  // 빈 과목명
                .teacherName("김철수")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/teacherSubjects")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("subjectName"))
                .andExpect(jsonPath("$.fieldErrors[0].reason")
                        .value("과목명은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("교사-과목 배정 추가 시 유효성 검사 실패 테스트 - 교사명 누락")
    @WithMockUser(username = "testUser", roles = {"TEACHER"})
    void saveValidationFailTeacherNameTest() throws Exception {
        // given
        CreateTeacherSubjectDto invalidDto = CreateTeacherSubjectDto.builder()
                .subjectName("수학")
                .teacherName("")  // 빈 교사명
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/teacherSubjects")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("teacherName"))
                .andExpect(jsonPath("$.fieldErrors[0].reason")
                        .value("교사명은 필수 입력 값입니다."));
    }
}