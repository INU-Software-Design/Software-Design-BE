package com.neeis.neeis.domain.notification.controller;

import com.neeis.neeis.domain.notification.dto.res.NotificationPageResDto;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "[학생 전용] 알림 목록 조회", description = "사용자의 알림 목록을 페이지 단위로 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<NotificationPageResDto>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        NotificationPageResDto response = notificationService.getNotifications(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_NOTIFICATION.getMessage(), response));
    }

    @Operation(summary = "[학생 전용] 개별 알림 읽음 처리", description = "특정 알림을 읽음 상태로 처리합니다.")
    @PatchMapping("/read/{notificationId}")
    public ResponseEntity<CommonResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "[학생 전용] 전체 알림 읽음 처리", description = "현재 로그인한 사용자의 모든 알림을 읽음 상태로 처리합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<CommonResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
