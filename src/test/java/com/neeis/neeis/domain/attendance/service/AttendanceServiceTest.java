package com.neeis.neeis.domain.attendance.service;

import com.neeis.neeis.domain.attendance.*;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceBulkRequestDto;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceFeedbackReqDto;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.req.StudentAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.counsel.CounselRepository;
import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.semester.Semester;
import com.neeis.neeis.domain.semester.SemesterRepository;
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

import java.sql.Ref;
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

    @MockBean private ClassroomStudentRepository classroomStudentRepository;
    @MockBean private AttendanceRepository attendanceRepository;
    @MockBean private SemesterRepository semesterRepository;
    @MockBean private AttendanceFeedbackRepository feedbackRepository;

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

    @Test
    @DisplayName("학급 출결 조회 성공")
    void getAttendances_success() {
        // given
        int year = 2025, month = 4;
        // 날짜별 출결 2건 준비
        Attendance a1 = Attendance.builder()
                .student(s1)
                .date(LocalDate.of(year,month,2))
                .status(AttendanceStatus.ABSENT)
                .build();

        Attendance a2 = Attendance.builder()
                .student(s1)
                .date(LocalDate.of(year,month,5))
                .status(AttendanceStatus.LATE)
                .build();

        given(attendanceRepository.findByStudentAndDateBetween(
                eq(s1), any(LocalDate.class), any(LocalDate.class)
        )).willReturn(List.of(a1, a2));

        // when
        List<StudentAttendanceResDto> result =
                attendanceService.getAttendances("teacher1", year, 2, 1, month);

        // then
        assertThat(result).hasSize(1);
        StudentAttendanceResDto dto = result.get(0);
        assertThat(dto.getStudentId()).isEqualTo(s1.getId());
        assertThat(dto.getAttendances())
                .extracting(DailyAttendanceDto::getStatus)
                .containsExactlyInAnyOrder(AttendanceStatus.ABSENT, AttendanceStatus.LATE);
    }

    @Test
    @DisplayName("개별 학생 월별 출결 조회 - 성공")
    void getStudentMonthlyAttendance_success() {
        int year = 2025, month = 3, number = 5;
        given(classroomStudentRepository.findByClassroomAndNumber(classroom, number))
                .willReturn(Optional.of(cs));

        Attendance a = Attendance.builder()
                .student(s1)
                .status(AttendanceStatus.EARLY)
                .date(LocalDate.of(year,month, 10))
                .build();

        given(attendanceRepository.findByStudentAndDateBetween(
                eq(s1), any(LocalDate.class), any(LocalDate.class)
        )).willReturn(List.of(a));

        StudentAttendanceResDto dto = attendanceService.getStudentMonthlyAttendance(
                "teacher1", year, 2, 1, number, month);

        assertThat(dto.getStudentName()).isEqualTo(s1.getName());
        assertThat(dto.getAttendances()).hasSize(1);
        assertThat(dto.getAttendances().get(0).getStatus()).isEqualTo(AttendanceStatus.EARLY);
    }

    @Test
    @DisplayName("학생 출결 통계 조회 - 성공")
    void getStudentAttendanceSummary_success() {
        int year = 2025, semester = 1, month =1 , number = 1;
        given(classroomStudentRepository.findByClassroomAndNumber(classroom, number))
                .willReturn(Optional.of(cs));
        // 학기 기간
        Semester sem = Semester.builder()
                .year(2025)
                .startDate(LocalDate.of(2025,1,1))
                .endDate(LocalDate.of(2025,1,31))
                .semester(1)
                .build();

        given(semesterRepository.findByYearAndSemester(year, semester))
                .willReturn(Optional.of(sem));

        // 출결 데이터: 1일 absent, 2일 late, 3일 early
        given(attendanceRepository.findByStudentAndDateBetween(
                eq(s1), any(LocalDate.class), any(LocalDate.class)
        )).willReturn(List.of(
                Attendance.builder()
                        .student(s1)
                        .status(AttendanceStatus.ABSENT)
                        .date(LocalDate.of(year, month, 1))
                        .build(),
                Attendance.builder()
                        .student(s1)
                        .status(AttendanceStatus.LATE)
                        .date(LocalDate.of(year,month, 2))
                        .build(),
                Attendance.builder()
                        .student(s1)
                        .status(AttendanceStatus.EARLY)
                        .date(LocalDate.of(year,month, 3))
                        .build()
                ));

        StudentAttendanceSummaryDto summary = attendanceService.getStudentAttendanceSummary(
                "teacher1", year, semester, 2, 1, number);

        // totalSchoolDays: 평일만 1월1~31 => 23일
        assertThat(summary.getAbsentDays()).isEqualTo(1);
        assertThat(summary.getLateDays()).isEqualTo(1);
        assertThat(summary.getLeaveEarlyDays()).isEqualTo(1);
        assertThat(summary.getPresentDays())
                .isEqualTo(summary.getTotalSchoolDays() - 3);
    }

    @Test
    @DisplayName("피드백 저장 - 성공")
    void saveFeedback_success() {
        int year = 2025, number = 1;
        given(classroomStudentRepository.findByClassroomAndNumber(classroom, number))
                .willReturn(Optional.ofNullable(cs));

        AttendanceFeedbackReqDto req = AttendanceFeedbackReqDto.builder()
                .feedback("OK").build();

        AttendanceFeedback saved = AttendanceFeedback.builder()
                .classroomStudent(cs).feedback("OK").build();

        ReflectionTestUtils.setField( saved, "id", 1L);

        given(feedbackRepository.save(any(AttendanceFeedback.class)))
                .willReturn(saved);

        AttendanceFeedbackResDto dto = attendanceService.saveFeedback(
                "teacher1", year, 2, 1, number, req);

        assertThat(dto.getFeedbackId()).isEqualTo(1L);
        assertThat(dto.getFeedback()).isEqualTo("OK");
    }

    @Test
    @DisplayName("피드백 수정 정상 동작")
    void updateFeedback_success() {
        // given
        Long fbId = 123L;
        AttendanceFeedback existing = AttendanceFeedback.builder()
                .classroomStudent(cs)
                .feedback("old").build();

        ReflectionTestUtils.setField(existing, "id", fbId);

        given(feedbackRepository.findById(fbId))
                .willReturn(Optional.of(existing));
        AttendanceFeedbackReqDto req = AttendanceFeedbackReqDto.builder()
                .feedback("updated").build();

        // when
        AttendanceFeedbackResDto dto = attendanceService.updateFeedback(
                "teacher1", fbId, req);

        // then
        assertThat(dto.getFeedbackId()).isEqualTo(fbId);
        assertThat(dto.getFeedback()).isEqualTo("updated");
    }

    @Test
    @DisplayName("피드백 수정 권한 없는 경우 예외")
    void updateFeedback_accessDenied() {
        Long fbId = 1L;
        given(feedbackRepository.findById(fbId))
                .willReturn(Optional.of(
                        AttendanceFeedback.builder()
                                .classroomStudent(cs).feedback("X").build()
                ));
        // authenticate return 다른 교사
        Teacher other = Teacher.builder().build();

        ReflectionTestUtils.setField(other, "id", 2L);

        given(teacherService.authenticate("other")).willReturn(other);

        assertThatThrownBy(() ->
                attendanceService.updateFeedback("other", fbId,
                        AttendanceFeedbackReqDto.builder().feedback("new").build())
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("피드백 조회 성공 동작")
    void getFeedback_success() {
        int year = 2025, number = 1;
        // 학생 매핑 정상
        given(classroomStudentRepository.findByClassroomAndNumber(classroom, number))
                .willReturn(Optional.of(cs));
        // 피드백 존재
        Long fbId = 77L;
        AttendanceFeedback feedback = AttendanceFeedback.builder()
                .classroomStudent(cs)
                .feedback("Good work").build();
        ReflectionTestUtils.setField(feedback, "id", fbId);

        given(feedbackRepository.findByClassroomStudent(cs))
                .willReturn(Optional.of(feedback));

        AttendanceFeedbackResDto dto = attendanceService.getFeedback(
                "teacher1", year, 2, 1, number);

        assertThat(dto.getFeedbackId()).isEqualTo(fbId);
        assertThat(dto.getFeedback()).isEqualTo("Good work");
    }


    @Test
    @DisplayName("피드백 조회 데이터 없으면 예외")
    void getFeedback_notFound() {
        int year=2025, number=1;
        given(classroomStudentRepository.findByClassroomAndNumber(classroom, number))
                .willReturn(Optional.of(cs));
        given(feedbackRepository.findByClassroomStudent(cs))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                attendanceService.getFeedback("teacher1", year, 2, 1, number)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DATA_NOT_FOUND.getMessage());
    }
}