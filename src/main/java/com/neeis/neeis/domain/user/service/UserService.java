package com.neeis.neeis.domain.user.service;

import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.teacher.TeacherRepository;
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

import static com.neeis.neeis.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final TeacherRepository teacherRepository;

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
            if (!storedPw.equals(inputPw)) {
                throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
            }

            // 기존 평문화 되었던 비번은 암호화 되어 저장.
            user.updatePassword(passwordEncoder.encode(inputPw));
            userRepository.save(user);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole().name());
        String name = getNameByRole(user);

        return TokenResponseDto.of(accessToken,name,user.getRole().toString());
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

    private String getNameByRole(User user) {
        switch (user.getRole()) {
            case STUDENT -> {
                return studentRepository.findByUser(user)
                        .orElseThrow(() -> new CustomException(USER_NOT_FOUND))
                        .getName();
            }
            case TEACHER -> {
                return teacherRepository.findByUser(user)
                        .orElseThrow(() -> new CustomException(USER_NOT_FOUND))
                        .getName();
            }
            case PARENT -> {
                return parentRepository.findByUser(user)
                        .orElseThrow(() -> new CustomException(USER_NOT_FOUND))
                        .getName();
            }
            default -> throw new CustomException(USER_NOT_FOUND);
        }
    }
}
