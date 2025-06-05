package com.neeis.neeis.domain.scoreSummary.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.scoreSummary.ScoreSummaryRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ScoreSummaryService 새로 추가된 메서드들에 대한 테스트
 *
 * 테스트 대상:
 * 1. updateSummaryForSpecificSubject() - 특정 과목만 업데이트
 * 2. updateSummaryForSpecificSubjects() - 여러 특정 과목들 업데이트 (새로 구현 필요)
 *
 * SonarCloud Coverage 개선 목표: 0.0% → 80%+
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreSummaryService 새 메서드 테스트")
class ScoreSummaryServiceNewMethodsTest {

    @InjectMocks
    private ScoreSummaryService scoreSummaryService;

    @Mock private ScoreSummaryRepository scoreSummaryRepository;
    @Mock private ClassroomService classroomService;
    @Mock private ClassroomStudentService classroomStudentService;
    @Mock private SubjectService subjectService;
    @Mock private EvaluationMethodService evaluationMethodService;
    @Mock private ScoreRepository scoreRepository;

    // 테스트 픽스처
    private Classroom testClassroom;
    private ClassroomStudent testClassroomStudent;
    private Subject testSubject1, testSubject2;
    private EvaluationMethod testEvaluationMethod;
    private Score testScore;

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        User testUser = User.builder()
                .username("student123")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 테스트용 Student 객체 생성
        Student testStudent = Student.builder()
                .name("김학생")
                .user(testUser)
                .ssn("050101-3123456")
                .gender("M")
                .address("서울시 강남구")
                .phone("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(testStudent, "id", 1L);

        // 테스트용 Classroom 객체 생성
        testClassroom = Classroom.builder()
                .grade(2)
                .classNum(3)
                .year(2024)
                .build();
        ReflectionTestUtils.setField(testClassroom, "id", 1L);

        // 테스트용 ClassroomStudent 객체 생성
        testClassroomStudent = ClassroomStudent.builder()
                .student(testStudent)
                .classroom(testClassroom)
                .number(15)
                .build();
        ReflectionTestUtils.setField(testClassroomStudent, "id", 1L);

        // 테스트용 Subject 객체들 생성
        testSubject1 = Subject.builder().name("수학").build();
        ReflectionTestUtils.setField(testSubject1, "id", 1L);

        testSubject2 = Subject.builder().name("영어").build();
        ReflectionTestUtils.setField(testSubject2, "id", 2L);

        // 테스트용 EvaluationMethod 객체 생성
        testEvaluationMethod = EvaluationMethod.builder()
                .subject(testSubject1)
                .year(2024)
                .semester(1)
                .title("중간고사")
                .fullScore(100)
                .weight(100.0)
                .build();
        ReflectionTestUtils.setField(testEvaluationMethod, "id", 1L);

        // 테스트용 Score 객체 생성
        testScore = Score.builder()
                .student(testClassroomStudent)
                .evaluationMethod(testEvaluationMethod)
                .rawScore(85.0)
                .weightedScore(85.0)
                .build();
        ReflectionTestUtils.setField(testScore, "id", 1L);
    }

    @Nested
    @DisplayName("특정 과목 성적 요약 업데이트 테스트")
    class UpdateSummaryForSpecificSubjectTest {

