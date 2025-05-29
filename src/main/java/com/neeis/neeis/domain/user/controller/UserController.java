package com.neeis.neeis.domain.user.controller;

import com.neeis.neeis.domain.user.dto.TokenResponseDto;
import com.neeis.neeis.domain.user.dto.FcmTokenRequestDto;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.user.dto.UpdatePasswordRequestDto;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.neeis.neeis.global.common.StatusCode.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "통합 로그인", description =
            """
            아이디는 유저에게 주어진 고유 ID 입니다. (변경불가능) <br>
            초기 비밀번호는 핸드폰 뒷자리 4자리이며, 추후 변경 가능합니다. <br>
            로그인은 통합로그인입니다. 권한에 따라 API 접근이 제한됩니다. <br>
            <br>
            name : 로그인한 본인의 이름 <br>
            studentName : 부모 로그인 시 자녀의 이름 <br>
            """)
    public ResponseEntity<CommonResponse<TokenResponseDto>> login( @Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_LOGIN.getMessage(),userService.login(loginRequestDto)));
    }

    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경", description = """
        사용자의 기존 비밀번호를 확인한 후 새 비밀번호로 변경합니다.
        <br><br>
        비밀번호 정책은 다음과 같습니다:
        <ul>
            <li>8자 이상</li>
            <li>영문 대문자, 소문자 포함</li>
            <li>숫자 또는 특수문자 중 최소 하나 포함</li>
        </ul>
        <br>
        잘못된 기존 비밀번호를 입력하거나, 새 비밀번호가 정책에 맞지 않는 경우 예외가 발생합니다.
        """)
    public ResponseEntity<CommonResponse<Object>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto) {
        userService.updatePassword(updatePasswordRequestDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_UPDATE_PASSWORD.getMessage()));
    }

    @Operation(
            summary = "FCM 토큰 등록",
            description = """
        로그인한 사용자의 디바이스 FCM 토큰을 서버에 등록합니다. <br><br>
        이 토큰은 푸시 알림(Firebase Cloud Messaging)을 전송할 때 사용됩니다.<br><br>
        웹 또는 모바일 앱에서 발급된 FCM 토큰을 클라이언트에서 받아<br>
        해당 토큰을 이 API를 통해 서버에 전송하세요.<br><br>
        동일 사용자가 기존 토큰을 등록한 상태라면 갱신 처리됩니다.
        """)
    @PostMapping("/fcm/register")
    public ResponseEntity<CommonResponse<Object>> registerFcmToken(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @RequestBody FcmTokenRequestDto fcmTokenRequestDto) {
        userService.registerToken(userDetails.getUsername(), fcmTokenRequestDto);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_SAVE_FCM_TOKEN.getMessage()));
    }
}
