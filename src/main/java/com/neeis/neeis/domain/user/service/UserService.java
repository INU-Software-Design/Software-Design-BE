package com.neeis.neeis.domain.user.service;

import com.neeis.neeis.domain.student.dto.res.TokenResponseDto;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
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

    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsername(loginRequestDto.getLoginId()).orElseThrow(
                () -> new CustomException(ErrorCode.LOGIN_INPUT_INVALID));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole().name());

        return TokenResponseDto.of(accessToken);
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

        user.updatePassword(requestDto.getNewPassword());
    }


    public User getUser(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));
    }

}
