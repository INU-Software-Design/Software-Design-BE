package com.neeis.neeis.domain.teacher.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neeis.neeis.global.exception.ErrorCode.HANDLE_ACCESS_DENIED;
import static com.neeis.neeis.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final ParentService parentService;
    private final UserService userService;
    private final StudentService studentService;


    // 교사 전용 -> 학생의 출석번호 이름 조회
    public ClassroomStudentDto getStudentsFlexible(String username, int year, Integer grade, Integer classNum) {
        Teacher teacher = authenticate(username);

        Classroom classroom;
        if (grade != null && classNum != null) {
            // 학년, 반 직접 지정
            classroom = classroomRepository.findByYearAndGradeAndClassNum(year, grade, classNum)
                    .orElseThrow(() -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));
        } else {
            // 담임 반
            classroom = checkClassroom(teacher.getId(), year);
        }

        return buildClassroomStudentDto(classroom);
    }


    private ClassroomStudentDto buildClassroomStudentDto(Classroom classroom) {
        List<ClassroomStudent> classroomStudentList = classroomStudentRepository.findByClassroom(classroom);

        Map<Student, ClassroomStudent> studentList = new HashMap<>();
        for (ClassroomStudent classroomStudent : classroomStudentList) {
            studentList.put(classroomStudent.getStudent(), classroomStudent);
        }

        List<StudentResponseDto> studentResponseDtos = new ArrayList<>();
        for (Student student : studentList.keySet()) {
            studentResponseDtos.add(StudentResponseDto.of(student, studentList.get(student)));
        }

        studentResponseDtos.sort((s1, s2) -> Integer.compare(s1.getNumber(), s2.getNumber()));
        return ClassroomStudentDto.toDto(classroom, studentResponseDtos);
    }

    // 학생 학적 정보 가져오기
    public StudentDetailResDto getStudentDetail( String username, Long studentId, int year) {
        User user = userService.getUser(username);
        switch(user.getRole()) {
            case STUDENT -> {
                Student student = studentService.getStudent(studentId);

                // 본인 아니면 정보 조회 불가
                if (!user.getId().equals(student.getUser().getId())) {
                    throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
                }
            }
            case TEACHER -> {
                // 교사는 권한만 가지면 학생 학적 정보는 조회 가능
                authenticate(username);
            }
            case PARENT -> {
                // 부모 체크
                Parent parent = parentService.getParentByUser(user);

                // 본인의 자녀가 맞는지 확인
                if (!studentId.equals(parent.getStudent().getId())) {
                    throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
                }
            }

            default -> throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);

        }
        return studentService.getStudentDetails(studentId, year);
    }


    // 교사 개인 정보 조회
    public TeacherResponseDto getProfile(String username) {
        Teacher teacher = authenticate(username);
        return TeacherResponseDto.toDto(teacher);
    }

    public Teacher authenticate(String username) {
        // loginId -> user -> student 로
        User user = userService.getUser(username);

        // 접근 제한
        if (user.getRole() != Role.TEACHER){
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        return teacherRepository.findByUser(user).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public Teacher checkTeacher(String username) {
        return teacherRepository.findByName(username).orElseThrow(
                () -> new CustomException(ErrorCode.TEACHER_NOT_FOUND));
    }

    // 학급 확인
    public Classroom checkClassroom(Long teacherId, int year) {
        return classroomRepository.findByTeacherIdAndYear(teacherId,year).orElseThrow(
                () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND)
        );
    }

    // 담임 학생 체크
    public boolean checkClassroomStudent(String username, Long studentId, int year) {
        Teacher teacher = authenticate(username);

        boolean isMyStudent = classroomStudentRepository.existsByStudentAndTeacher(studentId, teacher.getId(), year);

        if (!isMyStudent){
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        return true;
    }

}
