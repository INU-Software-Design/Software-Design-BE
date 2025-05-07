package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentUpdateRequestDto;
import com.neeis.neeis.domain.student.dto.res.PasswordResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentSaveResponseDto;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.user.Role;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private ParentRepository parentRepository;
    @Mock private ClassroomStudentRepository classroomStudentRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks private StudentService studentService;

    private Classroom classroom;
    private User studentUser, teacherUser;
    private Teacher teacher;
    private StudentRequestDto saveDto;
    private StudentUpdateRequestDto updateDto;
    private User adminUser;
    private Student existingStudent;
    private Parent father, mother;
    private ClassroomStudent cs;

    @BeforeEach
    void init() {
        // set uploadPath for saveImage
        ReflectionTestUtils.setField(studentService, "uploadPath", "/tmp/images");
        studentUser = User.builder()
                .username("user")
                .role(Role.STUDENT)
                .build();

        // 공통 DTO 준비
        saveDto = StudentRequestDto.builder()
                .name("홍길동")
                .phone("010-1234-5678")
                .school("인천중학교")
                .role("STUDENT")
                .admissionDate(LocalDate.of(2025, 3, 1))
                .address("인천시")
                .build();

        updateDto = StudentUpdateRequestDto.builder()
                .name("최길동")
                .phone("010-8765-4321")
                .address("부평구")
                .fatherName("최아버지")
                .fatherPhone("010-0000-1111")
                .motherName("최어머니")
                .motherPhone("010-2222-3333")
                .build();

        // 관리자, 교사 유저
        adminUser   = User.builder().school("S").username("admin").role(Role.ADMIN).build();
        ReflectionTestUtils.setField(adminUser, "id", 1L);

        teacherUser = User.builder().username("teacher").role(Role.TEACHER).build();
        ReflectionTestUtils.setField(teacherUser, "id", 2L);

        teacher = Teacher.builder()
                .name("김교사")
                .phone("010-1234-5678")
                .email("teacher1@test.com")
                .user(teacherUser)
                .build();
        ReflectionTestUtils.setField(teacher,   "id", 10L);

        // 기존 학생 + 부모 엔티티
        existingStudent = Student.builder()
                .name("박학생")
                .phone("010-1111-2222")
                .address("강남구")
                .ssn("000101-1234567")
                .gender("M")
                .image(null)
                .user(studentUser)
                .admissionDate(LocalDate.of(2024, 3, 1))
                .build();
        ReflectionTestUtils.setField(existingStudent, "id", 5L);

        father = Parent.builder().relationShip("부").name("부아버지").phone("010-5555-6666").build();
        ReflectionTestUtils.setField(father, "id", 11L);

        mother = Parent.builder().relationShip("모").name("모어머니").phone("010-7777-8888").build();
        ReflectionTestUtils.setField(mother, "id", 12L);

        cs = ClassroomStudent.builder().student(existingStudent).classroom(classroom).build();
        ReflectionTestUtils.setField(cs, "id", 20L);
        // uploadPath 테스트용으로 임시 디렉토리 지정
        ReflectionTestUtils.setField(studentService, "uploadPath", System.getProperty("java.io.tmpdir"));

    }

    @Test
    @DisplayName("findUsername: 성공")
    void findUsername_success() {
        Student student = Student.builder()
                .phone("010-1111-2222").name("홍길동").ssn("123").address("A").image("img").gender("M")
                .admissionDate(LocalDate.of(2025,1,1))
                .user(User.builder().school("S").username("u").password("p").role(Role.STUDENT).build())
                .build();
        given(studentRepository.findByPhone("010-1111-2222")).willReturn(Optional.of(student));

        StudentResponseDto dto = studentService.findUsername(
                FindIdRequestDto.builder().name("홍길동").phone("010-1111-2222").school("S").build());
        assertThat(dto.getLoginId()).isEqualTo(student.getUser().getUsername());
    }

    @Test
    @DisplayName("findUsername: 유저 없음 -> USER_NOT_FOUND")
    void findUsername_notFound() {
        given(studentRepository.findByPhone(anyString())).willReturn(Optional.empty());
        assertThatThrownBy(() -> studentService.findUsername(
                FindIdRequestDto.builder().name("N").phone("010").school("S").build()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findUsername: 이름/학교 불일치 -> INVALID_INPUT_VALUE")
    void findUsername_invalidInput() {
        Student s = Student.builder()
                .phone("010").name("A")
                .user(User.builder().school("X").build())
                .build();
        given(studentRepository.findByPhone("010")).willReturn(Optional.of(s));
        assertThatThrownBy(() -> studentService.findUsername(
                FindIdRequestDto.builder().name(" B").phone("010").school("X").build()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("findPassword: 성공")
    void findPassword_success() {
        User u = User.builder().school("S").username("u").password("p").role(Role.STUDENT).build();
        Student s = Student.builder().phone("010").name("A").ssn("SSN").user(u).build();
        given(studentRepository.findByPhone("010")).willReturn(Optional.of(s));

        PasswordResponseDto dto = studentService.findPassword(
                PasswordRequestDto.builder().name("A").phone("010").school("S").ssn("SSN").build());
        assertThat(dto.getPassword()).isEqualTo(s.getUser().getPassword());
    }

    @Test
    @DisplayName("findPassword: 불일치 -> INVALID_INPUT_VALUE")
    void findPassword_invalidInput() {
        Student s = Student.builder()
                .phone("010").name("A").ssn("SSN")
                .user(User.builder().school("S").build())
                .build();
        given(studentRepository.findByPhone("010")).willReturn(Optional.of(s));
        assertThatThrownBy(() -> studentService.findPassword(
                PasswordRequestDto.builder().name("B").phone("010").school("S").ssn("SSN").build()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("getStudentDetails: 성공")
    void getStudentDetails_success() {
        Long id = 5L;
        int year = 2025;
        Student s = Student.builder()
                .name("N").phone("010")
                .user(User.builder().school("S").build())
                .build();

        classroom = Classroom.builder()
                .grade(2)
                .classNum(1)
                .year(2025)
                .teacher(teacher)
                .build();
        ReflectionTestUtils.setField(classroom, "id", 20L);


        ReflectionTestUtils.setField(s, "id", id);

        ClassroomStudent cs = ClassroomStudent.builder().student(s)
                .number(3).classroom(classroom).build();

        given(studentRepository.findById(id)).willReturn(Optional.of(s));
        given(classroomStudentRepository.findByStudentAndClassroomYear(id, year))
                .willReturn(Optional.of(cs));
        Parent father = Parent.builder().name("F").relationShip("부").build();
        Parent mother =  Parent.builder().name("M").relationShip("모").build();
        given(parentRepository.findByStudent(s))
                .willReturn(List.of(father, mother));

        StudentDetailResDto dto = studentService.getStudentDetails(id, year);
        assertThat(dto.getName()).isEqualTo("N");
        assertThat(dto.getFatherName()).isEqualTo("F");
        assertThat(dto.getMotherName()).isEqualTo("M");
    }

    @Test
    @DisplayName("getStudentDetails: 학생 없음 -> USER_NOT_FOUND")
    void getStudentDetails_notFoundStudent() {
        given(studentRepository.findById(any()))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> studentService.getStudentDetails(1L,2025))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getLast4Digits: 길이 부족 시 INVALID_INPUT_VALUE 예외")
    void getLast4Digits_invalid() throws Exception {
        // private 메서드를 리플렉션으로 가져오기
        Method getLast4Digits = StudentService.class
                .getDeclaredMethod("getLast4Digits", String.class);
        getLast4Digits.setAccessible(true);

        // "123" 처럼 4자리 미만을 넘기면 내부에서 CustomException이 터져야 한다
        Throwable thrown = catchThrowable(() ->
                getLast4Digits.invoke(studentService, "123")
        );

        // 리플렉션 호출 시 InvocationTargetException 안에 CustomException이 래핑되어 있으니
        // hasRootCauseInstanceOf 로 꺼내서 검증
        assertThat(thrown)
                .hasRootCauseInstanceOf(CustomException.class)
                .rootCause()
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("saveStudent: 사용자 없으면 USER_NOT_FOUND")
    void saveStudent_userNotFound() {
        given(userRepository.findByUsername("nope")).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                studentService.saveStudent("nope", saveDto, null)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("saveStudent: 권한 없으면 HANDLE_ACCESS_DENIED")
    void saveStudent_noPermission() {
        User u = User.builder().role(Role.STUDENT).build();
        given(userRepository.findByUsername("stu")).willReturn(Optional.of(u));

        assertThatThrownBy(() ->
                studentService.saveStudent("stu", saveDto, null)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }


    @Test
    @DisplayName("saveStudent: 성공 (이미지 없이)")
    void saveStudent_success_withoutImage() {
        // given: ADMIN 권한
        given(userRepository.findByUsername("admin")).willReturn(Optional.of(adminUser));
        // 첫 save -> 새로운 User 리턴
        User newUser = User.builder().build();
        ReflectionTestUtils.setField(newUser, "id", 99L);
        given(userRepository.save(any(User.class))).willReturn(newUser);
        // passwordEncoder
        given(passwordEncoder.encode(anyString())).willReturn("~encoded~");
        // studentRepository.save
        given(studentRepository.save(any(Student.class))).willReturn(existingStudent);

        // when
        StudentSaveResponseDto dto = studentService.saveStudent("admin", saveDto, null);

        // then
        // password 는 전화번호 뒷4자리
        assertThat(dto.getPassword()).isEqualTo("5678");
        // UserRepository#save 두 번 호출 (create + updateUsername)
        then(userRepository).should(times(2)).save(any(User.class));
        // student 저장
        then(studentRepository).should().save(any(Student.class));
        // encoded 호출
        then(passwordEncoder).should().encode("5678");
    }

    @Test
    @DisplayName("updateStudent: 사용자 없으면 USER_NOT_FOUND")
    void updateStudent_userNotFound() {
        given(userRepository.findByUsername("nope")).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                studentService.updateStudent("nope", 5L, updateDto, null)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("updateStudent: 권한 없으면 HANDLE_ACCESS_DENIED")
    void updateStudent_noPermission() {
        // 권한 STUDENT 로 stub
        User stu = User.builder().role(Role.STUDENT).build();
        given(userRepository.findByUsername("stu")).willReturn(Optional.of(stu));

        assertThatThrownBy(() ->
                studentService.updateStudent("stu", 5L, updateDto, null)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("updateStudent: 성공 (이미지 변경 없이)")
    void updateStudent_success_withoutImage() {
        // given: TEACHER 권한
        given(userRepository.findByUsername("teacher")).willReturn(Optional.of(teacherUser));
        given(studentRepository.findById(5L)).willReturn(Optional.of(existingStudent));
        given(parentRepository.findByStudent(existingStudent)).willReturn(List.of(father, mother));

        // when
        studentService.updateStudent("teacher", 5L, updateDto, null);

        // then: Student 필드가 업데이트 되었는지 확인
        assertThat(existingStudent.getName()).isEqualTo("최길동");
        assertThat(existingStudent.getPhone()).isEqualTo("010-8765-4321");
        assertThat(existingStudent.getAddress()).isEqualTo("부평구");
        // 부모 정보도 업데이트
        assertThat(father.getName()).isEqualTo("최아버지");
        assertThat(father.getPhone()).isEqualTo("010-0000-1111");
        assertThat(mother.getName()).isEqualTo("최어머니");
        assertThat(mother.getPhone()).isEqualTo("010-2222-3333");
    }
}
