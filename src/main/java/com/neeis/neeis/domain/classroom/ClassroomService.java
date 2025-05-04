package com.neeis.neeis.domain.classroom;

import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    // 담당 학급 확인
    public Classroom findClassroom(int year,int grade, int classNum, Long teacherId){
        return classroomRepository.findByClassroomInfo(year, grade, classNum, teacherId).orElseThrow(
                () -> new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));
    }

    // 성적 입력 용
    public Classroom findClassroom(int year, int grade, int classNum){
        return classroomRepository.findByYearAndGradeAndClassNum(year, grade, classNum).orElseThrow(
                () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));
    }
}
