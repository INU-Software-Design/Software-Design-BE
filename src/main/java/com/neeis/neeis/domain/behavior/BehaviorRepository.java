package com.neeis.neeis.domain.behavior;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BehaviorRepository extends JpaRepository<Behavior, Long> {
    Optional<Behavior> findByClassroomStudentId(Long classroomStudentId);
}
