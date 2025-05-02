package com.neeis.neeis.domain.subject.service;

import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.SubjectRepository;
import com.neeis.neeis.domain.subject.dto.req.CreateSubjectRequestDto;
import com.neeis.neeis.domain.subject.dto.res.SubjectResponseDto;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final TeacherService teacherService;

    @Transactional
    public void createSubject(String username, CreateSubjectRequestDto createSubjectRequestDto) {
        teacherService.authenticate(username);

        if(subjectRepository.existsSubjectByName(createSubjectRequestDto.getName())){
            throw new CustomException(ErrorCode.SUBJECT_DUPLICATE);
        }

        subjectRepository.save(CreateSubjectRequestDto.of(createSubjectRequestDto));
    }

    @Transactional
    public void updateSubject(String username, Long subjectId, CreateSubjectRequestDto createSubjectRequestDto) {
        teacherService.authenticate(username);

        Subject subject =  subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        subject.update(createSubjectRequestDto.getName());
    }

    // 과목 조회
    public List<SubjectResponseDto> getSubjects(){
        return subjectRepository.findAllByOrderByNameAsc()
                .stream()
                .map(SubjectResponseDto::toDto)
                .toList();
    }

    @Transactional
    public void deleteSubject(String username, Long subjectId) {
        teacherService.authenticate(username);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));
        subjectRepository.delete(subject);
    }
}

