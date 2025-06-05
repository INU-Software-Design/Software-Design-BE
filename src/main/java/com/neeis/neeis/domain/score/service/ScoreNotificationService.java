package com.neeis.neeis.domain.score.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.fcm.event.SendScoreFcmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreNotificationService {
    private final ClassroomService classroomService;
    private final ClassroomStudentService classroomStudentService;
    private final SubjectService subjectService;
    private final ScoreSummaryService scoreSummaryService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    public void sendNotificationsForAffectedSubjects(int year, int semester, int grade, int classNum, Set<Long> affectedSubjectIds) {
        try {
            Classroom classroom = classroomService.findClassroom(year, grade, classNum);
            List<ClassroomStudent> students = classroomStudentService.findByClassroom(classroom);

            // 🔥 핵심: 영향받는 과목들만 조회
            List<Subject> affectedSubjects = affectedSubjectIds.stream()
                    .map(subjectService::findById)
                    .toList();

            log.info("알림 발송 시작 - 대상 과목: {}",
                    affectedSubjects.stream().map(Subject::getName).toList());

            for (ClassroomStudent student : students) {
                for (Subject subject : affectedSubjects) { // 영향받는 과목들만 순회
                    try {
                        scoreSummaryService.findByStudentAndSubjectOptional(student.getId(), subject.getId())
                                .ifPresent(summary -> {
                                    try {
                                        // FCM 이벤트 발행
                                        eventPublisher.publishEvent(new SendScoreFcmEvent(summary));

                                        // 알림 기록 저장
                                        User user = student.getStudent().getUser();
                                        String content = subject.getName() + " 과목의 성적이 입력되었습니다.";
                                        notificationService.sendNotification(user, content);

                                        log.debug("알림 발송 완료: 학생={}, 과목={}", student.getStudent().getName(), subject.getName());
                                    } catch (Exception e) {
                                        log.warn("개별 알림 발송 실패 (계속 진행): 학생={}, 과목={}, 오류={}", student.getStudent().getName(), subject.getName(), e.getMessage());
                                    }
                                });
                    } catch (Exception e) {
                        log.warn("성적 요약 조회 실패 (계속 진행): 학생ID={}, 과목={}, 오류={}", student.getId(), subject.getName(), e.getMessage());
                    }
                }
            }

            log.info("알림 발송 완료 - 처리된 과목 수: {}", affectedSubjects.size());

        } catch (Exception e) {
            log.error("알림 발송 과정에서 전체 오류 발생: year={}, semester={}, grade={}, classNum={}, 과목수={}, 오류={}",
                    year, semester, grade, classNum, affectedSubjectIds.size(), e.getMessage(), e);
        }
    }
}