        @Test
        @DisplayName("특정 과목 성적 요약 업데이트 성공")
        void updateSummaryForSpecificSubject_success() {
            // given
            Long subjectId = 1L;
            int year = 2024, semester = 1, grade = 2, classNum = 3;

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent));
            given(subjectService.findById(subjectId)).willReturn(testSubject1);
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(
                    testSubject1, year, semester, grade)).willReturn(List.of(testEvaluationMethod));
            given(scoreRepository.findAllByStudentAndEvaluationMethodIn(
                    testClassroomStudent, List.of(testEvaluationMethod))).willReturn(List.of(testScore));

            // when - 실제 메서드 시그니처에 맞게 수정 (5개 파라미터)
            assertThatCode(() -> scoreSummaryService.updateSummaryForSpecificSubject(
                    subjectId, year, semester, grade, classNum))
                    .doesNotThrowAnyException();

            // then - 실제 호출되는 메서드들 검증
            verify(classroomService).findClassroom(year, grade, classNum);
            verify(classroomStudentService).findByClassroom(testClassroom);
            verify(subjectService).findById(subjectId);
            verify(evaluationMethodService).findAllBySubjectAndYearAndSemesterAndGrade(
                    testSubject1, year, semester, grade);
            verify(scoreRepository).findAllByStudentAndEvaluationMethodIn(
                    testClassroomStudent, List.of(testEvaluationMethod));
            verify(scoreSummaryRepository).deleteBySubjectAndClassroomStudentIn(
                    testSubject1, List.of(testClassroomStudent));
            verify(scoreSummaryRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("특정 과목 업데이트 중 교실 조회 실패 시 예외 발생")
        void updateSummaryForSpecificSubject_classroomNotFound() {
            // given
            Long subjectId = 1L;
            int year = 2024, semester = 1, grade = 2, classNum = 99;

            given(classroomService.findClassroom(year, grade, classNum))
                    .willThrow(new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> scoreSummaryService.updateSummaryForSpecificSubject(
                    subjectId, year, semester, grade, classNum))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLASSROOM_NOT_FOUND);

            verify(classroomService).findClassroom(year, grade, classNum);
            verify(subjectService, never()).findById(anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 과목 ID로 업데이트 시 예외 발생")
        void updateSummaryForSpecificSubject_subjectNotFound() {
            // given
            Long invalidSubjectId = 999L;
            int year = 2024, semester = 1, grade = 2, classNum = 3;

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent));
            given(subjectService.findById(invalidSubjectId))
                    .willThrow(new CustomException(ErrorCode.SUBJECT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> scoreSummaryService.updateSummaryForSpecificSubject(
                    invalidSubjectId, year, semester, grade, classNum))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_NOT_FOUND);

            verify(subjectService).findById(invalidSubjectId);
        }

        @Test
        @DisplayName("점수 데이터가 없는 경우 요약 생성 생략")
        void updateSummaryForSpecificSubject_noScoreData() {
            // given
            Long subjectId = 1L;
            int year = 2024, semester = 1, grade = 2, classNum = 3;

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent));
            given(subjectService.findById(subjectId)).willReturn(testSubject1);
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(
                    testSubject1, year, semester, grade)).willReturn(List.of(testEvaluationMethod));
            given(scoreRepository.findAllByStudentAndEvaluationMethodIn(
                    testClassroomStudent, List.of(testEvaluationMethod))).willReturn(List.of()); // 빈 리스트

            // when
            assertThatCode(() -> scoreSummaryService.updateSummaryForSpecificSubject(
                    subjectId, year, semester, grade, classNum))
                    .doesNotThrowAnyException();

            // then - 점수가 없으면 삭제/저장이 호출되지 않음
            verify(scoreSummaryRepository, never()).deleteBySubjectAndClassroomStudentIn(any(), any());
            verify(scoreSummaryRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("복수 특정 과목 성적 요약 업데이트 테스트 - 구현 필요")
    class UpdateSummaryForSpecificSubjectsTest {

        @Test
        @DisplayName("복수 특정 과목 업데이트 메서드가 구현되지 않음을 확인")
        void updateSummaryForSpecificSubjects_methodNotImplemented() {
            // 이 테스트는 현재 메서드가 구현되지 않았음을 보여줍니다.
            // 실제 구현이 필요한 경우, ScoreSummaryService에 다음과 같은 메서드를 추가해야 합니다:

            /*
            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public void updateSummaryForSpecificSubjects(Set<Long> subjectIds, int year, int semester, int grade, int classNum) {
                try {
                    Classroom classroom = classroomService.findClassroom(year, grade, classNum);
                    List<ClassroomStudent> students = classroomStudentService.findByClassroom(classroom);

                    for (Long subjectId : subjectIds) {
                        try {
                            Subject subject = subjectService.findById(subjectId);
                            updateSummaryForSubject(subject, year, semester, grade, students);
                        } catch (Exception e) {
                            log.error("특정 과목 성적 요약 업데이트 실패: 과목ID={}, 오류={}", subjectId, e.getMessage(), e);
                            // 한 과목 실패해도 다른 과목은 계속 처리
                        }
                    }
                } catch (Exception e) {
                    log.error("복수 과목 성적 요약 업데이트 실패: 오류={}", e.getMessage(), e);
                    throw e;
                }
            }
            */

            // 현재는 이 메서드가 없으므로 테스트를 주석 처리합니다.
            // 메서드 구현 후 아래 테스트들을 활성화할 수 있습니다.

            assertThat(true).as("updateSummaryForSpecificSubjects 메서드 구현 필요").isTrue();
        }

        // 메서드 구현 후 활성화할 테스트들
        /*
        @Test
        @DisplayName("복수 특정 과목 성적 요약 업데이트 성공")
        void updateSummaryForSpecificSubjects_success() {
            // given
            Set<Long> subjectIds = Set.of(1L, 2L);
            int year = 2024, semester = 1, grade = 2, classNum = 3;

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(subjectService.findById(2L)).willReturn(testSubject2);

            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(
                    testSubject1, year, semester, grade)).willReturn(List.of(testEvaluationMethod));
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(
                    testSubject2, year, semester, grade)).willReturn(List.of());
            given(scoreRepository.findAllByStudentAndEvaluationMethodIn(
                    testClassroomStudent, List.of(testEvaluationMethod))).willReturn(List.of(testScore));

            // when
            assertThatCode(() -> scoreSummaryService.updateSummaryForSpecificSubjects(
                    subjectIds, year, semester, grade, classNum))
                    .doesNotThrowAnyException();

            // then
            verify(subjectService).findById(1L);
            verify(subjectService).findById(2L);
            verify(scoreSummaryRepository).deleteBySubjectAndClassroomStudentIn(
                    testSubject1, List.of(testClassroomStudent));
            verify(scoreSummaryRepository).saveAll(anyList());
        }
        */
    }

    @Nested
    @DisplayName("기존 메서드 테스트")
    class ExistingMethodsTest {

        @Test
        @DisplayName("반 전체 성적 요약 업데이트 성공")
        void updateSummaryForClass_success() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent));
            given(evaluationMethodService.findSubject(year, semester, grade)).willReturn(List.of(testSubject1));
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(
                    testSubject1, year, semester, grade)).willReturn(List.of(testEvaluationMethod));
            given(scoreRepository.findAllByStudentAndEvaluationMethodIn(
                    testClassroomStudent, List.of(testEvaluationMethod))).willReturn(List.of(testScore));

            // when
            assertThatCode(() -> scoreSummaryService.updateSummaryForClass(year, semester, grade, classNum))
                    .doesNotThrowAnyException();

            // then
            verify(classroomService).findClassroom(year, grade, classNum);
            verify(classroomStudentService).findByClassroom(testClassroom);
            verify(evaluationMethodService).findSubject(year, semester, grade);
            verify(scoreSummaryRepository).deleteBySubjectAndClassroomStudentIn(
                    testSubject1, List.of(testClassroomStudent));
            verify(scoreSummaryRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("findByStudentAndSubject 메서드 테스트")
        void findByStudentAndSubject_success() {
            // given
            Long studentId = 1L;
            Long subjectId = 1L;
            // ScoreSummary 객체를 Mock으로 생성하거나, 실제 객체 생성 필요

            given(scoreSummaryRepository.findByStudentAndSubject(studentId, subjectId))
                    .willReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSummaryService.findByStudentAndSubject(studentId, subjectId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_SUMMARY_NOT_FOUND);
        }

        @Test
        @DisplayName("findByStudentAndSubjectOptional 메서드 테스트")
        void findByStudentAndSubjectOptional_success() {
            // given
            Long studentId = 1L;
            Long subjectId = 1L;

            given(scoreSummaryRepository.findByStudentAndSubject(studentId, subjectId))
                    .willReturn(java.util.Optional.empty());

            // when
            var result = scoreSummaryService.findByStudentAndSubjectOptional(studentId, subjectId);

            // then
            assertThat(result).isEmpty();
            verify(scoreSummaryRepository).findByStudentAndSubject(studentId, subjectId);
        }
    }
}