package com.neeis.neeis.domain.evaluationMethod;

import com.neeis.neeis.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationMethodRepository extends JpaRepository<EvaluationMethod, Long> {

    boolean existsBySubjectAndYearAndSemesterAndGradeAndExamTypeAndTitle(
            Subject subject, int year, int semester, int grade, ExamType examType, String title);

    List<EvaluationMethod> findAllBySubjectAndYearAndSemesterAndGrade(
            Subject subject, int year, int semester, int grade);
}