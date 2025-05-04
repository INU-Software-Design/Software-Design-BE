package com.neeis.neeis.domain.score;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findAllByStudentAndEvaluationMethodIn(ClassroomStudent student, List<EvaluationMethod> methods);

    List<Score> findAllByEvaluationMethod(EvaluationMethod evaluationMethod);

    Optional<Score> findByEvaluationMethodAndStudent(EvaluationMethod evaluationMethod, ClassroomStudent student);

    List<Score> findAllByStudentAndEvaluationMethod_YearAndEvaluationMethod_Semester(
            ClassroomStudent student, int year, int semester);
}
