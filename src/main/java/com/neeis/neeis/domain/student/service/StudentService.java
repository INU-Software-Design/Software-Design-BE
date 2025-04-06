package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.dto.LoginRequestDto;
import com.neeis.neeis.domain.student.dto.StudentResponseDto;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsername(loginRequestDto.getLoginId()).orElseThrow(
                () -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Student student = studentRepository.findByUser(user).orElseThrow(
                ()  -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if(!user.getPassword().equals(loginRequestDto.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
        }

        return StudentResponseDto.of(student);
    }


}
