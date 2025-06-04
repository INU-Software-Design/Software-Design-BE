package com.neeis.neeis.domain.behavior.service;

import com.neeis.neeis.domain.behavior.Behavior;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendBehaviorFcmEvent;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * BehaviorService 테스트
 */
@ExtendWith(MockitoExtension.class)
class BehaviorServiceUpdatedTest {

    @Mock private BehaviorRepository behaviorRepository;
    @Mock private TeacherService teacherService;
    @Mock private ClassroomStudentService classroomStudentService;
    @Mock private ClassroomService classroomService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;
    @Mock private UserService userService;
    @Mock private ClassroomStudentRepository classroomStudentRepository;
    @Mock private ParentService parentService;

    @InjectMocks
    private BehaviorService behaviorService;

    // 테스트 픽스처
    private User teacherUser, studentUser, parentUser;
    private Teacher teacher;
    private Student student;
    private Parent parent;
    private Classroom classroom;
    private ClassroomStudent classroomStudent;
    private Behavior behavior;

    @BeforeEach
    void setUp() {
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

        // 행동 데이터
        behavior = createBehavior(classroomStudent, "활발하고 적극적인 성격", "전반적으로 우수한 태도를 보임");
        ReflectionTestUtils.setField(behavior, "id", 1L);
    }

    @Nested
    @DisplayName("행동 작성 테스트")
    class CreateBehaviorTest {

        @Test
        @DisplayName("교사가 담당 학생의 행동을 정상적으로 작성한다")
        void should_CreateBehavior_When_TeacherSubmitsValidData() {
            // Given: 교사가 담당 학생의 행동을 작성할 때
            BehaviorRequestDto requestDto = BehaviorRequestDto.builder()
                    .behavior("긍정적이고 협력적인 태도")
                    .generalComment("학급 활동에 적극 참여하는 모습이 인상적입니다.")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomStudentService.checkMyStudents(2025, 2, 1, teacher.getId(), 1L))
                    .willReturn(classroomStudent);
            given(behaviorRepository.save(any(Behavior.class))).willReturn(behavior);

            // When: 행동을 작성하면
            BehaviorResponseDto result = behaviorService.createBehavior(
                    "teacher1", 2025, 2, 1, 1L, requestDto);

            // Then: 행동이 정상적으로 저장된다
            assertThat(result.getBehaviorId()).isEqualTo(1L);

            // FCM 이벤트 발행과 알림 전송 검증
            then(eventPublisher).should().publishEvent(any(SendBehaviorFcmEvent.class));
            then(notificationService).should().sendNotification(eq(studentUser), anyString());
        }

        @Test
        @DisplayName("교사가 담당하지 않는 학생의 행동을 작성하려 하면 예외가 발생한다")
        void should_ThrowException_When_TeacherSubmitsForNonAssignedStudent() {
            // Given: 교사가 담당하지 않는 학생의 행동을 작성할 때
            BehaviorRequestDto requestDto = BehaviorRequestDto.builder()
                    .behavior("test")
                    .generalComment("test")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomStudentService.checkMyStudents(2025, 2, 1, teacher.getId(), 999L))
                    .willThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> behaviorService.createBehavior(
                    "teacher1", 2025, 2, 1, 999L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("행동 조회 테스트")
    class GetBehaviorTest {

        @Test
        @DisplayName("교사가 담당 학생의 행동을 조회할 수 있다")
        void should_ReturnBehavior_When_TeacherRequestsAssignedStudent() {
            // Given: 교사가 담당 학생의 행동을 조회할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1))
                    .willReturn(Optional.of(classroomStudent));
            given(behaviorRepository.findByClassroomStudentId(classroomStudent.getId()))
                    .willReturn(Optional.of(behavior));

            // When: 행동을 조회하면
            BehaviorDetailResponseDto result = behaviorService.getBehavior("teacher1", 2025, 2, 1, 1);

            // Then: 행동 정보가 반환된다
            assertThat(result.getBehavior()).isEqualTo("활발하고 적극적인 성격");
            assertThat(result.getGeneralComment()).isEqualTo("전반적으로 우수한 태도를 보임");
        }

        @Test
        @DisplayName("학생이 자신의 행동을 조회할 수 있다")
        void should_ReturnBehavior_When_StudentRequestsOwnData() {
            // Given: 학생이 자신의 행동을 조회할 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser))
                    .willReturn(Optional.of(classroomStudent));
            given(behaviorRepository.findByClassroomStudentId(classroomStudent.getId()))
                    .willReturn(Optional.of(behavior));

            // When: 본인 행동을 조회하면
            BehaviorDetailResponseDto result = behaviorService.getBehavior("student1", 2025, 2, 1, 1);

            // Then: 본인의 행동 정보가 반환된다
            assertThat(result.getBehavior()).isEqualTo("활발하고 적극적인 성격");
        }

        @Test
        @DisplayName("학생이 다른 학생의 행동을 조회하면 예외가 발생한다")
        void should_ThrowException_When_StudentRequestsOthersData() {
            // Given: 학생이 다른 학생의 행동을 조회할 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser))
                    .willReturn(Optional.of(classroomStudent));

            // When & Then: 다른 학생 번호로 조회하면 예외가 발생한다
            assertThatThrownBy(() -> behaviorService.getBehavior("student1", 2025, 2, 1, 2))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("학부모가 자녀의 행동을 조회할 수 있다")
        void should_ReturnBehavior_When_ParentRequestsChildData() {
            // Given: 학부모가 자녀의 행동을 조회할 때
            given(userService.getUser("parent1")).willReturn(parentUser);
            given(parentService.getParentByUser(parentUser)).willReturn(parent);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1))
                    .willReturn(Optional.of(classroomStudent));
            given(behaviorRepository.findByClassroomStudentId(classroomStudent.getId()))
                    .willReturn(Optional.of(behavior));

            // When: 자녀 행동을 조회하면
            BehaviorDetailResponseDto result = behaviorService.getBehavior("parent1", 2025, 2, 1, 1);

            // Then: 자녀의 행동 정보가 반환된다
            assertThat(result.getBehavior()).isEqualTo("활발하고 적극적인 성격");
        }

        @Test
        @DisplayName("학부모가 다른 학생의 행동을 조회하면 예외가 발생한다")
        void should_ThrowException_When_ParentRequestsOthersChildData() {
            // Given: 학부모가 다른 학생의 행동을 조회할 때
            Student anotherStudent = createStudent(createUser("student2", Role.STUDENT), "김철수");
            ReflectionTestUtils.setField(anotherStudent, "id", 2L);

            ClassroomStudent anotherClassroomStudent = createClassroomStudent(2, anotherStudent, classroom);
            ReflectionTestUtils.setField(anotherClassroomStudent, "id", 2L);

            given(userService.getUser("parent1")).willReturn(parentUser);
            given(parentService.getParentByUser(parentUser)).willReturn(parent);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 2))
                    .willReturn(Optional.of(anotherClassroomStudent));

