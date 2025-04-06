package com.neeis.neeis.domain.student;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String num;

    private String name;

    private String image;

    private String gender;

    private String ssn;

    private String address;

    private String phone;

    private LocalDate admissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Student(String num, String name, String image, String gender, String ssn, String address, String phone, LocalDate admissionDate, Classroom classroom) {
        this.num = num;
        this.name = name;
        this.image = image;
        this.gender = gender;
        this.ssn = ssn;
        this.address = address;
        this.phone = phone;
        this.admissionDate = admissionDate;
        this.classroom = classroom;
    }
}
