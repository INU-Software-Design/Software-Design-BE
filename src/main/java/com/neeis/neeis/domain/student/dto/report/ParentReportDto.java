package com.neeis.neeis.domain.student.dto.report;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParentReportDto {
    private List<ParentInfo> parents;

    @Getter
    @Builder
    public static class ParentInfo {
        private String name;
        private String relationship;  // 부, 모, 보호자 등
        private String phoneNumber;
    }
}