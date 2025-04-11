package com.neeis.neeis.domain.behavior;

import com.neeis.neeis.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Behavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String behavior;

    @Column(nullable = false)
    private String beFeedback;

    @Column(nullable = false)
    private String attitude;

    @Column(nullable = false)
    private String attFeedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Builder
    private Behavior(String behavior, String beFeedback, String attitude, String attFeedback, Student student) {
        this.behavior = behavior;
        this.beFeedback = beFeedback;
        this.attitude = attitude;
        this.attFeedback = attFeedback;
        this.student = student;
    }
}
