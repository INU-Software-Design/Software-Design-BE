package com.neeis.neeis.domain.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    @Query(value = "select * from classroom " +
            "where teacher_id = :teacherId and year = :year;", nativeQuery = true)
    Optional<Classroom> findByTeacherIdAndYear(Long teacherId, int year);

    @Query(value = "select * from classroom " +
            "where year = :year and grade = :grade and class_num = :classNum and teacher_id = :teacherId;", nativeQuery = true)
    Optional<Classroom> findByClassroomInfo(int year, int grade, int classNum, Long teacherId);

    @Query(value = "select * from classroom " +
            "where year = :year and grade = :grade and class_num = :classNum ;", nativeQuery = true)
    Optional<Classroom> findByYearAndGradeAndClassNum(int year, int grade, int classNum);
}
