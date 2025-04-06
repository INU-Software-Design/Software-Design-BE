package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.LoginRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.dto.res.PasswordResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public StudentResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsername(loginRequestDto.getLoginId()).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        Student student = studentRepository.findByUser(user).orElseThrow(
                ()  -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if(!user.getPassword().equals(loginRequestDto.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
        }

        return StudentResponseDto.of(student);
    }

    public StudentResponseDto findUsername(FindIdRequestDto findIdRequestDto) {
        Student student = studentRepository.findByPhone(findIdRequestDto.getPhone()).orElseThrow(
                () -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if(!student.getName().equals(findIdRequestDto.getName()) ||
        !student.getUser().getSchool().equals(findIdRequestDto.getSchool())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return StudentResponseDto.of(student);
    }

    public PasswordResponseDto findPassword(PasswordRequestDto passwordRequestDto) {
        Student student = studentRepository.findByPhone(passwordRequestDto.getPhone()).orElseThrow(
                () -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if(!student.getName().equals(passwordRequestDto.getName()) ||
                !student.getUser().getSchool().equals(passwordRequestDto.getSchool()) ||
        !student.getSsn().equals(passwordRequestDto.getSsn())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return PasswordResponseDto.of(student);
    }
}
