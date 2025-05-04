package com.neeis.neeis.domain.scoreSummary.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.score.service.ScoreService;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import com.neeis.neeis.domain.scoreSummary.ScoreSummaryRepository;
import com.neeis.neeis.domain.scoreSummary.dto.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.SubjectScoreDto;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreSummaryService {
    private final ScoreSummaryRepository scoreSummaryRepository;
    private final ClassroomService classroomService;
    private final ClassroomStudentService classroomStudentService;
    private final EvaluationMethodService evaluationMethodService;
    private final ScoreRepository scoreRepository;
    private final TeacherService teacherService;

    public StudentScoreSummaryDto getStudentSummary(String username, int year, int semester, int grade, int classNum, int number) {
        teacherService.authenticate(username);

        Classroom classroom = classroomService.findClassroom(year, grade, classNum);
        ClassroomStudent student = classroomStudentService.findByClassroomAndNumber(classroom, number);

        // 1. 해당 학기의 점수 불러오기
        List<Score> scores = scoreRepository
                .findAllByStudentAndEvaluationMethod_YearAndEvaluationMethod_Semester(student, year, semester);

        // 2. 과목 이름별로 점수 분류
        Map<String, List<Score>> scoreBySubject = scores.stream()
                .collect(Collectors.groupingBy(s -> s.getEvaluationMethod().getSubject().getName()));

        // 3. 해당 학기 과목 리스트
        Set<String> subjectNamesInThisSemester = scores.stream()
                .map(s -> s.getEvaluationMethod().getSubject().getName())
                .collect(Collectors.toSet());

        // 4. 모든 ScoreSummary 중에서 해당 학기의 과목에 속한 것만 필터링
        List<ScoreSummary> allSummaries = scoreSummaryRepository.findAllByClassroomStudent(student);
        List<ScoreSummary> filteredSummaries = allSummaries.stream()
                .filter(summary -> subjectNamesInThisSemester.contains(summary.getSubject().getName()))
                .toList();

        // 5. DTO 변환
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
    public void saveSummaries(int year, int semester, int grade, int classNum) {
        Classroom classroom = classroomService.findClassroom(year, grade, classNum);
        List<ClassroomStudent> students = classroomStudentService.findByClassroom(classroom);

        Set<Subject> subjects = new HashSet<>(evaluationMethodService.findSubject(year, semester, grade));

        for (Subject subject : subjects) {
            List<EvaluationMethod> methods = evaluationMethodService
                    .findAllBySubjectAndYearAndSemesterAndGrade(subject, year, semester, grade);

            // 학생별 점수 합산
            Map<ClassroomStudent, List<Score>> scoreMap = new HashMap<>();
            for (ClassroomStudent student : students) {
                List<Score> scores = scoreRepository.findAllByStudentAndEvaluationMethodIn(student, methods);
                scoreMap.put(student, scores);
            }

            // 총점 기준으로 석차 산정 (반영 점수 기반)
            Map<Long, Double> totalMap = scoreMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().getId(),
                            e -> e.getValue().stream().mapToDouble(Score::getWeightedScore).sum()
                    ));

            Map<Long, Integer> rankMap = ScoreLevelUtil.calculateRanks(totalMap);
            double avg = totalMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double stdDev = ScoreStatUtil.standardDeviation(totalMap.values());

            // 기존 요약 삭제
            scoreSummaryRepository.deleteBySubjectAndClassroomStudentIn(subject, students);

            for (Map.Entry<ClassroomStudent, List<Score>> entry : scoreMap.entrySet()) {
                ClassroomStudent student = entry.getKey();
                List<Score> scores = entry.getValue();

                // 반영 점수 총합
                double weighted = scores.stream().mapToDouble(Score::getWeightedScore).sum();

                // 원점수 ( (raw / fullScore * weight) 합산 후 소수 첫째 자리 반올림)
                double rawScaledSum = scores.stream()
                        .mapToDouble(s -> {
                            EvaluationMethod e = s.getEvaluationMethod();
                            return (s.getRawScore() / e.getFullScore()) * e.getWeight();
                        }).sum();
                int originalScore = Math.toIntExact(Math.round(rawScaledSum)); // 정수로 반올림

                int rank = rankMap.get(student.getId());
                int gradeValue = ScoreLevelUtil.getGrade(rank, students.size());
                String achievement = ScoreLevelUtil.toAchievementLevel(gradeValue);

                scoreSummaryRepository.save(ScoreSummary.builder()
                        .classroomStudent(student)
                        .subject(subject)
                        .originalScore(originalScore)
                        .sumScore(weighted)
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
}