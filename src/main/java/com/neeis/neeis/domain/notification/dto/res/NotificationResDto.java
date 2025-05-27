package com.neeis.neeis.domain.notification.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neeis.neeis.domain.notification.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResDto {
    private final Long id;
    private final String content;
    private final boolean isRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    private final LocalDateTime createdAt;

    @Builder
    public NotificationResDto(Long id, String content, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static NotificationResDto toDto(Notification notification) {
        return NotificationResDto.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}