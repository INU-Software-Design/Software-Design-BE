package com.neeis.neeis.domain.classroomStudent;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = "SELECT cs FROM ClassroomStudent cs " +
            "WHERE cs.student.user = :user")
    Optional<ClassroomStudent> findByStudentUser(@Param("user") User user);

    /**
     * 학생 User로 모든 ClassroomStudent 조회 (연도별)
     */
    @Query("SELECT cs FROM ClassroomStudent cs WHERE cs.student.user = :user ORDER BY cs.classroom.year DESC")
    List<ClassroomStudent> findAllByStudentUserOrderByYear(@Param("user") User user);

    /**
     * 특정 연도, 학년, 반의 모든 학생 조회
     */
    @Query("SELECT cs FROM ClassroomStudent cs WHERE cs.classroom.year = :year AND cs.classroom.grade = :grade AND cs.classroom.classNum = :classNum ORDER BY cs.number")
    List<ClassroomStudent> findByClassroomInfo(@Param("year") int year, @Param("grade") int grade, @Param("classNum") int classNum);

    /**
     * 학생 ID와 연도로 ClassroomStudent 조회
     */
    @Query("SELECT cs FROM ClassroomStudent cs WHERE cs.student.id = :studentId AND cs.classroom.year = :year")
    Optional<ClassroomStudent> findByStudentIdAndClassroomYear(@Param("studentId") Long studentId, @Param("year") int year);

    /**
     * 학생 ID로 ClassroomStudent 조회 (연도 내림차순) - 기존에 있던 메서드
     */
    @Query("SELECT cs FROM ClassroomStudent cs WHERE cs.student.id = :studentId ORDER BY cs.classroom.year DESC")
    List<ClassroomStudent> findByStudentIdOrderByClassroomYearDesc(@Param("studentId") Long studentId);
}