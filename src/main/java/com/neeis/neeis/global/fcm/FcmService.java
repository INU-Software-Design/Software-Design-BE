package com.neeis.neeis.global.fcm;

import com.google.firebase.messaging.*;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.retry.annotation.Recover;

import java.util.List;
import java.util.Map;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository;
    private final ParentService parentService;

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

    @Async("FcmExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Retryable(
            recover = "recoverSendFeedbackFcm",
            retryFor = FirebaseMessagingException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendFeedbackFcm(SendFeedbackFcmEvent event) throws FirebaseMessagingException {
        ScoreSummary summary = event.getScoreSummary();
        ClassroomStudent classroomStudent = summary.getClassroomStudent();
        Student student = classroomStudent.getStudent();

        String subjectName = summary.getSubject().getName();
        String title = "새 피드백이 도착했어요!";
        String body = String.format("%s 학생의 [%s] 과목에 대한 피드백이 등록되었습니다.", student.getName(), subjectName);

        Map<String, String> data = Map.of(
                "type", "FEEDBACK",
                "scoreSummaryId", summary.getId().toString(),
                "subject", summary.getSubject().getName()
        );

        sendNotification(FcmMessageRequest.of(student.getUser().getFcmToken(), title, body, data));

        // 부모에게도 발송
        List<Parent> parents = parentService.getParents(student);
        for (Parent parent : parents) {
            User parentUser = parent.getUser();
            sendNotification(FcmMessageRequest.of(parentUser.getFcmToken(), title, body, data));
        };
    }

    @Async("FcmExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Retryable(
            recover = "recoverSendCounselFcm",
            retryFor = FirebaseMessagingException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendCounselFcm(SendCounselFcmEvent event) throws FirebaseMessagingException {
        Student student = event.getCounsel().getStudent();

        String title = "새 상담 내역이 등록되었어요";
        String body = String.format("%s 학생에 대한 상담이 등록되었습니다.", student.getName());

        Map<String, String> data = Map.of(
                "type", "COUNSEL",
                "counselId", event.getCounsel().getId().toString()
        );

        sendNotification(FcmMessageRequest.of(student.getUser().getFcmToken(), title, body, data));

        List<Parent> parents = parentService.getParents(student);
        for (Parent parent : parents) {
            User parentUser = parent.getUser();
            sendNotification(FcmMessageRequest.of(parentUser.getFcmToken(), title, body, data));
        }
    }

    @Async("FcmExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Retryable(
            recover = "recoverSendScoreFcm",
            retryFor = FirebaseMessagingException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendScoreFcm(SendScoreFcmEvent event) throws FirebaseMessagingException {
        ScoreSummary summary = event.getScoreSummary();
        ClassroomStudent cs = summary.getClassroomStudent();
        Student student = cs.getStudent();

        String subject = summary.getSubject().getName();

        String title = "성적이 입력되었어요";
        String body = String.format("%s 과목의 성적이 입력 및 분석되었습니다.", subject);

        Map<String, String> data = Map.of(
                "type", "SCORE",
                "summaryId", summary.getId().toString(),
                "subject", subject
        );

        sendNotification(FcmMessageRequest.of(student.getUser().getFcmToken(), title, body, data));

        List<Parent> parents = parentService.getParents(student);
        for (Parent parent : parents) {
            User parentUser = parent.getUser();
            sendNotification(FcmMessageRequest.of(parentUser.getFcmToken(), title, body, data));
        }
    }

    @Recover
    public void recoverSendFeedbackFcm(FirebaseMessagingException e, SendFeedbackFcmEvent event) {
        log.error("[FCM] 피드백 알림 재시도 실패 - summaryId: {}", event.getScoreSummary().getId(), e);
    }

    @Recover
    public void recoverSendCounselFcm(FirebaseMessagingException e, SendCounselFcmEvent event) {
        log.error("[FCM] 상담 알림 재시도 실패 - counselId: {}", event.getCounsel().getId(), e);
    }

    @Recover
    public void recoverSendScoreFcm(FirebaseMessagingException e, SendScoreFcmEvent event) {
        log.error("[FCM] 성적 알림 재시도 실패 - summaryId: {}", event.getScoreSummary().getId(), e);
    }



}
