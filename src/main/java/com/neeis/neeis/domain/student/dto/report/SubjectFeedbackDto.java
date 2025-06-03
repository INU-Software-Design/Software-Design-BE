package com.neeis.neeis.domain.student.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectFeedbackDto {
    private String subjectName;
    private String feedback;
}