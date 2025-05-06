package com.neeis.neeis.domain.scoreSummary.dto.res;

import com.neeis.neeis.domain.scoreSummary.ScoreSummary;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ScoreFeedbackDto {
    private final String feedback;

    @Builder
    private ScoreFeedbackDto(String feedback){
        this.feedback = feedback;
    }

    public static ScoreFeedbackDto toDto(ScoreSummary scoreSummary) {
        return ScoreFeedbackDto.builder()
                .feedback(scoreSummary.getFeedback())
                .build();
    }
}
