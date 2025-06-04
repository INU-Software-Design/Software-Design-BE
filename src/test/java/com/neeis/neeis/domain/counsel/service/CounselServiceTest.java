package com.neeis.neeis.domain.counsel.service;

import com.neeis.neeis.domain.counsel.Counsel;
import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.CounselRepository;
import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselResponseDto;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendCounselFcmEvent;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * CounselService 테스트 (업데이트 버전)
 *
 * 변경사항:
 * 1. 역할별 권한 검사 로직 추가 (학생/교사/부모)
 * 2. FCM 이벤트 발행 및 알림 서비스 추가
 * 3. getUserAndValidateAccess 메서드를 통한 세밀한 접근 제어
 */
@ExtendWith(MockitoExtension.class)
class CounselServiceUpdatedTest {

    @Mock private CounselRepository counselRepository;
    @Mock private UserService userService;
    @Mock private TeacherService teacherService;
    @Mock private ParentService parentService;
    @Mock private StudentService studentService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;

    @InjectMocks private CounselService counselService;

    // 테스트 픽스처
    private User teacherUser, studentUser, parentUser;
    private Teacher teacher;
    private Student student;
    private Parent parent;
    private Counsel counsel;

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

        // 상담 데이터 (dateTime 명시적으로 설정)
        counsel = Counsel.builder()
                .teacher(teacher)
                .student(student)
                .category(CounselCategory.FAMILY)
                .content("가족 상담 내용")
                .dateTime(LocalDate.of(2025, 6, 4)) // 명시적으로 날짜 설정
                .build();
        ReflectionTestUtils.setField(counsel, "id", 1L);
    }

    @Nested
    @DisplayName("상담 생성 테스트")
    class CreateCounselTest {

        @Test
        @DisplayName("교사가 학생의 상담을 정상적으로 생성한다")
        void should_CreateCounsel_When_TeacherSubmitsValidData() {
            // Given: 교사가 학생의 상담을 생성할 때
            CounselRequestDto requestDto = CounselRequestDto.builder()
                    .category("FAMILY")
                    .content("학생의 가족 관계에 대한 상담을 진행했습니다.")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(studentService.getStudent(1L)).willReturn(student);
            given(counselRepository.save(any(Counsel.class))).willReturn(counsel);

            // When: 상담을 생성하면
            CounselResponseDto result = counselService.createCounsel("teacher1", 1L, requestDto);

            // Then: 상담이 정상적으로 생성된다
            assertThat(result.getCounselId()).isEqualTo(1L);

            // FCM 이벤트 발행과 알림 전송 검증
            then(eventPublisher).should().publishEvent(any(SendCounselFcmEvent.class));
            then(notificationService).should().sendNotification(eq(studentUser), anyString());
        }

        @Test
        @DisplayName("잘못된 카테고리로 상담 생성 시 예외가 발생한다")
        void should_ThrowException_When_InvalidCategoryProvided() {
            // Given: 잘못된 카테고리로 상담을 생성할 때
            CounselRequestDto requestDto = CounselRequestDto.builder()
                    .category("INVALID_CATEGORY")
                    .content("test content")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(studentService.getStudent(1L)).willReturn(student);

            // When & Then: 카테고리가 존재하지 않는다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.createCounsel("teacher1", 1L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.COUNSEL_CATEGORY_NOT_EXIST);
        }

        @Test
        @DisplayName("존재하지 않는 학생에 대한 상담 생성 시 예외가 발생한다")
        void should_ThrowException_When_StudentNotFound() {
            // Given: 존재하지 않는 학생에 대한 상담을 생성할 때
            CounselRequestDto requestDto = CounselRequestDto.builder()
                    .category("FAMILY")
                    .content("test content")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(studentService.getStudent(999L)).willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // When & Then: 학생을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.createCounsel("teacher1", 999L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("개별 상담 조회 테스트")
    class GetCounselTest {

        @Test
        @DisplayName("교사가 상담을 조회할 수 있다")
        void should_ReturnCounsel_When_TeacherRequests() {
            // Given: 교사가 상담을 조회할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(counselRepository.findById(1L)).willReturn(Optional.of(counsel));
            given(teacherService.authenticate("teacher1")).willReturn(teacher);

            // When: 상담을 조회하면
            CounselDetailDto result = counselService.getCounsel("teacher1", 1L);

            // Then: 상담 정보가 반환된다
            assertThat(result.getContent()).isEqualTo("가족 상담 내용");
            assertThat(result.getCategory()).isEqualTo(CounselCategory.FAMILY);
        }

        @Test
        @DisplayName("학생이 자신의 상담을 조회할 수 있다")
        void should_ReturnCounsel_When_StudentRequestsOwnCounsel() {
            // Given: 학생이 자신의 상담을 조회할 때
            given(userService.getUser("student1")).willReturn(studentUser);
            given(counselRepository.findById(1L)).willReturn(Optional.of(counsel));

            // When: 본인 상담을 조회하면
            CounselDetailDto result = counselService.getCounsel("student1", 1L);

            // Then: 상담 정보가 반환된다
            assertThat(result.getContent()).isEqualTo("가족 상담 내용");
        }

        @Test
        @DisplayName("학생이 다른 학생의 상담을 조회하면 예외가 발생한다")
        void should_ThrowException_When_StudentRequestsOthersCounsel() {
            // Given: 학생이 다른 학생의 상담을 조회할 때
            User anotherStudentUser = createUser("student2", Role.STUDENT);
            ReflectionTestUtils.setField(anotherStudentUser, "id", 2L);

            Student anotherStudent = createStudent(anotherStudentUser, "김철수");
            ReflectionTestUtils.setField(anotherStudent, "id", 2L);

            Counsel anotherCounsel = createCounsel(teacher, anotherStudent, CounselCategory.CAREER, "진로 상담");
            ReflectionTestUtils.setField(anotherCounsel, "id", 2L);

            given(userService.getUser("student1")).willReturn(studentUser);
            given(counselRepository.findById(2L)).willReturn(Optional.of(anotherCounsel));

            // When & Then: 다른 학생의 상담을 조회하면 예외가 발생한다
            assertThatThrownBy(() -> counselService.getCounsel("student1", 2L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("부모가 자녀의 상담을 조회할 수 있다")
        void should_ReturnCounsel_When_ParentRequestsChildCounsel() {
            // Given: 부모가 자녀의 상담을 조회할 때
            given(userService.getUser("parent1")).willReturn(parentUser);
            given(counselRepository.findById(1L)).willReturn(Optional.of(counsel));
            given(parentService.getParentByUser(parentUser)).willReturn(parent);

            // When: 자녀 상담을 조회하면
            CounselDetailDto result = counselService.getCounsel("parent1", 1L);

            // Then: 상담 정보가 반환된다
            assertThat(result.getContent()).isEqualTo("가족 상담 내용");
        }

        @Test
        @DisplayName("존재하지 않는 상담을 조회하면 예외가 발생한다")
        void should_ThrowException_When_CounselNotFound() {
            // Given: 존재하지 않는 상담을 조회할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(counselRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then: 상담을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.getCounsel("teacher1", 999L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.COUNSEL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상담 목록 조회 테스트")
    class GetCounselsTest {

        @Test
        @DisplayName("교사가 학생의 상담 목록을 조회할 수 있다")
        void should_ReturnCounselList_When_TeacherRequests() {
            // Given: 교사가 학생의 상담 목록을 조회할 때
            List<Counsel> counselList = List.of(counsel);

            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(studentService.getStudent(1L)).willReturn(student);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(counselRepository.findByStudentId(1L)).willReturn(Optional.of(counselList));

            // When: 상담 목록을 조회하면
            List<CounselDetailDto> result = counselService.getCounsels("teacher1", 1L);

            // Then: 상담 목록이 반환된다
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContent()).isEqualTo("가족 상담 내용");
        }

        @Test
        @DisplayName("학생이 자신의 상담 목록을 조회할 수 있다")
        void should_ReturnCounselList_When_StudentRequestsOwnCounsels() {
            // Given: 학생이 자신의 상담 목록을 조회할 때
            List<Counsel> counselList = List.of(counsel);

            given(userService.getUser("student1")).willReturn(studentUser);
            given(studentService.getStudent(1L)).willReturn(student);
            given(counselRepository.findByStudentId(1L)).willReturn(Optional.of(counselList));

            // When: 본인 상담 목록을 조회하면
            List<CounselDetailDto> result = counselService.getCounsels("student1", 1L);

            // Then: 상담 목록이 반환된다
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContent()).isEqualTo("가족 상담 내용");
        }

        @Test
        @DisplayName("학생이 다른 학생의 상담 목록을 조회하면 예외가 발생한다")
        void should_ThrowException_When_StudentRequestsOthersCounsels() {
            // Given: 학생이 다른 학생의 상담 목록을 조회할 때
            User anotherStudentUser = createUser("student2", Role.STUDENT);
            ReflectionTestUtils.setField(anotherStudentUser, "id", 2L);

            Student anotherStudent = createStudent(anotherStudentUser, "김철수");
            ReflectionTestUtils.setField(anotherStudent, "id", 2L);

            given(userService.getUser("student1")).willReturn(studentUser);
            given(studentService.getStudent(2L)).willReturn(anotherStudent);

            // When & Then: 다른 학생의 상담 목록을 조회하면 예외가 발생한다
            assertThatThrownBy(() -> counselService.getCounsels("student1", 2L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 상담 목록을 조회하면 예외가 발생한다")
        void should_ThrowException_When_CounselListNotFound() {
            // Given: 상담 목록이 존재하지 않을 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(studentService.getStudent(1L)).willReturn(student);
            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(counselRepository.findByStudentId(1L)).willReturn(Optional.empty());

            // When & Then: 상담을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.getCounsels("teacher1", 1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.COUNSEL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상담 수정 테스트")
    class UpdateCounselTest {

        @Test
        @DisplayName("교사가 상담을 정상적으로 수정한다")
        void should_UpdateCounsel_When_TeacherModifies() {
            // Given: 교사가 상담을 수정할 때
            CounselRequestDto requestDto = CounselRequestDto.builder()
                    .category("CAREER")
                    .content("진로에 대한 추가 상담을 진행했습니다.")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(counselRepository.findById(1L)).willReturn(Optional.of(counsel));

            // When: 상담을 수정하면
            CounselDetailDto result = counselService.updateCounsel("teacher1", 1L, requestDto);

            // Then: 상담이 정상적으로 수정된다
            assertThat(result.getContent()).isEqualTo("진로에 대한 추가 상담을 진행했습니다.");

            // FCM 이벤트 발행과 알림 전송 검증
            then(eventPublisher).should().publishEvent(any(SendCounselFcmEvent.class));
            then(notificationService).should().sendNotification(eq(studentUser), anyString());
        }

        @Test
        @DisplayName("존재하지 않는 상담을 수정하려 하면 예외가 발생한다")
        void should_ThrowException_When_CounselNotFoundForUpdate() {
            // Given: 존재하지 않는 상담을 수정할 때
            CounselRequestDto requestDto = CounselRequestDto.builder()
                    .category("FAMILY")
                    .content("test")
                    .build();

            given(teacherService.authenticate("teacher1")).willReturn(teacher);
            given(counselRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then: 상담을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.updateCounsel("teacher1", 999L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.COUNSEL_NOT_FOUND);
        }

        @Test
        @DisplayName("교사가 아닌 사용자가 상담을 수정하려 하면 예외가 발생한다")
        void should_ThrowException_When_NonTeacherTriesToUpdate() {
            // Given: 교사가 아닌 사용자가 상담을 수정할 때
            CounselRequestDto requestDto = CounselRequestDto.builder()
                    .category("FAMILY")
                    .content("test")
                    .build();

            given(teacherService.authenticate("student1"))
                    .willThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

            // When & Then: 접근 권한이 없다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.updateCounsel("student1", 1L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
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
            given(counselRepository.findById(1L)).willReturn(Optional.of(counsel)); // 상담은 존재하도록 설정

            // When & Then: 역할이 null이므로 switch문에서 NullPointerException이 발생한다
            assertThatThrownBy(() -> counselService.getCounsel("nullrole1", 1L))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("존재하지 않는 상담에 접근하면 COUNSEL_NOT_FOUND 예외가 발생한다")
        void should_ThrowException_When_CounselNotFoundInValidation() {
            // Given: 존재하지 않는 상담에 접근할 때
            given(userService.getUser("teacher1")).willReturn(teacherUser);
            given(counselRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then: 상담을 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.getCounsel("teacher1", 999L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.COUNSEL_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 사용자가 접근하면 예외가 발생한다")
        void should_ThrowException_When_UserNotFound() {
            // Given: 존재하지 않는 사용자가 접근할 때
            given(userService.getUser("nonexistent")).willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // When & Then: 사용자를 찾을 수 없다는 예외가 발생한다
            assertThatThrownBy(() -> counselService.getCounsel("nonexistent", 1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(ex -> ((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
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

    private Parent createParent(User user, Student student) {
        return Parent.builder()
                .student(student)
                .user(user)
                .build();
    }

    private Counsel createCounsel(Teacher teacher, Student student, CounselCategory category, String content) {
        return Counsel.builder()
                .teacher(teacher)
                .student(student)
                .category(category)
                .content(content)
                .dateTime(LocalDate.now()) // dateTime을 현재 시간으로 설정
                .build();
    }
}