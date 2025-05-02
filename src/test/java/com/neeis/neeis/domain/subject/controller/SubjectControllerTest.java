package com.neeis.neeis.domain.subject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neeis.neeis.domain.subject.dto.req.CreateSubjectRequestDto;
import com.neeis.neeis.domain.subject.service.SubjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubjectController.class)
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubjectService subjectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("과목 생성 성공")
    @WithMockUser
    void createSubject_success() throws Exception {
        CreateSubjectRequestDto request = CreateSubjectRequestDto
                                                        .builder()
                                                        .name("수학")
                                                        .build();

        mockMvc.perform(post("/subject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("과목 전체 조회 성공")
    void getSubjects_success() throws Exception {
        Mockito.when(subjectService.getSubjects()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/subject"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("과목 수정 성공")
    @WithMockUser
    void updateSubject_success() throws Exception {
        CreateSubjectRequestDto request = CreateSubjectRequestDto
                .builder()
                .name("과학")
                .build();


        mockMvc.perform(put("/subject/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("과목 삭제 성공")
    @WithMockUser
    void deleteSubject_success() throws Exception {
        mockMvc.perform(delete("/subject/1"))
                .andExpect(status().isNoContent());
    }
}