package com.neeis.neeis.domain.semester;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByYearAndSemester(int year, int semester);

}
