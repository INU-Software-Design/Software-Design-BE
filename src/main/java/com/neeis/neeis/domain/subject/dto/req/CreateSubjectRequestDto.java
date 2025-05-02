package com.neeis.neeis.domain.subject.dto.req;

import com.neeis.neeis.domain.subject.Subject;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateSubjectRequestDto {
    private String name;

    @Builder
    private CreateSubjectRequestDto(String name) {
        this.name = name;
    }

    public static Subject of(CreateSubjectRequestDto dto) {
        return Subject.builder()
                .name(dto.getName())
                .build();
    }
}
