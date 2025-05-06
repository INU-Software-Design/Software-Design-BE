package com.neeis.neeis.domain.parent;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "relation_ship", nullable = false)
    private String relationShip;

    @Column(unique = true, nullable = false)
    private String phone;

    @JoinColumn(name = "student_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;

    @Builder
    private Parent(String name, String relationShip, String phone, Student student) {
        this.name = name;
        this.relationShip = relationShip;
        this.phone = phone;
        this.student = student;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }
}
