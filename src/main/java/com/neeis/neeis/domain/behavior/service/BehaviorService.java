package com.neeis.neeis.domain.behavior.service;


import com.neeis.neeis.domain.behavior.Behavior;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorService {
    private final BehaviorRepository behaviorRepository;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @Transactional //false
    public BehaviorResponseDto postBehavior(Long studentId, BehaviorRequestDto behaviorRequestDto) {
        Student student = studentService.getStudent(studentId);

        Behavior behavior = behaviorRepository.save(BehaviorRequestDto.of(behaviorRequestDto, student));

        return BehaviorResponseDto.of(behavior);
    }

    public BehaviorDetailResponseDto getBehavior(String username, Long studentId) {
        // 교사 확인
        Teacher teacher = teacherService.authenticate(username);

        // 담당 학급 확인
       Classroom classroom = teacherService.checkClassroom(teacher.getId(), LocalDate.now().getYear());

        Student student = studentService.getStudent(studentId);

//        // 담당 학생 아닐 경우 접근 제한.
//        if(!(classroom.getTeacher() == teacher) && !(classroom.getStudent() == student)) {
//            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
//        }

        Behavior behavior = behaviorRepository.findByStudentId(student.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        return BehaviorDetailResponseDto.of(behavior);
    }


}
