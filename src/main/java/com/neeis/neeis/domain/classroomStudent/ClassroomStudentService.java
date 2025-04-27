package com.neeis.neeis.domain.classroomStudent;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassroomStudentService {

    private final ClassroomService classroomService;
    private final ClassroomStudentRepository classroomStudentRepository;

    // 담당 학생 확인
    public ClassroomStudent checkMyStudents(Integer year, Integer grade, Integer classNum, Long teacherId, Long studentId) {
        Classroom classroom = classroomService.findClassroom(year, grade, classNum, teacherId);

        return classroomStudentRepository.findByStudentAndClassroom(studentId, classroom.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));
    }
}
