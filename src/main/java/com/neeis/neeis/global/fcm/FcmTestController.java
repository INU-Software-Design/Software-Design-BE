package com.neeis.neeis.global.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FcmTestController {

    private final FcmService fcmService;

    @PostMapping("/test")
    @Operation(
            summary = "FCM 테스트 알림 전송",
            description = "토큰, 제목, 내용을 포함한 테스트 알림을 수동으로 전송합니다."
    )
    public ResponseEntity<String> testPush(@RequestBody FcmMessageRequest req) throws FirebaseMessagingException {
        fcmService.sendNotification(req);
        return ResponseEntity.ok("FCM 테스트 전송 완료");
    }
}
