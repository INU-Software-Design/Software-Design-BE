package com.neeis.neeis.domain.scoreSummary;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double sumScore;  // 합계 (반영 점수 총합)

    @Column(nullable = false)
    private Integer originalScore; // 원점수 (각 평가의 (rawScore / fullScore * weight) 총합, 소수 첫째 자리에서 반올림한 값)

    @Column(nullable = false)
    private Double stdDeviation; //표준편차

    @Column(nullable = false)
    private Integer totalStudentCount; // 해당 과목 수강자수

    @Column(name = "ranking", nullable = false)
    private Integer rank;  // 석차

    @Column(nullable = false)
    private Integer grade; // 등급

    @Column(nullable = false)
    private Double average;  // 평균

    @Column(nullable = false)
    private String achievementLevel; // 성취도 A,B,C..

    @Column(nullable = true)
    private String feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;  //과목

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_student_id", nullable = false)
    private ClassroomStudent classroomStudent;  // 학생

    @Builder
    private ScoreSummary( Double sumScore, Integer originalScore, Double stdDeviation, Integer totalStudentCount,
            Integer rank, Integer grade, Double average, String achievementLevel, ClassroomStudent classroomStudent, Subject subject, String feedback) {
        this.sumScore = sumScore;
        this.originalScore = originalScore;
        this.stdDeviation = stdDeviation;
        this.totalStudentCount = totalStudentCount;
        this.rank = rank;
        this.grade = grade;
        this.average = average;
        this.achievementLevel = achievementLevel;
        this.feedback = feedback;
        this.classroomStudent = classroomStudent;
        this.subject = subject;
    }

    public void update(String feedback){
        this.feedback = feedback;
    }
}