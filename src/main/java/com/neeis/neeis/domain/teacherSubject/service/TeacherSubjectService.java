package com.neeis.neeis.domain.teacherSubject.service;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.service.SubjectService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.teacherSubject.TeacherSubject;
import com.neeis.neeis.domain.teacherSubject.TeacherSubjectRepository;
import com.neeis.neeis.domain.teacherSubject.dto.req.CreateTeacherSubjectDto;
import com.neeis.neeis.domain.teacherSubject.dto.res.TeacherSubjectResponseDto;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherSubjectService {

    private final TeacherSubjectRepository teacherSubjectRepository;
    private final SubjectService subjectService;
    private final TeacherService teacherService;

    @Transactional
    public void save(String username, CreateTeacherSubjectDto dto) {
        teacherService.authenticate(username);

        // 과목 조회
        Subject subject = subjectService.getSubject(dto.getSubjectName());
        // 교사 조회
        Teacher teacher = teacherService.authenticate(dto.getTeacherName());

        if(teacherSubjectRepository.existsByTeacherAndSubject(teacher, subject)) {
            throw new CustomException(ErrorCode.TEACHER_SUBJECT_DUPLICATE);
        }

        teacherSubjectRepository.save(CreateTeacherSubjectDto.of(subject,teacher));
    }

    @Transactional
    public void update(String username, Long id, CreateTeacherSubjectDto dto) {
        teacherService.authenticate(username);
        TeacherSubject teacherSubject = findById(id);

        Subject subject = subjectService.getSubject(dto.getSubjectName());
        Teacher teacher = teacherService.authenticate(dto.getTeacherName());

        teacherSubject.update(teacher, subject);
    }

    public List<TeacherSubjectResponseDto> getTeacherSubjects() {
        return teacherSubjectRepository.findAll().stream()
                .map(TeacherSubjectResponseDto::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String username, Long id) {
        teacherService.authenticate(username);
        TeacherSubject teacherSubject = findById(id);

        teacherSubjectRepository.delete(teacherSubject);
    }


    private TeacherSubject findById(Long id) {
        return teacherSubjectRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.DATA_NOT_FOUND));
    }
}
