package com.neeis.neeis.domain.attendance.dto.req;

import com.neeis.neeis.domain.attendance.AttendanceFeedback;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendanceFeedbackReqDto {
    private String feedback;

    @Builder
    private AttendanceFeedbackReqDto(String feedback) {
        this.feedback = feedback;
    }

    public static AttendanceFeedback of(AttendanceFeedbackReqDto dto, ClassroomStudent classroomStudent) {
        return AttendanceFeedback.builder()
                .classroomStudent(classroomStudent)
                .feedback(dto.getFeedback())
                .build();
    }
}
