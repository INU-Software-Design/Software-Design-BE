package com.neeis.neeis.domain.notification.controller;

import com.neeis.neeis.domain.notification.dto.res.NotificationPageResDto;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_NOTIFICATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * 사용자 알림 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<CommonResponse<NotificationPageResDto>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        NotificationPageResDto response = notificationService.getNotifications(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_NOTIFICATION.getMessage(), response));
    }

    /**
     * 개별 알림 읽음 처리
     */
    @PatchMapping("/read/{notificationId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 전체 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
