package com.neeis.neeis.domain.behavior.dto.req;

import com.neeis.neeis.domain.behavior.Behavior;
import com.neeis.neeis.domain.student.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BehaviorRequestDto {

    @NotBlank
    @Schema(example = "행동")
    private String behavior;

    @Schema(example = "행동 피드백")
    private String beFeedback;

    @NotBlank
    @Schema(example = "태도")
    private String attitude;

    @Schema(example = "태도 피드백")
    private String attFeedback;

    @Builder
    private BehaviorRequestDto(String behavior, String beFeedback, String attitude, String attFeedback) {
        this.behavior = behavior;
        this.beFeedback = beFeedback;
        this.attitude = attitude;
        this.attFeedback = attFeedback;
    }

    public static Behavior of(BehaviorRequestDto dto, Student student ) {
        return Behavior.builder()
                .behavior(dto.getBehavior())
                .beFeedback(dto.getBeFeedback())
                .attitude(dto.getAttitude())
                .attFeedback(dto.getAttFeedback())
                .student(student)
                .build();
    }

}
