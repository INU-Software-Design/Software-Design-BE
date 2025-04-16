package com.neeis.neeis.domain.student;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByPhone(String phone);

}
