package com.neeis.neeis.domain.behavior;

import com.neeis.neeis.domain.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BehaviorRepository extends JpaRepository<Behavior, Long> {
    Optional<Behavior> findByStudentId(Long studentId);
}
