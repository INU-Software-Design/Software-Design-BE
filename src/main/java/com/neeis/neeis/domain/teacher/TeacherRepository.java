package com.neeis.neeis.domain.teacher;

import com.neeis.neeis.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User user);

    Optional<Teacher> findByName(String name);
}