package com.neeis.neeis.domain.score;

import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Score extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double rawScore; // 받은 점수
    private Double weightedScore; // 반영된 점수 (raw/full) * weight

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_method_id", nullable = false)
    private EvaluationMethod evaluationMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private ClassroomStudent student;

    @Builder
    private Score(Double rawScore, Double weightedScore, EvaluationMethod evaluationMethod, ClassroomStudent student) {
        this.rawScore = rawScore;
        this.weightedScore = weightedScore;
        this.evaluationMethod = evaluationMethod;
        this.student = student;
    }

    public void update(Double rawScore, Double weightedScore) {
        this.rawScore = rawScore;
        this.weightedScore = weightedScore;
    }
}