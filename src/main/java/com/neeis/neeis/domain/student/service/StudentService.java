package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.dto.res.PasswordResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;


    // 아이디 찾기
    public StudentResponseDto findUsername(FindIdRequestDto findIdRequestDto) {
        Student student = studentRepository.findByPhone(findIdRequestDto.getPhone()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!student.getName().equals(findIdRequestDto.getName()) ||
        !student.getUser().getSchool().equals(findIdRequestDto.getSchool())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return StudentResponseDto.of(student);
    }

    // 비밀번호 찾기
    public PasswordResponseDto findPassword(PasswordRequestDto passwordRequestDto) {
        Student student = studentRepository.findByPhone(passwordRequestDto.getPhone()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!student.getName().equals(passwordRequestDto.getName()) ||
                !student.getUser().getSchool().equals(passwordRequestDto.getSchool()) ||
        !student.getSsn().equals(passwordRequestDto.getSsn())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return PasswordResponseDto.of(student);
    }

    public StudentDetailResDto getStudentDetails(Long studentId, int year) {
        Student student = getStudent(studentId);

        ClassroomStudent classroomStudent = classroomStudentRepository.findByStudentAndClassroomYear(student.getId(), year).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        Classroom classroom = classroomStudent.getClassroom();

        List<Parent> parents = parentRepository.findByStudent(student);

        Parent father = parents.stream()
                .filter(p -> "부".equalsIgnoreCase(p.getRelationShip()))
                .findFirst().orElseThrow( () -> new CustomException(ErrorCode.INVALID_DATA));
        Parent mother = parents.stream()
                .filter(p -> "모".equalsIgnoreCase(p.getRelationShip()))
                .findFirst().orElseThrow( () -> new CustomException(ErrorCode.INVALID_DATA));

        return StudentDetailResDto.of(student, father, mother, classroom, classroomStudent);
    }

    public Student getStudent(Long studentId) {
        return studentRepository.findById(studentId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
    }
}
