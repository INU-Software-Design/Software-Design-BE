package com.neeis.neeis.domain.counsel.dto.res;

import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.Counsel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CounselDetailDto {
    private final Long id;
    private final CounselCategory category;
    private final String content;
    private final String nextPlan;
    private final LocalDate dateTime;
    private final String teacher;
    private final Boolean isPublic;

    @Builder
    private CounselDetailDto(Long id, CounselCategory category, String content, String nextPlan, LocalDate dateTime, String teacher, Boolean isPublic) {
       this.id = id;
       this.category = category;
       this.content = content;
       this.nextPlan = nextPlan;
       this.dateTime = dateTime;
       this.teacher = teacher;
       this.isPublic = isPublic;
    }

    public static CounselDetailDto toDto(Counsel counsel) {
        return CounselDetailDto.builder()
                .id(counsel.getId())
                .category(counsel.getCategory())
                .content(counsel.getContent())
                .nextPlan(counsel.getNextPlan())
                .dateTime(counsel.getDateTime())
                .isPublic(counsel.getIsPublic())
                .teacher(counsel.getTeacher().getName())
                .build();
    }
}
