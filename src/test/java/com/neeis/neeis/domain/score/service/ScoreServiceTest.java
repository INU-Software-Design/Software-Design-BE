package com.neeis.neeis.domain.score.service;


import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.score.dto.req.ScoreRequestDto;
import com.neeis.neeis.domain.score.dto.res.ScoreSummaryBySubjectDto;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.teacherSubject.service.TeacherSubjectService;
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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock private ScoreRepository scoreRepository;
    @Mock private TeacherService teacherService;
    @Mock private TeacherSubjectService teacherSubjectService;
    @Mock private EvaluationMethodService evaluationMethodService;
    @Mock private ClassroomStudentService classroomStudentService;
    @Mock private ClassroomService classroomService;
    @Mock private SubjectService subjectService;
    @Mock private ScoreSummaryService scoreSummaryService;

    @InjectMocks private ScoreService scoreService;

    private Teacher teacher;
    private Classroom classroom;
    private ClassroomStudent cs1, cs2;
    private Subject subjectMath;
    private EvaluationMethod eval1, eval2;
    private ScoreSummary summary1, summary2;
    private Student s1,s2;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder().build();
        ReflectionTestUtils.setField(teacher, "id", 10L);

        classroom = Classroom.builder().build();
        ReflectionTestUtils.setField(classroom, "id", 20L);

        s1 = Student.builder()
                .admissionDate(LocalDate.now())
                .name("테스트11")
                .phone("010-3333-3333")
                .ssn("000802-3333333")
                .gender("F")
                .address("인천광역시 송도 1129")
                .build();

        s2 = Student.builder()
                .admissionDate(LocalDate.now())
                .name("테스트22")
                .phone("010-3333-4444")
                .ssn("000802-4444444")
                .gender("F")
                .address("인천광역시 송도 1129")
                .build();

        ReflectionTestUtils.setField(s1, "id", 1L);
        ReflectionTestUtils.setField(s2, "id", 1L);

        cs1 = ClassroomStudent.builder().number(1).student(s1).build();
        ReflectionTestUtils.setField(cs1, "id", 30L);
        cs2 = ClassroomStudent.builder().number(2).student(s2).build();
        ReflectionTestUtils.setField(cs2, "id", 31L);
        subjectMath = Subject.builder().name("Math").build();
        ReflectionTestUtils.setField(subjectMath, "id", 40L);

        // two evaluation methods
        eval1 = EvaluationMethod.builder().fullScore(50).weight(30.0).title("Quiz").build();
        ReflectionTestUtils.setField(eval1, "id", 50L);
        ReflectionTestUtils.setField(eval1, "subject", subjectMath);
        ReflectionTestUtils.setField(eval1, "year", 2025);
        ReflectionTestUtils.setField(eval1, "semester", 1);
        ReflectionTestUtils.setField(eval1, "grade", 3);

        eval2 = EvaluationMethod.builder().fullScore(100).weight(70.0).title("Exam").build();
        ReflectionTestUtils.setField(eval2, "id", 51L);
        ReflectionTestUtils.setField(eval2, "subject", subjectMath);
        ReflectionTestUtils.setField(eval2, "year", 2025);
        ReflectionTestUtils.setField(eval2, "semester", 1);
        ReflectionTestUtils.setField(eval2, "grade", 3);

        // summaries
        summary1 = ScoreSummary.builder().sumScore(40.0).originalScore(24)
                .average(50.0).stdDeviation(5.0).rank(1).grade(1).achievementLevel("A").build();
        summary2 = ScoreSummary.builder().sumScore(80.0).originalScore(56)
                .average(75.0).stdDeviation(4.0).rank(2).grade(2).achievementLevel("B").build();
    }

    @Test
    @DisplayName("getScoreSummaryBySubject: 정상 조회 전체 리스트")
    void getScoreSummaryBySubject_success() {
        // given
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(classroomService.findClassroom(2025, 3, 1)).willReturn(classroom);
        given(classroomStudentService.findByClassroom(classroom)).willReturn(List.of(cs1, cs2));
        given(subjectService.getSubject("Math")).willReturn(subjectMath);

        given(evaluationMethodService
                .findAllBySubjectAndYearAndSemesterAndGrade(
                        subjectMath, 2025, 1, 3))
                .willReturn(List.of(eval1, eval2));
        // scores for cs1
        Score s1q = Score.builder().rawScore(40.0).weightedScore(24.0).build();
        Score s1e = Score.builder().rawScore(80.0).weightedScore(56.0).build();
        given(scoreRepository.findByEvaluationMethodAndStudent(eval1, cs1)).willReturn(Optional.of(s1q));
        given(scoreRepository.findByEvaluationMethodAndStudent(eval2, cs1)).willReturn(Optional.of(s1e));
        given(scoreSummaryService.findByStudentAndSubject(30L, 40L)).willReturn(summary1);
        // scores for cs2: none
        given(scoreRepository.findByEvaluationMethodAndStudent(eval1, cs2)).willReturn(Optional.empty());
        given(scoreRepository.findByEvaluationMethodAndStudent(eval2, cs2)).willReturn(Optional.empty());
        given(scoreSummaryService.findByStudentAndSubject(31L, 40L)).willReturn(summary2);

        // when
        List<ScoreSummaryBySubjectDto> result = scoreService.getScoreSummaryBySubject(
                "t1", 2025,1,3,1, "Math");

        // then
        assertThat(result).hasSize(1);
        ScoreSummaryBySubjectDto dto = result.get(0);
        assertThat(dto.getSubjectName()).isEqualTo("Math");
        assertThat(dto.getStudents().stream().map(ScoreSummaryBySubjectDto.StudentScoreDto::getNumber)
                .collect(Collectors.toList()))
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("getScoreSummaryBySubject: 과목 필터 null일 때 모든 과목 조회")
    void getScoreSummaryBySubject_noSubjectName() {
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(classroomService.findClassroom(anyInt(),anyInt(),anyInt())).willReturn(classroom);
        given(classroomStudentService.findByClassroom(any())).willReturn(List.of());
        // no eval => empty list
        given(evaluationMethodService.findSubject(anyInt(),anyInt(),anyInt())).willReturn(List.of());

        List<ScoreSummaryBySubjectDto> res = scoreService.getScoreSummaryBySubject(
                "t1",2025,1,3,1, null);
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("saveOrUpdateScores: 신규 저장 및 요약 업데이트 호출")
    void saveOrUpdateScores_new() {
        // given
        ScoreRequestDto.StudentScoreDto stuDto = ScoreRequestDto.StudentScoreDto.builder()
                .number(1).rawScore(40.0).build();
        ScoreRequestDto request = ScoreRequestDto.builder()
                .evaluationId(50L).classNum(1)
                .students(List.of(stuDto)).build();

        given(teacherService.authenticate("t1")).willReturn(teacher);
        ReflectionTestUtils.setField(eval1, "id", 50L);
        ReflectionTestUtils.setField(eval1, "fullScore", 50.0);
        ReflectionTestUtils.setField(eval1, "weight", 30.0);
        given(evaluationMethodService.findById(50L)).willReturn(eval1);
        given(teacherSubjectService.findByTeacherAndSubject(teacher, subjectMath))
                .willReturn(null);
        given(classroomService.findClassroom(anyInt(),anyInt(),anyInt())).willReturn(classroom);
        given(classroomStudentService.findByClassroomAndNumber(classroom, 1))
                .willReturn(cs1);
        given(scoreRepository.findByEvaluationMethodAndStudent(eval1, cs1))
                .willReturn(Optional.empty());
        given(scoreRepository.save(any(Score.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        scoreService.saveOrUpdateScores("t1", List.of(request));

        // then: save and summary update
        then(scoreRepository).should().save(any(Score.class));
        then(scoreSummaryService).should().updateSummaryForClass(2025,1,3,1);
    }

    @Test
    @DisplayName("saveOrUpdateScores: 기존 점수 업데이트")
    void saveOrUpdateScores_updateExisting() {
        ScoreRequestDto.StudentScoreDto stuDto = ScoreRequestDto.StudentScoreDto.builder()
                .number(1).rawScore(45.0).build();
        ScoreRequestDto request = ScoreRequestDto.builder()
                .evaluationId(50L).classNum(1)
                .students(List.of(stuDto)).build();

        Score existing = Score.builder().rawScore(40.0).weightedScore(24.0).build();
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(evaluationMethodService.findById(50L)).willReturn(eval1);
        given(teacherSubjectService.findByTeacherAndSubject(teacher, subjectMath))
                .willReturn(null);
        given(classroomService.findClassroom(anyInt(),anyInt(),anyInt())).willReturn(classroom);
        given(classroomStudentService.findByClassroomAndNumber(classroom, 1)).willReturn(cs1);
        given(scoreRepository.findByEvaluationMethodAndStudent(eval1, cs1))
                .willReturn(Optional.of(existing));

        scoreService.saveOrUpdateScores("t1", List.of(request));

        assertThat(existing.getRawScore()).isEqualTo(45);
        // weighted recalculated: 45/50*30 = 27
        assertThat(existing.getWeightedScore()).isEqualTo(27);
        then(scoreSummaryService).should().updateSummaryForClass(2025,1,3,1);
    }

    @Test
    @DisplayName("saveOrUpdateScores: 잘못된 점수 -> INVALID_INPUT_VALUE")
    void saveOrUpdateScores_invalidScore() {
        ScoreRequestDto.StudentScoreDto stuDto = ScoreRequestDto.StudentScoreDto.builder()
                .number(1).rawScore(60.0).build(); // exceeds fullScore
        ScoreRequestDto request = ScoreRequestDto.builder()
                .evaluationId(50L).classNum(1)
                .students(List.of(stuDto)).build();
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(evaluationMethodService.findById(50L)).willReturn(eval1);
        given(teacherSubjectService.findByTeacherAndSubject(teacher, subjectMath))
                .willReturn(null);
        given(classroomService.findClassroom(anyInt(),anyInt(),anyInt())).willReturn(classroom);
        given(classroomStudentService.findByClassroomAndNumber(classroom, 1)).willReturn(cs1);

        assertThatThrownBy(() ->
                scoreService.saveOrUpdateScores("t1", List.of(request)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.SCORE_OVER_FULL.getMessage());
    }

    @Test
    @DisplayName("saveOrUpdateScores: 권한 없으면 HANDLE_ACCESS_DENIED")
    void saveOrUpdateScores_noPermission() {
        ScoreRequestDto.StudentScoreDto stuDto = ScoreRequestDto.StudentScoreDto.builder()
                .number(1).rawScore(30.0).build();
        ScoreRequestDto request = ScoreRequestDto.builder()
                .evaluationId(50L).classNum(1)
                .students(List.of(stuDto)).build();
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(evaluationMethodService.findById(50L)).willReturn(eval1);
        given(teacherSubjectService.findByTeacherAndSubject(teacher, subjectMath))
                .willThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

        assertThatThrownBy(() ->
                scoreService.saveOrUpdateScores("t1", List.of(request)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }
}
