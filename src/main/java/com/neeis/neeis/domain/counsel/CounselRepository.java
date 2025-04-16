package com.neeis.neeis.domain.counsel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CounselRepository extends JpaRepository<Counsel, Long> {

    Optional<List<Counsel>>  findByStudentId(Long studentId);
}
