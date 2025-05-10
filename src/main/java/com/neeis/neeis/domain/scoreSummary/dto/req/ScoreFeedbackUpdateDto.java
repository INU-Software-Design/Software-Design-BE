package com.neeis.neeis.domain.scoreSummary.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScoreFeedbackUpdateDto {

    @Schema(description = "피드백 내용", example = "성적이 많이 향상되었습니다. 지속적으로 노력하세요!")
    @NotBlank(message = "피드백 내용은 필수입니다.")
    private String feedback;

    @Builder
    private ScoreFeedbackUpdateDto(String feedback) {
        this.feedback = feedback;
    }
}
