package com.neeis.neeis.domain.student.dto.res;

import com.neeis.neeis.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudentResponseDto {
    private final String loginId;

    @Builder
    public StudentResponseDto(String loginId) {
       this.loginId = loginId;
    }

    public static StudentResponseDto of(Student student){
        return StudentResponseDto.builder()
                .loginId(student.getUser().getUsername())
                .build();
    }
}
