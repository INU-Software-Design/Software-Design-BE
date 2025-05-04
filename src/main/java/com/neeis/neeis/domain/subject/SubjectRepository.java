package com.neeis.neeis.domain.subject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
   Optional<Subject> findByName(String name);
   Boolean existsSubjectByName(String name);
   List<Subject> findAllByOrderByNameAsc();
}
