package com.neeis.neeis.domain.classroom;

import com.neeis.neeis.domain.teacher.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    @Query(value = "select * from classroom " +
            "where teacher_id = :teacherId and year = :year;", nativeQuery = true)
    Optional<Classroom> findByTeacherAndYear(Long teacherId, int year);
}
