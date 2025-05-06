package com.neeis.neeis.domain.attendance.service;

import com.neeis.neeis.domain.attendance.AttendanceRepository;
import com.neeis.neeis.domain.attendance.AttendanceStatus;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceBulkRequestDto;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.req.StudentAttendanceDto;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.counsel.CounselRepository;
import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AttendanceServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Resource
    private AttendanceService attendanceService;
    @Resource
    private ClassroomRepository classroomRepository;
    @Resource
    private ClassroomStudentRepository classroomStudentRepository;
    @Resource
    private StudentRepository studentRepository;
    @Resource
    private TeacherRepository teacherRepository;
    @Resource
    private AttendanceRepository attendanceRepository;

    @Resource
    private BehaviorRepository behaviorRepository;

    @Resource
    private CounselRepository counselRepository;

    @Resource
    private ParentRepository parentRepository;

    private User teacherUser;
    private Teacher teacher;
    private Classroom classroom;
    private List<Student> students = new ArrayList<>();


    @BeforeEach
    void setUp() {
        counselRepository.deleteAllInBatch();
        behaviorRepository.deleteAllInBatch();
        attendanceRepository.deleteAllInBatch();
        classroomStudentRepository.deleteAllInBatch();
        classroomRepository.deleteAllInBatch();
        parentRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        teacherRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 유저, 교사 생성
        teacherUser = User.builder()
                .school("대한중학교")
                .username("teacher1")
                .password("pw")
                .role(Role.TEACHER)
                .build();

        userRepository.save(teacherUser);

        teacher = Teacher.builder()
                .name("김교사")
                .phone("010-1234-5678")
                .email("teacher1@test.com")
                .user(teacherUser)
                .build();
        teacherRepository.save(teacher);

        // 담당 반 생성
        classroom = Classroom.builder()
                .grade(2)
                .classNum(1)
                .year(2025)
                .teacher(teacher)
                .build();
        classroomRepository.save(classroom);

        //학생 10명 생성
        // 학생 5명 생성해서 반 배정
        for (int i = 1; i <= 10; i++) {
            User studentUser = User.builder()
                    .school("대한중학교")
                    .username("student" + i)
                    .password("student" + i)
                    .role(Role.STUDENT)
                    .build();

            userRepository.save(studentUser);

            Student student = Student.builder()
                    .name("학생" + i)
                    .image("student" + i +".img")
                    .gender("남")
                    .ssn("100401"+"000181"+i)
                    .address("인천광역시 연수구 송도동")
                    .phone("010-1234-1234")
                    .admissionDate(LocalDate.now())
                    .user(studentUser)
                    .build();

            studentRepository.save(student);
            classroomStudentRepository.save(ClassroomStudent.builder()
                    .number(i)
                    .student(student)
                    .classroom(classroom)
                    .build());

            students.add(student);
        }

        CustomUserDetails userDetails = new CustomUserDetails(teacherUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("출결 정상 저장 성공")
    void saveOrUpdateAttendancesBulk_success() {
        AttendanceBulkRequestDto requestDto =AttendanceBulkRequestDto.builder()
                .year(2025)
                .month(4)
                .grade(2)
                .classNumber(1)
                .students(
                        List.of(
                                StudentAttendanceDto.builder()
                                        .studentId(students.get(0).getId())
                                        .attendances(
                                                List.of(
                                                        DailyAttendanceDto.builder()
                                                                .date(LocalDate.of(2025,4,1))
                                                                .status(AttendanceStatus.ABSENT)
                                                                .build()
                                                )
                                        )
                                        .build(),
                                StudentAttendanceDto.builder()
                                        .studentId(students.get(1).getId())
                                        .attendances(
                                                List.of(
                                                        DailyAttendanceDto.builder()
                                                                .date(LocalDate.of(2025,4,1))
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                )
                .build();

        attendanceService.saveOrUpdateAttendance(teacherUser.getUsername(), requestDto);

        // 출결이 정상 저장되었는지 검증
        assertThat(attendanceRepository.findByStudentAndDate(students.get(0), LocalDate.of(2025,4,1)))
                .isPresent();
        assertThat(attendanceRepository.findByStudentAndDate(students.get(1), LocalDate.of(2025,4,1)))
                .isPresent();
    }

    @Test
    @DisplayName("반 잘못 입력시 발생")
    void saveOrUpdateAttendancesBulk_wrongClass() {
        // 다른 반을 요청하는 케이스
        AttendanceBulkRequestDto requestDto = AttendanceBulkRequestDto.builder()
                .year(2025)
                .month(4)
                .grade(3) // 다른 학년
                .classNumber(1)
                .students(List.of(
                        StudentAttendanceDto.builder()
                        .studentId(students.getFirst().getId())
                        .attendances(
                                List.of(DailyAttendanceDto.builder()
                                .date(LocalDate.of(2025,4,1))
                                .status(AttendanceStatus.PRESENT)
                                .build())
                        )
                        .build()))
                .build();

        assertThatThrownBy(() -> attendanceService.saveOrUpdateAttendance(teacherUser.getUsername(), requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CLASSROOM_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("다른 반 접근 시 ACCESS_DENIED 발생")
    void saveOrUpdateAttendancesBulk_accessDenied() {

        // 유저, 교사 생성
        User user31 = User.builder()
                .school("대한중학교")
                .username("teacher31")
                .password("pw")
                .role(Role.TEACHER)
                .build();

        userRepository.save(user31);

        Teacher teacher31 = Teacher.builder()
                .name("김교사")
                .phone("010-1234-5678")
                .email("teacher1@test.com")
                .user(user31)
                .build();
        teacherRepository.save(teacher31);

        // 담당 반 생성
        Classroom classroom31 = Classroom.builder()
                .grade(3) // 다른 반 생성
                .classNum(1)
                .year(2025)
                .teacher(teacher31)
                .build();
        classroomRepository.save(classroom31);

        CustomUserDetails userDetails = new CustomUserDetails(user31);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        System.out.println(students.getFirst().getId() + students.get(0).getName());
        System.out.println(classroom31.getTeacher().getName());
        System.out.println();

        // 다른 반을 요청하는 케이스
        AttendanceBulkRequestDto requestDto = AttendanceBulkRequestDto.builder()
                .year(2025)
                .month(4)
                .grade(3)
                .classNumber(1)
                .students(List.of(
                        StudentAttendanceDto.builder()
                                .studentId(students.getFirst().getId())
                                .attendances(
                                        List.of(DailyAttendanceDto.builder()
                                                .date(LocalDate.of(2025,4,1))
                                                .status(AttendanceStatus.PRESENT)
                                                .build())
                                )
                                .build()))
                .build();

        assertThatThrownBy(() -> attendanceService.saveOrUpdateAttendance(userDetails.getUsername(), requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }
}