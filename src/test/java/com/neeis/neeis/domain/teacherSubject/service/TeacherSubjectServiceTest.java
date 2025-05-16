package com.neeis.neeis.domain.teacherSubject.service;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.teacherSubject.TeacherSubject;
import com.neeis.neeis.domain.teacherSubject.TeacherSubjectRepository;
import com.neeis.neeis.domain.teacherSubject.dto.req.CreateTeacherSubjectDto;
import com.neeis.neeis.domain.teacherSubject.dto.res.TeacherSubjectResponseDto;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherSubjectServiceTest {

    @Mock TeacherSubjectRepository repo;
    @Mock SubjectService subjectService;
    @Mock TeacherService teacherService;
    @InjectMocks TeacherSubjectService service;

    private CreateTeacherSubjectDto dto;
    private Subject subject;
    private User teacherUser;
    private Teacher teacher ;
    private TeacherSubject entity;

    @BeforeEach
    void setUp() {
        dto = CreateTeacherSubjectDto.builder()
                .subjectName("Math")
                .teacherName("kim")
                .build();

        subject = Subject.builder().name("Math").build();

        teacherUser = User.builder()
                .username("tuser")
                .role(Role.TEACHER)
                .build();
        teacher = Teacher.builder()
                .user(teacherUser)
                .name("kim")
                .build();

        ReflectionTestUtils.setField(teacher, "id", 10L);
        entity = TeacherSubject.builder()
                .subject(subject)
                .teacher(teacher)
                .build();
    }

    @Test
    @DisplayName("save: 정상 저장")
    void save_success() {
        // given
        given(teacherService.authenticate("u")).willReturn(teacher);
        given(subjectService.getSubject("Math")).willReturn(subject);
        given(teacherService.checkTeacher("kim")).willReturn(teacher);
        given(repo.existsByTeacherAndSubject(teacher, subject)).willReturn(false);

        // when
        service.save("u", dto);

        // then
        then(repo).should().save(any(TeacherSubject.class));
    }

    @Test
    @DisplayName("save: 중복이면 TEACHER_SUBJECT_DUPLICATE")
    void save_duplicate() {
        // given
        given(teacherService.authenticate("u")).willReturn(teacher);
        given(subjectService.getSubject("Math")).willReturn(subject);
        given(teacherService.checkTeacher("kim")).willReturn(teacher);
        given(repo.existsByTeacherAndSubject(teacher, subject)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> service.save("u", dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TEACHER_SUBJECT_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("update: 정상 수정")
    void update_success() {
        // given
        given(teacherService.authenticate("u")).willReturn(teacher);
        given(repo.findById(1L)).willReturn(Optional.of(entity));
        given(subjectService.getSubject("Math")).willReturn(subject);
        given(teacherService.checkTeacher("kim")).willReturn(teacher);

        // when
        service.update("u", 1L, dto);

        // then
        // entity.update(...) 내부 로직이 호출돼야 하므로 변경 후 확인
        assertThat(entity.getSubject()).isSameAs(subject);
        assertThat(entity.getTeacher()).isSameAs(teacher);
    }

    @Test
    @DisplayName("update: id 없으면 TEACHER_SUBJECT_NOT_FOUND")
    void update_notFound() {
        given(teacherService.authenticate("u")).willReturn(teacher);
        given(repo.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("u", 1L, dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TEACHER_SUBJECT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getTeacherSubjects: 전체 조회")
    void getTeacherSubjects() {
        // given
        var e1 = TeacherSubject.builder().subject(subject).teacher(teacher).build();
        var e2 = TeacherSubject.builder().subject(subject).teacher(teacher).build();
        given(repo.findAll()).willReturn(List.of(e1, e2));

        // when
        List<TeacherSubjectResponseDto> list = service.getTeacherSubjects();

        // then
        assertThat(list).hasSize(2)
                .allSatisfy(dto -> {
                    assertThat(dto.getSubjectName()).isEqualTo("Math");
                    assertThat(dto.getTeacherName()).isEqualTo("kim");
                });
    }

    @Test
    @DisplayName("delete: 정상 삭제")
    void delete_success() {
        // given
        given(teacherService.authenticate("u")).willReturn(teacher);
        given(repo.findById(1L)).willReturn(Optional.of(entity));

        // when
        service.delete("u", 1L);

        // then
        then(repo).should().delete(entity);
    }

    @Test
    @DisplayName("delete: id 없으면 TEACHER_SUBJECT_NOT_FOUND")
    void delete_notFound() {
        given(teacherService.authenticate("u")).willReturn(teacher);
        given(repo.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("u", 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TEACHER_SUBJECT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findByTeacherAndSubject: 정상 반환")
    void findByTeacherAndSubject_success() {
        given(repo.findByTeacherAndSubject(teacher, subject))
                .willReturn(Optional.of(entity));

        TeacherSubject ts = service.findByTeacherAndSubject(teacher, subject);
        assertThat(ts).isSameAs(entity);
    }

    @Test
    @DisplayName("findByTeacherAndSubject: 없으면 HANDLE_ACCESS_DENIED")
    void findByTeacherAndSubject_notFound() {
        given(repo.findByTeacherAndSubject(teacher, subject))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByTeacherAndSubject(teacher, subject))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }
}