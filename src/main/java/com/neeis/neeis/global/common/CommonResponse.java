package com.neeis.neeis.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)// Null 값인 필드 json으로 보낼 시 제외
//@RequiredArgsConstructor(staticName = "of") 이 어노테이션으로 아래 static 메소드를 만들 수 있음.
public class CommonResponse<T> {
    private final String message;
    private final T response;

    @Builder
    private CommonResponse(String message, T response) {
        this.message = message;
        this.response = response;
    }

    // <제네릭 타입> 반환타입 메소드이름()
    // 제네릭 타입을 정의해야 메소드를 호출할 때마다 다른 타입을 사용할 수 있다
    public static <T> CommonResponse<T> from(String message, T response) {
        return new CommonResponse<T>(message, response);

    }

    // response 없이 메시지만 보낼 때
    public static CommonResponse<Object> from(String message){
        return CommonResponse.builder()
                .message(message)
                .build();
    }
}
