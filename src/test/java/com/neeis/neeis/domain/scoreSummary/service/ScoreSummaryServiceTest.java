package com.neeis.neeis.domain.scoreSummary.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.ExamType;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.ScoreSummaryRepository;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackRequestDto;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackUpdateDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.ScoreFeedbackDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.dto.report.SubjectFeedbackDto;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendFeedbackFcmEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ScoreSummaryService 테스트 (업데이트 버전)
 *
 * 변경사항:
 * 1. checkValidate 메서드를 통한 역할별 권한 검사 추가
 * 2. getUserAndValidateAccess 메서드를 통한 세밀한 접근 제어
 * 3. FCM 이벤트 발행 및 알림 서비스 추가
 * 4. 학생/교사 역할별 접근 권한 처리
 */
@ExtendWith(MockitoExtension.class)
class ScoreSummaryServiceTest {

    @Mock private ScoreSummaryRepository scoreSummaryRepository;
    @Mock private ClassroomService classroomService;
    @Mock private UserService userService;
    @Mock private ClassroomStudentService classroomStudentService;
    @Mock private ClassroomStudentRepository classroomStudentRepository;
    @Mock private EvaluationMethodService evaluationMethodService;
    @Mock private ScoreRepository scoreRepository;
    @Mock private TeacherService teacherService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;

    @InjectMocks private ScoreSummaryService scoreSummaryService;

    // 테스트 픽스처
    private User teacherUser, studentUser;
    private Teacher teacher;
    private Student student;
    private Classroom classroom;
    private ClassroomStudent classroomStudent;
    private Subject subject;
    private EvaluationMethod evaluationMethod;
    private Score score1, score2;
    private ScoreSummary scoreSummary;

    @BeforeEach
    void setUp() {
        setupTestFixtures();
    }

    private void setupTestFixtures() {
        // 사용자 관련 데이터
        teacherUser = createUser("teacher1", Role.TEACHER);
        studentUser = createUser("student1", Role.STUDENT);

        teacher = createTeacher(teacherUser, "김교사");
        ReflectionTestUtils.setField(teacher, "id", 1L);

        student = createStudent(studentUser, "홍길동");
        ReflectionTestUtils.setField(student, "id", 1L);

        // 교실 및 학급 학생 데이터
        classroom = createClassroom(2025, 2, 1, teacher);
        ReflectionTestUtils.setField(classroom, "id", 1L);

        classroomStudent = createClassroomStudent(1, student, classroom);
        ReflectionTestUtils.setField(classroomStudent, "id", 1L);

        // 과목 및 평가 방법
        subject = createSubject("수학");
        ReflectionTestUtils.setField(subject, "id", 1L);

        evaluationMethod = createEvaluationMethod(subject, 2025, 2, ExamType.WRITTEN, "중간고사", 100, 100.0);
        ReflectionTestUtils.setField(evaluationMethod, "id", 1L);

        // 점수 데이터
        score1 = createScore(classroomStudent, evaluationMethod, 85.0, 85.0);
        score2 = createScore(classroomStudent, evaluationMethod, 90.0, 90.0);

        // 성적 요약 데이터
        scoreSummary = createScoreSummary(classroomStudent, subject, 175.0, 87.5, 5.0, 1, 1, "A");
        ReflectionTestUtils.setField(scoreSummary, "id", 1L);
    }

    @Nested
    @DisplayName("학생 성적 요약 조회 테스트")
    class GetStudentSummaryTest {

        @Test
        @DisplayName("교사가 학생의 성적 요약을 정상적으로 조회한다")
        void should_ReturnStudentSummary_When_TeacherRequests() {
            // Given: 교사가 학생의 성적 요약을 조회할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1))
                    .willReturn(Optional.of(classroomStudent));
            given(scoreRepository.findAllByStudentAndEvaluationMethod_YearAndEvaluationMethod_Semester(
                    classroomStudent, 2025, 2)).willReturn(List.of(score1, score2));
            given(scoreSummaryRepository.findAllByClassroomStudent(classroomStudent))
                    .willReturn(List.of(scoreSummary));

            // When: 성적 요약을 조회하면
            StudentScoreSummaryDto result = scoreSummaryService.getStudentSummary(
                    "teacher1", 2025, 2, 2, 1, 1);

            // Then: 성적 요약이 반환된다
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getStudentName()).isEqualTo("홍길동");
            assertThat(result.getSubjects()).hasSize(1);

