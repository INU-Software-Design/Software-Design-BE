package com.neeis.neeis.domain.counsel.service;

import com.neeis.neeis.domain.counsel.CounselCategory;
import com.neeis.neeis.domain.counsel.Counsel;
import com.neeis.neeis.domain.counsel.CounselRepository;
import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselResponseDto;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendCounselFcmEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.neeis.neeis.domain.user.Role.STUDENT;
import static com.neeis.neeis.domain.user.Role.TEACHER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselService {
    private final CounselRepository counselRepository;
    private final UserService userService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Transactional
    public CounselResponseDto createCounsel(String username, Long studentId, CounselRequestDto requestDto) {
        Teacher teacher = teacherService.authenticate(username);
        Student student = studentService.getStudent(studentId);

        CounselCategory category = findCategory(requestDto.getCategory()) ;

        Counsel counsel = counselRepository.save(CounselRequestDto.of(teacher,student, requestDto, category));

        eventPublisher.publishEvent(new SendCounselFcmEvent(counsel));

        User user = student.getUser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년  MM월 dd일");
        String content = counsel.getDateTime().format(formatter) + "자의 상담 내역이 등록되었습니다.";
        notificationService.sendNotification(user, content);

        return CounselResponseDto.toDto(counsel);
    }

    // 상담 개별 조회
    public CounselDetailDto getCounsel(String username, Long counselId){
        getUserAndValidateAccess(username, counselId);

        Counsel counsel = counselRepository.findById(counselId).orElseThrow(
                () -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND)
        );

        return CounselDetailDto.toDto(counsel);
    }

    // 상담 목록 조회
    public List<CounselDetailDto> getCounsels(String username, Long studentId){
        User user = userService.getUser(username);

        Student student = studentService.getStudent(studentId);

        // 학생이면 본인만 조회 가능
        if (user.getRole() == STUDENT && !user.getId().equals(student.getUser().getId())) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 교사일 경우에는 통과
        if (user.getRole() == TEACHER) {
            teacherService.authenticate(username); // 검증 차원
        }

        List<Counsel> counselList = counselRepository.findByStudentId(student.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND));

        List<CounselDetailDto> detailDtoList = new ArrayList<>();
        for(Counsel counsel : counselList) {
            detailDtoList.add(CounselDetailDto.toDto(counsel));
        }

        return detailDtoList;
    }

    // 상담 내용 수정
    @Transactional
    public CounselDetailDto updateCounsel(String username, Long counselId, CounselRequestDto requestDto) {
        teacherService.authenticate(username);

        Counsel counsel = counselRepository.findById(counselId).orElseThrow(
                () -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND)
        );
        counsel.update(requestDto);

        eventPublisher.publishEvent(new SendCounselFcmEvent(counsel));

        User user = counsel.getStudent().getUser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년  MM월 dd일");
        String content = counsel.getDateTime().format(formatter) + "자의 상담 내역이 수정되었습니다.";
        notificationService.sendNotification(user, content);

        return CounselDetailDto.toDto(counsel);
    }

    // 상담 카테고리
    private CounselCategory findCategory(String category) {
        if (CounselCategory.exists(category)){
            return CounselCategory.valueOf(category);
        }
        else throw new CustomException(ErrorCode.COUNSEL_CATEGORY_NOT_EXIST);
    }

    private void getUserAndValidateAccess(String username, Long counselId) {
        User user = userService.getUser(username);
        Counsel counsel = counselRepository.findById(counselId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND));

        // 학생 → 본인만 접근 가능
        if (user.getRole() == STUDENT && !user.getId().equals(counsel.getStudent().getUser().getId())) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 교사 → 조회 허용
        if (user.getRole() == TEACHER) {
            teacherService.authenticate(username); // 추가 보안
        }
    }
}