package com.neeis.neeis.domain.attendance.service;

import com.neeis.neeis.domain.attendance.Attendance;
import com.neeis.neeis.domain.attendance.AttendanceRepository;
import com.neeis.neeis.domain.attendance.AttendanceStatus;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceBulkRequestDto;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.req.StudentAttendanceDto;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.counsel.CounselRepository;
import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.CustomUserDetails;
import org.apache.catalina.Authenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SpringBootTest
@Transactional
class AttendanceServiceTest {

    @MockBean
    private ClassroomStudentRepository classroomStudentRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private ClassroomService classroomService;

    @Autowired
    private AttendanceService attendanceService;

    private User teacherUser;
    private Teacher teacher;
    private Classroom classroom;
    private ClassroomStudent cs;
    private Student s1;

    @BeforeEach
    void setUp() {
        // 유저, 교사 생성
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

        given(teacherService.authenticate("teacher1"))
                .willReturn(teacher);
        given(classroomService.findClassroom(2025, 2, 1, 10L))
                .willReturn(classroom);
        given(classroomStudentRepository.findByClassroom(classroom))
                .willReturn(List.of(cs));

        LocalDate d = LocalDate.of(2025,4,1);
        given(attendanceRepository.findByStudentAndDate(s1, d))
                .willReturn(Optional.empty());


        CustomUserDetails ud = new CustomUserDetails(teacherUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(ud, "", ud.getAuthorities())
        );
    }

    @Test
    @DisplayName("출결 정상 저장 성공")
    void saveOrUpdateAttendancesBulk_success() {
        AttendanceBulkRequestDto requestDto =AttendanceBulkRequestDto.builder()
                .year(2025).month(4).grade(2).classNumber(1)
                .students(List.of(
                        StudentAttendanceDto.builder()
                                        .studentId(1L)
                                        .attendances(List.of(
                                                DailyAttendanceDto.builder()
                                                        .date(LocalDate.of(2025,4,1))
                                                        .status(AttendanceStatus.ABSENT).build()))
                                        .build()))
                .build();

        attendanceService.saveOrUpdateAttendance(teacherUser.getUsername(), requestDto);

        // given
        then(attendanceRepository).should().save(argThat(att ->
                att.getStudent().equals(s1) &&
                        att.getDate().equals(LocalDate.of(2025,4,1)) &&
                        att.getStatus() == AttendanceStatus.ABSENT
        ));
    }

    @Test
    @DisplayName("반 잘못 입력시 발생")
    void saveOrUpdateAttendancesBulk_wrongClass() {

        // given
        given(teacherService.authenticate("teacher1"))
                .willReturn(teacher);
        given(classroomService.findClassroom(2025, 3, 1, 10L))
                .willThrow(new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));


        // 다른 반을 요청하는 케이스
        AttendanceBulkRequestDto requestDto = AttendanceBulkRequestDto.builder()
                .year(2025).month(4).grade(3).classNumber(1) // 다른 학년.classNumber(1)
                .students(List.of(
                        StudentAttendanceDto.builder()
                        .studentId(1L)
                        .attendances(List.of(
                                DailyAttendanceDto.builder()
                                        .date(LocalDate.of(2025,4,1))
                                        .status(AttendanceStatus.PRESENT)
                                        .build())).build())).build();

        assertThatThrownBy(() -> attendanceService.saveOrUpdateAttendance("teacher1", requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CLASSROOM_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("다른 반 접근 시 ACCESS_DENIED 발생")
    void saveOrUpdateAttendancesBulk_accessDenied() {

        // 유저, 교사 생성
        // 1) 권한 없는 교사(otherTeacher) 준비
        Teacher otherTeacher = Teacher.builder()
                .name("다른교사")
                .user(User.builder()
                        .username("teacher31")
                        .role(Role.TEACHER)
                        .build())
                .build();
        // id 세팅
        ReflectionTestUtils.setField(otherTeacher, "id", 99L);

        given(teacherService.authenticate("teacher31"))
                .willReturn(otherTeacher);

        given(classroomService.findClassroom(2025, 3, 1, 99L))
                .willReturn(classroom);


        // 다른 반을 요청하는 케이스
        AttendanceBulkRequestDto requestDto = AttendanceBulkRequestDto.builder()
                .year(2025)
                .month(4)
                .grade(3)
                .classNumber(1)
                .students(List.of(
                        StudentAttendanceDto.builder()
                                .studentId(1L)
                                .attendances(List.of(
                                        DailyAttendanceDto.builder()
                                                .date(LocalDate.of(2025,4,1))
                                                .status(AttendanceStatus.PRESENT)
                                                .build())).build())).build();

        assertThatThrownBy(() -> attendanceService.saveOrUpdateAttendance("teacher31", requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }
}