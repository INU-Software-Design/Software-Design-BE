package com.neeis.neeis.domain.counsel.dto.res;

import com.neeis.neeis.domain.counsel.Counsel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CounselResponseDto {
    private final Long counselId;

    @Builder
    private CounselResponseDto(Long counselId) {
        this.counselId = counselId;
    }

    public static CounselResponseDto toDto(Counsel counsel){
        return CounselResponseDto.builder()
                .counselId(counsel.getId())
                .build();
    }
}
