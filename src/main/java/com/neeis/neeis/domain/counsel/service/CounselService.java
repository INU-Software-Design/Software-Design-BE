package com.neeis.neeis.domain.counsel.service;

import com.neeis.neeis.domain.counsel.Counseling;
import com.neeis.neeis.domain.counsel.CounselingRepository;
import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselResponseDto;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselService {
    private final CounselRepository counselRepository;
    private final TeacherService teacherService;
    private final StudentService studentService;

    @Transactional
    public CounselResponseDto createCounsel(String username, Long studentId, CounselRequestDto requestDto) {
        Teacher teacher = teacherService.authenticate(username);
        Student student = studentService.getStudent(studentId);

        CounselCategory category = findCategory(requestDto.getCategory()) ;

        Counsel counsel = counselRepository.save(CounselRequestDto.of(teacher,student, requestDto, category));

        return CounselResponseDto.toDto(counsel);
    }
}
