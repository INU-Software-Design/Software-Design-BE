package com.neeis.neeis.domain.teacherSubject;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.teacher.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {
    Boolean existsByTeacherAndSubject(Teacher teacher, Subject subject);
    Optional<TeacherSubject> findByTeacherAndSubject(Teacher teacher, Subject subject);
    Optional<TeacherSubject> findByTeacher(Teacher teacher);
}
