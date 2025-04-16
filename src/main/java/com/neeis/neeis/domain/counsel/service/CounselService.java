package com.neeis.neeis.domain.counsel.service;

import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.Counsel;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    public CounselDetailDto getCounsel(String username, Long counselId){
        teacherService.authenticate(username);

        Counsel counsel = counselRepository.findById(counselId).orElseThrow(
                () -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND)
        );

        return CounselDetailDto.toDto(counsel);
    }

    public List<CounselDetailDto> getCounsels(String username, Long studentId){
        teacherService.authenticate(username);

        Student student = studentService.getStudent(studentId);

        List<Counsel> counselList = counselRepository.findByStudentId(student.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND));

        List<CounselDetailDto> detailDtoList = new ArrayList<>();
        for(Counsel counsel : counselList) {
            detailDtoList.add(CounselDetailDto.toDto(counsel));
        }

        return detailDtoList;
    }

    // 상담 카테고리
    private CounselCategory findCategory(String category) {
        if (CounselCategory.exists(category)){
            return CounselCategory.valueOf(category);
        }
        else throw new CustomException(ErrorCode.COUNSEL_CATEGORY_NOT_EXIST);
    }

}
