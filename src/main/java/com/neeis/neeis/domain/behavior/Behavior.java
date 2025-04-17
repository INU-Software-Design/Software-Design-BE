package com.neeis.neeis.domain.behavior;

import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Behavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String behavior;

    @Column(nullable = true)
    private String generalComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroomStudent_id")
    private ClassroomStudent classroomStudent;

    @Builder
    private Behavior(String behavior, String generalComment, ClassroomStudent classroomStudent) {
        this.behavior = behavior;
        this.generalComment = generalComment;
        this.classroomStudent = classroomStudent;
    }

    public void update (BehaviorRequestDto requestDto) {
        this.behavior = requestDto.getBehavior();
        this.generalComment = requestDto.getGeneralComment();
    }
}
