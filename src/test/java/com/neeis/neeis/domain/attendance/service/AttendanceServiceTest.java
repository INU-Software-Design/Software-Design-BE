package com.neeis.neeis.domain.attendance.service;

import com.neeis.neeis.domain.attendance.*;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceBulkRequestDto;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceFeedbackReqDto;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.req.StudentAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.semester.Semester;
import com.neeis.neeis.domain.semester.SemesterRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * AttendanceService TDD 테스트
 *
 * TDD 원칙:
 * 1. Red: 실패하는 테스트를 먼저 작성
 * 2. Green: 테스트를 통과하는 최소한의 코드 작성
 * 3. Refactor: 코드 개선 및 리팩토링
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTDDTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private UserService userService;
    @Mock private TeacherService teacherService;
    @Mock private ClassroomService classroomService;
    @Mock private ClassroomStudentRepository classroomStudentRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private AttendanceFeedbackRepository feedbackRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;
    @Mock private ParentService parentService;

    @InjectMocks
    private AttendanceService attendanceService;

    // 테스트 픽스처 (Test Fixture) - 테스트에 필요한 기본 데이터
    private User teacherUser, studentUser, parentUser;
    private Teacher teacher;
    private Student student;
    private Parent parent;
    private Classroom classroom;
    private ClassroomStudent classroomStudent;

    @BeforeEach
    void setUp() {
        // Given: 테스트에 필요한 기본 데이터 준비
        setupTestFixtures();
    }

    private void setupTestFixtures() {
        // 교사 관련 데이터
        teacherUser = createUser("teacher1", Role.TEACHER);
        teacher = createTeacher(teacherUser, "김교사");
        ReflectionTestUtils.setField(teacher, "id", 1L);

        // 학생 관련 데이터
        studentUser = createUser("student1", Role.STUDENT);
        student = createStudent(studentUser, "홍길동");
        ReflectionTestUtils.setField(student, "id", 1L);

        // 학부모 관련 데이터
        parentUser = createUser("parent1", Role.PARENT);
        parent = createParent(parentUser, student);

        // 교실 및 학급 학생 데이터
        classroom = createClassroom(2025, 2, 1, teacher);
        ReflectionTestUtils.setField(classroom, "id", 1L);

        classroomStudent = createClassroomStudent(1, student, classroom);
        ReflectionTestUtils.setField(classroomStudent, "id", 1L);
    }

    @Nested
    @DisplayName("출결 저장 및 업데이트 테스트")
    class SaveOrUpdateAttendanceTest {

        @Test
        @DisplayName("교사가 학급 출결을 정상적으로 저장한다")
        void should_SaveAttendance_When_TeacherSubmitsValidData() {
            // Given: 교사가 출결 데이터를 제출할 때
            String username = "teacher1";
            AttendanceBulkRequestDto requestDto = createAttendanceBulkRequest();

            given(teacherService.authenticate(username)).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1, teacher.getId())).willReturn(classroom);
            given(classroomStudentRepository.findByClassroom(classroom)).willReturn(List.of(classroomStudent));
            given(attendanceRepository.findByStudentAndDate(any(Student.class), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            // When: 출결을 저장하면
            assertThatCode(() -> attendanceService.saveOrUpdateAttendance(username, requestDto))
                    .doesNotThrowAnyException();

            // Then: 출결 데이터가 저장된다
            then(attendanceRepository).should(atLeastOnce()).save(any(Attendance.class));
        }

        @Test
        @DisplayName("교사가 담당하지 않는 반에 접근하면 예외가 발생한다")
        void should_ThrowException_When_TeacherAccessesUnauthorizedClass() {
            // Given: 다른 교사의 반에 접근할 때
            Teacher anotherTeacher = createTeacher(createUser("teacher2", Role.TEACHER), "박교사");
            ReflectionTestUtils.setField(anotherTeacher, "id", 2L);

            Classroom anotherClassroom = createClassroom(2025, 2, 1, anotherTeacher);

            AttendanceBulkRequestDto requestDto = createAttendanceBulkRequest();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1, teacher.getId())).willReturn(anotherClassroom);

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> attendanceService.saveOrUpdateAttendance("teacher1", requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("담당 학생이 아닌 학생에 대한 출결 저장 시 예외가 발생한다")
        void should_ThrowException_When_SubmittingAttendanceForNonClassStudent() {
            // Given: 담당하지 않는 학생의 출결을 제출할 때
            Student anotherStudent = createStudent(createUser("student2", Role.STUDENT), "김철수");
            ReflectionTestUtils.setField(anotherStudent, "id", 2L);

            AttendanceBulkRequestDto requestDto = AttendanceBulkRequestDto.builder()
                    .year(2025).month(4).grade(2).classNumber(1)
                    .students(List.of(
                            StudentAttendanceDto.builder()
                                    .studentId(2L) // 담당하지 않는 학생
                                    .attendances(List.of(
                                            DailyAttendanceDto.builder()
                                                    .date(LocalDate.of(2025, 4, 1))
                                                    .status(AttendanceStatus.ABSENT)
                                                    .build()))
                                    .build()))
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1, teacher.getId())).willReturn(classroom);
            given(classroomStudentRepository.findByClassroom(classroom)).willReturn(List.of(classroomStudent));

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> attendanceService.saveOrUpdateAttendance("teacher1", requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("기존 출결 데이터가 있으면 업데이트한다")
        void should_UpdateAttendance_When_ExistingAttendanceExists() {
            // Given: 기존 출결 데이터가 있을 때
            Attendance existingAttendance = Attendance.builder()
                    .student(student)
                    .date(LocalDate.of(2025, 4, 1))
                    .status(AttendanceStatus.LATE)
                    .build();

            AttendanceBulkRequestDto requestDto = createAttendanceBulkRequest();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1, teacher.getId())).willReturn(classroom);
            given(classroomStudentRepository.findByClassroom(classroom)).willReturn(List.of(classroomStudent));
            given(attendanceRepository.findByStudentAndDate(student, LocalDate.of(2025, 4, 1)))
                    .willReturn(Optional.of(existingAttendance));

            // When: 새로운 출결 상태로 업데이트하면
            attendanceService.saveOrUpdateAttendance("teacher1", requestDto);

            // Then: 기존 데이터가 업데이트된다
            then(attendanceRepository).should().save(existingAttendance);
            assertThat(existingAttendance.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
        }
    }

    @Nested
    @DisplayName("출결 조회 테스트")
    class GetAttendanceTest {

        @Test
        @DisplayName("교사가 학급 전체 학생의 월별 출결을 조회할 수 있다")
        void should_ReturnClassAttendances_When_TeacherRequestsValidClass() {
            // Given: 교사가 담당 학급의 출결을 조회할 때
            List<Attendance> attendances = List.of(
                    createAttendance(student, LocalDate.of(2025, 4, 1), AttendanceStatus.ABSENT),
                    createAttendance(student, LocalDate.of(2025, 4, 2), AttendanceStatus.LATE)
            );

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroom(classroom)).willReturn(List.of(classroomStudent));
            given(attendanceRepository.findByStudentAndDateBetween(eq(student), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(attendances);

            // When: 학급 출결을 조회하면
            List<StudentAttendanceResDto> result = attendanceService.getAttendances("teacher1", 2025, 2, 1, 4);

            // Then: 학급 전체 학생의 출결 정보가 반환된다
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(student.getId());
            assertThat(result.get(0).getAttendances()).hasSize(2);
        }

        @Test
        @DisplayName("학생이 자신의 월별 출결을 조회할 수 있다")
        void should_ReturnOwnAttendance_When_StudentRequestsOwnData() {
            // Given: 학생이 자신의 출결을 조회할 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser)).willReturn(Optional.of(classroomStudent));
            given(attendanceRepository.findByStudentAndDateBetween(eq(student), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(List.of());

            // When: 본인 출결을 조회하면
            StudentAttendanceResDto result = attendanceService.getStudentMonthlyAttendance("student1", 2025, 2, 1, 1, 4);

            // Then: 본인의 출결 정보가 반환된다
            assertThat(result.getStudentId()).isEqualTo(student.getId());
            assertThat(result.getStudentName()).isEqualTo(student.getName());
        }

        @Test
        @DisplayName("학생이 다른 학생의 출결을 조회하면 예외가 발생한다")
        void should_ThrowException_When_StudentAccessesOthersData() {
            // Given: 학생이 다른 학생의 출결을 조회할 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser)).willReturn(Optional.of(classroomStudent));

            // When & Then: 다른 학생 번호로 조회하면 예외가 발생한다
            assertThatThrownBy(() -> attendanceService.getStudentMonthlyAttendance("student1", 2025, 2, 1, 2, 4))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("학부모가 자녀의 출결을 조회할 수 있다")
        void should_ReturnChildAttendance_When_ParentRequestsChildData() {
            // Given: 학부모가 자녀의 출결을 조회할 때
            given(userService.getUser("parent1")).willReturn(parentUser);
            given(parentService.getParentByUser(parentUser)).willReturn(parent);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1)).willReturn(Optional.of(classroomStudent));
            given(attendanceRepository.findByStudentAndDateBetween(eq(student), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(List.of());

            // When: 자녀 출결을 조회하면
            StudentAttendanceResDto result = attendanceService.getStudentMonthlyAttendance("parent1", 2025, 2, 1, 1, 4);

            // Then: 자녀의 출결 정보가 반환된다
            assertThat(result.getStudentId()).isEqualTo(student.getId());
        }
    }

    @Nested
    @DisplayName("출결 통계 조회 테스트")
    class GetAttendanceSummaryTest {

        @Test
        @DisplayName("학생의 학기별 출결 통계를 정확하게 계산한다")
        void should_CalculateCorrectSummary_When_RequestingAttendanceSummary() {
            // Given: 학기 정보와 출결 데이터가 주어질 때
            Semester semester = createSemester(2025, 1, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 7, 31));
            List<Attendance> attendances = List.of(
                    createAttendance(student, LocalDate.of(2025, 3, 3), AttendanceStatus.ABSENT),
                    createAttendance(student, LocalDate.of(2025, 3, 4), AttendanceStatus.LATE),
                    createAttendance(student, LocalDate.of(2025, 3, 5), AttendanceStatus.EARLY)
            );

            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1)).willReturn(Optional.of(classroomStudent));
            given(semesterRepository.findByYearAndSemester(2025, 1)).willReturn(Optional.of(semester));
            given(attendanceRepository.findByStudentAndDateBetween(eq(student), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(attendances);

            // When: 출결 통계를 조회하면
            StudentAttendanceSummaryDto summary = attendanceService.getStudentAttendanceSummary("teacher1", 2025, 1, 2, 1, 1);

            // Then: 정확한 통계가 계산된다
            assertThat(summary.getAbsentDays()).isEqualTo(1);
            assertThat(summary.getLateDays()).isEqualTo(1);
            assertThat(summary.getLeaveEarlyDays()).isEqualTo(1);
            assertThat(summary.getPresentDays()).isGreaterThan(0);
        }

        @Test
        @DisplayName("존재하지 않는 학기에 대한 조회 시 예외가 발생한다")
        void should_ThrowException_When_SemesterNotFound() {
            // Given: 존재하지 않는 학기를 조회할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1)).willReturn(Optional.of(classroomStudent));
            given(semesterRepository.findByYearAndSemester(2025, 3)).willReturn(Optional.empty());

            // When & Then: 데이터를 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> attendanceService.getStudentAttendanceSummary("teacher1", 2025, 3, 2, 1, 1))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.DATA_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("출결 피드백 테스트")
    class AttendanceFeedbackTest {

        @Test
        @DisplayName("교사가 학생에게 출결 피드백을 저장할 수 있다")
        void should_SaveFeedback_When_TeacherSubmitsFeedback() {
            // Given: 교사가 피드백을 작성할 때
            AttendanceFeedbackReqDto requestDto = AttendanceFeedbackReqDto.builder()
                    .feedback("출석 상태가 좋습니다. 계속 유지하세요.")
                    .build();

            AttendanceFeedback savedFeedback = AttendanceFeedback.builder()
                    .classroomStudent(classroomStudent)
                    .feedback(requestDto.getFeedback())
                    .build();
            ReflectionTestUtils.setField(savedFeedback, "id", 1L);

            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1)).willReturn(Optional.of(classroomStudent));
            given(feedbackRepository.save(any(AttendanceFeedback.class))).willReturn(savedFeedback);

            // When: 피드백을 저장하면
            AttendanceFeedbackResDto result = attendanceService.saveFeedback("teacher1", 2025, 2, 1, 1, requestDto);

            // Then: 피드백이 정상적으로 저장된다
            assertThat(result.getFeedbackId()).isEqualTo(1L);
            assertThat(result.getFeedback()).isEqualTo("출석 상태가 좋습니다. 계속 유지하세요.");
        //    then(eventPublisher).should().publishEvent(any());
            then(notificationService).should().sendNotification(any(User.class), anyString());
        }


        @Test
        @DisplayName("교사가 기존 피드백을 수정할 수 있다")
        void should_UpdateFeedback_When_TeacherModifiesFeedback() {
            // Given: 기존 피드백이 있고 교사가 수정할 때
            AttendanceFeedback existingFeedback = AttendanceFeedback.builder()
                    .classroomStudent(classroomStudent)
                    .feedback("기존 피드백")
                    .build();
            ReflectionTestUtils.setField(existingFeedback, "id", 1L);

            AttendanceFeedbackReqDto requestDto = AttendanceFeedbackReqDto.builder()
                    .feedback("수정된 피드백")
                    .build();

            // Mock 설정 시 순서 주의 - checkValidate 메서드에서 사용하는 순서대로
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1)).willReturn(Optional.of(classroomStudent));
            given(feedbackRepository.findByClassroomStudent(classroomStudent)).willReturn(Optional.of(existingFeedback));

            // When: 피드백을 수정하면
            AttendanceFeedbackResDto result = attendanceService.updateFeedback("teacher1", 2025, 2, 1, 1, requestDto);

            // Then: 피드백이 정상적으로 수정된다
            assertThat(result.getFeedback()).isEqualTo("수정된 피드백");

            // 이벤트 발행과 알림 전송이 한 번씩 호출되었는지 검증
          //  then(eventPublisher).should(times(1)).publishEvent(any());
            then(notificationService).should(times(1)).sendNotification(any(User.class), anyString());
        }


        @Test
        @DisplayName("다른 교사가 피드백을 수정하려 하면 예외가 발생한다")
        void should_ThrowException_When_UnauthorizedTeacherModifiesFeedback() {
            // Given: 다른 교사가 피드백을 수정하려 할 때
            Teacher anotherTeacher = createTeacher(createUser("teacher2", Role.TEACHER), "박교사");
            ReflectionTestUtils.setField(anotherTeacher, "id", 2L);

            AttendanceFeedback existingFeedback = AttendanceFeedback.builder()
                    .classroomStudent(classroomStudent)
                    .feedback("기존 피드백")
                    .build();

            given(teacherService.authenticate("teacher2")).willReturn(anotherTeacher);
            given(userService.getUser("teacher2")).willReturn(createUser("teacher2", Role.TEACHER));
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1)).willReturn(Optional.of(classroomStudent));
            given(feedbackRepository.findByClassroomStudent(classroomStudent)).willReturn(Optional.of(existingFeedback));

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> attendanceService.updateFeedback("teacher2", 2025, 2, 1, 1,
                    AttendanceFeedbackReqDto.builder().feedback("수정 시도").build()))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("학생이 자신의 피드백을 조회할 수 있다")
        void should_ReturnFeedback_When_StudentRequestsOwnFeedback() {
            // Given: 학생이 자신의 피드백을 조회할 때
            AttendanceFeedback feedback = AttendanceFeedback.builder()
                    .classroomStudent(classroomStudent)
                    .feedback("좋은 출석 태도입니다.")
                    .build();
            ReflectionTestUtils.setField(feedback, "id", 1L);

            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser)).willReturn(Optional.of(classroomStudent));
            given(feedbackRepository.findByClassroomStudent(classroomStudent)).willReturn(Optional.of(feedback));

            // When: 피드백을 조회하면
            AttendanceFeedbackResDto result = attendanceService.getFeedback("student1", 2025, 2, 1, 1);

            // Then: 자신의 피드백이 반환된다
            assertThat(result.getFeedbackId()).isEqualTo(1L);
            assertThat(result.getFeedback()).isEqualTo("좋은 출석 태도입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 피드백을 조회하면 예외가 발생한다")
        void should_ThrowException_When_FeedbackNotExists() {
            // Given: 피드백이 존재하지 않을 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser)).willReturn(Optional.of(classroomStudent));
            given(feedbackRepository.findByClassroomStudent(classroomStudent)).willReturn(Optional.empty());

            // When & Then: 데이터를 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> attendanceService.getFeedback("student1", 2025, 2, 1, 1))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.DATA_NOT_FOUND);
        }
    }

    // ===== 테스트 헬퍼 메서드들 =====

    private AttendanceBulkRequestDto createAttendanceBulkRequest() {
        return AttendanceBulkRequestDto.builder()
                .year(2025).month(4).grade(2).classNumber(1)
                .students(List.of(
                        StudentAttendanceDto.builder()
                                .studentId(1L)
                                .attendances(List.of(
                                        DailyAttendanceDto.builder()
                                                .date(LocalDate.of(2025, 4, 1))
                                                .status(AttendanceStatus.ABSENT)
                                                .build()))
                                .build()))
                .build();
    }

    private User createUser(String username, Role role) {
        return User.builder()
                .school("테스트중학교")
                .username(username)
                .password("password")
                .role(role)
                .build();
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
                .admissionDate(LocalDate.now())
                .name(name)
                .phone("010-9999-9999")
                .ssn("030101-1234567")
                .gender("M")
                .address("서울특별시 강남구")
                .user(user)
                .build();
    }

    private Parent createParent(User user, Student student) {
        return Parent.builder()
                .student(student)
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

    private Attendance createAttendance(Student student, LocalDate date, AttendanceStatus status) {
        return Attendance.builder()
                .student(student)
                .date(date)
                .status(status)
                .build();
    }

    private Semester createSemester(int year, int semester, LocalDate startDate, LocalDate endDate) {
        return Semester.builder()
                .year(year)
                .semester(semester)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}