package com.neeis.neeis.domain.attendance;

import com.neeis.neeis.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @JoinColumn(name ="student_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;

    @Builder
    private Attendance(LocalDate date, AttendanceStatus status, Student student) {
        this.date = date;
        this.status = status;
        this.student = student;
    }
}
