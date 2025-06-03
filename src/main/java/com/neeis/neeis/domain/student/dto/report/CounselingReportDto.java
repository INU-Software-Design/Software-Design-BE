package com.neeis.neeis.domain.student.dto.report;

import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class CounselingReportDto {
    private int totalSessions;
    private List<CounselingRecordDto> records;

    // 기존 CounselDetailDto 리스트에서 변환
    public static CounselingReportDto from(List<CounselDetailDto> counsels) {
        List<CounselingRecordDto> records = counsels.stream()
                .map(CounselingRecordDto::from)
                .toList();

        return CounselingReportDto.builder()
                .totalSessions(counsels.size())
                .records(records)
                .build();
    }
}
