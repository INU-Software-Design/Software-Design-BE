package com.neeis.neeis.global.fcm.event;

import com.neeis.neeis.domain.behavior.Behavior;
import lombok.Getter;

@Getter
public class SendBehaviorFcmEvent {
    private Behavior behavior;
    public SendBehaviorFcmEvent(Behavior behavior) {
        this.behavior = behavior;
    }
}
