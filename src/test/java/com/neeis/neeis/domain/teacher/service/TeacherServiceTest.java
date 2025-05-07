package com.neeis.neeis.domain.teacher.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.teacher.dto.ClassroomStudentDto;
import com.neeis.neeis.domain.teacher.dto.StudentResponseDto;
import com.neeis.neeis.domain.teacher.dto.TeacherResponseDto;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock TeacherRepository teacherRepository;
    @Mock ClassroomRepository classroomRepository;
    @Mock ClassroomStudentRepository classroomStudentRepository;
    @Mock UserService userService;
    @Mock StudentService studentService;

    @InjectMocks TeacherService teacherService;

    private User teacherUser;
    private Teacher teacher;
    private Classroom classroom;
    private Student s1, s2;
    private ClassroomStudent cs1, cs2;

    @BeforeEach
    void setUp() {
        // 공통 도메인 객체 생성
        teacherUser = User.builder()
                .username("tuser")
                .role(Role.TEACHER)
                .build();
        teacher = Teacher.builder()
                .user(teacherUser)
                .name("김교사")
                .build();
        ReflectionTestUtils.setField(teacher, "id", 10L);

        classroom = Classroom.builder()
                .grade(3)
                .classNum(1)
                .year(2025)
                .teacher(teacher)
                .build();
        ReflectionTestUtils.setField(classroom, "id", 20L);

        s1 = Student.builder().name("학생A").build();
        s2 = Student.builder().name("학생B").build();
        ReflectionTestUtils.setField(s1, "id", 1L);
        ReflectionTestUtils.setField(s2, "id", 2L);

        cs1 = ClassroomStudent.builder().number(2).student(s2).classroom(classroom).build();
        cs2 = ClassroomStudent.builder().number(1).student(s1).classroom(classroom).build();
        ReflectionTestUtils.setField(cs1, "id", 30L);
        ReflectionTestUtils.setField(cs2, "id", 31L);
    }

    @Test
    @DisplayName("getStudentsFlexible(grade,classNum) : 성공, 번호순 정렬")
    void getStudentsFlexible_withGradeClass_success() {
        // given
        given(userService.getUser("tuser")).willReturn(teacherUser);
        given(teacherRepository.findByUser(teacherUser)).willReturn(Optional.of(teacher));
        given(classroomRepository.findByYearAndGradeAndClassNum(2025, 3, 1))
                .willReturn(Optional.of(classroom));
        given(classroomStudentRepository.findByClassroom(classroom))
                .willReturn(List.of(cs1, cs2));

        // when
        ClassroomStudentDto dto =
                teacherService.getStudentsFlexible("tuser", 2025, 3, 1);

        // then: 정렬된 학생 번호 확인
        assertThat(dto.getGrade()).isEqualTo(3);
        assertThat(dto.getClassNum()).isEqualTo(1);
        assertThat(dto.getStudents()).extracting(StudentResponseDto::getNumber)
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("getStudentsFlexible(null,null) : 담임 반 조회 성공")
    void getStudentsFlexible_noGradeClass_useHomeroom() {
        // given
        given(userService.getUser("tuser")).willReturn(teacherUser);
        given(teacherRepository.findByUser(teacherUser)).willReturn(Optional.of(teacher));
        // 담임 반 조회
        given(classroomRepository.findByTeacherIdAndYear(10L, 2025))
                .willReturn(Optional.of(classroom));
        given(classroomStudentRepository.findByClassroom(classroom))
                .willReturn(List.of(cs2));

        // when
        ClassroomStudentDto dto =
                teacherService.getStudentsFlexible("tuser", 2025, null, null);

        // then
        assertThat(dto.getGrade()).isEqualTo(classroom.getGrade());
        assertThat(dto.getStudents()).hasSize(1)
                .first().extracting(StudentResponseDto::getName)
                .isEqualTo("학생A");
    }

    @Test
    @DisplayName("getStudentsFlexible: 학급 없음 -> CLASSROOM_NOT_FOUND")
    void getStudentsFlexible_classNotFound() {
        given(userService.getUser("tuser")).willReturn(teacherUser);
        given(teacherRepository.findByUser(teacherUser)).willReturn(Optional.of(teacher));
        given(classroomRepository.findByTeacherIdAndYear(10L, 2025))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                teacherService.getStudentsFlexible("tuser", 2025, null, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CLASSROOM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getStudentDetail: StudentService 호출 결과 반환")
    void getStudentDetail_success() {

        // given
        given(userService.getUser("any"))
                .willReturn(teacherUser);
        given(teacherRepository.findByUser(teacherUser))
                .willReturn(Optional.of(teacher));

        StudentDetailResDto detail = mock(StudentDetailResDto.class);
        given(studentService.getStudentDetails(1L, 2025)).willReturn(detail);

        // 아무 인증 로직 없이 바로 forward
        StudentDetailResDto res =
                teacherService.getStudentDetail("any", 1L, 2025);
        assertThat(res).isSameAs(detail);
    }

    @Test
    @DisplayName("getProfile: 성공")
    void getProfile_success() {
        // given
        given(userService.getUser("tuser")).willReturn(teacherUser);
        given(teacherRepository.findByUser(teacherUser))
                .willReturn(Optional.of(teacher));

        // when
        TeacherResponseDto dto =
                teacherService.getProfile("tuser");

        // then
        assertThat(dto.getName()).isEqualTo("김교사");
    }

    @Test
    @DisplayName("getProfile: 권한 부족 -> HANDLE_ACCESS_DENIED")
    void getProfile_noTeacherRole() {
        User u = User.builder().role(Role.STUDENT).username("stu").build();
        given(userService.getUser("stu")).willReturn(u);

        assertThatThrownBy(() -> teacherService.getProfile("stu"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("authenticate: 사용자 없거나 권한 없으면 예외")
    void authenticate_notTeacherOrNotFound() {
        User u = User.builder().role(Role.ADMIN).username("admin").build();
        given(userService.getUser("admin")).willReturn(u);
        assertThatThrownBy(() -> teacherService.authenticate("admin"))
                .isInstanceOf(CustomException.class);

        given(userService.getUser("tuser")).willReturn(teacherUser);
        given(teacherRepository.findByUser(teacherUser)).willReturn(Optional.empty());
        assertThatThrownBy(() -> teacherService.authenticate("tuser"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("checkTeacher: 이름으로 조회 성공/실패")
    void checkTeacher() {
        given(teacherRepository.findByName("김교사"))
                .willReturn(Optional.of(teacher));
        assertThat(teacherService.checkTeacher("김교사")).isSameAs(teacher);

        given(teacherRepository.findByName("존재안함"))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> teacherService.checkTeacher("존재안함"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TEACHER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("checkClassroom: 담임 반 조회 성공/실패")
    void checkClassroom() {
        given(classroomRepository.findByTeacherIdAndYear(10L, 2025))
                .willReturn(Optional.of(classroom));
        assertThat(teacherService.checkClassroom(10L, 2025)).isSameAs(classroom);

        given(classroomRepository.findByTeacherIdAndYear(10L, 2026))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> teacherService.checkClassroom(10L, 2026))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CLASSROOM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("checkClassroomStudent: 내 학생 여부 확인 및 예외")
    void checkClassroomStudent() {
        given(userService.getUser("tuser")).willReturn(teacherUser);
        given(teacherRepository.findByUser(teacherUser)).willReturn(Optional.of(teacher));

        // true 시 성공
        given(classroomStudentRepository.existsByStudentAndTeacher(1L, 10L, 2025))
                .willReturn(true);
        assertThat(teacherService.checkClassroomStudent("tuser", 1L, 2025)).isTrue();

        // false 시 예외
        given(classroomStudentRepository.existsByStudentAndTeacher(2L, 10L, 2025))
                .willReturn(false);
        assertThatThrownBy(() ->
                teacherService.checkClassroomStudent("tuser", 2L, 2025))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }
}