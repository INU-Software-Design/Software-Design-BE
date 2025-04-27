package com.neeis.neeis.domain.student;

import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false, unique = true)
    private String ssn;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDate admissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    private Student( String name, String image, String gender, String ssn, String address, String phone, LocalDate admissionDate, User user) {
        this.name = name;
        this.image = image;
        this.gender = gender;
        this.ssn = ssn;
        this.address = address;
        this.phone = phone;
        this.admissionDate = admissionDate;
        this.user = user;
    }
}
