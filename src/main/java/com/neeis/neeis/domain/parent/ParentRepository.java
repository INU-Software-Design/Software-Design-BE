package com.neeis.neeis.domain.parent;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    List<Parent> findByStudent(Student student);
    Optional<Parent> findByUser(User user);
    Optional<Parent> findByPhone(String phone);
}
