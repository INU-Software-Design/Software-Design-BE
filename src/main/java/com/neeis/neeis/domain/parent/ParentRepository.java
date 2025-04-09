package com.neeis.neeis.domain.parent;

import com.neeis.neeis.domain.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    List<Parent> findByStudent(Student student);
}
