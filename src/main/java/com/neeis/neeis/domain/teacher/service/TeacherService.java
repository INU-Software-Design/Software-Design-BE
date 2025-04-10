package com.neeis.neeis.domain.teacher.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.teacher.dto.StudentResponseDto;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;
    private final StudentService studentService;
    private final JwtProvider jwtProvider;

    // 담당 학생들 조회
    public List<StudentResponseDto> getStudents(String username) {
        Teacher teacher = authenticate(username);
        int year = LocalDate.now().getYear();

        // 담당 학급 확인
        Classroom classroom = classroomRepository.findByTeacherAndYear(teacher.getId(),year).orElseThrow(
                () -> new RuntimeException("Classroom not found")
        );

        List<Student> students = studentRepository.findByClassroom(classroom);
        List<StudentResponseDto> studentResponseDtos = new ArrayList<>();
        if(students != null && !students.isEmpty()) {
            for (Student student : students) {
                StudentResponseDto dto = StudentResponseDto.of(student);
                studentResponseDtos.add(dto);
            }
        }
        return studentResponseDtos;
    }

    public StudentDetailResDto getStudentDetail( Long studentId) {
//        Teacher teacher = authenticate(username);
        return studentService.getStudentDetails(studentId);


    }


    private Teacher authenticate(String username) {
        // loginId -> user -> student 로
        User user = userService.getUser(username);

        // 접근 제한
        if (user.getRole() != Role.TEACHER){
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        Teacher teacher = teacherRepository.findByUser(user).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return teacher;
    }

}
