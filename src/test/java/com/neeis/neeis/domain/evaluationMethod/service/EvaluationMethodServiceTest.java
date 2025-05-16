package com.neeis.neeis.domain.evaluationMethod.service;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethodRepository;
import com.neeis.neeis.domain.evaluationMethod.ExamType;
import com.neeis.neeis.domain.evaluationMethod.dto.req.CreateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.req.UpdateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.res.EvaluationMethodResponseDto;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.dto.res.SubjectResponseDto;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationMethodServiceTest {

    @Mock private EvaluationMethodRepository repository;
    @Mock private SubjectService subjectService;
    @Mock private TeacherService teacherService;

    @InjectMocks private EvaluationMethodService service;

    private Subject subject;
    private EvaluationMethod existing1;
    private EvaluationMethod existing2;
    private EvaluationMethod target;

    @BeforeEach
    void setUp() {
        subject = Subject.builder().name("Math").build();
        ReflectionTestUtils.setField(subject, "id", 100L);

        existing1 = EvaluationMethod.builder()
                .subject(subject)
                .year(2025).semester(1).grade(3)
                .examType(ExamType.WRITTEN)
                .title("Midterm").weight(30.0)
                .build();
        ReflectionTestUtils.setField(existing1, "id", 1L);

        existing2 = EvaluationMethod.builder()
                .subject(subject)
                .year(2025).semester(1).grade(3)
                .examType(ExamType.WRITTEN)
                .title("Final").weight(40.0)
                .build();
        ReflectionTestUtils.setField(existing2, "id", 2L);

        target = EvaluationMethod.builder()
                .subject(subject)
                .year(2025).semester(1).grade(3)
                .examType(ExamType.PRACTICAL)
                .title("Project").weight(20.0)
                .build();
        ReflectionTestUtils.setField(target, "id", 3L);
    }

    @Test
    @DisplayName("save: 정상 저장")
    void save_success() {
        // given
        CreateEvaluationMethodDto dto = CreateEvaluationMethodDto.builder()
                .subject("Math")
                .year(2025).semester(1).grade(3)
                .examType("WRITTEN")
                .title("Quiz").weight(10.0)
                .build();
        given(teacherService.authenticate("t1")).willReturn(null);
        given(subjectService.getSubject("Math")).willReturn(subject);
        given(repository.existsBySubjectAndYearAndSemesterAndGradeAndExamTypeAndTitle(
                subject,2025,1,3,ExamType.WRITTEN,"Quiz"))
                .willReturn(false);
        given(repository.findAllBySubjectAndYearAndSemesterAndGrade(
                subject,2025,1,3))
                .willReturn(List.of(existing1, existing2));
        // when
        service.save("t1", dto);
        // then
        then(repository).should().save(any(EvaluationMethod.class));
    }

    @Test
    @DisplayName("save: 잘못된 examType -> INVALID_INPUT_VALUE")
    void save_invalidExamType() {
        CreateEvaluationMethodDto dto = CreateEvaluationMethodDto.builder()
                .subject("Math").year(2025).semester(1).grade(3)
                .examType("BAD").title("Quiz").weight(10.0)
                .build();
        given(teacherService.authenticate("t1")).willReturn(null);
        given(subjectService.getSubject(anyString())).willReturn(subject);

        assertThatThrownBy(() -> service.save("t1", dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("save: 중복 -> EVALUATION_METHOD_DUPLICATE")
    void save_duplicate() {
        CreateEvaluationMethodDto dto = CreateEvaluationMethodDto.builder()
                .subject("Math").year(2025).semester(1).grade(3)
                .examType("WRITTEN").title("Midterm").weight(10.0)
                .build();
        given(teacherService.authenticate("t1")).willReturn(null);
        given(subjectService.getSubject(anyString())).willReturn(subject);
        given(repository.existsBySubjectAndYearAndSemesterAndGradeAndExamTypeAndTitle(
                any(),anyInt(),anyInt(),anyInt(),any(),anyString()))
                .willReturn(true);

        assertThatThrownBy(() -> service.save("t1", dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_METHOD_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("save: 총 합계 초과 -> EXCESS_TOTAL_100")
    void save_excessTotal() {
        CreateEvaluationMethodDto dto = CreateEvaluationMethodDto.builder()
                .subject("Math").year(2025).semester(1).grade(3)
                .examType("PRACTICAL").title("Quiz").weight(50.0)
                .build();
        given(teacherService.authenticate("t1")).willReturn(null);
        given(subjectService.getSubject(anyString())).willReturn(subject);
        given(repository.existsBySubjectAndYearAndSemesterAndGradeAndExamTypeAndTitle(
                any(),anyInt(),anyInt(),anyInt(),any(),anyString()))
                .willReturn(false);
        given(repository.findAllBySubjectAndYearAndSemesterAndGrade(
                any(),anyInt(),anyInt(),anyInt()))
                .willReturn(List.of(existing1, existing2)); // 30+40

        assertThatThrownBy(() -> service.save("t1", dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EXCESS_TOTAL_100.getMessage());
    }

    @Test
    @DisplayName("getEvaluationMethods: 목록 조회")
    void getEvaluationMethods_success() {
        given(subjectService.getSubject("Math")).willReturn(subject);
        given(repository.findAllBySubjectAndYearAndSemesterAndGrade(
                subject,2025,1,3))
                .willReturn(List.of(existing1, existing2));

        var list = service.getEvaluationMethods("Math",2025,1,3);
        assertThat(list).hasSize(2);
        assertThat(list.get(0)).isInstanceOf(EvaluationMethodResponseDto.class);
    }

    @Test
    @DisplayName("findSubjectList: 과목 리스트 조회")
    void findSubjectList_success() {
        given(repository.findDistinctSubjectsByYearSemesterGrade(2025,1,3))
                .willReturn(List.of(subject));

        var subs = service.findSubjectList(2025,1,3);
        assertThat(subs).hasSize(1)
                .allMatch(SubjectResponseDto.class::isInstance);
    }

    @Test
    @DisplayName("update: 평가방식 없으면 NOT_FOUND")
    void update_notFound() {
        var dto = UpdateEvaluationMethodDto.builder()
                .examType("PRACTICAL").weight(10.0).build();
        given(teacherService.authenticate("t1")).willReturn(null);
        given(repository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("t1",99L,dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_METHOD_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("update: invalid examType -> INVALID_INPUT_VALUE")
    void update_invalidExamType() {
        var dto = UpdateEvaluationMethodDto.builder()
                .examType("BAD").weight(10.0).build();
        given(teacherService.authenticate("t1")).willReturn(null);
        given(repository.findById(3L)).willReturn(Optional.of(existing1));

        assertThatThrownBy(() -> service.update("t1",3L,dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("update: 초과 weight -> EXCESS_TOTAL_100")
    void update_excessTotal() {
        var dto = UpdateEvaluationMethodDto.builder()
                .examType("WRITTEN").weight(100.0).build();
        given(teacherService.authenticate("t1")).willReturn(null);
        // method id=1L existing1, same as id
        ReflectionTestUtils.setField(existing1, "id", 1L);
        given(repository.findById(1L)).willReturn(Optional.of(existing1));
        given(repository.findAllBySubjectAndYearAndSemesterAndGrade(
                subject,2025,1,3))
                .willReturn(List.of(existing1,existing2)); // existing2=40

        assertThatThrownBy(() -> service.update("t1",1L,dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EXCESS_TOTAL_100.getMessage());
    }

    @Test
    @DisplayName("update: 정상 수정")
    void update_success() {
        var dto = UpdateEvaluationMethodDto.builder()
                .examType("PRACTICAL").weight(25.0).fullScore(25.0).build();
        given(teacherService.authenticate("t1")).willReturn(null);
        ReflectionTestUtils.setField(existing1, "id", 1L);
        given(repository.findById(1L)).willReturn(Optional.of(existing1));
        given(repository.findAllBySubjectAndYearAndSemesterAndGrade(
                subject,2025,1,3))
                .willReturn(List.of(existing1,existing2));

        service.update("t1",1L,dto);
        assertThat(existing1.getWeight()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("delete: 없으면 DATA_NOT_FOUND")
    void delete_notFound() {
        given(teacherService.authenticate("t1")).willReturn(null);
        given(repository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("t1",10L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DATA_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("delete: 정상 삭제")
    void delete_success() {
        given(teacherService.authenticate("t1")).willReturn(null);
        given(repository.findById(1L)).willReturn(Optional.of(existing1));

        service.delete("t1",1L);
        then(repository).should().delete(existing1);
    }

    @Test
    @DisplayName("findById: 없으면 NOT_FOUND")
    void findById_notFound() {
        given(repository.findById(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(5L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_METHOD_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findById: 정상 반환")
    void findById_success() {
        given(repository.findById(2L)).willReturn(Optional.of(existing2));

        EvaluationMethod m = service.findById(2L);
        assertThat(m).isSameAs(existing2);
    }

    @Test
    @DisplayName("findAllBySubjectAndYear...: 리스트 반환")
    void findAllBySubjectAndYear_success() {
        given(repository.findAllBySubjectAndYearAndSemesterAndGrade(
                subject,2025,1,3))
                .willReturn(List.of(existing1));

        var list = service.findAllBySubjectAndYearAndSemesterAndGrade(
                subject,2025,1,3);
        assertThat(list).containsExactly(existing1);
    }

    @Test
    @DisplayName("findSubject: 과목 리스트 반환")
    void findSubject_success() {
        given(repository.findDistinctSubjectsByYearSemesterGrade(2025,1,3))
                .willReturn(List.of(subject));

        var subs = service.findSubject(2025,1,3);
        assertThat(subs).containsExactly(subject);
    }
}
