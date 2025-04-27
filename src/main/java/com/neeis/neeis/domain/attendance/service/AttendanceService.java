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
import com.neeis.neeis.domain.semester.Semester;
import com.neeis.neeis.domain.semester.SemesterRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TeacherService teacherService;
    private final ClassroomService classroomService;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final SemesterRepository semesterRepository;
    private final AttendanceFeedbackRepository feedbackRepository;

    @Transactional
    public void saveAttendance(String username, AttendanceBulkRequestDto requestDto) {

        Teacher teacher = teacherService.authenticate(username);
        Classroom classroom = classroomService.findClassroom(requestDto.getYear(), requestDto.getGrade(), requestDto.getClassNumber(), teacher.getId());

        if(classroom.getTeacher() != teacher) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 담당 학생들 확인
        List<ClassroomStudent> classroomStudentList = classroomStudentRepository.findByClassroom(classroom);
        Map<Long, Student> studentMap = classroomStudentList.stream()
                .collect(Collectors.toMap(
                        cs -> cs.getStudent().getId(),
                        ClassroomStudent::getStudent
                ));

        // 학생과 DB 확인
        for (StudentAttendanceDto dto : requestDto.getStudents()) {
            if (!studentMap.containsKey(dto.getStudentId())) {
                throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
            }
        }

        // 출결 저장
        // 입력받은 연도 + 월 -> 마지막 일 구하기
        YearMonth yearMonth = YearMonth.of(requestDto.getYear(), requestDto.getMonth());
        int lastDay = yearMonth.lengthOfMonth(); // 해당 월의 마지막 일

        for (StudentAttendanceDto studentDto : requestDto.getStudents()) {
            Student student = studentMap.get(studentDto.getStudentId());

            // 날짜별 출결사항 매핑
            Map<LocalDate, AttendanceStatus> statusMap = new HashMap<>();
            for (DailyAttendanceDto dailyDto : studentDto.getAttendances()) {

                statusMap.put(dailyDto.getDate(), dailyDto.getStatus());
            }

            // 한달치 모든 날짜에 대해 저장
            for (int day = 1; day <= lastDay; day++) {
                LocalDate date = LocalDate.of(requestDto.getYear(), requestDto.getMonth(), day);
                AttendanceStatus status = statusMap.getOrDefault(date, AttendanceStatus.PRESENT); // 없으면 출석으로

                // 특이사항만 저장
                if (status != AttendanceStatus.PRESENT) {
                    Attendance attendance = attendanceRepository.findByStudentAndDate(student, date)
                            .orElse(Attendance.builder()
                                    .student(student)
                                    .date(date)
                                    .status(status)
                                    .build());

                    // 상태 업데이트
                    attendance = Attendance.builder()
                            .student(student)
                            .date(date)
                            .status(status)
                            .build();

                    attendanceRepository.save(attendance);
                }
            }
        }
    }

    // 학급 학생들 월별 조회
    public List<StudentAttendanceResDto> getAttendances(String username, int year, int grade, int classNum, int month) {
        Teacher teacher = teacherService.authenticate(username);
        Classroom classroom = classroomService.findClassroom(year, grade, classNum, teacher.getId());
        if(classroom.getTeacher() != teacher) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

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

                    return StudentAttendanceResDto.toDto(
                            attendances.isEmpty() ? Attendance.builder().student(student).build() : attendances.getFirst(),
                            dailyAttendanceDtos
                    );
                })
                .collect(Collectors.toList());
    }

    // 개인 학생 월별 조회
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

    @Transactional
    public AttendanceFeedbackResDto saveFeedback(String username, int year, int grade, int classNum, int number, AttendanceFeedbackReqDto requestDto) {
        ClassroomStudent classroomStudent = checkValidate(username, year, grade, classNum, number);

        AttendanceFeedback feedback = AttendanceFeedbackReqDto.of(requestDto, classroomStudent);
        feedbackRepository.save(feedback);

        return AttendanceFeedbackResDto.toDto(feedback);
    }

    @Transactional
    public AttendanceFeedbackResDto updateFeedback(String username, Long feedBackId, AttendanceFeedbackReqDto requestDto) {

        Teacher teacher = teacherService.authenticate(username);

        AttendanceFeedback feedback = feedbackRepository.findById(feedBackId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        if(feedback.getClassroomStudent().getClassroom().getTeacher() != teacher){
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        feedback.updateContent(requestDto.getFeedback());

        return AttendanceFeedbackResDto.toDto(feedback);
    }

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
        Teacher teacher = teacherService.authenticate(username);
        Classroom classroom = classroomService.findClassroom(year, grade, classNum, teacher.getId());

        return classroomStudentRepository.findByClassroomAndNumber(classroom, number)
                .orElseThrow(() -> new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));
    }
}
