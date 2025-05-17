package com.neeis.neeis.global.fcm.event;

import com.neeis.neeis.domain.score.Score;
import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import lombok.Getter;

@Getter
public class SendScoreFcmEvent {
    private ScoreSummary scoreSummary;

    public SendScoreFcmEvent(ScoreSummary scoreSummary) {
        this.scoreSummary = scoreSummary;
    }
}
