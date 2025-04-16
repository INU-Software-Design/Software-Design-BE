package com.neeis.neeis.domain.counsel;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counseling extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDate dateTime;

    @Column(nullable = true)
    private String nextPlan;

    @Column(nullable = false)
    private Boolean isPublic;

    @JoinColumn(name = "student_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Builder
    private Counseling(String content, LocalDate dateTime, String nextPlan, Boolean isPublic, Student student, Teacher teacher){
        this.content = content;
        this.dateTime = dateTime;
        this.nextPlan = nextPlan;
        this.isPublic = isPublic;
        this.student = student;
        this.teacher = teacher;
    }
}
