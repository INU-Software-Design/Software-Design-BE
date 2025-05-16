package com.neeis.neeis.domain.scoreSummary.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.ExamType;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.ScoreSummaryRepository;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackRequestDto;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackUpdateDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.EvaluationMethodScoreDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.ScoreFeedbackDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreSummaryServiceTest {

    @Mock private ScoreSummaryRepository scoreSummaryRepository;
    @Mock private ClassroomService classroomService;
    @Mock private ClassroomStudentService classroomStudentService;
    @Mock private EvaluationMethodService evaluationMethodService;
    @Mock private ScoreRepository scoreRepository;
    @Mock private TeacherService teacherService;

    @InjectMocks private ScoreSummaryService scoreSummaryService;

    private Classroom classroom;
    private ClassroomStudent cs;
    private Subject subjectA;
    private EvaluationMethod evalA;
    private Score scoreA1, scoreA2;
    private ScoreSummary summaryA;
    private Student student;

    @BeforeEach
    void setUp() {
        // Classroom and student
        student = Student.builder()
                .admissionDate(LocalDate.now())
                .name("테스트")
                .phone("010-3333-3333")
                .ssn("000802-3333333")
                .gender("F")
                .address("인천광역시 송도 1129")
                .build();

        ReflectionTestUtils.setField(student, "id", 1L);

        classroom = Classroom.builder().build();
        ReflectionTestUtils.setField(classroom, "id", 100L);
        cs = ClassroomStudent.builder().student(student).number(5).build();
        ReflectionTestUtils.setField(cs, "id", 200L);

        // Subjects and methods
        subjectA = Subject.builder().name("History").build();
        ReflectionTestUtils.setField(subjectA, "id", 300L);

        evalA = EvaluationMethod.builder().examType(ExamType.WRITTEN).fullScore(100).weight(100.0).title("Final").build();
        ReflectionTestUtils.setField(evalA, "id", 400L);
        ReflectionTestUtils.setField(evalA, "subject", subjectA);
        ReflectionTestUtils.setField(evalA, "year", 2025);
        ReflectionTestUtils.setField(evalA, "semester", 2);

        // Two scores for subjectA
        scoreA1 = Score.builder().rawScore(80.0).weightedScore(80.0).evaluationMethod(evalA).student(cs).build();
        scoreA2 = Score.builder().rawScore(60.0).weightedScore(60.0).evaluationMethod(evalA).student(cs).build();

        // Summary for subjectA
        summaryA = ScoreSummary.builder()
                .classroomStudent(cs)
                .subject(subjectA)
                .sumScore(140.0)
                .originalScore(140)
                .average(70.0)
                .stdDeviation(10.0)
                .rank(1)
                .grade(1)
                .achievementLevel("A")
                .totalStudentCount(20)
                .build();
        ReflectionTestUtils.setField(summaryA, "id", 500L);
    }

    @Test
    @DisplayName("getStudentSummary: 정상 조회")
    void getStudentSummary_success() {
        // given
        given(teacherService.authenticate("tuser")).willReturn(null);
        given(classroomService.findClassroom(2025, 1, 1)).willReturn(classroom);
        given(classroomStudentService.findByClassroomAndNumber(classroom, 5)).willReturn(cs);

        // repository returns two scores
        given(scoreRepository
                .findAllByStudentAndEvaluationMethod_YearAndEvaluationMethod_Semester(
                        cs, 2025, 2))
                .willReturn(List.of(scoreA1, scoreA2));

        // summaries repo: returns one summary matching subjectA
        given(scoreSummaryRepository.findAllByClassroomStudent(cs))
                .willReturn(List.of(summaryA));

        // when
        StudentScoreSummaryDto dto = scoreSummaryService
                .getStudentSummary("tuser", 2025,2,1,1,5);

        // then
        assertThat(dto.getNumber()).isEqualTo(5);
        assertThat(dto.getStudentName()).isEqualTo(cs.getStudent().getName());
        assertThat(dto.getSubjects()).hasSize(1);
        SubjectScoreDto sd = dto.getSubjects().get(0);
        assertThat(sd.getSubjectName()).isEqualTo("History");
        // 점수 리스트와 summary 일치
        var ems = sd.getEvaluationMethods();
        assertThat(ems).hasSize(2);
        assertThat(ems.stream()
                .map(EvaluationMethodScoreDto::getRawScore)
                .collect(Collectors.toList()))
             .containsExactlyInAnyOrder(80.0, 60.0);
    }

    @Test
    @DisplayName("getStudentSummary: summary 없으면 빈 리스트")
    void getStudentSummary_noSummary() {
        given(teacherService.authenticate("tuser")).willReturn(null);
        given(classroomService.findClassroom(anyInt(),anyInt(),anyInt())).willReturn(classroom);
        given(classroomStudentService.findByClassroomAndNumber(any(), anyInt())).willReturn(cs);
        given(scoreRepository.findAllByStudentAndEvaluationMethod_YearAndEvaluationMethod_Semester(
                any(), anyInt(), anyInt()))
                .willReturn(List.of());
        given(scoreSummaryRepository.findAllByClassroomStudent(cs)).willReturn(List.of());

        StudentScoreSummaryDto dto = scoreSummaryService
                .getStudentSummary("tuser",2025,2,1,1,5);
        assertThat(dto.getSubjects()).isEmpty();
    }

    @Test
    @DisplayName("updateSummaryForClass: 정상 처리")
    void updateSummaryForClass_success() {
        // 한 과목, 한 학생이 점수 보유
        given(classroomService.findClassroom(2025,1,1)).willReturn(classroom);
        given(classroomStudentService.findByClassroom(classroom)).willReturn(List.of(cs));
        given(evaluationMethodService.findSubject(2025,2,1)).willReturn(List.of(subjectA));
        given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(
                subjectA,2025,2,1)).willReturn(List.of(evalA));
        given(scoreRepository.findAllByStudentAndEvaluationMethodIn(cs, List.of(evalA)))
                .willReturn(List.of(scoreA1, scoreA2));

        // when
        scoreSummaryService.updateSummaryForClass(2025,2,1,1);

        // then: delete 후 save 호출
        then(scoreSummaryRepository).should().deleteBySubjectAndClassroomStudentIn(
                subjectA, List.of(cs));
        then(scoreSummaryRepository).should().save(any(ScoreSummary.class));
    }

    @Test
    @DisplayName("saveFeedback/updateFeedback/getFeedback: 정상 흐름")
    void feedbackLifecycle_success() {
        ScoreFeedbackRequestDto req = ScoreFeedbackRequestDto.builder()
                .scoreSummaryId(500L)
                .feedback("Good work")
                .build();
        ScoreFeedbackUpdateDto upd = ScoreFeedbackUpdateDto.builder()
                .feedback("Needs improvement")
                .build();
        // repository findById
        given(teacherService.authenticate("tuser")).willReturn(null);
        given(scoreSummaryRepository.findById(500L)).willReturn(Optional.of(summaryA));

        // when: saveFeedback
        scoreSummaryService.saveFeedback("tuser", req);
        assertThat(summaryA.getFeedback()).isEqualTo("Good work");

        // updateFeedback
        scoreSummaryService.updateFeedback("tuser", 500L, upd);
        assertThat(summaryA.getFeedback()).isEqualTo("Needs improvement");

        // getFeedback
        ScoreFeedbackDto dto = scoreSummaryService.getFeedback(500L);
        assertThat(dto.getFeedback()).isEqualTo("Needs improvement");
    }

    @Test
    @DisplayName("feedback: 없는 summary -> SCORE_SUMMARY_NOT_FOUND")
    void feedback_notFound() {
        given(teacherService.authenticate("tuser")).willReturn(null);
        given(scoreSummaryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> scoreSummaryService.saveFeedback("tuser",
                ScoreFeedbackRequestDto.builder().scoreSummaryId(999L).feedback("x").build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.SCORE_SUMMARY_NOT_FOUND.getMessage());

        assertThatThrownBy(() -> scoreSummaryService.updateFeedback("tuser", 999L,
                ScoreFeedbackUpdateDto.builder().feedback("x").build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.SCORE_SUMMARY_NOT_FOUND.getMessage());

        assertThatThrownBy(() -> scoreSummaryService.getFeedback(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.SCORE_SUMMARY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findByStudentAndSubject: 정상 조회 및 not found")
    void findByStudentAndSubject() {
        given(scoreSummaryRepository.findByStudentAndSubject(1L, 300L))
                .willReturn(Optional.of(summaryA));
        ScoreSummary found = scoreSummaryService.findByStudentAndSubject(1L, 300L);
        assertThat(found).isSameAs(summaryA);

        given(scoreSummaryRepository.findByStudentAndSubject(2L, 300L))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> scoreSummaryService.findByStudentAndSubject(2L,300L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.SCORE_SUMMARY_NOT_FOUND.getMessage());
    }
}