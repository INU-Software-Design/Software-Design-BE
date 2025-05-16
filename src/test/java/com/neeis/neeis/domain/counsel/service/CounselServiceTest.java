package com.neeis.neeis.domain.counsel.service;

import com.neeis.neeis.domain.counsel.Counsel;
import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.CounselRepository;
import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselResponseDto;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CounselServiceTest {

    @Mock private CounselRepository counselRepository;
    @Mock private TeacherService teacherService;
    @Mock private StudentService studentService;

    @InjectMocks private CounselService counselService;

    private Teacher teacher;
    private Student student;
    private Counsel counsel;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder().name("T").build();
        ReflectionTestUtils.setField(teacher, "id", 1L);

        student = Student.builder().name("S").build();
        ReflectionTestUtils.setField(student, "id", 2L);

        // default counsel
        counsel = Counsel.builder()
                .category(CounselCategory.FAMILY)
                .content("initial-comment")
                .teacher(teacher)
                .student(student)
                .build();
        ReflectionTestUtils.setField(counsel, "id", 3L);
    }

    @Test
    @DisplayName("createCounsel: 정상 저장 후 DTO 반환")
    void createCounsel_success() {
        // given
        CounselRequestDto req = CounselRequestDto.builder()
                .category("FAMILY")
                .content("new-comment")
                .build();
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(studentService.getStudent(2L)).willReturn(student);
        given(counselRepository.save(any(Counsel.class))).willReturn(counsel);

        // when
        CounselResponseDto res = counselService.createCounsel("t1", 2L, req);

        // then
        assertThat(res.getCounselId()).isEqualTo(3L);

    }

    @Test
    @DisplayName("createCounsel: 잘못된 카테고리 -> COUNSEL_CATEGORY_NOT_EXIST")
    void createCounsel_badCategory() {
        CounselRequestDto req = CounselRequestDto.builder()
                .category("BAD")
                .content("x").build();
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(studentService.getStudent(anyLong())).willReturn(student);

        assertThatThrownBy(() ->
                counselService.createCounsel("t1", 2L, req))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUNSEL_CATEGORY_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("getCounsel: 없으면 COUNSEL_NOT_FOUND")
    void getCounsel_notFound() {
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(counselRepository.findById(100L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                counselService.getCounsel("t1", 100L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUNSEL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getCounsel: 정상 조회")
    void getCounsel_success() {
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(counselRepository.findById(3L)).willReturn(Optional.of(counsel));

        CounselDetailDto dto = counselService.getCounsel("t1", 3L);

        assertThat(dto.getContent()).isEqualTo("initial-comment");
    }

    @Test
    @DisplayName("getCounsels: 없으면 COUNSEL_NOT_FOUND")
    void getCounsels_notFound() {
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(studentService.getStudent(2L)).willReturn(student);
        given(counselRepository.findByStudentId(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                counselService.getCounsels("t1", 2L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUNSEL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getCounsels: 정상 목록 조회")
    void getCounsels_success() {
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(studentService.getStudent(2L)).willReturn(student);
        given(counselRepository.findByStudentId(2L))
                .willReturn(Optional.of(List.of(counsel)));

        var list = counselService.getCounsels("t1", 2L);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getContent()).isEqualTo("initial-comment");
    }

    @Test
    @DisplayName("updateCounsel: 없으면 COUNSEL_NOT_FOUND")
    void updateCounsel_notFound() {
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(counselRepository.findById(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                counselService.updateCounsel("t1", 5L,
                        CounselRequestDto.builder().content("x").build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUNSEL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("updateCounsel: 정상 업데이트")
    void updateCounsel_success() {
        CounselRequestDto req = CounselRequestDto.builder()
                .category("FAMILY")
                .content("updated").build();
        given(teacherService.authenticate("t1")).willReturn(teacher);
        given(counselRepository.findById(3L)).willReturn(Optional.of(counsel));

        CounselDetailDto res = counselService.updateCounsel("t1", 3L, req);
        assertThat(res.getContent()).isEqualTo("updated");
    }
}