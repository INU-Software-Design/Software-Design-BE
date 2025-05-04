package com.neeis.neeis.domain.scoreSummary;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreSummaryRepository extends JpaRepository<ScoreSummary, Long> {
 List<ScoreSummary> findAllByClassroomStudent(ClassroomStudent students);
 void deleteBySubjectAndClassroomStudentIn(Subject subject, List<ClassroomStudent> students);
}

