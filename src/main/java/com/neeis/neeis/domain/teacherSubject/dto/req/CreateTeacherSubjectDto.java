package com.neeis.neeis.domain.teacherSubject.dto.req;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacherSubject.TeacherSubject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateTeacherSubjectDto {

    @NotBlank(message = "과목명은 필수 입력 값입니다.")
    @Schema(description = "과목명", example = "국어")
    private String subjectName;

    @NotBlank(message = "교사명은 필수 입력 값입니다.")
    @Schema(description = "교사명", example = "김철수")
    private String teacherName;

    @Builder
    private CreateTeacherSubjectDto(String subjectName, String teacherName) {
        this.subjectName = subjectName;
        this.teacherName = teacherName;
    }

    public static TeacherSubject of(Subject subject, Teacher teacher) {
        return TeacherSubject.builder()
                .subject(subject)
                .teacher(teacher)
                .build();
    }
}
