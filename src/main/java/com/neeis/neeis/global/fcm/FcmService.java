package com.neeis.neeis.global.fcm;

import com.google.firebase.messaging.*;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentRepository;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.global.fcm.event.SendCounselFcmEvent;
import com.neeis.neeis.global.fcm.event.SendFeedbackFcmEvent;
import com.neeis.neeis.global.fcm.event.SendScoreFcmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.retry.annotation.Recover;

import java.util.List;
import java.util.Map;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;

    public void sendNotification(FcmMessageRequest fcmMessageRequest) throws FirebaseMessagingException {
        if (fcmMessageRequest.getToken() == null || fcmMessageRequest.getToken().isBlank()) return;

        Notification notification = Notification.builder()
                .setTitle(fcmMessageRequest.getTitle())
                .setBody(fcmMessageRequest.getBody())
                .build();

        Message message = Message.builder()
                .setToken(fcmMessageRequest.getToken())
                .setNotification(notification)
                .putAllData(fcmMessageRequest.getData())
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            if (errorCode.equals(MessagingErrorCode.INTERNAL) || errorCode.equals(MessagingErrorCode.UNAVAILABLE)) {
                log.info("FCM 메세지 전송 실패");
                throw e;
            }
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED){
                log.warn("[FCM] 유효하지 않은 토큰입니다. 토큰 제거 필요: {}", fcmMessageRequest.getToken());
                userRepository.findByFcmToken(fcmMessageRequest.getToken())
                        .ifPresent(user -> user.updateFcmToken(null));
            }
            log.warn("FCM 전송 실패: {}", e.getMessage());

        }
    }

}
