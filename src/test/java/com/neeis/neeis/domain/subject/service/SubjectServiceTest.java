package com.neeis.neeis.domain.subject.service;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.SubjectRepository;
import com.neeis.neeis.domain.subject.dto.req.CreateSubjectRequestDto;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TeacherService teacherService;

    @InjectMocks
    private SubjectService subjectService;

    private final String username = "teacher01";


    @Test
    @DisplayName("과목 생성 성공")
    void create_subject_success() {
        // given
        CreateSubjectRequestDto requestDto = CreateSubjectRequestDto
                .builder()
                .name("국어")
                .build();

        when(subjectRepository.existsSubjectByName("국어")).thenReturn(false);

        // when
        subjectService.createSubject(username, requestDto);

        // then
        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("국어");
    }

    @Test
    @DisplayName("과목 중복 생성 예외")
    void create_subject_fail() {
        // given
        CreateSubjectRequestDto requestDto = CreateSubjectRequestDto
                .builder()
                .name("영어")
                .build();
        when(subjectRepository.existsSubjectByName("영어")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> subjectService.createSubject(username, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SUBJECT_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("과목_수정_성공")
    void update_subject_success() {
        // given
        Subject subject = Subject.builder().name("수학").build();
        setField(subject, "id", 1L);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        CreateSubjectRequestDto dto = CreateSubjectRequestDto
                .builder()
                .name("고급 수학")
                .build();

        // when
        subjectService.updateSubject(username, 1L, dto);

        // then
        assertThat(subject.getName()).isEqualTo("고급 수학");
    }

    @Test
    @DisplayName("과목_수정_실패_데이터없음")
    void update_subject_fail() {
        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());
        CreateSubjectRequestDto dto = CreateSubjectRequestDto
                .builder()
                .name("영어")
                .build();

        assertThatThrownBy(() -> subjectService.updateSubject(username, 999L, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
    }

    @Test
    void 과목_전체_조회() {
        List<Subject> subjects = List.of(
                Subject.builder().name("수학").build(),
                Subject.builder().name("영어").build()
        );
        when(subjectRepository.findAllByOrderByNameAsc()).thenReturn(subjects);

        var result = subjectService.getSubjects();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("수학");
    }

    @Test
    void 과목_삭제_성공() {
        Subject subject = Subject.builder().name("지리").build();
        setField(subject, "id", 1L);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        subjectService.deleteSubject(username, 1L);

        verify(subjectRepository).delete(subject);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
