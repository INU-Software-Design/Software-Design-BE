package com.neeis.neeis.domain.parent;

import com.neeis.neeis.domain.student.Student;
import jakarta.persistence.*;

@Entity
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String relationShip;

    @Column(unique = true, nullable = false)
    private String contact_num;

    @JoinColumn(name = "student_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;
}
