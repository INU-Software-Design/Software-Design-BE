package com.neeis.neeis.domain.teacher.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.teacher.dto.ClassroomStudentDto;
import com.neeis.neeis.domain.teacher.dto.StudentResponseDto;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final UserService userService;
    private final StudentService studentService;

    // 담당 학생들 조회
    public ClassroomStudentDto getStudents(String username, int year) {
        Teacher teacher = authenticate(username);
        Classroom classroom = checkClassroom(teacher.getId(), year);

        List<ClassroomStudent> classroomStudentList = classroomStudentRepository.findByClassroom(classroom);

        Map<Student, ClassroomStudent> studentList = new HashMap<>();
        for (ClassroomStudent classroomStudent : classroomStudentList) {
            studentList.put(classroomStudent.getStudent(), classroomStudent);
        }
        List<StudentResponseDto> studentResponseDtos = new ArrayList<>();
        if(!studentList.isEmpty()) {
            for (Student student : studentList.keySet()) {
                StudentResponseDto dto = StudentResponseDto.of(student,studentList.get(student));
                studentResponseDtos.add(dto);
            }
        }

        // HashMap -> 순서가 없으므로, number(출석번호)로 정렬해줘야 한다.
        studentResponseDtos.sort((s1, s2) -> Integer.compare(s1.getNumber(), s2.getNumber()));

        return ClassroomStudentDto.toDto(classroom, studentResponseDtos);
    }

    public StudentDetailResDto getStudentDetail( String username, Long studentId, int year) {
        authenticate(username);
        return studentService.getStudentDetails(studentId, year);
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
