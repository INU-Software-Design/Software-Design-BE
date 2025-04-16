package com.neeis.neeis.domain.counsel.dto.req;

import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.Counsel;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.teacher.Teacher;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CounselRequestDto {

    @Schema(example = "UNIVERSITY | CAREER | FAMILY | ACADEMIC | PERSONAL | OTHER")
    @NotBlank(message = "상담 종류는 필수 입력값입니다.")
    private String category;

    @Schema(example = "상담 내용" )
    @NotBlank(message = "상담 내용은 필수 입력값입니다.")
    private String content;

    @Schema(example = "다음 상담 계획" )
    private String nextPlan;

    @Schema(example = "2025-04-16" )
    @NotNull(message = "상담일자는 필수로 입력해주세요.")
    private LocalDate dateTime;

    @Schema(example = "공개 여부" )
    @NotNull(message = "공개 여부는 필수 입력값입니다.")
    private Boolean isPublic;

    @Builder
    private CounselRequestDto(String category,String content, String nextPlan, LocalDate dateTime, Boolean isPublic) {
        this.category = category;
        this.content = content;
        this.nextPlan = nextPlan;
        this.dateTime = dateTime;
        this.isPublic = isPublic;
    }

    public static Counsel of(Teacher teacher, Student student, CounselRequestDto requestDto, CounselCategory category) {
        return Counsel.builder()
                .category(category)
                .content(requestDto.getContent())
                .nextPlan(requestDto.getNextPlan())
                .dateTime(requestDto.getDateTime())
                .isPublic(requestDto.getIsPublic())
                .teacher(teacher)
                .student(student)
                .build();
    }

}
