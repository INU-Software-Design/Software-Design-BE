package com.neeis.neeis.domain.score.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendScoreFcmEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ScoreNotificationService 테스트 클래스
 * 테스트 대상:
 * - sendNotificationsForAffectedSubjects() - 영향받은 과목들에 대한 알림 발송
 * 주요 테스트 시나리오:
 * 1. 정상적인 알림 발송
 * 2. 예외 상황 처리 (교실 없음, 학생 없음, 과목 없음 등)
 * 3. 부분 실패 시나리오 (일부 알림 실패해도 계속 진행)
 * 4. 빈 데이터 처리
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreNotificationService 테스트")
class ScoreNotificationServiceTest {

    @InjectMocks
    private ScoreNotificationService scoreNotificationService;

    @Mock private ClassroomService classroomService;
    @Mock private ClassroomStudentService classroomStudentService;
    @Mock private SubjectService subjectService;
    @Mock private ScoreSummaryService scoreSummaryService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;

    // 테스트 픽스처
    private Classroom testClassroom;
    private ClassroomStudent testClassroomStudent1, testClassroomStudent2;
    private Subject testSubject1, testSubject2;
    private ScoreSummary testScoreSummary1, testScoreSummary2;
    private User testUser1, testUser2;
    private Student testStudent1, testStudent2;

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체들 생성
        testUser1 = User.builder()
                .username("student1")
                .build();
        ReflectionTestUtils.setField(testUser1, "id", 1L);

        testUser2 = User.builder()
                .username("student2")
                .build();
        ReflectionTestUtils.setField(testUser2, "id", 2L);

        // 테스트용 Student 객체들 생성
        testStudent1 = Student.builder()
                .name("김학생")
                .user(testUser1)
                .ssn("050101-3123456")
                .gender("M")
                .address("서울시 강남구")
                .phone("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(testStudent1, "id", 1L);

        testStudent2 = Student.builder()
                .name("이학생")
                .user(testUser2)
                .ssn("050202-4234567")
                .gender("F")
                .address("서울시 서초구")
                .phone("010-2345-6789")
                .build();
        ReflectionTestUtils.setField(testStudent2, "id", 2L);

        // 테스트용 Classroom 객체 생성
        testClassroom = Classroom.builder()
                .grade(2)
                .classNum(3)
                .year(2024)
                .build();
        ReflectionTestUtils.setField(testClassroom, "id", 1L);

        // 테스트용 ClassroomStudent 객체들 생성
        testClassroomStudent1 = ClassroomStudent.builder()
                .student(testStudent1)
                .classroom(testClassroom)
                .number(15)
                .build();
        ReflectionTestUtils.setField(testClassroomStudent1, "id", 1L);

        testClassroomStudent2 = ClassroomStudent.builder()
                .student(testStudent2)
                .classroom(testClassroom)
                .number(16)
                .build();
        ReflectionTestUtils.setField(testClassroomStudent2, "id", 2L);

        // 테스트용 Subject 객체들 생성
        testSubject1 = Subject.builder().name("수학").build();
        ReflectionTestUtils.setField(testSubject1, "id", 1L);

        testSubject2 = Subject.builder().name("영어").build();
        ReflectionTestUtils.setField(testSubject2, "id", 2L);

        // 테스트용 ScoreSummary 객체들 생성
        testScoreSummary1 = ScoreSummary.builder()
                .classroomStudent(testClassroomStudent1)
                .subject(testSubject1)
                .originalScore(85)
                .sumScore(85.0)
                .average(80.0)
                .stdDeviation(10.0)
                .rank(1)
                .grade(1)
                .achievementLevel("A")
                .totalStudentCount(30)
                .build();
        ReflectionTestUtils.setField(testScoreSummary1, "id", 1L);

        testScoreSummary2 = ScoreSummary.builder()
                .classroomStudent(testClassroomStudent2)
                .subject(testSubject2)
                .originalScore(78)
                .sumScore(78.0)
                .average(75.0)
                .stdDeviation(12.0)
                .rank(5)
                .grade(2)
                .achievementLevel("B")
                .totalStudentCount(30)
                .build();
        ReflectionTestUtils.setField(testScoreSummary2, "id", 2L);
    }

    @Nested
    @DisplayName("알림 발송 성공 테스트")
    class SuccessfulNotificationTest {

