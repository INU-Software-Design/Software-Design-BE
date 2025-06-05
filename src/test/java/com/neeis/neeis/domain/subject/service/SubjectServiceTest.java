package com.neeis.neeis.domain.subject.service;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.SubjectRepository;
import com.neeis.neeis.domain.subject.dto.req.CreateSubjectRequestDto;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubjectService 테스트")
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectService subjectService;

    private Subject testSubject;

    @BeforeEach
    void setUp() {
        // 테스트용 Subject 객체 생성
        testSubject = Subject.builder()
                .name("수학")
                .build();
        ReflectionTestUtils.setField(testSubject, "id", 1L);
    }

    @Nested
    @DisplayName("과목 생성 테스트")
    class CreateSubjectTest {

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
            subjectService.createSubject(requestDto);

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
            assertThatThrownBy(() -> subjectService.createSubject(requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.SUBJECT_DUPLICATE.getMessage());
        }
    }

    @Nested
    @DisplayName("과목 수정 테스트")
    class UpdateSubjectTest {

        @Test
        @DisplayName("과목 수정 성공")
        void update_subject_success() {
            // given
            when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));

            CreateSubjectRequestDto dto = CreateSubjectRequestDto
                    .builder()
                    .name("고급 수학")
                    .build();

            // when
            subjectService.updateSubject(1L, dto);

            // then
            assertThat(testSubject.getName()).isEqualTo("고급 수학");
        }

        @Test
        @DisplayName("과목 수정 실패 - 데이터 없음")
        void update_subject_fail() {
            // given
            when(subjectRepository.findById(999L)).thenReturn(Optional.empty());
            CreateSubjectRequestDto dto = CreateSubjectRequestDto
                    .builder()
                    .name("영어")
                    .build();

            // when & then
            assertThatThrownBy(() -> subjectService.updateSubject(999L, dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("과목 조회 테스트")
    class GetSubjectsTest {

        @Test
        @DisplayName("과목 전체 조회 성공")
        void get_subjects_success() {
            // given
            List<Subject> subjects = List.of(
                    Subject.builder().name("수학").build(),
                    Subject.builder().name("영어").build()
            );
            when(subjectRepository.findAllByOrderByNameAsc()).thenReturn(subjects);

            // when
            var result = subjectService.getSubjects();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("수학");
            assertThat(result.get(1).getName()).isEqualTo("영어");
        }

        @Test
        @DisplayName("과목 전체 조회 - 빈 리스트")
        void get_subjects_empty() {
            // given
            when(subjectRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

            // when
            var result = subjectService.getSubjects();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("과목 삭제 테스트")
    class DeleteSubjectTest {

        @Test
        @DisplayName("과목 삭제 성공")
        void delete_subject_success() {
            // given
            when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));

            // when
            subjectService.deleteSubject(1L);

            // then
            verify(subjectRepository).delete(testSubject);
        }

        @Test
        @DisplayName("과목 삭제 실패 - 데이터 없음")
        void delete_subject_fail() {
            // given
            when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subjectService.deleteSubject(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("과목명으로 조회 테스트 - 새로 추가된 메서드")
    class GetSubjectByNameTest {

        @Test
        @DisplayName("과목명으로 조회 성공")
        void getSubject_by_name_success() {
            // given
            String subjectName = "수학";
            when(subjectRepository.findByName(subjectName)).thenReturn(Optional.of(testSubject));

            // when
            Subject result = subjectService.getSubject(subjectName);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("수학");
            assertThat(result.getId()).isEqualTo(1L);
            verify(subjectRepository).findByName(subjectName);
        }

        @Test
        @DisplayName("과목명으로 조회 실패 - 존재하지 않는 과목")
        void getSubject_by_name_not_found() {
            // given
            String nonExistentSubjectName = "존재하지않는과목";
            when(subjectRepository.findByName(nonExistentSubjectName)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subjectService.getSubject(nonExistentSubjectName))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());

            verify(subjectRepository).findByName(nonExistentSubjectName);
        }

        @Test
        @DisplayName("과목명으로 조회 - 빈 문자열")
        void getSubject_by_empty_name() {
            // given
            String emptyName = "";
            when(subjectRepository.findByName(emptyName)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subjectService.getSubject(emptyName))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("과목명으로 조회 - null")
        void getSubject_by_null_name() {
            // given
            when(subjectRepository.findByName(null)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subjectService.getSubject(null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("ID로 조회 테스트 - 새로 추가된 메서드")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 조회 성공")
        void findById_success() {
            // given
            Long subjectId = 1L;
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

            // when
            Subject result = subjectService.findById(subjectId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("수학");
            verify(subjectRepository).findById(subjectId);
        }

        @Test
        @DisplayName("ID로 조회 실패 - 존재하지 않는 ID")
        void findById_not_found() {
            // given
            Long nonExistentId = 999L;
            when(subjectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subjectService.findById(nonExistentId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());

            verify(subjectRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("ID로 조회 실패 - null ID")
        void findById_null_id() {
            // given
            when(subjectRepository.findById(null)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subjectService.findById(null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("ID로 조회 실패 - 음수 ID")
        void findById_negative_id() {
            // given
            Long negativeId = -1L;
            when(subjectRepository.findById(negativeId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subjectService.findById(negativeId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("동일한 과목이 이름과 ID로 조회 시 같은 결과 반환")
        void same_subject_different_query_methods() {
            // given
            String subjectName = "수학";
            Long subjectId = 1L;

            when(subjectRepository.findByName(subjectName)).thenReturn(Optional.of(testSubject));
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

            // when
            Subject resultByName = subjectService.getSubject(subjectName);
            Subject resultById = subjectService.findById(subjectId);

            // then
            assertThat(resultByName).isEqualTo(resultById);
            assertThat(resultByName.getId()).isEqualTo(resultById.getId());
            assertThat(resultByName.getName()).isEqualTo(resultById.getName());
        }

        @Test
        @DisplayName("생성 후 조회가 정상 동작")
        void create_then_find_integration() {
            // given
            CreateSubjectRequestDto createDto = CreateSubjectRequestDto.builder()
                    .name("과학")
                    .build();

            Subject savedSubject = Subject.builder().name("과학").build();
            ReflectionTestUtils.setField(savedSubject, "id", 2L);

            when(subjectRepository.existsSubjectByName("과학")).thenReturn(false);
            when(subjectRepository.findById(2L)).thenReturn(Optional.of(savedSubject));

            // when
            subjectService.createSubject(createDto);
            Subject foundSubject = subjectService.findById(2L);

            // then
            assertThat(foundSubject.getName()).isEqualTo("과학");
            assertThat(foundSubject.getId()).isEqualTo(2L);
        }
    }
}