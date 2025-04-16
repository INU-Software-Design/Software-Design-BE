package com.neeis.neeis.domain.classroomStudent;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassroomStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number; // 반에서의 출석 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Builder
    private ClassroomStudent(int number, Student student, Classroom classroom) {
        this.number = number;
        this.student = student;
        this.classroom = classroom;
    }
}