            // When & Then: 다른 학생의 행동을 조회하면 예외가 발생한다
            assertThatThrownBy(() -> behaviorService.getBehavior("parent1", 2025, 2, 1, 2))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 행동을 조회하면 예외가 발생한다")
        void should_ThrowException_When_BehaviorNotExists() {
            // Given: 행동이 존재하지 않을 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(classroomService.findClassroom(2025, 2, 1)).willReturn(classroom);
            given(classroomStudentRepository.findByClassroomAndNumber(classroom, 1))
                    .willReturn(Optional.of(classroomStudent));
            given(behaviorRepository.findByClassroomStudentId(classroomStudent.getId()))
                    .willReturn(Optional.empty());

            // When & Then: 행동을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> behaviorService.getBehavior("teacher1", 2025, 2, 1, 1))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.BEHAVIOR_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("행동 수정 테스트")
    class UpdateBehaviorTest {

        @Test
        @DisplayName("교사가 담당 학생의 행동을 정상적으로 수정한다")
        void should_UpdateBehavior_When_TeacherModifiesAssignedStudent() {
            // Given: 교사가 담당 학생의 행동을 수정할 때
            BehaviorRequestDto requestDto = BehaviorRequestDto.builder()
                    .behavior("더욱 발전된 모습")
                    .generalComment("지속적인 성장이 돋보입니다.")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(behaviorRepository.findById(1L)).willReturn(Optional.of(behavior));

            // When: 행동을 수정하면
            BehaviorDetailResponseDto result = behaviorService.updateBehavior("teacher1", 1L, requestDto);

            // Then: 행동이 정상적으로 수정된다
            assertThat(result.getBehavior()).isEqualTo("더욱 발전된 모습");
            assertThat(result.getGeneralComment()).isEqualTo("지속적인 성장이 돋보입니다.");

            // FCM 이벤트 발행과 알림 전송 검증
            then(eventPublisher).should().publishEvent(any(SendBehaviorFcmEvent.class));
            then(notificationService).should().sendNotification(eq(studentUser), anyString());
        }

        @Test
        @DisplayName("다른 교사가 행동을 수정하려 하면 예외가 발생한다")
        void should_ThrowException_When_UnauthorizedTeacherModifies() {
            // Given: 다른 교사가 행동을 수정하려 할 때
            Teacher anotherTeacher = createTeacher(createUser("teacher2", Role.TEACHER), "박교사");
            ReflectionTestUtils.setField(anotherTeacher, "id", 2L);

            BehaviorRequestDto requestDto = BehaviorRequestDto.builder()
                    .behavior("수정 시도")
                    .generalComment("수정 시도")
                    .build();

            given(teacherService.authenticate("teacher2")).willReturn(anotherTeacher);
            given(behaviorRepository.findById(1L)).willReturn(Optional.of(behavior));

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> behaviorService.updateBehavior("teacher2", 1L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 행동을 수정하려 하면 예외가 발생한다")
        void should_ThrowException_When_BehaviorNotFound() {
            // Given: 존재하지 않는 행동을 수정할 때
            BehaviorRequestDto requestDto = BehaviorRequestDto.builder()
                    .behavior("test")
                    .generalComment("test")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(behaviorRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then: 행동을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> behaviorService.updateBehavior("teacher1", 999L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.BEHAVIOR_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("checkValidate 메서드 권한 검사 테스트")
    class CheckValidateTest {

        @Test
        @DisplayName("학생이 학급에 속하지 않은 경우 예외가 발생한다")
        void should_ThrowException_When_StudentNotInAnyClassroom() {
            // Given: 학생이 어떤 학급에도 속하지 않은 경우
            given(userService.getUser("student1")).willReturn(studentUser);
            given(classroomStudentRepository.findByStudentUser(studentUser))
                    .willReturn(Optional.empty());

            // When & Then: 학급을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> behaviorService.getBehavior("student1", 2025, 2, 1, 1))
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
            assertThatThrownBy(() -> behaviorService.getBehavior("teacher1", 2025, 2, 1, 99))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

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
            assertThatThrownBy(() -> behaviorService.getBehavior("nullrole1", 2025, 2, 1, 1))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ===== 테스트 헬퍼 메서드들 =====

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

    private Behavior createBehavior(ClassroomStudent classroomStudent, String behaviorText, String generalComment) {
        return Behavior.builder()
                .classroomStudent(classroomStudent)
                .behavior(behaviorText)
                .generalComment(generalComment)
                .build();
    }
}