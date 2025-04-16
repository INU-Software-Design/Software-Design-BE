package com.neeis.neeis.domain.teacher;

import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    private Teacher(String name, String phone, String email, User user) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.user = user;
    }
}
