package com.neeis.neeis.domain.behavior.dto.res;

import com.neeis.neeis.domain.behavior.Behavior;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BehaviorDetailResponseDto {
    private final Long behaviorId;
    private final String behavior;
    private final String generalComment;

    public static BehaviorDetailResponseDto of(Behavior behavior) {
        return BehaviorDetailResponseDto.builder()
                .behaviorId(behavior.getId())
                .behavior(behavior.getBehavior())
                .generalComment(behavior.getGeneralComment())
                .build();
    }
}
