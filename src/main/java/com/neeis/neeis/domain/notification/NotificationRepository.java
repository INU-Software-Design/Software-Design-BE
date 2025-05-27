package com.neeis.neeis.domain.notification;

import com.neeis.neeis.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

    // 페이징 처리된 알림 조회 (username 기준)
    Page<Notification> findByUser(User user, Pageable pageable);

    // 알림 ID로 단건 조회
    Optional<Notification> findById(Long id);

    // 모든 알림 읽음 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.username = :username AND n.isRead = false")
    void markAllAsReadByUsername(String username);
}
