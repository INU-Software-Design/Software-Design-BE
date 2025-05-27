package com.neeis.neeis.domain.notification.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class NotificationPageResDto {
    private final int totalPages;
    private final List<NotificationResDto> notifications;

    @Builder
    public NotificationPageResDto(int totalPages, List<NotificationResDto> notifications) {
        this.totalPages = totalPages;
        this.notifications = notifications;
    }

    public static NotificationPageResDto of(int totalPages, List<NotificationResDto> list) {
        return NotificationPageResDto.builder()
                .totalPages(totalPages)
                .notifications(list)
                .build();
    }
}
