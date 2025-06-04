package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.attendance.service.AttendanceService;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.service.BehaviorService;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.service.CounselService;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.dto.req.StudentReportRequestDto;
import com.neeis.neeis.domain.student.dto.res.StudentReportResponseDto;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * StudentReportService 테스트
 *
 * @ExtendWith(MockitoExtension.class)
 * - Mockito를 사용하여 Mock 객체를 자동으로 생성하고 주입해주는 어노테이션
 * - Spring 컨텍스트 없이 순수 단위 테스트를 위해 사용
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentReportService 테스트")
class StudentReportServiceTest {

    // @InjectMocks: 실제 테스트할 서비스 객체 (Mock들이 주입됨)
    @InjectMocks
    private StudentReportService studentReportService;

    // @Mock: 의존성으로 주입될 Mock 객체들
    @Mock private ScoreSummaryService scoreSummaryService;
    @Mock private ClassroomStudentService classroomStudentService;
    @Mock private TeacherService teacherService;
    @Mock private UserService userService;
    @Mock private StudentService studentService;
    @Mock private AttendanceService attendanceService;
    @Mock private CounselService counselService;
    @Mock private BehaviorService behaviorService;
    @Mock private ParentService parentService;

    // 테스트에서 공통으로 사용할 테스트 데이터
    private User testUser;
    private Student testStudent;
    private Classroom testClassroom;
    private ClassroomStudent testClassroomStudent;
    private Parent testFather;
    private Parent testMother;
    private Teacher testTeacher;
    private StudentReportRequestDto testRequestDto;

    /**
     * @BeforeEach: 각 테스트 메서드 실행 전에 실행되는 메서드
     * 테스트 데이터를 초기화하여 테스트 간 독립성을 보장
     */
    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        testUser = User.builder()
                .username("student123")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 테스트용 Teacher 객체 생성
        testTeacher = Teacher.builder()
                .name("김선생님")
                .build();
        ReflectionTestUtils.setField(testTeacher, "id", 1L);

        // 테스트용 Classroom 객체 생성
        testClassroom = Classroom.builder()
                .grade(2)
                .classNum(3)
                .year(2024)
                .teacher(testTeacher)
                .build();
        ReflectionTestUtils.setField(testClassroom, "id", 1L);

        // 테스트용 Student 객체 생성
        testStudent = Student.builder()
                .name("김학생")
                .user(testUser)
                .ssn("050101-3123456")
                .gender("M")
                .address("서울시 강남구")
                .phone("010-1234-5678")
                .admissionDate(LocalDate.of(2023, 3, 1))
                .image("student_image.jpg")
                .build();
        ReflectionTestUtils.setField(testStudent, "id", 1L);

        // 테스트용 ClassroomStudent 객체 생성
        testClassroomStudent = ClassroomStudent.builder()
                .student(testStudent)
                .classroom(testClassroom)
                .number(15)
                .build();
        ReflectionTestUtils.setField(testClassroomStudent, "id", 1L);

        // 테스트용 Parent 객체들 생성
        testFather = Parent.builder()
                .student(testStudent)
                .name("김아버지")
                .phone("010-1111-2222")
                .relationShip("아버지")
                .build();
        ReflectionTestUtils.setField(testFather, "id", 1L);

        testMother = Parent.builder()
                .student(testStudent)
                .name("김어머니")
                .phone("010-3333-4444")
                .relationShip("어머니")
                .build();
        ReflectionTestUtils.setField(testMother, "id", 2L);