        @Test
        @DisplayName("단일 과목, 단일 학생 알림 발송 성공")
        void sendNotifications_singleSubject_singleStudent_success() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L);

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent1));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(scoreSummaryService.findByStudentAndSubjectOptional(1L, 1L))
                    .willReturn(Optional.of(testScoreSummary1));

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            // then
            verify(classroomService).findClassroom(year, grade, classNum);
            verify(classroomStudentService).findByClassroom(testClassroom);
            verify(subjectService).findById(1L);
            verify(scoreSummaryService).findByStudentAndSubjectOptional(1L, 1L);

            // FCM 이벤트 발행 검증
            ArgumentCaptor<SendScoreFcmEvent> eventCaptor = ArgumentCaptor.forClass(SendScoreFcmEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getScoreSummary()).isEqualTo(testScoreSummary1);

            // 알림 발송 검증
            verify(notificationService).sendNotification(eq(testUser1), eq("수학 과목의 성적이 입력되었습니다."));
        }

        @Test
        @DisplayName("복수 과목, 복수 학생 알림 발송 성공")
        void sendNotifications_multipleSubjects_multipleStudents_success() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L, 2L);

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom))
                    .willReturn(List.of(testClassroomStudent1, testClassroomStudent2));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(subjectService.findById(2L)).willReturn(testSubject2);

            // 각 학생-과목 조합에 대한 ScoreSummary 설정
            given(scoreSummaryService.findByStudentAndSubjectOptional(1L, 1L))
                    .willReturn(Optional.of(testScoreSummary1));
            given(scoreSummaryService.findByStudentAndSubjectOptional(1L, 2L))
                    .willReturn(Optional.empty()); // 첫 번째 학생은 영어 성적 없음
            given(scoreSummaryService.findByStudentAndSubjectOptional(2L, 1L))
                    .willReturn(Optional.empty()); // 두 번째 학생은 수학 성적 없음
            given(scoreSummaryService.findByStudentAndSubjectOptional(2L, 2L))
                    .willReturn(Optional.of(testScoreSummary2));

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            // then
            verify(subjectService).findById(1L);
            verify(subjectService).findById(2L);

            // FCM 이벤트가 2번 발행되어야 함 (실제 ScoreSummary가 있는 경우만)
            verify(eventPublisher, times(2)).publishEvent(any(SendScoreFcmEvent.class));

            // 알림이 2번 발송되어야 함
            verify(notificationService).sendNotification(eq(testUser1), eq("수학 과목의 성적이 입력되었습니다."));
            verify(notificationService).sendNotification(eq(testUser2), eq("영어 과목의 성적이 입력되었습니다."));
        }

        @Test
        @DisplayName("빈 과목 ID 셋으로 호출 시 아무것도 처리하지 않음")
        void sendNotifications_emptySubjectIds() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> emptySubjectIds = Set.of();

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent1));

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, emptySubjectIds))
                    .doesNotThrowAnyException();

            // then
            verify(subjectService, never()).findById(anyLong());
            verify(eventPublisher, never()).publishEvent(any());
            verify(notificationService, never()).sendNotification(any(), any());
        }
    }

    @Nested
    @DisplayName("예외 상황 처리 테스트")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("교실 조회 실패 시 예외 로깅 후 정상 종료")
        void sendNotifications_classroomNotFound() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 99;
            Set<Long> affectedSubjectIds = Set.of(1L);

            given(classroomService.findClassroom(year, grade, classNum))
                    .willThrow(new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));

            // when & then - 예외가 로깅되고 정상 종료되어야 함
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            verify(classroomService).findClassroom(year, grade, classNum);
            verify(subjectService, never()).findById(anyLong());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("과목 조회 실패 시 해당 과목만 건너뛰고 다른 과목은 처리")
        void sendNotifications_subjectNotFound_continueWithOthers() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L, 999L); // 999L은 존재하지 않는 과목

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent1));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(subjectService.findById(999L)).willThrow(new CustomException(ErrorCode.SUBJECT_NOT_FOUND));
            given(scoreSummaryService.findByStudentAndSubjectOptional(1L, 1L))
                    .willReturn(Optional.of(testScoreSummary1));

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            // then - 존재하는 과목(1L)에 대해서는 정상 처리되어야 함
            verify(subjectService).findById(1L);
            verify(subjectService).findById(999L);
            verify(eventPublisher, times(1)).publishEvent(any(SendScoreFcmEvent.class));
            verify(notificationService).sendNotification(eq(testUser1), eq("수학 과목의 성적이 입력되었습니다."));
        }

        @Test
        @DisplayName("알림 발송 실패 시 로깅하고 다른 알림은 계속 처리")
        void sendNotifications_notificationFailure_continueWithOthers() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L);

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom))
                    .willReturn(List.of(testClassroomStudent1, testClassroomStudent2));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(scoreSummaryService.findByStudentAndSubjectOptional(1L, 1L))
                    .willReturn(Optional.of(testScoreSummary1));
            given(scoreSummaryService.findByStudentAndSubjectOptional(2L, 1L))
                    .willReturn(Optional.of(testScoreSummary2));

            // 첫 번째 학생 알림 발송 실패 설정
            doThrow(new RuntimeException("FCM 발송 실패"))
                    .doNothing() // 두 번째 호출부터는 정상 처리
                    .when(eventPublisher).publishEvent(any(SendScoreFcmEvent.class));

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            // then - 두 번째 학생의 알림은 정상 처리되어야 함
            verify(eventPublisher, times(2)).publishEvent(any(SendScoreFcmEvent.class));
            verify(notificationService).sendNotification(eq(testUser2), eq("수학 과목의 성적이 입력되었습니다."));
        }

        @Test
        @DisplayName("ScoreSummary 조회 실패 시 해당 학생만 건너뛰고 계속 처리")
        void sendNotifications_scoreSummaryError_continueWithOthers() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L);

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom))
                    .willReturn(List.of(testClassroomStudent1, testClassroomStudent2));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(scoreSummaryService.findByStudentAndSubjectOptional(1L, 1L))
                    .willThrow(new RuntimeException("DB 조회 실패"));
            given(scoreSummaryService.findByStudentAndSubjectOptional(2L, 1L))
                    .willReturn(Optional.of(testScoreSummary2));

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            // then - 두 번째 학생은 정상 처리되어야 함
            verify(eventPublisher, times(1)).publishEvent(any(SendScoreFcmEvent.class));
            verify(notificationService).sendNotification(eq(testUser2), eq("수학 과목의 성적이 입력되었습니다."));
        }
    }

    @Nested
    @DisplayName("경계값 및 특수 케이스 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("학생이 없는 교실의 경우 알림 발송하지 않음")
        void sendNotifications_emptyClassroom() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L);

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of()); // 빈 학생 리스트
            given(subjectService.findById(1L)).willReturn(testSubject1);

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            // then
            verify(scoreSummaryService, never()).findByStudentAndSubjectOptional(anyLong(), anyLong());
            verify(eventPublisher, never()).publishEvent(any());
            verify(notificationService, never()).sendNotification(any(), any());
        }

        @Test
        @DisplayName("모든 학생이 해당 과목 성적이 없는 경우")
        void sendNotifications_noScoreSummariesFound() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L);

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom))
                    .willReturn(List.of(testClassroomStudent1, testClassroomStudent2));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(scoreSummaryService.findByStudentAndSubjectOptional(anyLong(), eq(1L)))
                    .willReturn(Optional.empty()); // 모든 학생이 해당 과목 성적 없음

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds))
                    .doesNotThrowAnyException();

            // then
            verify(scoreSummaryService, times(2)).findByStudentAndSubjectOptional(anyLong(), eq(1L));
            verify(eventPublisher, never()).publishEvent(any());
            verify(notificationService, never()).sendNotification(any(), any());
        }

        @Test
        @DisplayName("매우 많은 과목 ID가 포함된 경우 모두 처리")
        void sendNotifications_manySubjects() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> manySubjectIds = Set.of(1L, 2L, 3L, 4L, 5L); // 5개 과목

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent1));

            // 모든 과목에 대해 Mock 설정
            for (Long subjectId : manySubjectIds) {
                Subject subject = Subject.builder().name("과목" + subjectId).build();
                ReflectionTestUtils.setField(subject, "id", subjectId);
                given(subjectService.findById(subjectId)).willReturn(subject);
                given(scoreSummaryService.findByStudentAndSubjectOptional(1L, subjectId))
                        .willReturn(Optional.of(testScoreSummary1));
            }

            // when
            assertThatCode(() -> scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, manySubjectIds))
                    .doesNotThrowAnyException();

            // then
            verify(subjectService, times(5)).findById(anyLong());
            verify(eventPublisher, times(5)).publishEvent(any(SendScoreFcmEvent.class));
            verify(notificationService, times(5)).sendNotification(any(), anyString());
        }
    }

    @Nested
    @DisplayName("로깅 검증 테스트")
    class LoggingVerificationTest {

        @Test
        @DisplayName("정상 처리 시 시작 및 완료 로그 검증")
        void sendNotifications_loggingVerification() {
            // given
            int year = 2024, semester = 1, grade = 2, classNum = 3;
            Set<Long> affectedSubjectIds = Set.of(1L);

            given(classroomService.findClassroom(year, grade, classNum)).willReturn(testClassroom);
            given(classroomStudentService.findByClassroom(testClassroom)).willReturn(List.of(testClassroomStudent1));
            given(subjectService.findById(1L)).willReturn(testSubject1);
            given(scoreSummaryService.findByStudentAndSubjectOptional(1L, 1L))
                    .willReturn(Optional.of(testScoreSummary1));

            // when
            scoreNotificationService.sendNotificationsForAffectedSubjects(
                    year, semester, grade, classNum, affectedSubjectIds);

            // then - 실제 로깅 검증은 로그 테스트 프레임워크가 필요하지만,
            // 여기서는 메서드가 정상 완료되었는지 확인
            verify(classroomService).findClassroom(year, grade, classNum);
            verify(eventPublisher).publishEvent(any(SendScoreFcmEvent.class));
            verify(notificationService).sendNotification(any(), any());
        }
    }
}