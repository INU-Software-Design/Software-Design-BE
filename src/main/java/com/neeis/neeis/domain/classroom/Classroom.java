package com.neeis.neeis.domain.classroom;

import com.neeis.neeis.domain.teacher.Teacher;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Classroom {

    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column(nullable = false)
    private int grade;

    @Column(nullable = false)
    private int classNum;

    @Column(nullable = false)
    private int year;

    @JoinColumn(name = "teacher_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Teacher teacher;

    @Builder
    private Classroom(int grade, int classNum, int year, Teacher teacher) {
        this.grade = grade;
        this.classNum = classNum;
        this.year = year;
        this.teacher = teacher;
    }
}