            SubjectScoreDto subjectScore = result.getSubjects().get(0);
            assertThat(subjectScore.getSubjectName()).isEqualTo("수학");
            assertThat(subjectScore.getAverage()).isEqualTo(87.5);
        }

        @Test
        @DisplayName("학생이 자신의 성적 요약을 조회할 수 있다")
        void should_ReturnStudentSummary_When_StudentRequestsOwnData() {
            // Given: 학생이 자신의 성적 요약을 조회할 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser))
                    .willReturn(Optional.of(classroomStudent));
            given(scoreRepository.findAllByStudentAndEvaluationMethod_YearAndEvaluationMethod_Semester(
                    classroomStudent, 2025, 2)).willReturn(List.of(score1, score2));
            given(scoreSummaryRepository.findAllByClassroomStudent(classroomStudent))
                    .willReturn(List.of(scoreSummary));

            // When: 본인 성적 요약을 조회하면
            StudentScoreSummaryDto result = scoreSummaryService.getStudentSummary(
                    "student1", 2025, 2, 2, 1, 1);

            // Then: 성적 요약이 반환된다
            assertThat(result.getStudentName()).isEqualTo("홍길동");
            assertThat(result.getSubjects()).hasSize(1);
        }

        @Test
        @DisplayName("학생이 다른 학생의 성적을 조회하면 예외가 발생한다")
        void should_ThrowException_When_StudentRequestsOthersData() {
            // Given: 학생이 다른 학생의 성적을 조회할 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser))
                    .willReturn(Optional.of(classroomStudent));

            // When & Then: 다른 학생 번호로 조회하면 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.getStudentSummary(
                    "student1", 2025, 2, 2, 1, 2))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 학급에 접근하면 예외가 발생한다")
        void should_ThrowException_When_ClassroomNotFound() {
            // Given: 존재하지 않는 학급에 접근할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 3, 5))
                    .willThrow(new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));

            // When & Then: 학급을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.getStudentSummary(
                    "teacher1", 2025, 2, 3, 5, 1))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.CLASSROOM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("반 전체 성적 요약 업데이트 테스트")
    class UpdateSummaryForClassTest {

        @Test
        @DisplayName("반 전체 성적 요약을 정상적으로 업데이트한다")
        void should_UpdateSummaryForClass_When_ValidDataProvided() {
            // Given: 반 전체 성적 요약을 업데이트할 때
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentService.findByClassroom(classroom)).willReturn(List.of(classroomStudent));
            given(evaluationMethodService.findSubject(2025, 2, 2)).willReturn(List.of(subject));
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(subject, 2025, 2, 2))
                    .willReturn(List.of(evaluationMethod));
            given(scoreRepository.findAllByStudentAndEvaluationMethodIn(classroomStudent, List.of(evaluationMethod)))
                    .willReturn(List.of(score1, score2));

            // When: 성적 요약을 업데이트하면
            assertThatCode(() -> scoreSummaryService.updateSummaryForClass(2025, 2, 2, 1))
                    .doesNotThrowAnyException();

            // Then: 기존 요약이 삭제되고 새로운 요약이 저장된다
            then(scoreSummaryRepository).should().deleteBySubjectAndClassroomStudentIn(
                    subject, List.of(classroomStudent));
            then(scoreSummaryRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("점수 데이터가 없는 과목은 요약 생성을 건너뛴다")
        void should_SkipSummary_When_NoScoreData() {
            // Given: 점수 데이터가 없는 과목이 있을 때
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentService.findByClassroom(classroom)).willReturn(List.of(classroomStudent));
            given(evaluationMethodService.findSubject(2025, 2, 2)).willReturn(List.of(subject));
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(subject, 2025, 2, 2))
                    .willReturn(List.of(evaluationMethod));
            given(scoreRepository.findAllByStudentAndEvaluationMethodIn(classroomStudent, List.of(evaluationMethod)))
                    .willReturn(List.of()); // 빈 점수 리스트

            // When: 성적 요약을 업데이트하면
            assertThatCode(() -> scoreSummaryService.updateSummaryForClass(2025, 2, 2, 1))
                    .doesNotThrowAnyException();

            // Then: 삭제나 저장이 호출되지 않는다
            then(scoreSummaryRepository).should(never()).deleteBySubjectAndClassroomStudentIn(any(), any());
            then(scoreSummaryRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("과목별 업데이트 실패 시에도 다른 과목은 계속 처리한다")
        void should_ContinueOtherSubjects_When_OneSubjectFails() {
            // Given: 여러 과목 중 하나가 실패할 때
            Subject subject2 = createSubject("영어");
            ReflectionTestUtils.setField(subject2, "id", 2L);

            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentService.findByClassroom(classroom)).willReturn(List.of(classroomStudent));
            given(evaluationMethodService.findSubject(2025, 2, 2)).willReturn(List.of(subject, subject2));

            // 첫 번째 과목은 정상, 두 번째 과목은 실패
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(subject, 2025, 2, 2))
                    .willReturn(List.of(evaluationMethod));
            given(evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(subject2, 2025, 2, 2))
                    .willThrow(new RuntimeException("Database error"));

            given(scoreRepository.findAllByStudentAndEvaluationMethodIn(classroomStudent, List.of(evaluationMethod)))
                    .willReturn(List.of(score1, score2));

            // When: 성적 요약을 업데이트하면
            assertThatCode(() -> scoreSummaryService.updateSummaryForClass(2025, 2, 2, 1))
                    .doesNotThrowAnyException();

            // Then: 첫 번째 과목은 정상 처리되어야 한다
            then(scoreSummaryRepository).should().deleteBySubjectAndClassroomStudentIn(
                    subject, List.of(classroomStudent));
            then(scoreSummaryRepository).should().saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("성적 피드백 테스트")
    class ScoreFeedbackTest {

        @Test
        @DisplayName("교사가 성적 피드백을 정상적으로 저장한다")
        void should_SaveFeedback_When_TeacherSubmits() {
            // Given: 교사가 성적 피드백을 저장할 때
            ScoreFeedbackRequestDto requestDto = ScoreFeedbackRequestDto.builder()
                    .scoreSummaryId(1L)
                    .feedback("수학 성적이 향상되었습니다. 지속적인 노력이 돋보입니다.")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(scoreSummaryRepository.findById(1L)).willReturn(Optional.of(scoreSummary));

            // When: 피드백을 저장하면
            assertThatCode(() -> scoreSummaryService.saveFeedback("teacher1", requestDto))
                    .doesNotThrowAnyException();

            // Then: 피드백이 저장되고 이벤트와 알림이 발송된다
            assertThat(scoreSummary.getFeedback()).isEqualTo("수학 성적이 향상되었습니다. 지속적인 노력이 돋보입니다.");
            then(eventPublisher).should().publishEvent(any(SendFeedbackFcmEvent.class));
            then(notificationService).should().sendNotification(eq(studentUser), anyString());
        }

        @Test
        @DisplayName("교사가 성적 피드백을 정상적으로 수정한다")
        void should_UpdateFeedback_When_TeacherModifies() {
            // Given: 교사가 기존 피드백을 수정할 때
            ScoreFeedbackUpdateDto requestDto = ScoreFeedbackUpdateDto.builder()
                    .feedback("수정된 피드백: 더욱 발전된 모습을 보여주시기 바랍니다.")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(scoreSummaryRepository.findById(1L)).willReturn(Optional.of(scoreSummary));

            // When: 피드백을 수정하면
            assertThatCode(() -> scoreSummaryService.updateFeedback("teacher1", 1L, requestDto))
                    .doesNotThrowAnyException();

            // Then: 피드백이 수정되고 이벤트와 알림이 발송된다
            assertThat(scoreSummary.getFeedback()).isEqualTo("수정된 피드백: 더욱 발전된 모습을 보여주시기 바랍니다.");
            then(eventPublisher).should().publishEvent(any(SendFeedbackFcmEvent.class));
            then(notificationService).should().sendNotification(eq(studentUser), anyString());
        }

        @Test
        @DisplayName("교사가 성적 피드백을 조회할 수 있다")
        void should_ReturnFeedback_When_TeacherRequests() {
            // Given: 교사가 성적 피드백을 조회할 때
            scoreSummary.update("훌륭한 성과입니다!");

            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(scoreSummaryRepository.findById(1L)).willReturn(Optional.of(scoreSummary));
            given(teacherService.authenticate("teacher1")).willReturn(teacher);

            // When: 피드백을 조회하면
            ScoreFeedbackDto result = scoreSummaryService.getFeedback("teacher1", 1L);

            // Then: 피드백이 반환된다
            assertThat(result.getFeedback()).isEqualTo("훌륭한 성과입니다!");
        }

        @Test
        @DisplayName("학생이 자신의 성적 피드백을 조회할 수 있다")
        void should_ReturnFeedback_When_StudentRequestsOwnFeedback() {
            // Given: 학생이 자신의 성적 피드백을 조회할 때
            scoreSummary.update("지속적인 노력이 필요합니다.");

            given(userService.getUser("student1")).willReturn(studentUser);
            given(scoreSummaryRepository.findById(1L)).willReturn(Optional.of(scoreSummary));

            // When: 본인 피드백을 조회하면
            ScoreFeedbackDto result = scoreSummaryService.getFeedback("student1", 1L);

            // Then: 피드백이 반환된다
            assertThat(result.getFeedback()).isEqualTo("지속적인 노력이 필요합니다.");
        }

        @Test
        @DisplayName("학생이 다른 학생의 성적 피드백을 조회하면 예외가 발생한다")
        void should_ThrowException_When_StudentRequestsOthersFeedback() {
            // Given: 학생이 다른 학생의 성적 피드백을 조회할 때
            User anotherStudentUser = createUser("student2", Role.STUDENT);
            ReflectionTestUtils.setField(anotherStudentUser, "id", 2L);

            Student anotherStudent = createStudent(anotherStudentUser, "김철수");
            ReflectionTestUtils.setField(anotherStudent, "id", 2L);

            ClassroomStudent anotherClassroomStudent = createClassroomStudent(2, anotherStudent, classroom);
            ScoreSummary anotherScoreSummary = createScoreSummary(anotherClassroomStudent, subject, 160.0, 80.0, 10.0, 2, 2, "B");
            ReflectionTestUtils.setField(anotherScoreSummary, "id", 2L);

            given(userService.getUser("student1")).willReturn(studentUser);
            given(scoreSummaryRepository.findById(2L)).willReturn(Optional.of(anotherScoreSummary));

            // When & Then: 다른 학생의 피드백을 조회하면 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.getFeedback("student1", 2L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 성적 요약에 대한 피드백 저장 시 예외가 발생한다")
        void should_ThrowException_When_ScoreSummaryNotFound() {
            // Given: 존재하지 않는 성적 요약에 피드백을 저장할 때
            ScoreFeedbackRequestDto requestDto = ScoreFeedbackRequestDto.builder()
                    .scoreSummaryId(999L)
                    .feedback("test feedback")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(scoreSummaryRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then: 성적 요약을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.saveFeedback("teacher1", requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.SCORE_SUMMARY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("과목별 피드백 조회 테스트")
    class GetSubjectFeedbacksTest {

        @Test
        @DisplayName("학생의 과목별 피드백 목록을 정상적으로 조회한다")
        void should_ReturnSubjectFeedbacks_When_ValidRequest() {
            // Given: 학생의 과목별 피드백을 조회할 때
            scoreSummary.update("수학 피드백입니다.");

            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1))
                    .willReturn(Optional.of(classroomStudent));
            given(evaluationMethodService.findSubject(2025, 2, 2)).willReturn(List.of(subject));
            given(scoreSummaryRepository.findAllByClassroomStudent(classroomStudent))
                    .willReturn(List.of(scoreSummary));

            // When: 과목별 피드백을 조회하면
            List<SubjectFeedbackDto> result = scoreSummaryService.getSubjectFeedbacks(
                    "teacher1", 2025, 2, 2, 1, 1);

            // Then: 과목별 피드백 목록이 반환된다
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSubjectName()).isEqualTo("수학");
            assertThat(result.get(0).getFeedback()).isEqualTo("수학 피드백입니다.");
        }

        @Test
        @DisplayName("해당 학기에 속하지 않은 과목은 필터링된다")
        void should_FilterOutSubjectsNotInThisTerm() {
            // Given: 해당 학기에 속하지 않은 과목이 있을 때
            Subject anotherSubject = createSubject("영어");
            ReflectionTestUtils.setField(anotherSubject, "id", 2L);

            ScoreSummary anotherSummary = createScoreSummary(classroomStudent, anotherSubject, 150.0, 75.0, 8.0, 3, 3, "C");
            anotherSummary.update("영어 피드백입니다.");

            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1))
                    .willReturn(Optional.of(classroomStudent));
            given(evaluationMethodService.findSubject(2025, 2, 2)).willReturn(List.of(subject)); // 수학만 이 학기 과목
            given(scoreSummaryRepository.findAllByClassroomStudent(classroomStudent))
                    .willReturn(List.of(scoreSummary, anotherSummary)); // 수학, 영어 모두 존재

            // When: 과목별 피드백을 조회하면
            List<SubjectFeedbackDto> result = scoreSummaryService.getSubjectFeedbacks(
                    "teacher1", 2025, 2, 2, 1, 1);

            // Then: 해당 학기 과목(수학)만 반환된다
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSubjectName()).isEqualTo("수학");
        }
    }

    @Nested
    @DisplayName("권한 검사 테스트")
    class AccessValidationTest {

        @Test
        @DisplayName("역할이 null인 사용자가 접근하면 NullPointerException이 발생한다")
        void should_ThrowNPE_When_UserRoleIsNull() {
            // Given: 역할이 null인 사용자가 접근할 때
            User nullRoleUser = User.builder()
                    .school("테스트중학교")
                    .username("nullrole1")
                    .password("password")
                    .role(null)
                    .build();

            given(userService.getUser("nullrole1")).willReturn(nullRoleUser);

            // When & Then: NullPointerException이 발생한다
            assertThatThrownBy(() -> scoreSummaryService.getStudentSummary(
                    "nullrole1", 2025, 2, 2, 1, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("학생이 학급에 속하지 않은 경우 예외가 발생한다")
        void should_ThrowException_When_StudentNotInAnyClassroom() {
            // Given: 학생이 어떤 학급에도 속하지 않은 경우
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser))
                    .willReturn(Optional.empty());

            // When & Then: 학급을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.getStudentSummary(
                    "student1", 2025, 2, 2, 1, 1))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.CLASSROOM_NOT_FOUND);
        }

        @Test
        @DisplayName("교사가 존재하지 않는 학생 번호로 접근하면 예외가 발생한다")
        void should_ThrowException_When_TeacherAccessesNonExistentStudent() {
            // Given: 교사가 존재하지 않는 학생 번호로 접근할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 99))
                    .willReturn(Optional.empty());

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.getStudentSummary(
                    "teacher1", 2025, 2, 2, 1, 99))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("지원하지 않는 역할의 사용자가 접근하면 예외가 발생한다")
        void should_ThrowException_When_UnsupportedRoleAccesses() {
            // Given: 지원하지 않는 역할(PARENT 등)의 사용자가 접근할 때
            User parentUser = createUser("parent1", Role.PARENT);
            given(userService.getUser("parent1")).willReturn(parentUser);

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.getStudentSummary(
                    "parent1", 2025, 2, 2, 1, 1))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("기타 메서드 테스트")
    class OtherMethodsTest {

        @Test
        @DisplayName("학생과 과목으로 성적 요약을 정상적으로 조회한다")
        void should_ReturnScoreSummary_When_StudentAndSubjectFound() {
            // Given: 학생과 과목으로 성적 요약을 조회할 때
            given(scoreSummaryRepository.findByStudentAndSubject(1L, 1L))
                    .willReturn(Optional.of(scoreSummary));

            // When: 성적 요약을 조회하면
            ScoreSummary result = scoreSummaryService.findByStudentAndSubject(1L, 1L);

            // Then: 성적 요약이 반환된다
            assertThat(result).isEqualTo(scoreSummary);
        }

        @Test
        @DisplayName("존재하지 않는 학생과 과목으로 조회하면 예외가 발생한다")
        void should_ThrowException_When_StudentAndSubjectNotFound() {
            // Given: 존재하지 않는 학생과 과목으로 조회할 때
            given(scoreSummaryRepository.findByStudentAndSubject(999L, 999L))
                    .willReturn(Optional.empty());

            // When & Then: 성적 요약을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> scoreSummaryService.findByStudentAndSubject(999L, 999L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.SCORE_SUMMARY_NOT_FOUND);
        }

        @Test
        @DisplayName("Optional을 반환하는 안전한 조회 메서드가 정상 작동한다")
        void should_ReturnOptional_When_UsingSafeMethod() {
            // Given: 안전한 조회 메서드를 사용할 때
            given(scoreSummaryRepository.findByStudentAndSubject(1L, 1L))
                    .willReturn(Optional.of(scoreSummary));
            given(scoreSummaryRepository.findByStudentAndSubject(999L, 999L))
                    .willReturn(Optional.empty());

            // When: 존재하는 데이터를 조회하면
            Optional<ScoreSummary> existingResult = scoreSummaryService.findByStudentAndSubjectOptional(1L, 1L);
            // When: 존재하지 않는 데이터를 조회하면
            Optional<ScoreSummary> nonExistingResult = scoreSummaryService.findByStudentAndSubjectOptional(999L, 999L);

            // Then: 적절한 Optional이 반환된다
            assertThat(existingResult).isPresent();
            assertThat(existingResult.get()).isEqualTo(scoreSummary);
            assertThat(nonExistingResult).isEmpty();
        }
    }

    // ===== 테스트 헬퍼 메서드들 =====

    private User createUser(String username, Role role) {
        User user = User.builder()
                .school("테스트중학교")
                .username(username)
                .password("password")
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", username.hashCode() % 1000L + 1L);
        return user;
    }

    private Teacher createTeacher(User user, String name) {
        return Teacher.builder()
                .name(name)
                .phone("010-1234-5678")
                .email(user.getUsername() + "@test.com")
                .user(user)
                .build();
    }

    private Student createStudent(User user, String name) {
        return Student.builder()
                .name(name)
                .phone("010-9999-9999")
                .ssn("030101-1234567")
                .gender("M")
                .address("서울특별시 강남구")
                .user(user)
                .build();
    }

    private Classroom createClassroom(int year, int grade, int classNum, Teacher teacher) {
        return Classroom.builder()
                .year(year)
                .grade(grade)
                .classNum(classNum)
                .teacher(teacher)
                .build();
    }

    private ClassroomStudent createClassroomStudent(int number, Student student, Classroom classroom) {
        return ClassroomStudent.builder()
                .number(number)
                .student(student)
                .classroom(classroom)
                .build();
    }

    private Subject createSubject(String name) {
        return Subject.builder()
                .name(name)
                .build();
    }

    private EvaluationMethod createEvaluationMethod(Subject subject, int year, int semester,
                                                    ExamType examType, String title, int fullScore, double weight) {
        return EvaluationMethod.builder()
                .subject(subject)
                .year(year)
                .semester(semester)
                .examType(examType)
                .title(title)
                .fullScore(fullScore)
                .weight(weight)
                .build();
    }

    private Score createScore(ClassroomStudent student, EvaluationMethod evaluationMethod,
                              double rawScore, double weightedScore) {
        return Score.builder()
                .student(student)
                .evaluationMethod(evaluationMethod)
                .rawScore(rawScore)
                .weightedScore(weightedScore)
                .build();
    }

    private ScoreSummary createScoreSummary(ClassroomStudent classroomStudent, Subject subject,
                                            double sumScore, double average, double stdDeviation,
                                            int rank, int grade, String achievementLevel) {
        return ScoreSummary.builder()
                .classroomStudent(classroomStudent)
                .subject(subject)
                .sumScore(sumScore)
                .originalScore((int) Math.round(sumScore))
                .average(average)
                .stdDeviation(stdDeviation)
                .rank(rank)
                .grade(grade)
                .achievementLevel(achievementLevel)
                .totalStudentCount(20)
                .build();
    }
}