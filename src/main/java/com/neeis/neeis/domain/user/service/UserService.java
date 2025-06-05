package com.neeis.neeis.domain.user.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.TeacherRepository;
import com.neeis.neeis.domain.teacherSubject.TeacherSubject;
import com.neeis.neeis.domain.teacherSubject.TeacherSubjectRepository;
import com.neeis.neeis.domain.user.dto.TokenResponseDto;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.domain.user.dto.FcmTokenRequestDto;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.user.dto.UpdatePasswordRequestDto;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.neeis.neeis.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final ParentService parentService;
    private final ClassroomRepository classroomRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    @Transactional
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsername(loginRequestDto.getLoginId()).orElseThrow(
                () -> new CustomException(ErrorCode.LOGIN_INPUT_INVALID));

        String inputPw = loginRequestDto.getPassword();
        String storedPw = user.getPassword();

        if (isEncoded(storedPw)) {
            if (!passwordEncoder.matches(inputPw, storedPw)) {
                throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
            }
        } else {
            if (storedPw == null || !storedPw.equals(inputPw)) {
                throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
            }

            // 기존 평문화 되었던 비번은 암호화 되어 저장.
            user.updatePassword(passwordEncoder.encode(inputPw));
            userRepository.save(user);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole().name());

        switch (user.getRole()) {
            case STUDENT -> {
                Student student = studentRepository.findByUser(user)
                        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
                int year = LocalDate.now().getYear();
                ClassroomStudent classroomStudent = classroomStudentRepository.findByStudentAndClassroomYear(student.getId(), year).orElseThrow(
                        () -> new CustomException(CLASSROOM_NOT_FOUND));
                Classroom classroom = classroomStudent.getClassroom();

                return TokenResponseDto.ofStudent(accessToken,
                        student.getName(),
                        user.getRole().name(),
                        classroom.getYear(),
                        classroom.getGrade(),
                        classroom.getClassNum(),
                        classroomStudent.getNumber(),
                        student.getId());
            }
            case TEACHER -> {
               Teacher teacher = teacherRepository.findByUser(user)
                        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

               TeacherSubject subject = teacherSubjectRepository.findByTeacher(teacher).orElseThrow(
                       () -> new CustomException(ErrorCode.TEACHER_SUBJECT_NOT_FOUND));

               Classroom classroom = classroomRepository.findByTeacher(teacher).orElseThrow(
                       ()-> new CustomException(CLASSROOM_NOT_FOUND)
               );

               return TokenResponseDto.ofTeacher(accessToken, teacher.getName(), user.getRole().name(), subject.getSubject().getName(),
                       classroom.getYear(), classroom.getGrade(), classroom.getClassNum());
            }
            case PARENT -> {
                Parent parent = parentService.getParentByUser(user);

                Long studentId = parent.getStudent().getId();
                Student student = studentRepository.findById(studentId).orElseThrow(
                        () -> new CustomException(USER_NOT_FOUND)
                );

                int year = LocalDate.now().getYear();
                ClassroomStudent classroomStudent = classroomStudentRepository.findByStudentAndClassroomYear(student.getId(), year).orElseThrow(
                        () -> new CustomException(CLASSROOM_NOT_FOUND));
                Classroom classroom = classroomStudent.getClassroom();

                return TokenResponseDto.ofParent(accessToken,
                        parent.getName(),
                        user.getRole().name(),
                        classroom.getYear(),
                        classroom.getGrade(),
                        classroom.getClassNum(),
                        classroomStudent.getNumber(),
                        student.getId(),
                        student.getName()
                );
            }
            default -> throw new CustomException(USER_NOT_FOUND);
        }
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getLoginId()).orElseThrow(
                () -> new CustomException(ErrorCode.LOGIN_INPUT_INVALID));

        if(requestDto.getOldPassword().equals(requestDto.getNewPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_EQUALS);
        }

        if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
        }

        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    @Transactional
    public void registerToken(String username, FcmTokenRequestDto requestDto) {
        User user = getUser(username);

        user.updateFcmToken(requestDto.getToken());
    }

    private boolean isEncoded(String password) {
        return password != null && password.startsWith("$2");
    }

    public User getUser(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));
    }
}
