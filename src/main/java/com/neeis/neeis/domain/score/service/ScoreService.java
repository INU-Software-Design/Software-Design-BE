package com.neeis.neeis.domain.score.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.service.EvaluationMethodService;
import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.score.ScoreRepository;
import com.neeis.neeis.domain.score.dto.req.ScoreRequestDto;
import com.neeis.neeis.domain.score.dto.res.ScoreSummaryBySubjectDto;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.teacherSubject.service.TeacherSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final TeacherService teacherService;
    private final TeacherSubjectService teacherSubjectService;
    private final EvaluationMethodService evaluationMethodService;
    private final ClassroomStudentService classroomStudentService;
    private final ClassroomService classroomService;
    private final SubjectService subjectService;
    private final ScoreSummaryService scoreSummaryService;


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

            List<ScoreSummaryBySubjectDto.EvaluationMethodScoreDto> evalList = new ArrayList<>();

            for (EvaluationMethod eval : evaluations) {
                List<Score> scores = scoreRepository.findAllByEvaluationMethod(eval);

                List<ScoreSummaryBySubjectDto.EvaluationMethodScoreDto.StudentScoreDto> scoreDtos = scores.stream()
                        .filter(score -> students.contains(score.getStudent()))
                        .map(score -> {
                            ClassroomStudent s = score.getStudent();
                            return ScoreSummaryBySubjectDto.EvaluationMethodScoreDto.StudentScoreDto.builder()
                                    .studentName(s.getStudent().getName())
                                    .number(s.getNumber())
                                    .rawScore(score.getRawScore())
                                    .weightedScore(score.getWeightedScore())
                                    .build();
                        })
                        .collect(Collectors.toList());

                evalList.add(ScoreSummaryBySubjectDto.EvaluationMethodScoreDto.builder()
                        .title(eval.getTitle())
                        .examType(eval.getExamType().name())
                        .weight(eval.getWeight())
                        .fullScore(eval.getFullScore())
                        .scores(scoreDtos)
                        .build());
            }

            result.add(ScoreSummaryBySubjectDto.builder()
                    .subjectName(subject.getName())
                    .evaluations(evalList)
                    .build());
        }

        return result;
    }

    @Transactional
    public void saveOrUpdateScores(String username, List<ScoreRequestDto> requestList) {
        Teacher teacher = teacherService.authenticate(username);

        Map<String, List<Score>> scoreBuffer = new HashMap<>();

        for (ScoreRequestDto requestDto : requestList) {
            EvaluationMethod eval = evaluationMethodService.findById(requestDto.getEvaluationId());
            Subject subject = eval.getSubject();
            teacherSubjectService.findByTeacherAndSubject(teacher, subject); // 권한 검증

            Classroom classroom = classroomService.findClassroom(eval.getYear(), eval.getGrade(), requestDto.getClassNum());

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

                // 과목별 누적 요약 계산용 버퍼
                String key = eval.getYear() + "_" + eval.getSemester() + "_" + eval.getGrade() + "_" + requestDto.getClassNum();
                scoreBuffer.computeIfAbsent(key, k -> new ArrayList<>()).add(score);
            }
        }

        // 요약 저장 로직은 key 단위로 처리
        for (String key : scoreBuffer.keySet()) {
            String[] parts = key.split("_");
            int year = Integer.parseInt(parts[0]);
            int semester = Integer.parseInt(parts[1]);
            int grade = Integer.parseInt(parts[2]);
            int classNum = Integer.parseInt(parts[3]);

            scoreSummaryService.updateSummaryForClass(year, semester, grade, classNum);
        }

    }

}
