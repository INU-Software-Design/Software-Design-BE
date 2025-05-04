package com.neeis.neeis.domain.teacherSubject;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.teacher.Teacher;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Builder
    private TeacherSubject(Teacher teacher, Subject subject) {
        this.teacher = teacher;
        this.subject = subject;
    }

    public void update(Teacher teacher, Subject subject) {
        this.teacher = teacher;
        this.subject = subject;
    }
}
