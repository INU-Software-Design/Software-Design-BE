package com.neeis.neeis.domain.attendance;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceFeedbackRepository extends JpaRepository<AttendanceFeedback, Long> {
    Optional<AttendanceFeedback> findByClassroomStudent(ClassroomStudent classroomStudent);
}
