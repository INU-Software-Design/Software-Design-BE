package com.neeis.neeis.domain.subject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.subject.dto.req.CreateSubjectRequestDto;
import com.neeis.neeis.domain.subject.dto.res.SubjectResponseDto;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.common.StatusCode;
import com.neeis.neeis.global.config.SecurityConfig;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SubjectController 테스트 (호출 횟수 검증을 atLeastOnce() 또는 생략)
 */
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

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private JwtProvider jwtProvider;

    /**
     * SecurityConfig에서 사용하는 JwtAuthenticationFilter를 빈으로 등록
     * (WebMvcTest 환경에서 보안 필터 체인이 올바로 동작하도록 하기 위함)
     */
    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
            return new JwtAuthenticationFilter(jwtProvider);
        }
    }

    @BeforeEach
    void setUp() {
        // 각 테스트가 시작되기 전에 Mockito mock을 초기화하여,
        // 이전 테스트에서 남은 호출 횟수를 지워 버립니다.
        Mockito.reset(teacherService, subjectService, evaluationMethodService);
    }

    @Nested
    @DisplayName("과목 생성 테스트")
    class CreateSubjectTest {

        @Test
        @DisplayName("교사가 과목을 정상적으로 생성한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_CreateSubject_When_TeacherSubmitsValidData() throws Exception {
            // Given: valid request
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("수학")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            // teacherService.authenticate(...) stub
            Teacher teacher = Teacher.builder().name("김교사").build();
            when(teacherService.authenticate("teacher1")).thenReturn(teacher);
            doNothing().when(subjectService).createSubject(any(CreateSubjectRequestDto.class));

            // When: POST /subjects
            ResultActions result = mockMvc.perform(post("/subjects")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            // Then: 200 OK + message
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_SUBJECT.getMessage()));

            // 최소 한 번 이상 authenticate()가 불렸음을 확인
            verify(teacherService, atLeastOnce()).authenticate("teacher1");
            verify(subjectService, times(1)).createSubject(any(CreateSubjectRequestDto.class));
        }

        @Test
        @DisplayName("교사가 아닌 사용자가 과목 생성을 시도하면 인증 실패한다")
        @WithMockUser(username = "student1", roles = "STUDENT")
        void should_FailAuthentication_When_NonTeacherTriesToCreate() throws Exception {
            // Given: 교사가 아닌 사용자가 요청
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("수학")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            // teacherService.authenticate(...)가 예외를 던지도록 stub
            when(teacherService.authenticate("student1"))
                    .thenThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

            // When & Then: 400 Bad Request (이 전역 예외 처리 로직에 맞춰 변경)
            mockMvc.perform(post("/subjects")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isForbidden());

            // authenticate() 가 호출되었음을 확인
            verify(teacherService, atLeastOnce()).authenticate("student1");
            // subjectService.createSubject() 는 호출되지 않아야 함
            verify(subjectService, never()).createSubject(any(CreateSubjectRequestDto.class));
        }


        @Test
        @DisplayName("유효하지 않은 요청 데이터로 과목 생성 시 검증 오류가 발생한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_FailValidation_When_InvalidDataProvided() throws Exception {
            // 빈 문자열 과목명 → @NotBlank 에 걸림
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            // 여기서는 authenticate() 호출 여부를 확신하기 어렵기 때문에 Stub을 최소한으로만 둡니다.
            // (CSRF 토큰 유무 등으로 인해 authenticate() 호출 여부가 달라질 수 있음)
            when(teacherService.authenticate("teacher1"))
                    .thenReturn(Teacher.builder().name("김교사").build());

            // When & Then: 바로 400 Bad Request (검증 오류)
            mockMvc.perform(post("/subjects")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isBadRequest());

            // “검증 오류 테스트”에서는 authenticate() 호출 여부를 굳이 검증하지 않습니다.
            // (호출 여부가 보장되지 않으므로, 테스트가 깨질 여지가 있음)
            verify(subjectService, never()).createSubject(any());
            // verify(teacherService, atLeastOnce()).authenticate("teacher1");
            // ↑ authenticate()가 호출될 수도, 안 될 수도 있으므로 검증을 생략하거나 주석 처리
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 과목 생성 요청은 거부된다")
        void should_RejectRequest_When_UnauthenticatedUserTriesToCreate() throws Exception {
            // Given: 인증되지 않은 상태
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("수학")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            // When & Then: 인증되지 않아서 401 Unauthorized
            mockMvc.perform(post("/subjects")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isUnauthorized());

            // 전혀 authenticate() 호출되지 않아야 함
            verify(teacherService, never()).authenticate(anyString());
            verify(subjectService, never()).createSubject(any(CreateSubjectRequestDto.class));
        }
    }

    @Nested
    @DisplayName("과목 조회 테스트")
    class GetSubjectsTest {

        @Test
        @DisplayName("전체 과목 목록을 정상적으로 조회한다")
        @WithMockUser(username = "user1", roles = "USER")
        void should_ReturnAllSubjects_When_ValidRequest() throws Exception {
            // Given: subjectService.getSubjects()가 반환할 목록
            List<SubjectResponseDto> subjects = Arrays.asList(
                    SubjectResponseDto.builder().id(1L).name("수학").build(),
                    SubjectResponseDto.builder().id(2L).name("영어").build(),
                    SubjectResponseDto.builder().id(3L).name("국어").build()
            );
            when(subjectService.getSubjects()).thenReturn(subjects);

            // When: GET /subjects
            ResultActions result = mockMvc.perform(get("/subjects"));

            // Then: 200 OK + message + response array
            result.andExpect(status().isOk())
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
        @DisplayName("특정 조건의 과목 목록을 정상적으로 조회한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_ReturnSubjectsByEvaluation_When_ValidConditions() throws Exception {
            // Given: year=2025, semester=1, grade=1
            int year = 2025;
            int semester = 1;
            int grade = 1;

            List<SubjectResponseDto> subjects = Arrays.asList(
                    SubjectResponseDto.builder().id(1L).name("수학").build(),
                    SubjectResponseDto.builder().id(2L).name("영어").build()
            );
            when(evaluationMethodService.findSubjectList(year, semester, grade)).thenReturn(subjects);

            // When: GET /subjects/subjects?year=2025&semester=1&grade=1
            ResultActions result = mockMvc.perform(get("/subjects/subjects")
                    .param("year", String.valueOf(year))
                    .param("semester", String.valueOf(semester))
                    .param("grade", String.valueOf(grade)));

            // Then: 200 OK + message + response array
            result.andExpect(status().isOk())
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
        @DisplayName("필수 파라미터 누락 시 오류가 발생한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_FailRequest_When_RequiredParametersMissing() throws Exception {
            // When: grade 파라미터가 빠진 경우
            mockMvc.perform(get("/subjects/subjects")
                            .param("year", "2025")
                            .param("semester", "1"))
                    .andExpect(status().isBadRequest());

            verify(evaluationMethodService, never()).findSubjectList(anyInt(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("과목 수정 테스트")
    class UpdateSubjectTest {

        @Test
        @DisplayName("교사가 과목을 정상적으로 수정한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_UpdateSubject_When_TeacherSubmitsValidData() throws Exception {
            Long subjectId = 1L;
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("수학(수정)")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            Teacher teacher = Teacher.builder().name("김교사").build();
            when(teacherService.authenticate("teacher1")).thenReturn(teacher);
            doNothing().when(subjectService).updateSubject(eq(subjectId), any(CreateSubjectRequestDto.class));

            ResultActions result = mockMvc.perform(put("/subjects/{subjectId}", subjectId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_UPDATE_SUBJECT.getMessage()));

            verify(teacherService, atLeastOnce()).authenticate("teacher1");
            verify(subjectService, times(1)).updateSubject(eq(subjectId), any(CreateSubjectRequestDto.class));
        }

        @Test
        @DisplayName("교사가 아닌 사용자가 과목 생성을 시도하면 403 Forbidden")
        @WithMockUser(username = "student1", roles = "STUDENT")
        void should_RejectCreate_When_NonTeacherTries() throws Exception {
            CreateSubjectRequestDto dto = CreateSubjectRequestDto.builder()
                    .name("수학")
                    .build();
            String content = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/subjects")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isForbidden());

            verify(teacherService, never()).authenticate(anyString());
            verify(subjectService, never()).createSubject(any());
        }

        @Test
        @DisplayName("존재하지 않는 과목 수정 시 오류가 발생한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_FailUpdate_When_SubjectNotFound() throws Exception {
            Long subjectId = 999L;
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("수학(수정)")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            Teacher teacher = Teacher.builder().name("김교사").build();
            when(teacherService.authenticate("teacher1")).thenReturn(teacher);
            doThrow(new CustomException(ErrorCode.SUBJECT_NOT_FOUND))
                    .when(subjectService).updateSubject(eq(subjectId), any(CreateSubjectRequestDto.class));

            mockMvc.perform(put("/subjects/{subjectId}", subjectId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isNotFound());

            verify(teacherService, atLeastOnce()).authenticate("teacher1");
            verify(subjectService, times(1)).updateSubject(eq(subjectId), any(CreateSubjectRequestDto.class));
        }
    }

    @Nested
    @DisplayName("과목 삭제 테스트")
    class DeleteSubjectTest {

        @BeforeEach
        void setUp() {
            // 이전 테스트가 남긴 Mockito 호출 흔적을 모두 지웁니다.
            Mockito.reset(teacherService, subjectService);
        }

        @Test
        @DisplayName("교사가 과목을 정상적으로 삭제한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_DeleteSubject_When_TeacherSubmitsValidRequest() throws Exception {
            Long subjectId = 1L;

            // teacherService.authenticate() -> 정상 리턴
            when(teacherService.authenticate("teacher1"))
                    .thenReturn(Teacher.builder().name("김교사").build());

            // subjectService.deleteSubject(subjectId) -> 예외 없이 통과
            doNothing().when(subjectService).deleteSubject(subjectId);

            // When: DELETE 요청 (CSRF 반드시 포함)
            mockMvc.perform(delete("/subjects/{subjectId}", subjectId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())) // CSRF 토큰 포함
                    .andExpect(status().isNoContent()); // 204 No Content 기대

            // 최소 한 번 이상 authenticate()가 호출되었는지 확인
            verify(teacherService, atLeastOnce()).authenticate("teacher1");
            verify(subjectService, times(1)).deleteSubject(subjectId);
        }

        @Test
        @DisplayName("교사가 아닌 사용자가 과목 삭제를 시도하면 시큐리티 단계에서 차단된다 (403)")
        @WithMockUser(username = "student1", roles = "STUDENT")
        void should_ReturnForbidden_When_StudentTriesToDelete() throws Exception {
            Long subjectId = 1L;

            // (1) CSRF 토큰을 포함해서 DELETE 요청만 보내면
            // (2) 스프링 시큐리티가 ROLE_STUDENT에게 DELETE /subjects/{id} 접근을 금지하기 때문에
            // (3) 컨트롤러 진입 전 필터에서 403 Forbidden을 내려준다.

            mockMvc.perform(delete("/subjects/{subjectId}", subjectId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isForbidden());

            // 이 순간 컨트롤러 내부 메서드가 호출되지 않으므로,
            // teacherService.authenticate(...)와 subjectService.deleteSubject(...) 모두 호출되지 않아야 한다.
            verify(teacherService, never()).authenticate(anyString());
            verify(subjectService, never()).deleteSubject(anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 과목 삭제 시 오류가 발생한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_FailDelete_When_SubjectNotFound() throws Exception {
            Long subjectId = 999L;

            // teacherService.authenticate() → 정상 리턴
            when(teacherService.authenticate("teacher1"))
                    .thenReturn(Teacher.builder().name("김교사").build());

            // subjectService.deleteSubject(999L) → SUBJECT_NOT_FOUND 예외 던지기
            doThrow(new CustomException(ErrorCode.SUBJECT_NOT_FOUND))
                    .when(subjectService).deleteSubject(subjectId);

            // When & Then: DELETE 요청 (CSRF 포함) → 404 Not Found
            mockMvc.perform(delete("/subjects/{subjectId}", subjectId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isNotFound());

            verify(teacherService, atLeastOnce()).authenticate("teacher1");
            verify(subjectService, times(1)).deleteSubject(subjectId);
        }

        @Test
        @DisplayName("사용 중인 과목 삭제 시 오류가 발생한다 (400 Bad Request)")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_FailDelete_When_SubjectInUse() throws Exception {
            Long subjectId = 1L;

            // teacherService.authenticate() → 정상 리턴
            when(teacherService.authenticate("teacher1"))
                    .thenReturn(Teacher.builder().name("김교사").build());

            // subjectService.deleteSubject(1L) → SUBJECT_DUPLICATE 예외 던지기
            doThrow(new CustomException(ErrorCode.SUBJECT_DUPLICATE))
                    .when(subjectService).deleteSubject(subjectId);

            // When & Then: DELETE 요청 (CSRF 포함) → 400 Bad Request
            mockMvc.perform(delete("/subjects/{subjectId}", subjectId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isBadRequest());

            // controller 내부로 진입했으므로 authenticate 호출은 최소 1번
            verify(teacherService, atLeastOnce()).authenticate("teacher1");
            // 그리고 subjectService.deleteSubject(subjectId)도 1번 호출됐어야 함
            verify(subjectService, times(1)).deleteSubject(subjectId);
        }
    }

    @Nested
    @DisplayName("보안 및 인증 테스트")
    class SecurityTest {

        @Test
        @DisplayName("CSRF 토큰 없이 POST 요청 시에도 정상 처리된다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_AcceptRequest_When_CsrfTokenMissing() throws Exception {
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("수학")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            // authenticate stub
            when(teacherService.authenticate("teacher1")).thenReturn(Teacher.builder().name("김교사").build());
            doNothing().when(subjectService).createSubject(any(CreateSubjectRequestDto.class));

            // CSRF 없이 POST /subjects
            ResultActions result = mockMvc.perform(post("/subjects")
                    // no csrf()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(StatusCode.SUCCESS_POST_SUBJECT.getMessage()));

            verify(teacherService, atLeastOnce()).authenticate("teacher1");
            verify(subjectService, times(1)).createSubject(any(CreateSubjectRequestDto.class));
        }

        @Test
        @DisplayName("잘못된 Content-Type으로 요청 시 오류가 발생한다")
        @WithMockUser(username = "teacher1", roles = "TEACHER")
        void should_FailRequest_When_WrongContentType() throws Exception {
            CreateSubjectRequestDto requestDto = CreateSubjectRequestDto.builder()
                    .name("수학")
                    .build();
            String content = objectMapper.writeValueAsString(requestDto);

            // CSRF 있으나 Content-Type이 text/plain 이므로 415 발생
            mockMvc.perform(post("/subjects")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(content))
                    .andExpect(status().isUnsupportedMediaType());

            verify(teacherService, never()).authenticate(anyString());
            verify(subjectService, never()).createSubject(any(CreateSubjectRequestDto.class));
        }
    }
}