package com.neeis.neeis.global.fcm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;


@Getter
@NoArgsConstructor
@Schema(description = "FCM 테스트용 요청 DTO")
public class FcmMessageRequest {

    @Schema(description = "FCM 디바이스 토큰", example = "your-fcm-test-token")
    private String token;

    @Schema(description = "알림 제목", example = "FCM 테스트")
    private String title;

    @Schema(description = "알림 본문", example = "이것은 테스트 메시지입니다.")
    private String body;

    @Schema(description = "추가 데이터 (선택)", example = "{\"type\": \"TEST\", \"customKey\": \"value\"}")
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