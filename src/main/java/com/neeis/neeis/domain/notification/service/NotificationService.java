package com.neeis.neeis.domain.notification.service;

import com.neeis.neeis.domain.notification.Notification;
import com.neeis.neeis.domain.notification.NotificationRepository;
import com.neeis.neeis.domain.notification.dto.res.NotificationPageResDto;
import com.neeis.neeis.domain.notification.dto.res.NotificationResDto;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Transactional
    public void sendNotification(User user, String content) {
        Notification notification = Notification.builder()
                .user(user)
                .content(content)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }


    public NotificationPageResDto getNotifications(String username, int page, int size) {
        User user = userService.getUser(username);
        Page<Notification> notifications = notificationRepository.findByUser(
                user,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return NotificationPageResDto.of(
                notifications.getTotalPages(),
                notifications.getContent().stream()
                        .map(NotificationResDto::toDto)
                        .toList()
        );
    }

    // 읽음 처리
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(String username) {
        notificationRepository.markAllAsReadByUsername(username);
    }
}