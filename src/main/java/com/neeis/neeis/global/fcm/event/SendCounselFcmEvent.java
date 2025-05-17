package com.neeis.neeis.global.fcm.event;

import com.neeis.neeis.domain.counsel.Counsel;
import lombok.Getter;

@Getter
public class SendCounselFcmEvent {
    private final Counsel counsel;
    public SendCounselFcmEvent(Counsel counsel) { this.counsel = counsel; }
}