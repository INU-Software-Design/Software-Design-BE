package com.neeis.neeis.domain.student.dto.report;


import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class BehaviorReportDto {
    private String behaviorGrade;
    private String comprehensiveOpinion;

    // 기존 BehaviorDetailResponseDto  변환
    public static BehaviorReportDto from(BehaviorDetailResponseDto behavior) {
        return BehaviorReportDto.builder()
                .behaviorGrade(behavior.getBehavior())
                .comprehensiveOpinion(behavior.getGeneralComment())
                .build();
    }
}