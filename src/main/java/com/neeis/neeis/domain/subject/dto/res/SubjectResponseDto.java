package com.neeis.neeis.domain.subject.dto.res;

import com.neeis.neeis.domain.subject.Subject;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SubjectResponseDto {
    private Long id;
    private String name;

    @Builder
    private SubjectResponseDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static SubjectResponseDto toDto(Subject subject) {
        return SubjectResponseDto.builder()
                .id(subject.getId())
                .name(subject.getName())
                .build();
    }
}
