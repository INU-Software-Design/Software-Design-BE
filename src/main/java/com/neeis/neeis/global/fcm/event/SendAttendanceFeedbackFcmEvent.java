package com.neeis.neeis.global.fcm.event;

import com.neeis.neeis.domain.attendance.AttendanceFeedback;
import lombok.Getter;

@Getter
public class SendAttendanceFeedbackFcmEvent {

    private AttendanceFeedback feedback;
    public SendAttendanceFeedbackFcmEvent(AttendanceFeedback feedback) {
        this.feedback = feedback;
    }
}
