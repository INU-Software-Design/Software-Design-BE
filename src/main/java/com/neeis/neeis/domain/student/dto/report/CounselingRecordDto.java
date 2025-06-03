package com.neeis.neeis.domain.student.dto.report;

import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class CounselingRecordDto {
    private LocalDate counselingDate;
    private String counselingType;
    private String content;
    private String counselorName;

    // 기존 CounselDetailDto에서 변환
    public static CounselingRecordDto from(CounselDetailDto counsel) {
        return CounselingRecordDto.builder()
                .counselingDate(counsel.getDateTime())
                .counselingType(counsel.getCategory().name()) // enum to string
                .content(counsel.getContent())
                .counselorName(counsel.getTeacher())
                .build();
    }
}
