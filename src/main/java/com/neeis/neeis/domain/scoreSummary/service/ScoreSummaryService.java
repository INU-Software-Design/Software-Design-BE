package com.neeis.neeis.domain.scoreSummary.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.ScoreSummaryRepository;
import com.neeis.neeis.domain.scoreSummary.dto.res.ScoreFeedbackDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackRequestDto;
import com.neeis.neeis.domain.scoreSummary.dto.req.ScoreFeedbackUpdateDto;
import com.neeis.neeis.domain.student.dto.report.SubjectFeedbackDto;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendFeedbackFcmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.neeis.neeis.domain.user.Role.STUDENT;
import static com.neeis.neeis.domain.user.Role.TEACHER;
import static com.neeis.neeis.global.exception.ErrorCode.HANDLE_ACCESS_DENIED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ScoreSummaryService {
    private final ScoreSummaryRepository scoreSummaryRepository;
    private final ClassroomService classroomService;
    private final UserService userService;
    private final ClassroomStudentService classroomStudentService;
    private final ClassroomStudentRepository classroomStudentRepository;

    private final EvaluationMethodService evaluationMethodService;
    private final ScoreRepository scoreRepository;
    private final TeacherService teacherService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    
    // 교사 및 학생
    // 성적 조회 (전체 과목)
    public StudentScoreSummaryDto getStudentSummary(String username, int year, int semester, int grade, int classNum, int number) {

        ClassroomStudent student = checkValidate(username, year, grade, classNum, number);


        // 해당 학기의 점수 불러오기
        List<Score> scores = scoreRepository
                .findAllByStudentAndEvaluationMethod_YearAndEvaluationMethod_Semester(student, year, semester);

        // 과목 이름별로 점수 분류
        Map<String, List<Score>> scoreBySubject = scores.stream()
                .collect(Collectors.groupingBy(s -> s.getEvaluationMethod().getSubject().getName()));

        // 해당 학기 과목 리스트
        Set<String> subjectNamesInThisSemester = scores.stream()
                .map(s -> s.getEvaluationMethod().getSubject().getName())
                .collect(Collectors.toSet());

        // 모든 ScoreSummary 중에서 해당 학기의 과목에 속한 것만 필터링
        List<ScoreSummary> allSummaries = scoreSummaryRepository.findAllByClassroomStudent(student);
        List<ScoreSummary> filteredSummaries = allSummaries.stream()
                .filter(summary -> subjectNamesInThisSemester.contains(summary.getSubject().getName()))
                .toList();

        // DTO 변환
        List<SubjectScoreDto> subjectScoreList = filteredSummaries.stream()
                .map(summary -> {
                    String subjectName = summary.getSubject().getName();
                    List<Score> subjectScores = scoreBySubject.getOrDefault(subjectName, Collections.emptyList());
                    return SubjectScoreDto.toDto(summary, subjectScores);
                })
                .toList();

        return StudentScoreSummaryDto.builder()
                .number(student.getNumber())
                .studentName(student.getStudent().getName())
                .subjects(subjectScoreList)
                .build();
    }

    @Transactional
    public void updateSummaryForClass(int year, int semester, int grade, int classNum) {
        Classroom classroom = classroomService.findClassroom(year, grade, classNum);
        List<ClassroomStudent> students = classroomStudentService.findByClassroom(classroom);

        List<Subject> subjects = evaluationMethodService.findSubject(year, semester, grade);

        for (Subject subject : subjects) {
            List<EvaluationMethod> methods = evaluationMethodService.findAllBySubjectAndYearAndSemesterAndGrade(subject, year, semester, grade);

            // 전체 학생 중 점수가 있는 학생만 필터링
            Map<ClassroomStudent, List<Score>> scoreMap = students.stream()
                    .map(student -> Map.entry(student, scoreRepository.findAllByStudentAndEvaluationMethodIn(student, methods)))
                    .filter(entry -> !entry.getValue().isEmpty()) // 점수가 아예 없는 경우 제외
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (scoreMap.isEmpty()) continue; // 이 과목에 대한 점수가 아무도 없으면 summary 건너뜀


            Map<Long, Double> totalMap = scoreMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().getId(),
                            e -> e.getValue().stream().mapToDouble(Score::getWeightedScore).sum()
                    ));

            Map<Long, Integer> rankMap = ScoreLevelUtil.calculateRanks(totalMap);
            double avg = ScoreStatUtil.average(totalMap.values().stream().toList());
            double stdDev = ScoreStatUtil.standardDeviation(totalMap.values());

            scoreSummaryRepository.deleteBySubjectAndClassroomStudentIn(subject, students);

            for (Map.Entry<ClassroomStudent, List<Score>> entry : scoreMap.entrySet()) {
                ClassroomStudent student = entry.getKey();
                List<Score> scores = entry.getValue();

                double weightedSum = scores.stream().mapToDouble(Score::getWeightedScore).sum();
                double rawScaledSum = scores.stream()
                        .mapToDouble(s -> (s.getRawScore() / s.getEvaluationMethod().getFullScore()) * s.getEvaluationMethod().getWeight())
                        .sum();

                int originalScore = (int) Math.round(rawScaledSum);
                int rank = rankMap.get(student.getId());
                int gradeValue = ScoreLevelUtil.getGrade(rank, students.size());
                String achievement = ScoreLevelUtil.toAchievementLevel(gradeValue);

                scoreSummaryRepository.save(ScoreSummary.builder()
                        .classroomStudent(student)
                        .subject(subject)
                        .originalScore(originalScore)
                        .sumScore(weightedSum)
                        .average(avg)
                        .stdDeviation(stdDev)
                        .rank(rank)
                        .grade(gradeValue)
                        .achievementLevel(achievement)
                        .totalStudentCount(students.size())
                        .build());
            }
        }
    }

    @Transactional
    public void saveFeedback(String username, ScoreFeedbackRequestDto requestDto) {
        // 교사 권환 확인
        teacherService.authenticate(username);

        // 추후, 담당 과목인지 권한 확인 필요 고려
        ScoreSummary summary = scoreSummaryRepository.findById(requestDto.getScoreSummaryId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCORE_SUMMARY_NOT_FOUND));

        summary.update(requestDto.getFeedback());

        // Notification용
        User user = summary.getClassroomStudent().getStudent().getUser();
        String content = summary.getSubject().getName() + "과목의 피드백이 등록되었습니다.";

        eventPublisher.publishEvent(new SendFeedbackFcmEvent(summary));
        notificationService.sendNotification(user, content);
    }

    @Transactional
    public void updateFeedback(String username, Long scoreSummaryId, ScoreFeedbackUpdateDto requestDto) {
        // 교사 권환 확인
        teacherService.authenticate(username);

        // 추후, 담당 과목인지 권한 확인 필요 고려
        ScoreSummary summary = scoreSummaryRepository.findById(scoreSummaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCORE_SUMMARY_NOT_FOUND));

        summary.update(requestDto.getFeedback());

        // Notification용
        User user = summary.getClassroomStudent().getStudent().getUser();
        String content = summary.getSubject().getName() + "과목의 피드백이 등록되었습니다.";

        eventPublisher.publishEvent(new SendFeedbackFcmEvent(summary));
        notificationService.sendNotification(user, content);
    }

    // 교사 및 학생 모두 접근 가능
    public ScoreFeedbackDto getFeedback(String username, Long scoreSummaryId) {
        getUserAndValidateAccess(username, scoreSummaryId);

        ScoreSummary summary = scoreSummaryRepository.findById(scoreSummaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCORE_SUMMARY_NOT_FOUND));

        return ScoreFeedbackDto.toDto(summary);
    }

    /**
     * 특정 학생에 대해 “해당 학기(연도, 학기, 학년, 반, 번호)”에 속한 과목들의 피드백을
     * SubjectFeedbackDto 형태로 반환한다.
     */
    public List<SubjectFeedbackDto> getSubjectFeedbacks(
            String username,
            int year,
            int semester,
            int grade,
            int classNum,
            int number
    ) {

        ClassroomStudent classroomStudent = checkValidate(username, year, grade, classNum, number);

        // 2. 해당 학기(연도, 학기, 학년)에 속한 “과목 목록” 조회
        List<Subject> subjectsInThisTerm = evaluationMethodService
                .findSubject(year, semester, grade);

        // 과목 이름만 모아서 필터링용 Set으로 준비
        Set<String> subjectNamesInThisTerm = subjectsInThisTerm.stream()
                .map(Subject::getName)
                .collect(Collectors.toSet());

        // 3. DB에서 이 학생의 모든 ScoreSummary(과목별 요약)를 조회
        List<ScoreSummary> allSummaries = scoreSummaryRepository
                .findAllByClassroomStudent(classroomStudent);

        // 4. “이 학기에 속한 과목(subjectNamesInThisTerm)에 해당하는” ScoreSummary만 필터링
        return allSummaries.stream()
                .filter(summary -> subjectNamesInThisTerm.contains(
                        summary.getSubject().getName()
                ))
                .map(summary -> {
                    // 5. SubjectFeedbackDto로 변환: (과목명, 피드백 내용)
                    return SubjectFeedbackDto.builder()
                            .subjectName(summary.getSubject().getName())
                            .feedback(summary.getFeedback())
                            .build();
                })
                .toList();
    }

    public ScoreSummary findByStudentAndSubject(Long studentId, Long subjectId) {
        return scoreSummaryRepository.findByStudentAndSubject(studentId, subjectId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCORE_SUMMARY_NOT_FOUND));
    }

    private ClassroomStudent checkValidate(String username, int year, int grade, int classNum, int number) {
        User user = userService.getUser(username);

        switch (user.getRole()) {
            case STUDENT -> {
                // 본인이 요청한 게 맞는지 확인
                ClassroomStudent cs = classroomStudentRepository.findByStudentUser(user).orElseThrow(
                        () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND)
                );
                if (cs.getNumber() != number) {
                    throw new CustomException(HANDLE_ACCESS_DENIED);
                }
                return cs;
            }

            case TEACHER -> {
                Teacher teacher = teacherService.authenticate(username);

                Classroom classroom = classroomService.findClassroom(year, grade, classNum);

                log.info("teacher " + teacher);
                log.info("classroom : " + classroom.getClassNum());
                return classroomStudentRepository.findByClassroomAndNumber(classroom, number)
                        .orElseThrow(() -> new CustomException(HANDLE_ACCESS_DENIED));
            }

            default -> throw new CustomException(HANDLE_ACCESS_DENIED);
        }
    }

    private void getUserAndValidateAccess(String username, Long scoreSummaryId) {
        User user = userService.getUser(username);
        ScoreSummary summary = scoreSummaryRepository.findById(scoreSummaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCORE_SUMMARY_NOT_FOUND));

        // 학생 → 본인만 접근 가능
        if (user.getRole() == STUDENT && !user.getId().equals(summary.getClassroomStudent().getStudent().getUser().getId())) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 교사 → 조회 허용
        if (user.getRole() == TEACHER) {
            // 교사 권한만 체크
            teacherService.authenticate(username); // 추가 보안
        }
    }
}