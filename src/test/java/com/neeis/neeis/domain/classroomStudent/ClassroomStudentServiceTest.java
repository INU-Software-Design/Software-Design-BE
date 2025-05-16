package com.neeis.neeis.domain.classroomStudent;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ClassroomStudentServiceTest {

    @Mock
    private ClassroomService classroomService;
    @Mock
    private ClassroomStudentRepository classroomStudentRepository;

    @InjectMocks
    private ClassroomStudentService classroomStudentService;

    private Student student;
    private Classroom classroom;
    private ClassroomStudent cs;

    @BeforeEach
    void setUp() {
        // Classroom with id 10L
        classroom = Classroom.builder()
                .grade(2)
                .classNum(1)
                .year(2025)
                .build();
        ReflectionTestUtils.setField(classroom, "id", 10L);

        student = Student.builder()
                .admissionDate(LocalDate.now())
                .name("테스트")
                .phone("010-3333-3333")
                .ssn("000802-3333333")
                .gender("F")
                .address("인천광역시 송도 1129")
                .build();

        ReflectionTestUtils.setField(student, "id", 1L);
        // ClassroomStudent with id 20L
        cs = ClassroomStudent.builder()
                .number(5)
                .student(student)
                .classroom(classroom)
                .build();
        ReflectionTestUtils.setField(cs, "id", 20L);
    }

    @Test
    @DisplayName("checkMyStudents: 정상 반환")
    void checkMyStudents_success() {
        // given
        given(classroomService.findClassroom(2025, 2, 1, 50L))
                .willReturn(classroom);
        given(classroomStudentRepository.findByStudentAndClassroom(100L, 10L))
                .willReturn(Optional.of(cs));

        // when
        ClassroomStudent result = classroomStudentService
                .checkMyStudents(2025, 2, 1, 50L, 100L);

        // then
        assertThat(result).isSameAs(cs);
    }

    @Test
    @DisplayName("checkMyStudents: 없으면 CLASSROOM_NOT_FOUND 예외")
    void checkMyStudents_notFound() {
        // given
        given(classroomService.findClassroom(2025, 2, 1, 50L))
                .willReturn(classroom);
        given(classroomStudentRepository.findByStudentAndClassroom(100L, 10L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                classroomStudentService.checkMyStudents(2025, 2, 1, 50L, 100L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CLASSROOM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findByClassroomAndNumber: 정상 반환")
    void findByClassroomAndNumber_success() {
        // given
        given(classroomStudentRepository.findByClassroomAndNumber(classroom, 5))
                .willReturn(Optional.of(cs));

        // when
        ClassroomStudent result = classroomStudentService
                .findByClassroomAndNumber(classroom, 5);

        // then
        assertThat(result).isSameAs(cs);
    }

    @Test
    @DisplayName("findByClassroomAndNumber: 없으면 CLASSROOM_NOT_FOUND 예외")
    void findByClassroomAndNumber_notFound() {
        // given
        given(classroomStudentRepository.findByClassroomAndNumber(classroom, 5))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                classroomStudentService.findByClassroomAndNumber(classroom, 5))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CLASSROOM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findByClassroom: 리스트 반환")
    void findByClassroom_list() {
        // given
        given(classroomStudentRepository.findByClassroom(classroom))
                .willReturn(List.of(cs));

        // when
        List<ClassroomStudent> list = classroomStudentService.findByClassroom(classroom);

        // then
        assertThat(list).containsExactly(cs);
    }
}
