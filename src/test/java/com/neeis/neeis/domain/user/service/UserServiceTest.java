package com.neeis.neeis.domain.user.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.teacherSubject.TeacherSubject;
import com.neeis.neeis.domain.teacherSubject.TeacherSubjectRepository;
import com.neeis.neeis.domain.user.dto.TokenResponseDto;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.domain.user.dto.FcmTokenRequestDto;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.user.dto.UpdatePasswordRequestDto;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private ClassroomStudentRepository classroomStudentRepository;
    @Mock private JwtProvider jwtProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private User user;
    private Student student;
    private Teacher teacher;
    private Parent parent;

    @BeforeEach
    void setUp() {
        // 공통으로 사용할 User 생성
        user = User.builder()
                .school("인천중학교")
                .username("user1")
                .password("storedPw")
                .role(com.neeis.neeis.domain.user.Role.STUDENT)
                .build();

        // Student 생성
        student = Student.builder()
                .name("김학생")
                .phone("010-1234-5678")
                .user(user)
                .build();

        // Teacher 생성
        teacher = Teacher.builder()
                .name("이교사")
                .phone("010-9876-5432")
                .user(user)
                .build();

        // Parent 생성
        parent = Parent.builder()
                .name("박부모")
                .phone("010-5555-6666")
                .user(user)
                .build();
    }

    @Test
    @DisplayName("login: 사용자 없으면 LOGIN_INPUT_INVALID")
    void login_userNotFound() {
        given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

        LoginRequestDto dto = new LoginRequestDto("unknown", "pw");
        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LOGIN_INPUT_INVALID.getMessage());
    }

    @Test
    @DisplayName("login: 암호화된 비번 매칭 성공 - STUDENT")
    void login_encodedPasswordSuccess_student() {
        // Given
        ReflectionTestUtils.setField(user, "password", "$2a$10$dummyhashed");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("input", "$2a$10$dummyhashed")).willReturn(true);
        given(jwtProvider.createAccessToken("user1", "STUDENT")).willReturn("tokenABC");

        // Student 로그인에 필요한 Mock 설정
        given(studentRepository.findByUser(user)).willReturn(Optional.of(student));

        Classroom classroom = Classroom.builder()
                .year(2025)
                .grade(1)
                .classNum(1)
                .build();

        ClassroomStudent classroomStudent = ClassroomStudent.builder()
                .student(student)
                .classroom(classroom)
                .number(1)
                .build();

        given(classroomStudentRepository.findByStudentAndClassroomYear(any(), eq(LocalDate.now().getYear())))
                .willReturn(Optional.of(classroomStudent));

        // When
        TokenResponseDto res = userService.login(new LoginRequestDto("user1", "input"));

        // Then
        assertThat(res.getAccessToken()).isEqualTo("tokenABC");
        assertThat(res.getName()).isEqualTo("김학생");
        assertThat(res.getRole()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("login: 암호화된 비번 매칭 실패")
    void login_encodedPasswordFail() {
        ReflectionTestUtils.setField(user, "password", "$2a$10$dummyhashed");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", "$2a$10$dummyhashed")).willReturn(false);

        assertThatThrownBy(() -> userService.login(new LoginRequestDto("user1", "wrong")))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LOGIN_INPUT_INVALID.getMessage());
    }

    @Test
    @DisplayName("login: 평문 비번일 때 일치하지 않으면 예외")
    void login_plainPasswordFail() {
        ReflectionTestUtils.setField(user, "password", "plainpw");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(new LoginRequestDto("user1", "nope")))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LOGIN_INPUT_INVALID.getMessage());
    }

    @Test
    @DisplayName("login: 평문 비번일 때 일치하면 암호화 후 저장하고 토큰 반환")
    void login_plainPasswordThenEncrypt() {
        // Given
        ReflectionTestUtils.setField(user, "password", "plainpw");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(user));
        given(passwordEncoder.encode("plainpw")).willReturn("encPw");
        given(jwtProvider.createAccessToken("user1", "STUDENT")).willReturn("gotToken");

        // Student 로그인에 필요한 Mock 설정
        given(studentRepository.findByUser(user)).willReturn(Optional.of(student));

        Classroom classroom = Classroom.builder()
                .year(2025)
                .grade(1)
                .classNum(1)
                .build();

        ClassroomStudent classroomStudent = ClassroomStudent.builder()
                .student(student)
                .classroom(classroom)
                .number(1)
                .build();

        given(classroomStudentRepository.findByStudentAndClassroomYear(any(), eq(LocalDate.now().getYear())))
                .willReturn(Optional.of(classroomStudent));

        // When
        TokenResponseDto res = userService.login(new LoginRequestDto("user1", "plainpw"));

        // Then
        // 비밀번호가 암호화되어 update, 저장 되었는지 검증
        assertThat(user.getPassword()).isEqualTo("encPw");
        then(userRepository).should().save(user);
        assertThat(res.getAccessToken()).isEqualTo("gotToken");
        assertThat(res.getName()).isEqualTo("김학생");
    }

    @Test
    @DisplayName("updatePassword: 사용자 없으면 LOGIN_INPUT_INVALID")
    void updatePassword_userNotFound() {
        given(userRepository.findByUsername("u")).willReturn(Optional.empty());

        UpdatePasswordRequestDto req = UpdatePasswordRequestDto.builder()
                .loginId("u").oldPassword("old").newPassword("new").build();
        assertThatThrownBy(() -> userService.updatePassword(req))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LOGIN_INPUT_INVALID.getMessage());
    }

    @Test
    @DisplayName("updatePassword: 새/기존 비번 동일 시 PASSWORD_EQUALS")
    void updatePassword_samePasswords() {
        given(userRepository.findByUsername("u")).willReturn(Optional.of(user));

        UpdatePasswordRequestDto req = UpdatePasswordRequestDto.builder()
                .loginId("u").oldPassword("a").newPassword("a").build();
        assertThatThrownBy(() -> userService.updatePassword(req))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.PASSWORD_EQUALS.getMessage());
    }

    @Test
    @DisplayName("updatePassword: 기존 비번 불일치 시 LOGIN_INPUT_INVALID")
    void updatePassword_oldMismatch() {
        ReflectionTestUtils.setField(user, "password", "$2a$hashed");
        given(userRepository.findByUsername("u")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongOld", "$2a$hashed")).willReturn(false);

        UpdatePasswordRequestDto req = UpdatePasswordRequestDto.builder()
                .loginId("u").oldPassword("wrongOld").newPassword("newpw").build();
        assertThatThrownBy(() -> userService.updatePassword(req))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LOGIN_INPUT_INVALID.getMessage());
    }

    @Test
    @DisplayName("updatePassword: 정상 변경")
    void updatePassword_success() {
        ReflectionTestUtils.setField(user, "password", "$2a$hashed");
        given(userRepository.findByUsername("u")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("oldpw", "$2a$hashed")).willReturn(true);
        given(passwordEncoder.encode("newpw")).willReturn("newEnc");

        userService.updatePassword(UpdatePasswordRequestDto.builder()
                .loginId("u").oldPassword("oldpw").newPassword("newpw").build());
        assertThat(user.getPassword()).isEqualTo("newEnc");
    }

    @Test
    @DisplayName("getUser: 없으면 USER_NOT_FOUND")
    void getUser_notFound() {
        given(userRepository.findByUsername("x")).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUser("x"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getUser: 정상 반환")
    void getUser_success() {
        given(userRepository.findByUsername("u")).willReturn(Optional.of(user));
        assertThat(userService.getUser("u")).isSameAs(user);
    }

    @Test
    @DisplayName("registerToken: FCM 토큰 등록 성공")
    void registerToken_success() {
        // Given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(user));
        FcmTokenRequestDto requestDto = FcmTokenRequestDto.builder()
                .token("fcm-token-123")
                .build();

        // When
        userService.registerToken("user1", requestDto);

        // Then
        assertThat(user.getFcmToken()).isEqualTo("fcm-token-123");
    }
}