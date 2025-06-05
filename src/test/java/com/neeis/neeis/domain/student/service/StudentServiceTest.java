package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.dto.res.PasswordResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentServiceFindTest {

    @Mock private StudentRepository studentRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private ParentRepository parentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private StudentService studentService;

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
                .password("$2a$10$dummyhashed")
                .role(com.neeis.neeis.domain.user.Role.STUDENT)
                .build();

        // Student 생성
        student = Student.builder()
                .name("김학생")
                .phone("010-1234-5678")
                .ssn("123456-1234567")
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

    // === 아이디 찾기 테스트 ===

    @Test
    @DisplayName("findUsername: 학생 - 정상 조회")
    void findUsername_student_success() {
        // Given
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .phone("010-1234-5678")
                .name("김학생")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-1234-5678")).willReturn(Optional.of(student));

        // When
        StudentResponseDto result = studentService.findUsername(requestDto);

        // Then
        assertThat(result.getLoginId()).isEqualTo(student.getUser().getUsername());
    }

    @Test
    @DisplayName("findUsername: 교사 - 정상 조회")
    void findUsername_teacher_success() {
        // Given
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .phone("010-9876-5432")
                .name("이교사")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-9876-5432")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-9876-5432")).willReturn(Optional.of(teacher));

        // When
        StudentResponseDto result = studentService.findUsername(requestDto);

        // Then
        assertThat(result.getLoginId()).isEqualTo(teacher.getUser().getUsername());

    }

    @Test
    @DisplayName("findUsername: 부모 - 정상 조회")
    void findUsername_parent_success() {
        // Given
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .phone("010-5555-6666")
                .name("박부모")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-5555-6666")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-5555-6666")).willReturn(Optional.empty());
        given(parentRepository.findByPhone("010-5555-6666")).willReturn(Optional.of(parent));

        // When
        StudentResponseDto result = studentService.findUsername(requestDto);

        // Then
        assertThat(result.getLoginId()).isEqualTo(parent.getUser().getUsername());
    }

    @Test
    @DisplayName("findUsername: 전화번호로 사용자를 찾을 수 없음")
    void findUsername_userNotFound() {
        // Given
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .phone("010-0000-0000")
                .name("없는사람")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-0000-0000")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-0000-0000")).willReturn(Optional.empty());
        given(parentRepository.findByPhone("010-0000-0000")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.findUsername(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findUsername: 이름 불일치")
    void findUsername_nameMismatch() {
        // Given
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .phone("010-1234-5678")
                .name("잘못된이름")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-1234-5678")).willReturn(Optional.of(student));

        // When & Then
        assertThatThrownBy(() -> studentService.findUsername(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findUsername: 학교 불일치")
    void findUsername_schoolMismatch() {
        // Given
        FindIdRequestDto requestDto = FindIdRequestDto.builder()
                .phone("010-1234-5678")
                .name("김학생")
                .school("다른학교")
                .build();

        given(studentRepository.findByPhone("010-1234-5678")).willReturn(Optional.of(student));

        // When & Then
        assertThatThrownBy(() -> studentService.findUsername(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    // === 비밀번호 찾기 테스트 ===

    @Test
    @DisplayName("findPassword: 학생 - 정상 조회 및 임시 비밀번호 생성")
    void findPassword_student_success() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-1234-5678")
                .name("김학생")
                .school("인천중학교")
                .ssn("123456-1234567")
                .build();

        given(studentRepository.findByPhone("010-1234-5678")).willReturn(Optional.of(student));
        given(passwordEncoder.encode(anyString())).willReturn("encodedTempPassword");

        // When
        PasswordResponseDto result = studentService.findPassword(requestDto);

        // Then
        // 6자리 임시 비밀번호가 생성되었는지 확인
        assertThat(result.getPassword()).matches("\\d{6}"); // 6자리 숫자 패턴

        // User 저장이 호출되었는지 확인
        then(userRepository).should().save(user);

        // 비밀번호가 암호화되어 저장되었는지 확인
        then(passwordEncoder).should().encode(anyString());
        assertThat(user.getPassword()).isEqualTo("encodedTempPassword");
    }

    @Test
    @DisplayName("findPassword: 교사 - 정상 조회 및 임시 비밀번호 생성")
    void findPassword_teacher_success() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-9876-5432")
                .name("이교사")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-9876-5432")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-9876-5432")).willReturn(Optional.of(teacher));
        given(passwordEncoder.encode(anyString())).willReturn("encodedTempPassword");

        // When
        PasswordResponseDto result = studentService.findPassword(requestDto);

        // Then
        // 6자리 임시 비밀번호가 생성되었는지 확인
        assertThat(result.getPassword()).matches("\\d{6}"); // 6자리 숫자 패턴

        // User 저장이 호출되었는지 확인
        then(userRepository).should().save(user);

        // 비밀번호가 암호화되어 저장되었는지 확인
        then(passwordEncoder).should().encode(anyString());
    }

    @Test
    @DisplayName("findPassword: 부모 - 정상 조회 및 임시 비밀번호 생성")
    void findPassword_parent_success() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-5555-6666")
                .name("박부모")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-5555-6666")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-5555-6666")).willReturn(Optional.empty());
        given(parentRepository.findByPhone("010-5555-6666")).willReturn(Optional.of(parent));
        given(passwordEncoder.encode(anyString())).willReturn("encodedTempPassword");

        // When
        PasswordResponseDto result = studentService.findPassword(requestDto);

        // Then
        // 6자리 임시 비밀번호가 생성되었는지 확인
        assertThat(result.getPassword()).matches("\\d{6}"); // 6자리 숫자 패턴

        // User 저장이 호출되었는지 확인
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("findPassword: 학생 - 주민번호 불일치")
    void findPassword_student_ssnMismatch() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-1234-5678")
                .name("김학생")
                .school("인천중학교")
                .ssn("999999-9999999") // 잘못된 주민번호
                .build();

        given(studentRepository.findByPhone("010-1234-5678")).willReturn(Optional.of(student));

        // When & Then
        assertThatThrownBy(() -> studentService.findPassword(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        // 비밀번호 변경이 호출되지 않았는지 확인
        then(userRepository).should(never()).save(any());
        then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("findPassword: 전화번호로 사용자를 찾을 수 없음")
    void findPassword_userNotFound() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-0000-0000")
                .name("없는사람")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-0000-0000")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-0000-0000")).willReturn(Optional.empty());
        given(parentRepository.findByPhone("010-0000-0000")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.findPassword(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("findPassword: 교사/부모 - 이름 불일치")
    void findPassword_teacher_nameMismatch() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-9876-5432")
                .name("잘못된교사이름")
                .school("인천중학교")
                .build();

        given(studentRepository.findByPhone("010-9876-5432")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-9876-5432")).willReturn(Optional.of(teacher));

        // When & Then
        assertThatThrownBy(() -> studentService.findPassword(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        // 비밀번호 변경이 호출되지 않았는지 확인
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("findPassword: 교사/부모 - 학교 불일치")
    void findPassword_teacher_schoolMismatch() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-9876-5432")
                .name("이교사")
                .school("다른학교")
                .build();

        given(studentRepository.findByPhone("010-9876-5432")).willReturn(Optional.empty());
        given(teacherRepository.findByPhone("010-9876-5432")).willReturn(Optional.of(teacher));

        // When & Then
        assertThatThrownBy(() -> studentService.findPassword(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("generateTempPassword: 임시 비밀번호가 6자리 숫자 형식인지 검증")
    void generateTempPassword_format_test() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-1234-5678")
                .name("김학생")
                .school("인천중학교")
                .ssn("123456-1234567")
                .build();

        given(studentRepository.findByPhone("010-1234-5678")).willReturn(Optional.of(student));
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

        // When
        PasswordResponseDto result = studentService.findPassword(requestDto);

        // Then
        String tempPassword = result.getPassword();

        // 6자리인지 확인
        assertThat(tempPassword).hasSize(6);

        // 모두 숫자인지 확인
        assertThat(tempPassword).matches("\\d{6}");

        // 000000 ~ 999999 범위인지 확인
        int passwordInt = Integer.parseInt(tempPassword);
        assertThat(passwordInt).isBetween(0, 999999);
    }

    @Test
    @DisplayName("findPassword: 여러 번 호출 시 매번 다른 임시 비밀번호 생성")
    void findPassword_generates_different_passwords() {
        // Given
        PasswordRequestDto requestDto = PasswordRequestDto.builder()
                .phone("010-1234-5678")
                .name("김학생")
                .school("인천중학교")
                .ssn("123456-1234567")
                .build();

        given(studentRepository.findByPhone("010-1234-5678")).willReturn(Optional.of(student));
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

        // When - 여러 번 호출
        PasswordResponseDto result1 = studentService.findPassword(requestDto);
        PasswordResponseDto result2 = studentService.findPassword(requestDto);
        PasswordResponseDto result3 = studentService.findPassword(requestDto);

        // Then - 매번 다른 비밀번호가 생성되어야 함 (높은 확률로)
        assertThat(result1.getPassword()).isNotEqualTo(result2.getPassword());
        assertThat(result2.getPassword()).isNotEqualTo(result3.getPassword());
        assertThat(result1.getPassword()).isNotEqualTo(result3.getPassword());
    }
}