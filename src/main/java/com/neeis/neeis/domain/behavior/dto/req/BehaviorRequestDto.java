package com.neeis.neeis.domain.behavior.dto.req;

import com.neeis.neeis.domain.behavior.Behavior;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BehaviorRequestDto {

    @NotBlank(message = "행동 특성은 필수 입력값입니다.")
    @Schema( example = "책임감이 강하며 수업시간에 적극적으로 참여함.")
    private String behavior;


    @NotBlank(message= "종합 의견은 필수 입력값입니다.")
    @Schema(example = "전반적으로 성실하며, 친구들과의 관계도 원만합니다.")
    private String generalComment;

    @Builder
    private BehaviorRequestDto(String behavior, String generalComment) {
        this.behavior = behavior;
        this.generalComment = generalComment;

    }

    public static Behavior of(BehaviorRequestDto dto, ClassroomStudent student) {
        return Behavior.builder()
                .behavior(dto.getBehavior())
                .generalComment(dto.getGeneralComment())
                .classroomStudent(student)
                .build();
    }

}
