package com.neeis.neeis.domain.classroomStudent;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public ClassroomStudent findByClassroomAndNumber(Classroom classroom, int number) {
        return classroomStudentRepository.findByClassroomAndNumber(classroom, number)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND));
    }

    public List<ClassroomStudent> findByClassroom(Classroom classroom) {
        return classroomStudentRepository.findByClassroom(classroom);
    }

    /**
     * PDF 생성을 위해 추가된 메서드
     * User 객체로 ClassroomStudent 조회
     */
    public Optional<ClassroomStudent> findByStudent(User user) {
        return classroomStudentRepository.findByStudentUser(user);
    }

    /**
     * 학생 ID와 연도로 ClassroomStudent 조회
     */
    public Optional<ClassroomStudent> findByStudentIdAndYear(Long studentId, int year) {
        return classroomStudentRepository.findByStudentIdAndClassroomYear(studentId, year);
    }

    /**
     * 학생 ID로 현재 활성 ClassroomStudent 조회 (최신 연도 기준)
     * @param studentId 학생 ID
     * @return ClassroomStudent Optional
     */
    public Optional<ClassroomStudent> findByStudentId(Long studentId) {
        return classroomStudentRepository.findByStudentIdOrderByClassroomYearDesc(studentId)
                .stream()
                .findFirst();
    }
}
