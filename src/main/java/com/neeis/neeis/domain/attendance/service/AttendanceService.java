package com.neeis.neeis.domain.attendance.service;

import com.neeis.neeis.domain.attendance.*;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceBulkRequestDto;
import com.neeis.neeis.domain.attendance.dto.req.AttendanceFeedbackReqDto;
import com.neeis.neeis.domain.attendance.dto.req.DailyAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.req.StudentAttendanceDto;
import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceResDto;
import com.neeis.neeis.domain.attendance.dto.res.StudentAttendanceSummaryDto;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.semester.Semester;
import com.neeis.neeis.domain.semester.SemesterRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendAttendanceFeedbackFcmEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.neeis.neeis.global.exception.ErrorCode.CLASSROOM_NOT_FOUND;
import static com.neeis.neeis.global.exception.ErrorCode.HANDLE_ACCESS_DENIED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserService userService;
    private final TeacherService teacherService;
    private final ClassroomService classroomService;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final SemesterRepository semesterRepository;
    private final AttendanceFeedbackRepository feedbackRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final ParentService parentService;

    // [교사] - 출결 저장 및 업데이트
    @Transactional
    public void saveOrUpdateAttendance(String username, AttendanceBulkRequestDto requestDto) {

        Teacher teacher = teacherService.authenticate(username);
        Classroom classroom = classroomService.findClassroom(requestDto.getYear(), requestDto.getGrade(), requestDto.getClassNumber(), teacher.getId());

        if(classroom.getTeacher() != teacher) {
            throw new CustomException(HANDLE_ACCESS_DENIED);
        }

        // 담당 학생들 확인
        List<ClassroomStudent> classroomStudentList = classroomStudentRepository.findByClassroom(classroom);
        Map<Long, Student> studentMap = classroomStudentList.stream()
                .collect(Collectors.toMap(
                        cs -> cs.getStudent().getId(),
                        ClassroomStudent::getStudent));

        // 학생과 DB 확인
        for (StudentAttendanceDto dto : requestDto.getStudents()) {
            if (!studentMap.containsKey(dto.getStudentId())) {
                throw new CustomException(HANDLE_ACCESS_DENIED);
            }
        }

        // 출결 저장
        // 입력받은 연도 + 월 -> 마지막 일 구하기
        YearMonth yearMonth = YearMonth.of(requestDto.getYear(), requestDto.getMonth());
        int lastDay = yearMonth.lengthOfMonth(); // 해당 월의 마지막 일

        for (StudentAttendanceDto studentDto : requestDto.getStudents()) {
            Student student = studentMap.get(studentDto.getStudentId());

            // 날짜별 출결사항 매핑
            Map<LocalDate, AttendanceStatus> statusMap = studentDto.getAttendances().stream()
                    .collect(Collectors.toMap(DailyAttendanceDto::getDate, DailyAttendanceDto::getStatus));

            // 한달치 모든 날짜에 대해 저장
            for (int day = 1; day <= lastDay; day++) {
                LocalDate date = LocalDate.of(requestDto.getYear(), requestDto.getMonth(), day);
                AttendanceStatus status = statusMap.getOrDefault(date, AttendanceStatus.PRESENT); // 없으면 출석으로

                // 특이사항만 저장
                if (status != AttendanceStatus.PRESENT) {
                    attendanceRepository.findByStudentAndDate(student, date)
                            .ifPresentOrElse(
                                    existing -> {
                                        if (existing.getStatus() != status) {
                                            existing.updateStatus(status);
                                            attendanceRepository.save(existing);
                                        }
                                    },
                                    () -> {
                                        Attendance newAttendance = Attendance.builder()
                                                .student(student)
                                                .date(date)
                                                .status(status)
                                                .build();
                                        attendanceRepository.save(newAttendance);
                                    }
                            );
                }
            }
        }
    }

    // [교사권한] 학급 학생들 월별 조회
    public List<StudentAttendanceResDto> getAttendances(String username, int year, int grade, int classNum, int month) {
        teacherService.authenticate(username);
        Classroom classroom = classroomService.findClassroom(year, grade, classNum);

        List<ClassroomStudent> classroomStudentList = classroomStudentRepository.findByClassroom(classroom);

        return classroomStudentList.stream()
                .map(cs -> {
                    Student student = cs.getStudent();
                    List<Attendance> attendances = attendanceRepository
                            .findByStudentAndDateBetween(student,
                                    LocalDate.of(year, month, 1),
                                    LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth()))
                            .stream()
                            .filter(a -> a.getStatus() != AttendanceStatus.PRESENT) // 출석은 제외
                            .toList();

                    List<DailyAttendanceDto> dailyAttendanceDtos = attendances.stream()
                            .map(a -> DailyAttendanceDto.builder()
                                    .date(a.getDate())
                                    .status(a.getStatus())
                                    .build())
                            .toList();

                    return StudentAttendanceResDto.toDto( cs,
                            attendances.isEmpty() ? Attendance.builder().student(student).build() : attendances.getFirst(),
                            dailyAttendanceDtos
                    );
                })
                .collect(Collectors.toList());
    }

    // [교사 / 학생] 개인 학생 월별 조회
    public StudentAttendanceResDto getStudentMonthlyAttendance(String username, int year, int grade, int classNum, int number, int month) {
        ClassroomStudent classroomStudent = checkValidate(username, year, grade, classNum, number);

        Student student = classroomStudent.getStudent();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Attendance> attendances = attendanceRepository.findByStudentAndDateBetween(student, start, end);

        List<DailyAttendanceDto> dailyAttendanceDtoList = attendances.stream()
                .filter(a -> a.getStatus() != AttendanceStatus.PRESENT)
                .map(a -> DailyAttendanceDto.builder()
                        .date(a.getDate())
                        .status(a.getStatus())
                        .build())
                .toList();

        return StudentAttendanceResDto.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .attendances(dailyAttendanceDtoList)
                .build();
    }


    // [교사 / 학생] 출결 통계 조회
    public StudentAttendanceSummaryDto getStudentAttendanceSummary(String username, int year, int semester, int grade, int classNum, int number) {
        ClassroomStudent classroomStudent = checkValidate(username, year, grade, classNum, number);

        Student student = classroomStudent.getStudent();

        Semester semesterEntity = semesterRepository.findByYearAndSemester(year, semester)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate startDate = semesterEntity.getStartDate();
        LocalDate endDate = today.isBefore(semesterEntity.getEndDate()) ? today : semesterEntity.getEndDate();

        if (today.isBefore(startDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 수업일수: startDate ~ endDate 까지 평일(월~금)만 카운트
        int totalSchoolDays = (int) startDate.datesUntil(endDate.plusDays(1))
                .filter(d -> d.getDayOfWeek().getValue() <= 5) // 1~5 : 월~금
                .count();

        List<Attendance> attendances = attendanceRepository.findByStudentAndDateBetween(student, startDate, endDate);

        int absentCount = 0;
        int lateCount = 0;
        int leaveEarlyCount = 0;

        for (Attendance attendance : attendances) {
            if (attendance.getStatus() == AttendanceStatus.ABSENT) absentCount++;
            else if (attendance.getStatus() == AttendanceStatus.LATE) lateCount++;
            else if (attendance.getStatus() == AttendanceStatus.EARLY) leaveEarlyCount++;
        }

        int presentDays = totalSchoolDays - (absentCount + lateCount + leaveEarlyCount);

        return StudentAttendanceSummaryDto.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .totalSchoolDays(totalSchoolDays)
                .presentDays(presentDays)
                .absentDays(absentCount)
                .lateDays(lateCount)
                .leaveEarlyDays(leaveEarlyCount)
                .build();
    }

    // [교사] 출결 피드백 저장
    @Transactional
    public AttendanceFeedbackResDto saveFeedback(String username, int year, int grade, int classNum, int number, AttendanceFeedbackReqDto requestDto) {
        ClassroomStudent classroomStudent = checkValidate(username, year, grade, classNum, number);

        AttendanceFeedback feedback = AttendanceFeedbackReqDto.of(requestDto, classroomStudent);

        // Notification용
        User user = classroomStudent.getStudent().getUser();
        String content = classroomStudent.getStudent().getName() + "님의 출결 피드백이 작성되었습니다. ";

        eventPublisher.publishEvent(new SendAttendanceFeedbackFcmEvent(feedback));
        notificationService.sendNotification(user, content);

        return AttendanceFeedbackResDto.toDto(feedbackRepository.save(feedback));
    }

    // [교사] 출결 피드백 수정
    @Transactional
    public AttendanceFeedbackResDto updateFeedback(String username, Long feedBackId, AttendanceFeedbackReqDto requestDto) {

        Teacher teacher = teacherService.authenticate(username);

        AttendanceFeedback feedback = feedbackRepository.findById(feedBackId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        if(feedback.getClassroomStudent().getClassroom().getTeacher() != teacher){
            throw new CustomException(HANDLE_ACCESS_DENIED);
        }
        feedback.updateContent(requestDto.getFeedback());

        // Notification용
        User user = feedback.getClassroomStudent().getStudent().getUser();
        String content = feedback.getClassroomStudent().getStudent().getName() + "님의 출결 피드백이 수정되었습니다. ";

        eventPublisher.publishEvent(new SendAttendanceFeedbackFcmEvent(feedback));
        notificationService.sendNotification(user, content);

        return AttendanceFeedbackResDto.toDto(feedback);
    }

    // [교사 / 학생] 출결 피드백 조회
    public AttendanceFeedbackResDto getFeedback(String username, int year, int grade, int classNum, int number) {

        ClassroomStudent classroomStudent = checkValidate(username, year, grade, classNum, number);

        AttendanceFeedback attendanceFeedback = feedbackRepository.findByClassroomStudent(classroomStudent).orElseThrow(
                (() -> new CustomException(ErrorCode.DATA_NOT_FOUND))
        );

        return AttendanceFeedbackResDto.builder()
                .feedbackId(attendanceFeedback.getId())
                .feedback(attendanceFeedback.getFeedback())
                .build();
    }

    private ClassroomStudent checkValidate(String username, int year, int grade, int classNum, int number) {
        User user = userService.getUser(username);

        switch (user.getRole()) {
            case STUDENT -> {
                // 본인이 요청한 게 맞는지 확인
                ClassroomStudent cs = classroomStudentRepository.findByStudentUser(user).orElseThrow(
                        () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND)
                );
                if (cs.getNumber() != number) {
                    throw new CustomException(HANDLE_ACCESS_DENIED);
                }
                return cs;
            }

            case TEACHER -> {
                teacherService.authenticate(username);
                // 교사임을 체크 ->
                Classroom classroom = classroomService.findClassroom(year, grade, classNum);

                return classroomStudentRepository.findByClassroomAndNumber(classroom, number)
                        .orElseThrow(() -> new CustomException(HANDLE_ACCESS_DENIED));
            }

            case PARENT -> {
                Parent parent = parentService.getParentByUser(user);

                Student student = parent.getStudent();
                Classroom classroom = classroomService.findClassroom(year, grade, classNum);

                ClassroomStudent classroomStudent = classroomStudentRepository.findByClassroomAndNumber(classroom, number)
                        .orElseThrow(() -> new CustomException(CLASSROOM_NOT_FOUND));

                // 자녀가 아니면 접근 불가함.
                if(!student.getId().equals(classroomStudent.getStudent().getId())){
                    throw new CustomException(HANDLE_ACCESS_DENIED);
                }

                return classroomStudent;
            }

            default -> throw new CustomException(HANDLE_ACCESS_DENIED);
        }
    }
}
