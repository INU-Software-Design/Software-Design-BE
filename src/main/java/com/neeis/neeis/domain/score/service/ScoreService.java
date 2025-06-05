package com.neeis.neeis.domain.score.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.score.dto.req.ScoreRequestDto;
import com.neeis.neeis.domain.score.dto.res.ScoreSummaryBySubjectDto;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.teacherSubject.service.TeacherSubjectService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendScoreFcmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final TeacherService teacherService;
    private final TeacherSubjectService teacherSubjectService;
    private final EvaluationMethodService evaluationMethodService;
    private final ClassroomStudentService classroomStudentService;
    private final ClassroomService classroomService;
    private final SubjectService subjectService;
    private final ScoreSummaryService scoreSummaryService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;


    // 교사 전용 -> 과목에 대한 해당 반 학생들의 점수 조회
    public List<ScoreSummaryBySubjectDto> getScoreSummaryBySubject(String username, int year, int semester, int grade, int classNum, String subjectName) {
        // 교사 인증
        teacherService.authenticate(username);

        // 반 조회
        Classroom classroom = classroomService.findClassroom(year, grade, classNum);

        // 해당 반의 학생들
        List<ClassroomStudent> students = classroomStudentService.findByClassroom(classroom);

        // 과목 리스트 조회
        List<Subject> subjects = (subjectName != null && !subjectName.isBlank())
                ? List.of(subjectService.getSubject(subjectName))
                : evaluationMethodService.findSubject(year, semester, grade);

        List<ScoreSummaryBySubjectDto> result = new ArrayList<>();

        for (Subject subject : subjects) {
            List<EvaluationMethod> evaluations = evaluationMethodService
                    .findAllBySubjectAndYearAndSemesterAndGrade(subject, year, semester, grade);

            if (evaluations.isEmpty()) continue;

            List<ScoreSummaryBySubjectDto.EvaluationDto> evaluationDtos = evaluations.stream()
                    .map(eval -> ScoreSummaryBySubjectDto.EvaluationDto.builder()
                            .evaluationId(eval.getId())
                            .title(eval.getTitle())
                            .build())
                    .toList();

            List<ScoreSummaryBySubjectDto.StudentScoreDto> studentDtos = new ArrayList<>();

            for (ClassroomStudent cs : students) {
                List<ScoreSummaryBySubjectDto.ScoreItemDto> scoreItems = new ArrayList<>();

                for (EvaluationMethod eval : evaluations) {
                    scoreRepository.findByEvaluationMethodAndStudent(eval, cs).ifPresent(score -> {
                        scoreItems.add(ScoreSummaryBySubjectDto.ScoreItemDto.builder()
                                .evaluationId(eval.getId())
                                .rawScore(score.getRawScore())
                                .weightedScore(score.getWeightedScore())
                                .build());
                    });
                }

                ScoreSummary summary = scoreSummaryService.findByStudentAndSubject(cs.getId(), subject.getId());

                studentDtos.add(ScoreSummaryBySubjectDto.StudentScoreDto.builder()
                        .studentName(cs.getStudent().getName())
                        .number(cs.getNumber())
                        .scores(scoreItems)
                        .rawTotal(summary.getSumScore())
                        .weightedTotal(summary.getOriginalScore())
                        .average(summary.getAverage())
                        .stdDev(summary.getStdDeviation())
                        .rank(summary.getRank())
                        .grade(summary.getGrade())
                        .achievementLevel(summary.getAchievementLevel())
                        .build());

            }
            result.add(ScoreSummaryBySubjectDto.builder()
                    .subjectName(subject.getName())
                    .evaluations(evaluationDtos)
                    .students(studentDtos)
                    .build());
        }

        return result;
    }

    @Transactional
    public void saveOrUpdateScores(String username, List<ScoreRequestDto> requestList) {
        // 교사 권한 체크
        Teacher teacher = teacherService.authenticate(username);

        Map<String, Set<Long>> affectedSubjectsByClass = new HashMap<>();
        Map<String, List<Score>> scoreBuffer = new HashMap<>();


        for (ScoreRequestDto requestDto : requestList) {
            EvaluationMethod eval = evaluationMethodService.findById(requestDto.getEvaluationId());
            Subject subject = eval.getSubject();
            teacherSubjectService.findByTeacherAndSubject(teacher, subject); // 권한 검증

            Classroom classroom = classroomService.findClassroom(eval.getYear(), eval.getGrade(), requestDto.getClassNum());

            // 클래스별 영향받는 과목 추적
            String classKey = eval.getYear() + "_" + eval.getSemester() + "_" + eval.getGrade() + "_" + requestDto.getClassNum();
            affectedSubjectsByClass.computeIfAbsent(classKey, k -> new HashSet<>()).add(subject.getId());

            for (ScoreRequestDto.StudentScoreDto studentDto : requestDto.getStudents()) {
                ClassroomStudent student = classroomStudentService.findByClassroomAndNumber(classroom, studentDto.getNumber());

                double raw = studentDto.getRawScore();
                ScoreValidator.validateRawScore(raw, eval);
                double weighted = (raw / eval.getFullScore()) * eval.getWeight();

                // 점수 저장
                Score score = scoreRepository.findByEvaluationMethodAndStudent(eval, student)
                        .map(s -> {
                            s.update(raw, weighted);
                            return s;
                        })
                        .orElseGet(() -> scoreRepository.save(
                                Score.builder()
                                        .student(student)
                                        .evaluationMethod(eval)
                                        .rawScore(raw)
                                        .weightedScore(weighted)
                                        .build()
                        ));

                scoreBuffer.computeIfAbsent(classKey, k -> new ArrayList<>()).add(score);
            }
        }

        scoreRepository.flush();

        for (Map.Entry<String, Set<Long>> entry : affectedSubjectsByClass.entrySet()) {
            String classKey = entry.getKey();
            Set<Long> affectedSubjectIds = entry.getValue();

            String[] parts = classKey.split("_");
            int year = Integer.parseInt(parts[0]);
            int semester = Integer.parseInt(parts[1]);
            int grade = Integer.parseInt(parts[2]);
            int classNum = Integer.parseInt(parts[3]);

            log.info("성적 업데이트 - 영향받는 과목 수: {} ({}년 {}학기 {}학년 {}반)",
                    affectedSubjectIds.size(), year, semester, grade, classNum);

            // 영향받는 과목들만 업데이트
            for (Long subjectId : affectedSubjectIds) {
                scoreSummaryService.updateSummaryForSpecificSubject(subjectId, year, semester, grade, classNum);
            }
        }

        // 영향받는 과목들에만 알림 발송
        for (Map.Entry<String, Set<Long>> entry : affectedSubjectsByClass.entrySet()) {
            String classKey = entry.getKey();
            Set<Long> affectedSubjectIds = entry.getValue();

            String[] parts = classKey.split("_");
            int year = Integer.parseInt(parts[0]);
            int semester = Integer.parseInt(parts[1]);
            int grade = Integer.parseInt(parts[2]);
            int classNum = Integer.parseInt(parts[3]);

            // 영향받는 과목들에만 알림 발송
            sendNotificationsForAffectedSubjects(year, semester, grade, classNum, affectedSubjectIds);
        }
    }

    /*
     * 특정 과목들에만 알림 발송
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
