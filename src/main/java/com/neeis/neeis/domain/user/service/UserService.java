package com.neeis.neeis.domain.user.service;

import com.neeis.neeis.domain.student.dto.res.TokenResponseDto;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.neeis.neeis.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsername(loginRequestDto.getLoginId()).orElseThrow(
                () -> new CustomException(ErrorCode.LOGIN_INPUT_INVALID));

        if (!user.getPassword().equals(loginRequestDto.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_INPUT_INVALID);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole().name());

        return TokenResponseDto.of(accessToken);
    }


    public User getUser(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));
    }

}
