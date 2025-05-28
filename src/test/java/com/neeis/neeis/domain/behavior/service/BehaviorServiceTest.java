package com.neeis.neeis.domain.behavior.service;

import com.neeis.neeis.domain.behavior.Behavior;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class BehaviorServiceTest {

    @Mock private BehaviorRepository behaviorRepository;
    @Mock private TeacherService teacherService;
    @Mock private ClassroomStudentService classroomStudentService;

    @InjectMocks
    private BehaviorService behaviorService;


    private User teacherUser;
    private Teacher teacher;
    private Classroom classroom;
    private ClassroomStudent cs;
    private Student s1;
    private Behavior behavior;

    @BeforeEach
    void setUp() {
        teacherUser = User.builder()
                .school("대한중학교")
                .username("teacher1")
                .password("pw")
                .role(Role.TEACHER)
                .build();

        teacher = Teacher.builder()
                .name("김교사")
                .phone("010-1234-5678")
                .email("teacher1@test.com")
                .user(teacherUser)
                .build();
        ReflectionTestUtils.setField(teacher,   "id", 10L);

        // 담당 반 생성
        classroom = Classroom.builder()
                .grade(2)
                .classNum(1)
                .year(2025)
                .teacher(teacher)
                .build();
        ReflectionTestUtils.setField(classroom, "id", 20L);

        s1 = Student.builder()
                .admissionDate(LocalDate.now())
                .name("테스트")
                .phone("010-3333-3333")
                .ssn("000802-3333333")
                .gender("F")
                .address("인천광역시 송도 1129")
                .build();

        ReflectionTestUtils.setField(s1, "id", 1L);

        cs = ClassroomStudent.builder()
                .number(1)
                .student(s1)
                .classroom(classroom)
                .build();
        ReflectionTestUtils.setField(cs, "id", 30L);

        behavior = Behavior.builder()
                .classroomStudent(cs)
                .behavior("comment")
                .generalComment("initial")
                .build();

        ReflectionTestUtils.setField(behavior, "id", 1L);

    }


    @Test
    @DisplayName("createBehavior: 정상 저장 후 DTO 반환")
    void createBehavior_success() {
        // given
        BehaviorRequestDto req = BehaviorRequestDto.builder()
                .behavior("test-behavior")
                .generalComment("test-general-comment")
                .build();
        given(teacherService.authenticate("t1"))
                .willReturn(teacher);
        given(classroomStudentService.checkMyStudents(
                2025, 1, 1, teacher.getId(), 2L))
                .willReturn(cs);
        given(behaviorRepository.save(any(Behavior.class)))
                .willReturn(behavior);

        // when
        BehaviorResponseDto res = behaviorService.createBehavior(
                "t1", 2025, 1, 1, 2L, req);

        // then
        assertThat(res.getBehaviorId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createBehavior: 권한 없는 학생 -> HANDLE_ACCESS_DENIED")
    void createBehavior_accessDenied() {
        BehaviorRequestDto req = BehaviorRequestDto.builder()
                .behavior("x")
                .generalComment("y")
                .build();
        // 1) 인증은 정상
        given(teacherService.authenticate("t1")).willReturn(teacher);
        // 2) 내 반 학생이 아니어서 예외
        given(classroomStudentService.checkMyStudents(
                anyInt(), anyInt(), anyInt(), eq(teacher.getId()), eq(2L)))
                .willThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

        // when & then
        assertThatThrownBy(() ->
                behaviorService.createBehavior("t1", 2025,1,1, 2L, req))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("getBehavior: 없으면 BEHAVIOR_NOT_FOUND")
    void getBehavior_notFound() {
        // given
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(classroomStudentService.checkMyStudents(
                anyInt(), anyInt(), anyInt(), anyLong(), anyLong()))
                .willReturn(cs);
        given(behaviorRepository.findByClassroomStudentId(cs.getId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                behaviorService.getBehavior("t1", 2025, 1, 1, 2))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BEHAVIOR_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getBehavior: 권한 없으면 HANDLE_ACCESS_DENIED")
    void getBehavior_accessDenied() {
        // given: 다른 교사 인증
        Teacher other = Teacher.builder().build();
        ReflectionTestUtils.setField(other, "id", 100L);
        given(teacherService.authenticate("t1")).willReturn(other);
        given(classroomStudentService.checkMyStudents(
                anyInt(), anyInt(), anyInt(), anyLong(), anyLong()))
                .willReturn(cs);
        given(behaviorRepository.findByClassroomStudentId(cs.getId()))
                .willReturn(Optional.of(behavior));

        // when & then
        assertThatThrownBy(() ->
                behaviorService.getBehavior("t1", 2025, 1, 1, 2))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("getBehavior: 정상 조회")
    void getBehavior_success() {
        // given
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(classroomStudentService.checkMyStudents(
                anyInt(), anyInt(), anyInt(), anyLong(), anyLong()))
                .willReturn(cs);
        given(behaviorRepository.findByClassroomStudentId(cs.getId()))
                .willReturn(Optional.of(behavior));

        // when
        BehaviorDetailResponseDto dto =
                behaviorService.getBehavior("t1", 2025, 1, 1, 2);

        // then
        assertThat(dto.getGeneralComment()).isEqualTo("initial");
    }

    @Test
    @DisplayName("updateBehavior: 없으면 BEHAVIOR_NOT_FOUND")
    void updateBehavior_notFound() {
        // given
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(behaviorRepository.findById(3L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                behaviorService.updateBehavior("t1", 3L,
                        BehaviorRequestDto.builder().behavior("x").build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BEHAVIOR_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("updateBehavior: 권한 없으면 HANDLE_ACCESS_DENIED")
    void updateBehavior_accessDenied() {
        // given: 권한 없는 교사
        Teacher other = Teacher.builder().build();
        ReflectionTestUtils.setField(other, "id", 100L);
        given(teacherService.authenticate("t1")).willReturn(other);
        given(behaviorRepository.findById(3L))
                .willReturn(Optional.of(behavior));

        // when & then
        assertThatThrownBy(() ->
                behaviorService.updateBehavior("t1", 3L,
                        BehaviorRequestDto.builder().behavior("x").build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("updateBehavior: 정상 업데이트")
    void updateBehavior_success() {
        // given
        BehaviorRequestDto req = BehaviorRequestDto.builder()
                .behavior("updated").build();
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(behaviorRepository.findById(3L))
                .willReturn(Optional.of(behavior));

        // when
        BehaviorDetailResponseDto res =
                behaviorService.updateBehavior("t1", 3L, req);

        // then: content 필드가 변경되었나 확인
        assertThat(res.getBehavior()).isEqualTo("updated");
    }
}
