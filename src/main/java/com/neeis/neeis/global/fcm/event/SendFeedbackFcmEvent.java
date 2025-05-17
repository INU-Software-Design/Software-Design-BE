package com.neeis.neeis.global.fcm.event;

import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import lombok.Getter;

@Getter
public class SendFeedbackFcmEvent {
    private final ScoreSummary scoreSummary;

    public SendFeedbackFcmEvent(ScoreSummary scoreSummary) {
        this.scoreSummary = scoreSummary;
    }

}