        // 테스트용 Request DTO 생성
        testRequestDto = StudentReportRequestDto.builder()
                .studentId(1L)
                .year(2024)
                .semester(1)
                .includeGrades(true)
                .includeAttendance(true)
                .includeCounseling(true)
                .includeBehavior(true)
                .includeFeedback(true)
                .build();
    }

    /**
     * 기본 Mock 설정 헬퍼 메서드
     * 모든 테스트에서 공통으로 필요한 Mock 설정
     */
    private void setupBasicMocks() {
        given(studentService.getStudent(1L)).willReturn(testStudent);
        given(classroomStudentService.findByStudentIdAndYear(1L, 2024))
                .willReturn(Optional.of(testClassroomStudent));
        // fallback을 위한 findByStudentId Mock 설정 추가
        given(classroomStudentService.findByStudentId(1L))
                .willReturn(Optional.of(testClassroomStudent));
        given(parentService.getParents(testStudent))
                .willReturn(List.of(testFather, testMother));
    }

    /**
     * 서비스별 Mock 데이터 설정 헬퍼 메서드
     */
    private void setupServiceMocks() {
        // 성적 정보 Mock
        StudentScoreSummaryDto mockScoreSummary = createMockScoreSummary();
        given(scoreSummaryService.getStudentSummary(
                eq("student123"), eq(2024), eq(1), eq(2), eq(3), eq(15)))
                .willReturn(mockScoreSummary);

        // 출결 정보 Mock
        StudentAttendanceSummaryDto mockAttendance = createMockAttendanceSummary();
        given(attendanceService.getStudentAttendanceSummary(
                eq("student123"), eq(2024), eq(1), eq(2), eq(3), eq(15)))
                .willReturn(mockAttendance);

        // 상담 정보 Mock
        List<CounselDetailDto> mockCounsels = createMockCounsels();
        given(counselService.getCounsels("student123", 1L))
                .willReturn(mockCounsels);

        // 행동평가 정보 Mock
        BehaviorDetailResponseDto mockBehavior = createMockBehavior();
        given(behaviorService.getBehavior(
                eq("student123"), eq(2024), eq(2), eq(3), eq(15)))
                .willReturn(mockBehavior);

        // 피드백 관련 Mock 추가
        given(scoreSummaryService.getSubjectFeedbacks(
                eq("student123"), eq(2024), eq(1), eq(2), eq(3), eq(15)))
                .willReturn(List.of());

        // 출결 피드백 Mock 추가
        AttendanceFeedbackResDto mockAttendanceFeedback = AttendanceFeedbackResDto.builder()
                .feedbackId(1L)
                .feedback("출결 상태가 양호합니다.")
                .build();
        given(attendanceService.getFeedback(
                eq("student123"), eq(2024), eq(2), eq(3), eq(15)))
                .willReturn(mockAttendanceFeedback);
    }

    @Nested
    @DisplayName("일반 학생 보고서 생성 테스트")
    class GenerateStudentReportTest {

        @Test
        @DisplayName("모든 정보를 포함한 학생 보고서 생성 성공")
        void generateStudentReport_success_with_all_info() {
            // given - 테스트 시나리오를 위한 Mock 설정
            setupBasicMocks();
            setupServiceMocks();

            // when - 실제 테스트할 메서드 호출
            StudentReportResponseDto result = studentReportService.generateStudentReport(testRequestDto);

            // then - 결과 검증
            assertThat(result).isNotNull();
            assertThat(result.getStudentInfo()).isNotNull();
            assertThat(result.getStudentInfo().getName()).isEqualTo("김학생");
            assertThat(result.getStudentInfo().getGrade()).isEqualTo(2);
            assertThat(result.getStudentInfo().getClassroom()).isEqualTo(3);
            assertThat(result.getStudentInfo().getNumber()).isEqualTo(15);
            assertThat(result.getStudentInfo().getFatherName()).isEqualTo("김아버지");
            assertThat(result.getStudentInfo().getMotherName()).isEqualTo("김어머니");

            // 성적 정보 상세 검증
            assertThat(result.getGrades()).isNotNull();
            assertThat(result.getGrades().getSubjects()).hasSize(3);
            assertThat(result.getGrades().getSubjects().get(0).getSubjectName()).isEqualTo("수학");

            // 출결 정보 상세 검증
            assertThat(result.getAttendance()).isNotNull();
            assertThat(result.getAttendance().getTotalDays()).isEqualTo(180);

            // 상담 정보 상세 검증
            assertThat(result.getCounseling()).isNotNull();
            assertThat(result.getCounseling().getTotalSessions()).isEqualTo(2);

            // 행동평가 정보 상세 검증
            assertThat(result.getBehavior()).isNotNull();

            // Mock 메서드들이 예상대로 호출되었는지 검증
            verify(studentService).getStudent(1L);
            verify(classroomStudentService).findByStudentIdAndYear(1L, 2024);
            verify(parentService).getParents(testStudent);
        }

        @Test
        @DisplayName("선택 정보 제외한 기본 보고서 생성")
        void generateStudentReport_success_basic_only() {
            // given
            StudentReportRequestDto basicRequestDto = StudentReportRequestDto.builder()
                    .studentId(1L)
                    .year(2024)
                    .semester(1)
                    .includeGrades(true)  // 성적만 포함
                    .includeAttendance(false)
                    .includeCounseling(false)
                    .includeBehavior(false)
                    .includeFeedback(false)
                    .build();

            setupBasicMocks();

            // 성적 정보만 Mock 설정
            StudentScoreSummaryDto mockScoreSummary = createMockScoreSummary();
            given(scoreSummaryService.getStudentSummary(
                    eq("student123"), eq(2024), eq(1), eq(2), eq(3), eq(15)))
                    .willReturn(mockScoreSummary);

            // when
            StudentReportResponseDto result = studentReportService.generateStudentReport(basicRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStudentInfo()).isNotNull();
            assertThat(result.getGrades()).isNotNull();

            // 선택하지 않은 정보들은 null이어야 함
            assertThat(result.getAttendance()).isNull();
            assertThat(result.getCounseling()).isNull();
            assertThat(result.getBehavior()).isNull();

            // 선택하지 않은 서비스들은 호출되지 않아야 함
            verify(attendanceService, never()).getStudentAttendanceSummary(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
            verify(counselService, never()).getCounsels(anyString(), anyLong());
            verify(behaviorService, never()).getBehavior(anyString(), anyInt(), anyInt(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("존재하지 않는 학생 ID로 보고서 생성 시 예외 발생")
        void generateStudentReport_student_not_found() {
            // given
            given(studentService.getStudent(999L))
                    .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            StudentReportRequestDto invalidRequestDto = StudentReportRequestDto.builder()
                    .studentId(999L)
                    .year(2024)
                    .semester(1)
                    .build();

            // when & then
            assertThatThrownBy(() -> studentReportService.generateStudentReport(invalidRequestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("ClassroomStudent 정보가 없을 때 기본값으로 처리")
        void generateStudentReport_classroom_student_fallback() {
            // given
            ClassroomStudent defaultClassroomStudent = ClassroomStudent.builder()
                    .student(testStudent)
                    .classroom(testClassroom)
                    .number(1)
                    .build();
            ReflectionTestUtils.setField(defaultClassroomStudent, "id", 2L);

            given(studentService.getStudent(1L)).willReturn(testStudent);
            // 해당 연도의 ClassroomStudent가 없는 경우
            given(classroomStudentService.findByStudentIdAndYear(1L, 2024))
                    .willReturn(Optional.empty());
            // 기본 ClassroomStudent 반환
            given(classroomStudentService.findByStudentId(1L))
                    .willReturn(Optional.of(defaultClassroomStudent));
            given(parentService.getParents(testStudent))
                    .willReturn(List.of(testFather, testMother));

            // when
            StudentReportResponseDto result = studentReportService.generateStudentReport(testRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStudentInfo()).isNotNull();

            // 메서드 호출 확인
            verify(classroomStudentService).findByStudentIdAndYear(1L, 2024);
            verify(classroomStudentService).findByStudentId(1L);
        }
    }

    @Nested
    @DisplayName("본인 학생 보고서 생성 테스트")
    class GenerateMyStudentReportTest {

        @Test
        @DisplayName("학생이 본인 보고서 조회 성공")
        void generateMyStudentReport_success() {
            // given
            given(userService.getUser("student123")).willReturn(testUser);
            given(studentService.findByUser(testUser)).willReturn(testStudent);
            setupBasicMocks();

            // when
            StudentReportResponseDto result = studentReportService
                    .generateMyStudentReport("student123", testRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStudentInfo()).isNotNull();
            assertThat(result.getStudentInfo().getName()).isEqualTo("김학생");

            verify(userService).getUser("student123");
            verify(studentService).findByUser(testUser);
        }

        @Test
        @DisplayName("다른 학생의 보고서 조회 시도 시 접근 거부")
        void generateMyStudentReport_access_denied() {
            // given
            given(userService.getUser("student123")).willReturn(testUser);
            given(studentService.findByUser(testUser)).willReturn(testStudent);

            // 다른 학생의 ID로 요청
            StudentReportRequestDto otherStudentRequest = StudentReportRequestDto.builder()
                    .studentId(999L)  // 본인 ID(1L)와 다른 ID
                    .year(2024)
                    .semester(1)
                    .build();

            // when & then
            assertThatThrownBy(() -> studentReportService
                    .generateMyStudentReport("student123", otherStudentRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("교사용 학생 보고서 생성 테스트")
    class GenerateStudentReportByTeacherTest {

        @Test
        @DisplayName("교사가 학생 보고서 조회 성공")
        void generateStudentReportByTeacher_success() {
            // given
            String teacherUsername = "teacher123";

            // 교사 인증 성공으로 Mock 설정
            given(teacherService.authenticate(teacherUsername)).willReturn(testTeacher);
            setupBasicMocks();

            // when
            StudentReportResponseDto result = studentReportService
                    .generateStudentReportByTeacher(teacherUsername, testRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStudentInfo()).isNotNull();

            verify(teacherService).authenticate(teacherUsername);
            verify(studentService).getStudent(1L);
        }

        @Test
        @DisplayName("인증되지 않은 교사의 보고서 조회 시도")
        void generateStudentReportByTeacher_authentication_failed() {
            // given
            String invalidTeacher = "invalid_teacher";

            // 교사 인증 실패로 Mock 설정
            given(teacherService.authenticate(invalidTeacher))
                    .willThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

            // when & then
            assertThatThrownBy(() -> studentReportService
                    .generateStudentReportByTeacher(invalidTeacher, testRequestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HANDLE_ACCESS_DENIED);

            verify(teacherService).authenticate(invalidTeacher);
            // 인증 실패 시 다른 서비스는 호출되지 않아야 함
            verify(studentService, never()).getStudent(anyLong());
        }
    }

    @Nested
    @DisplayName("예외 상황 처리 테스트")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("성적 정보 조회 실패 시 기본값 반환")
        void handleScoreServiceException() {
            // given
            setupBasicMocks();

            // 성적 서비스에서 예외 발생
            given(scoreSummaryService.getStudentSummary(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
                    .willThrow(new RuntimeException("성적 조회 오류"));

            // when
            StudentReportResponseDto result = studentReportService.generateStudentReport(testRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStudentInfo()).isNotNull();
            assertThat(result.getGrades()).isNotNull();
        }

        @Test
        @DisplayName("출결 정보 조회 실패 시 기본값 반환")
        void handleAttendanceServiceException() {
            // given
            setupBasicMocks();

            // 출결 서비스에서 예외 발생
            given(attendanceService.getStudentAttendanceSummary(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
                    .willThrow(new RuntimeException("출결 조회 오류"));

            // when
            StudentReportResponseDto result = studentReportService.generateStudentReport(testRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAttendance()).isNotNull();
        }

        @Test
        @DisplayName("상담 정보 조회 실패 시 기본값 반환")
        void handleCounselServiceException() {
            // given
            setupBasicMocks();

            // 상담 서비스에서 예외 발생
            given(counselService.getCounsels(anyString(), anyLong()))
                    .willThrow(new RuntimeException("상담 조회 오류"));

            // when
            StudentReportResponseDto result = studentReportService.generateStudentReport(testRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCounseling()).isNotNull();
        }

        @Test
        @DisplayName("행동평가 정보 조회 실패 시 기본값 반환")
        void handleBehaviorServiceException() {
            // given
            setupBasicMocks();

            // 행동평가 서비스에서 예외 발생
            given(behaviorService.getBehavior(anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
                    .willThrow(new RuntimeException("행동평가 조회 오류"));

            // when
            StudentReportResponseDto result = studentReportService.generateStudentReport(testRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBehavior()).isNotNull();
        }
    }

    // ========== 테스트 헬퍼 메서드들 ==========

    /**
     * 테스트용 Mock 성적 데이터 생성
     */
    private StudentScoreSummaryDto createMockScoreSummary() {
        List<SubjectScoreDto> subjects = List.of(
                SubjectScoreDto.builder()
                        .subjectName("수학")
                        .weightedTotal(85.0)
                        .rank(5)
                        .totalStudentCount(30)
                        .achievementLevel("우수")
                        .build(),
                SubjectScoreDto.builder()
                        .subjectName("국어")
                        .weightedTotal(88.0)
                        .rank(3)
                        .totalStudentCount(30)
                        .achievementLevel("우수")
                        .build(),
                SubjectScoreDto.builder()
                        .subjectName("영어")
                        .weightedTotal(82.0)
                        .rank(7)
                        .totalStudentCount(30)
                        .achievementLevel("보통")
                        .build()
        );

        return StudentScoreSummaryDto.builder()
                .number(15)
                .studentName("김학생")
                .subjects(subjects)
                .build();
    }

    /**
     * 테스트용 Mock 출결 데이터 생성
     */
    private StudentAttendanceSummaryDto createMockAttendanceSummary() {
        return StudentAttendanceSummaryDto.builder()
                .studentId(1L)
                .studentName("김학생")
                .totalSchoolDays(180)
                .presentDays(175)
                .absentDays(3)
                .lateDays(2)
                .leaveEarlyDays(0)
                .build();
    }

    /**
     * 테스트용 Mock 상담 데이터 생성
     */
    private List<CounselDetailDto> createMockCounsels() {
        CounselDetailDto counsel1 = CounselDetailDto.builder()
                .id(1L)
                .category(CounselCategory.CAREER)
                .content("진로에 대한 고민 상담")
                .nextPlan("진로 탐색 활동 계획")
                .dateTime(LocalDate.now().minusDays(10))
                .teacher("김선생님")
                .isPublic(true)
                .build();

        CounselDetailDto counsel2 = CounselDetailDto.builder()
                .id(2L)
                .category(CounselCategory.ACADEMIC)
                .content("학습 방법 상담")
                .nextPlan("학습 계획 수립")
                .dateTime(LocalDate.now().minusDays(5))
                .teacher("이선생님")
                .isPublic(false)
                .build();

        return List.of(counsel1, counsel2);
    }

    /**
     * 테스트용 Mock 행동평가 데이터 생성
     */
    private BehaviorDetailResponseDto createMockBehavior() {
        return BehaviorDetailResponseDto.builder()
                .behaviorId(1L)
                .behavior("우수")
                .generalComment("성실하고 적극적인 학습 태도를 보임")
                .build();
    }
}