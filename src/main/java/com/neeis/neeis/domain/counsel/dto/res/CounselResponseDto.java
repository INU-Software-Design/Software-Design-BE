package com.neeis.neeis.domain.counsel.dto.res;

import com.neeis.neeis.domain.counsel.Counseling;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CounselResponseDto {
    private final Long counselId;

    @Builder
    private CounselResponseDto(Long counselId) {
        this.counselId = counselId;
    }

    public static CounselResponseDto toDto(Counseling counseling){
        return CounselResponseDto.builder()
                .counselId(counseling.getId())
                .build();
    }
}
