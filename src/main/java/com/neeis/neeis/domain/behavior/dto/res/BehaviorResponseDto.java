package com.neeis.neeis.domain.behavior.dto.res;

import com.neeis.neeis.domain.behavior.Behavior;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BehaviorResponseDto {
    private final Long behaviorId;

    @Builder
    private BehaviorResponseDto(Long behaviorId) {
        this.behaviorId = behaviorId;
    }

    public static BehaviorResponseDto of(Behavior behavior) {
        return BehaviorResponseDto.builder()
                .behaviorId(behavior.getId())
                .build();
    }
}