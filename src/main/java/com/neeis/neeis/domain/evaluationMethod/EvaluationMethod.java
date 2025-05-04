package com.neeis.neeis.domain.evaluationMethod;

import com.neeis.neeis.domain.evaluationMethod.dto.req.UpdateEvaluationMethodDto;
import com.neeis.neeis.domain.subject.Subject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.sql.Update;


/*
Ex) 2025년도 1학기 과목의 평가방식을 등록하려고 한다.
이때 필요한 데이터
 - 과목 배정된 교사 (teacherSubject)
 - 시험 구분 (자필/수행) (examType)
 - 학기 (2025년도 1학기) (semester)
 - 학년 (grade)
 - 고사명
 - 반영 비율
 - 만점 점수
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "evaluation_method",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_evaluation_method",
                columnNames = {"subject_id", "year", "semester", "grade"}
        )
)
public class EvaluationMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private int year;  // 연도

    @Column(nullable = false)
    private int semester; //학기

    @Column(nullable = false)
    private int grade; // 학년

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExamType examType; //자필 수행

    @Column(nullable = false)
    private String title;  // 고사명

    @Column(nullable = false)
    private double weight;  // 반영 비율

    @Column(nullable = false)
    private double fullScore; // 만점 점수

    @Builder
    private EvaluationMethod(ExamType examType, Subject subject, int year, int semester, int grade, String title, double weight, double fullScore ) {
        this.examType = examType;
        this.subject = subject;
        this.year = year;
        this.semester = semester;
        this.grade = grade;
        this.title = title;
        this.weight = weight;
        this.fullScore = fullScore;
    }

    public void update(ExamType examType , UpdateEvaluationMethodDto dto){
        this.examType = examType;
        this.title = dto.getTitle();
        this.weight = dto.getWeight();
        this.fullScore = dto.getFullScore();
    }
}