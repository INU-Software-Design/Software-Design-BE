package com.neeis.neeis.domain.classroomStudent;

import com.neeis.neeis.domain.classroom.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long> {
    @Query(value = "SELECT COUNT(cs) > 0 FROM classroom_student cs " +
            "WHERE cs.student_id = :studentId AND cs.classroom_id.teacher_id = :teacherId AND cs.classroom.year = :year", nativeQuery = true)
    boolean existsByStudentAndTeacher(Long studentId, Long teacherId, int year);

    List<ClassroomStudent> findByClassroom(Classroom classroom);

    @Query(value = "SELECT cs.* FROM classroom_student cs " +
            "JOIN classroom c ON cs.classroom_id = c.id " +
            "WHERE cs.student_id = :studentId AND c.year = :year", nativeQuery = true)
    Optional<ClassroomStudent> findByStudentAndClassroomYear(Long studentId, int year);

    @Query(value = "SELECT cs.* FROM classroom_student cs " +
            "WHERE cs.student_id = :studentId AND cs.classroom_id = :classroomId ", nativeQuery = true)
    Optional<ClassroomStudent> findByStudentAndClassroom(Long studentId, Long classroomId);


    Optional<ClassroomStudent> findByClassroomAndNumber(Classroom classroom, int number);

}
