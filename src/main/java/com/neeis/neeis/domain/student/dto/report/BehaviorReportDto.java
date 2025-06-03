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
    private List<String> specialActivities;
    private List<String> awards;

    // 기존 BehaviorDetailResponseDto에서 변환
    public static BehaviorReportDto from(BehaviorDetailResponseDto behavior) {
        return BehaviorReportDto.builder()
                .behaviorGrade(behavior.getBehavior())
                .comprehensiveOpinion(behavior.getGeneralComment())
                .specialActivities(List.of()) // 별도 엔티티에서 조회 필요
                .awards(List.of()) // 별도 엔티티에서 조회 필요
                .build();
    }

    // 특별활동과 수상내역까지 포함한 생성자
    public static BehaviorReportDto from(BehaviorDetailResponseDto behavior,
                                         List<String> specialActivities,
                                         List<String> awards) {
        return BehaviorReportDto.builder()
                .behaviorGrade(behavior.getBehavior())
                .comprehensiveOpinion(behavior.getGeneralComment())
                .specialActivities(specialActivities != null ? specialActivities : List.of())
                .awards(awards != null ? awards : List.of())
                .build();
    }
}