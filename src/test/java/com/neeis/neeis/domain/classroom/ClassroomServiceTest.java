package com.neeis.neeis.domain.classroom;

import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @InjectMocks
    private ClassroomService classroomService;

    @Test
    @DisplayName("findClassroom(year,grade,classNum,teacherId): 성공")
    void findClassroom_withTeacher_success() {
        // given
        int year = 2025;
        int grade = 2;
        int classNum = 1;
        Long teacherId = 10L;
        Classroom dummy = Classroom.builder()
                .year(year)
                .grade(grade)
                .classNum(classNum)
                .build();
        given(classroomRepository
                .findByClassroomInfo(year, grade, classNum, teacherId))
                .willReturn(Optional.of(dummy));

        // when
        Classroom result = classroomService.findClassroom(year, grade, classNum, teacherId);

        // then
        assertThat(result).isSameAs(dummy);
    }

    @Test
    @DisplayName("findClassroom(year,grade,classNum,teacherId): 권한 없으면 HANDLE_ACCESS_DENIED")
    void findClassroom_withTeacher_accessDenied() {
        // given
        given(classroomRepository
                .findByClassroomInfo(anyInt(), anyInt(), anyInt(), anyLong()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> classroomService.findClassroom(2025, 3, 2, 99L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("findClassroom(year,grade,classNum): 성공")
    void findClassroom_withoutTeacher_success() {
        // given
        int year = 2025;
        int grade = 3;
        int classNum = 2;
        Classroom dummy = Classroom.builder()
                .year(year)
                .grade(grade)
                .classNum(classNum)
                .build();
        given(classroomRepository
                .findByYearAndGradeAndClassNum(year, grade, classNum))
                .willReturn(Optional.of(dummy));

        // when
        Classroom result = classroomService.findClassroom(year, grade, classNum);

        // then
        assertThat(result).isSameAs(dummy);
    }

    @Test
    @DisplayName("findClassroom(year,grade,classNum): 없으면 CLASSROOM_NOT_FOUND")
    void findClassroom_withoutTeacher_notFound() {
        // given
        given(classroomRepository
                .findByYearAndGradeAndClassNum(anyInt(), anyInt(), anyInt()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> classroomService.findClassroom(2025, 4, 3))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CLASSROOM_NOT_FOUND.getMessage());
    }
}
