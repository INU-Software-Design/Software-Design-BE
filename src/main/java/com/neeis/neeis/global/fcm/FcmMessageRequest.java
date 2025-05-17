package com.neeis.neeis.global.fcm;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;


@Getter
@NoArgsConstructor
public class FcmMessageRequest {
    private String token;
    private String title;
    private String body;
    private Map<String, String> data;

    @Builder
    public FcmMessageRequest(String token, String title, String body, Map<String, String> data) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.data = data;
    }

    public static FcmMessageRequest of(String token, String title, String body, Map<String, String> data) {
        return FcmMessageRequest.builder()
                .token(token)
                .title(title)
                .body(body)
                .data(data)
                .build();
    }
}