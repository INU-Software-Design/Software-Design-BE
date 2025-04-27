package com.neeis.neeis.domain.attendance;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_student_id", nullable = false)
    private ClassroomStudent classroomStudent;

    @Column(nullable = true)
    private String feedback;

    @Builder
    private AttendanceFeedback(ClassroomStudent classroomStudent, String feedback) {
        this.classroomStudent = classroomStudent;
        this.feedback = feedback;
    }

    public void updateContent(String feedback) {
        this.feedback = feedback;
    }
}
