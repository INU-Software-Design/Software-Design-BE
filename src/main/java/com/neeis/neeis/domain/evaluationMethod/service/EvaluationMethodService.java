package com.neeis.neeis.domain.evaluationMethod.service;

import com.neeis.neeis.domain.evaluationMethod.EvaluationMethod;
import com.neeis.neeis.domain.evaluationMethod.EvaluationMethodRepository;
import com.neeis.neeis.domain.evaluationMethod.ExamType;
import com.neeis.neeis.domain.evaluationMethod.dto.req.CreateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.req.UpdateEvaluationMethodDto;
import com.neeis.neeis.domain.evaluationMethod.dto.res.EvaluationMethodResponseDto;
import com.neeis.neeis.domain.subject.Subject;
import com.neeis.neeis.domain.subject.dto.res.SubjectResponseDto;
import com.neeis.neeis.domain.subject.service.SubjectService;
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
public class EvaluationMethodService {
    private final EvaluationMethodRepository evaluationMethodRepository;
    private final SubjectService subjectService;
    private final TeacherService teacherService;

    /**
     * 평가 방식 등록
     */
    @Transactional
    public void save(String username, CreateEvaluationMethodDto dto) {
        // 권한 확인
        teacherService.authenticate(username);

        // 과목확인
        Subject subject = subjectService.getSubject(dto.getSubject());

        // examType 검증
        if (!ExamType.exists(dto.getExamType())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        ExamType examType = ExamType.valueOf(dto.getExamType());

        // 중복 생성 확인
        boolean exists = evaluationMethodRepository
                .existsBySubjectAndYearAndSemesterAndGradeAndExamTypeAndTitle(subject, dto.getYear(), dto.getSemester(), dto.getGrade(), examType, dto.getTitle());
        if (exists) {
            throw new CustomException(ErrorCode.EVALUATION_METHOD_DUPLICATE);
        }

        // 반영비율 계산
        List<EvaluationMethod> existingMethods = evaluationMethodRepository
                .findAllBySubjectAndYearAndSemesterAndGrade(subject, dto.getYear(), dto.getSemester(), dto.getGrade());

        double totalWeight = existingMethods.stream()
                .mapToDouble(EvaluationMethod::getWeight)
                .sum();

        if (totalWeight + dto.getWeight() > 100.0) {
            throw new CustomException(ErrorCode.EXCESS_TOTAL_100);
        }

        evaluationMethodRepository.save(CreateEvaluationMethodDto.of(subject, examType, dto));
    }

    /**
     * 평가 방식 목록 조회
     */
    public List<EvaluationMethodResponseDto> getEvaluationMethods(
            String subjectName, int year, int semester, int grade) {

        Subject subject = subjectService.getSubject(subjectName);

        List<EvaluationMethod> methods = evaluationMethodRepository
                .findAllBySubjectAndYearAndSemesterAndGrade(subject, year, semester, grade);

        return methods.stream()
                .map(EvaluationMethodResponseDto::toDto)
                .toList();
    }

    /**
     * 평가방식이 존재하는 과목 리스트 조회
     */
    public List<SubjectResponseDto> findSubjectList(int year, int semester, int grade) {
        List<Subject> subjectList =  findSubject(year, semester, grade);
        return subjectList.stream()
                        .map(SubjectResponseDto::toDto)
                        .toList();
    }

    /**
     * 평가 방식 수정
     */
    @Transactional
    public void update(String username, Long id, UpdateEvaluationMethodDto dto) {
        teacherService.authenticate(username);

        EvaluationMethod method = evaluationMethodRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.EVALUATION_METHOD_NOT_FOUND));

        if (!ExamType.exists(dto.getExamType())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        ExamType examType = ExamType.valueOf(dto.getExamType());

        Subject subject = method.getSubject();
        int year = method.getYear();
        int semester = method.getSemester();
        int grade = method.getGrade();

        List<EvaluationMethod> existingMethods =
                evaluationMethodRepository.findAllBySubjectAndYearAndSemesterAndGrade(subject, year, semester, grade);

        double totalWeight = existingMethods.stream()
                .filter(m -> !m.getId().equals(id))  // 본인 제외
                .mapToDouble(EvaluationMethod::getWeight)
                .sum();

        if (totalWeight + dto.getWeight() > 100.0) {
            throw new CustomException(ErrorCode.EXCESS_TOTAL_100);
        }


        method.update( examType, dto);
    }

    /**
     * 평가 방식 삭제
     */
    @Transactional
    public void delete(String username, Long id) {
        teacherService.authenticate(username);

        EvaluationMethod method = evaluationMethodRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.DATA_NOT_FOUND));
        evaluationMethodRepository.delete(method);
    }

    public EvaluationMethod findById(Long id) {
        return evaluationMethodRepository.findById(id).orElseThrow(
                (() -> new CustomException(ErrorCode.EVALUATION_METHOD_NOT_FOUND))
        );
    }

    public List<EvaluationMethod> findAllBySubjectAndYearAndSemesterAndGrade(Subject subject, int year, int semester, int grade) {
        return evaluationMethodRepository.findAllBySubjectAndYearAndSemesterAndGrade(subject, year, semester, grade);
    }

    public List<Subject> findSubject(int year, int semester, int grade){
       return evaluationMethodRepository.findDistinctSubjectsByYearSemesterGrade(year, semester, grade);
    }

}