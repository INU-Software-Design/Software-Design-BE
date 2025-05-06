package com.neeis.neeis.domain.scoreSummary;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScoreSummaryRepository extends JpaRepository<ScoreSummary, Long> {
 List<ScoreSummary> findAllByClassroomStudent(ClassroomStudent students);
 void deleteBySubjectAndClassroomStudentIn(Subject subject, List<ClassroomStudent> students);
 @Query(value = "SELECT * FROM score_summary s WHERE s.classroom_student_id = :studentId AND s.subject_id = :subjectId", nativeQuery = true)
 Optional<ScoreSummary> findByStudentAndSubject(@Param("studentId") Long studentId,
                                                       @Param("subjectId") Long subjectId);
}

