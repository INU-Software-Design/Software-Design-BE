package com.neeis.neeis.domain.attendance.dto.res;

import com.neeis.neeis.domain.attendance.AttendanceFeedback;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AttendanceFeedbackResDto {
    private final Long feedbackId;
    private final String feedback;

    @Builder
    private AttendanceFeedbackResDto(Long feedbackId, String feedback) {
        this.feedbackId = feedbackId;
        this.feedback = feedback;
    }

    public static AttendanceFeedbackResDto toDto(AttendanceFeedback feedback) {
        return AttendanceFeedbackResDto.builder()
                .feedbackId(feedback.getId())
                .feedback(feedback.getFeedback())
                .build();
    }
}
