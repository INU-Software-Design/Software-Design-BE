package com.neeis.neeis.domain.behavior.service;


import com.neeis.neeis.domain.behavior.Behavior;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorService {
    private final BehaviorRepository behaviorRepository;
    private final TeacherService teacherService;
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;

    @Transactional //false
    public BehaviorResponseDto createBehavior( String username,
                                               Integer year, Integer grade, Integer classNum,
                                               Long studentId,
                                               BehaviorRequestDto behaviorRequestDto) {

        Teacher teacher = teacherService.authenticate(username);

        ClassroomStudent classroomStudent = checkMyStudents(year, grade,classNum, teacher.getId(), studentId);

        Behavior behavior = behaviorRepository.save(BehaviorRequestDto.of(behaviorRequestDto, classroomStudent));

        return BehaviorResponseDto.of(behavior);
    }

    public BehaviorDetailResponseDto getBehavior(String username, Integer year, Integer grade, Integer classNum, Long studentId ) {
        // 교사 확인
        Teacher teacher = teacherService.authenticate(username);

        ClassroomStudent classroomStudent = checkMyStudents(year, grade, classNum, teacher.getId(), studentId);

        Behavior behavior = behaviorRepository.findByClassroomStudentId(classroomStudent.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.BEHAVIOR_NOT_FOUND));

        // 담임학생 아니면 조회 접근 x
        if (behavior.getClassroomStudent().getClassroom().getTeacher() != teacher) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        return BehaviorDetailResponseDto.of(behavior);
    }

    private ClassroomStudent checkMyStudents(Integer year, Integer grade, Integer classNum, Long teacherId, Long studentId) {
        Classroom classroom = classroomRepository.findByClassroomInfo(year, grade, classNum, teacherId).orElseThrow(
                () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));

        return classroomStudentRepository.findByStudentAndClassroom(studentId, classroom.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));
    }



}
