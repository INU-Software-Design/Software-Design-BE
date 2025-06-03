package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.attendance.service.AttendanceService;
import com.neeis.neeis.domain.behavior.service.BehaviorService;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.service.CounselService;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.student.dto.report.AttendanceReportDto;
import com.neeis.neeis.domain.student.dto.report.BehaviorReportDto;
import com.neeis.neeis.domain.student.dto.report.CounselingReportDto;
import com.neeis.neeis.domain.student.dto.report.GradesReportDto;
import com.neeis.neeis.domain.student.dto.req.StudentReportRequestDto;
import com.neeis.neeis.domain.student.dto.res.*;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.classroom.Classroom;

import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;

import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StudentReportService {

    private final ScoreSummaryService scoreSummaryService;
    private final ClassroomStudentService classroomStudentService;
    private final ClassroomService classroomService;
    private final TeacherService teacherService;
    private final UserService userService;
    private final StudentService studentService; // StudentService 추가

    // TODO: 필요시 추가할 서비스들 (기존 서비스가 있다면 주입)
    private final AttendanceService attendanceService;
    private final CounselService counselService;
    private final BehaviorService behaviorService;
    private final ParentService parentService;

    /**
     * TODO: 학생 보고서 생성 (studentId 기반)
     */
    public StudentReportResponseDto generateStudentReport(StudentReportRequestDto requestDto) {
        log.info("학생 보고서 생성 시작 - 학생ID: {}, 년도: {}, 학기: {}",
                requestDto.getStudentId(), requestDto.getYear(), requestDto.getSemester());

        // 학생 정보 조회
        Student student = studentService.getStudent(requestDto.getStudentId());
        ClassroomStudent classroomStudent = getClassroomStudent(student, requestDto.getYear());

        // 보고서 데이터 생성
        StudentReportResponseDto.StudentReportResponseDtoBuilder builder = StudentReportResponseDto.builder();

        // 1. 학생 정보 (필수) - 기존 DTO 활용
        builder.studentInfo(createStudentInfo(classroomStudent));

        // 2. 성적 정보 (기본 포함)
        if (requestDto.isIncludeGrades()) {
            builder.grades(createGradesReport(classroomStudent, requestDto));
        }

        // 3. 출결 정보 (선택)
        if (requestDto.isIncludeAttendance()) {
            builder.attendance(createAttendanceReport(classroomStudent, requestDto));
        }

        // 4. 상담 정보 (선택)
        if (requestDto.isIncludeCounseling()) {
            builder.counseling(createCounselingReport(classroomStudent, requestDto));
        }

        // 5. 행동평가 정보 (선택)
        if (requestDto.isIncludeBehavior()) {
            builder.behavior(createBehaviorReport(classroomStudent, requestDto));
        }

        // 6. 생성 시간
        builder.generatedAt(LocalDateTime.now());

        StudentReportResponseDto result = builder.build();
        log.info("학생 보고서 생성 완료 - 학생ID: {}", requestDto.getStudentId());

        return result;
    }

    /**
     * TODO: 현재 로그인한 학생의 보고서 생성 (인증된 사용자용)
     */
    public StudentReportResponseDto generateMyStudentReport(String username, StudentReportRequestDto requestDto) {
        log.info("본인 학생 보고서 생성 - 사용자: {}", username);

        // 현재 로그인한 사용자의 학생 정보 조회
        User user = userService.getUser(username);
        Student student = studentService.findByUser(user);

        // 요청 DTO에 학생 ID 설정
        requestDto.setStudentId(student.getId());

        return generateStudentReport(requestDto);
    }

    /**
     * TODO: 교사가 특정 학생 보고서 조회
     */
    public StudentReportResponseDto generateStudentReportByTeacher(String teacherUsername,
                                                                   StudentReportRequestDto requestDto) {
        log.info("교사의 학생 보고서 조회 - 교사: {}, 대상 학생ID: {}", teacherUsername, requestDto.getStudentId());

        // 교사 권한 확인
        teacherService.authenticate(teacherUsername);

        // TODO: 추가 권한 검증 (담당 학급/과목 확인)
        // validateTeacherStudentAccess(teacherUsername, requestDto.getStudentId());

        return generateStudentReport(requestDto);
    }

    /**
     * TODO: 반 전체 학생 보고서 일괄 생성 (비동기 처리 권장)
     */
    @Transactional
    public String generateBulkClassReports(String teacherUsername, int year, int semester,
                                           int grade, int classNum, StudentReportRequestDto template) {
        log.info("반 전체 보고서 일괄 생성 - 교사: {}, {}년 {}학기 {}학년 {}반",
                teacherUsername, year, semester, grade, classNum);

        // 교사 권한 확인
        teacherService.authenticate(teacherUsername);

        // 해당 반 학생 목록 조회
        List<ClassroomStudent> students = classroomStudentService.findByClassroom(
                classroomService.findClassroom(year, grade, classNum));

        // 작업 ID 생성
        String jobId = UUID.randomUUID().toString();

        // TODO: 비동기 처리로 각 학생별 보고서 생성
        // 예: @Async 메서드 호출 또는 Queue 시스템 사용

        log.info("일괄 생성 작업 시작 - 작업 ID: {}, 대상 학생 수: {}", jobId, students.size());

        return jobId;
    }


    /**
     * 특정 연도의 ClassroomStudent 조회
     */
    private ClassroomStudent getClassroomStudent(Student student, int year) {
        return classroomStudentService.findByStudentIdAndYear(student.getId(), year)
                .orElse(classroomStudentService.findByStudentId(student.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND)));
    }

    /**
     * TODO: 학생 정보 생성
     */
    private StudentDetailResDto createStudentInfo(ClassroomStudent classroomStudent) {
        Student student = classroomStudent.getStudent();
        Classroom classroom = classroomStudent.getClassroom();

        // TODO: ParentService 에서 부모 정보 조회
        List<Parent> parents = parentService.getParents(student);

        // 부모 정보에서 아버지/어머니 구분
        Parent father = findFatherFromParents(parents);
        Parent mother = findMotherFromParents(parents);

        return StudentDetailResDto.of(student, father, mother, classroom, classroomStudent);
    }

    /**
     * TODO: 성적 정보 생성
     */
    private GradesReportDto createGradesReport(ClassroomStudent classroomStudent,
                                               StudentReportRequestDto requestDto) {
        log.debug("성적 정보 생성 중 - 학생: {}", classroomStudent.getStudent().getName());

        try {
            Student student = classroomStudent.getStudent();
            Classroom classroom = classroomStudent.getClassroom();

            // ScoreSummaryService를 통해 성적 데이터 조회
            StudentScoreSummaryDto scoreSummary = scoreSummaryService.getStudentSummary(
                    student.getUser().getUsername(),  // 학생 loginId
                    requestDto.getYear(),             // 연도
                    requestDto.getSemester(),         // 학기
                    classroom.getGrade(),             // 학년
                    classroom.getClassNum(),          // 반
                    classroomStudent.getNumber()      // 번호
            );

            // GradesReportDto로 변환
            GradesReportDto grades = GradesReportDto.from(scoreSummary);

            log.debug("성적 정보 생성 완료 - 학생: {}, 과목 수: {}, 평균: {}",
                    student.getName(),
                    grades.getSubjects() != null ? grades.getSubjects().size() : 0,
                    grades.getGpa());

            return grades;

        } catch (Exception e) {
            log.warn("성적 정보 조회 실패, 기본값 반환 - 학생: {}, 오류: {}",
                    classroomStudent.getStudent().getName(), e.getMessage());

            // 성적 정보가 없는 경우 기본 빈 데이터 반환
            return GradesReportDto.builder()
                    .gpa(0.0)
                    .totalSubjects(0)
                    .classRank(0)
                    .totalStudents(0)
                    .subjects(List.of())
                    .build();
        }
    }

    /**
     * 출결 정보 생성
     */
    private AttendanceReportDto createAttendanceReport(ClassroomStudent classroomStudent,
                                                       StudentReportRequestDto requestDto) {
        log.debug("출결 정보 생성 중 - 학생: {}", classroomStudent.getStudent().getName());

        try {
            // TODO: 실제 AttendanceService 구현 후 사용
             StudentAttendanceSummaryDto attendanceSummary = attendanceService.getStudentAttendanceSummary(
                     classroomStudent.getStudent().getUser().getUsername(),
                     requestDto.getYear(),
                     requestDto.getSemester(),
                     classroomStudent.getClassroom().getGrade(),
                     classroomStudent.getClassroom().getClassNum(),
                     classroomStudent.getNumber());

             return AttendanceReportDto.from(attendanceSummary);

        } catch (Exception e) {
            log.warn("출결 정보 조회 실패 - 학생: {}, 오류: {}",
                    classroomStudent.getStudent().getName(), e.getMessage());

            return AttendanceReportDto.builder()
                    .totalDays(0)
                    .presentDays(0)
                    .absentDays(0)
                    .lateDays(0)
                    .earlyLeaveDays(0)
                    .attendanceRate(0.0)
                    .details(List.of())
                    .build();
        }
    }

    /**
     * 상담 정보 생성
     */
    private CounselingReportDto createCounselingReport(ClassroomStudent classroomStudent,
                                                       StudentReportRequestDto requestDto) {
        log.debug("상담 정보 생성 중 - 학생: {}", classroomStudent.getStudent().getName());

        try {
            // TODO: 실제 CounselService 구현 후 사용

             List<CounselDetailDto> counsels = counselService.getCounsels(
                    classroomStudent.getStudent().getUser().getUsername(),
                     classroomStudent.getStudent().getId());

             return CounselingReportDto.from(counsels);


        } catch (Exception e) {
            log.warn("상담 정보 조회 실패 - 학생: {}, 오류: {}",
                    classroomStudent.getStudent().getName(), e.getMessage());

            return CounselingReportDto.builder()
                    .totalSessions(0)
                    .records(List.of())
                    .build();
        }
    }

    /**
     * 행동평가 정보 생성
     */
    private BehaviorReportDto createBehaviorReport(ClassroomStudent classroomStudent,
                                                   StudentReportRequestDto requestDto) {
        log.debug("행동평가 정보 생성 중 - 학생: {}", classroomStudent.getStudent().getName());

        try {
            // TODO: 실제 BehaviorService 구현 후 사용
             BehaviorDetailResponseDto behavior = behaviorService.getBehavior(
                     classroomStudent.getStudent().getUser().getUsername(),
                     requestDto.getYear(),
                     classroomStudent.getClassroom().getGrade(),
                     classroomStudent.getClassroom().getClassNum(),
                     classroomStudent.getNumber());

             return BehaviorReportDto.from(behavior);


        } catch (Exception e) {
            log.warn("행동평가 정보 조회 실패 - 학생: {}, 오류: {}",
                    classroomStudent.getStudent().getName(), e.getMessage());

            return BehaviorReportDto.builder()
                    .behaviorGrade("")
                    .comprehensiveOpinion("")
                    .build();
        }
    }

    /**
     * 교사가 해당 학생에 대한 접근 권한이 있는지 확인 (TODO: 구현 필요)
     */
    private void validateTeacherStudentAccess(String teacherUsername, Long studentId) {
        // TODO: 교사가 해당 학생의 담당교사이거나 과목교사인지 확인
        // 예: 담당 학급 확인, 담당 과목 확인 등
    }

    /**
     * 부모 리스트에서 아버지 찾기
     */
    private Parent findFatherFromParents(List<Parent> parents) {
        if (parents == null || parents.isEmpty()) {
            return null;
        }

        return parents.stream()
                .filter(parent -> {
                    String relationship = parent.getRelationShip();
                    return relationship != null && (
                            "부".equals(relationship) ||
                                    "아버지".equals(relationship) ||
                                    "FATHER".equalsIgnoreCase(relationship) ||
                                    "DAD".equalsIgnoreCase(relationship)
                    );
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 부모 리스트에서 어머니 찾기
     */
    private Parent findMotherFromParents(List<Parent> parents) {
        if (parents == null || parents.isEmpty()) {
            return null;
        }

        return parents.stream()
                .filter(parent -> {
                    String relationship = parent.getRelationShip();
                    return relationship != null && (
                            "모".equals(relationship) ||
                                    "어머니".equals(relationship) ||
                                    "MOTHER".equalsIgnoreCase(relationship) ||
                                    "MOM".equalsIgnoreCase(relationship)
                    );
                })
                .findFirst()
                .orElse(null);
    }
}