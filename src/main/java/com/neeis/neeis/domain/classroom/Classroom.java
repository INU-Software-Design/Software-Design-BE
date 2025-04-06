package com.neeis.neeis.domain.classroom;

import com.neeis.neeis.domain.teacher.Teacher;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Classroom {

    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long id;

    private int grade;

    private int number;

    private int year;

    @JoinColumn(name = "teacher_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Teacher teacher;

    @Builder
    private Classroom(int grade, int number, int year, Teacher teacher) {
        this.grade = grade;
        this.number = number;
        this.year = year;
        this.teacher = teacher;
    }
}
