package com.neeis.neeis.domain.notification;

import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 알림 메시지 내용
    @Column(nullable = false)
    private String content;

    // 읽음 여부
    private boolean isRead;

    @Builder
    public Notification(User user,  String content, boolean isRead) {
        this.user = user;
        this.content = content;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }

}
