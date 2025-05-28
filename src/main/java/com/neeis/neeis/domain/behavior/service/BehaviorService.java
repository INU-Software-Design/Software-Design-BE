package com.neeis.neeis.domain.behavior.service;


import com.neeis.neeis.domain.behavior.Behavior;
import com.neeis.neeis.domain.behavior.BehaviorRepository;
import com.neeis.neeis.domain.behavior.dto.req.BehaviorRequestDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorDetailResponseDto;
import com.neeis.neeis.domain.behavior.dto.res.BehaviorResponseDto;
import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroom.ClassroomRepository;
import com.neeis.neeis.domain.classroom.ClassroomService;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentService;
import com.neeis.neeis.domain.notification.service.NotificationService;
import com.neeis.neeis.domain.teacher.Teacher;
import com.neeis.neeis.domain.teacher.service.TeacherService;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import com.neeis.neeis.global.fcm.event.SendBehaviorFcmEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.neeis.neeis.domain.user.Role.STUDENT;
import static com.neeis.neeis.domain.user.Role.TEACHER;
import static com.neeis.neeis.global.exception.ErrorCode.HANDLE_ACCESS_DENIED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorService {
    private final BehaviorRepository behaviorRepository;
    private final TeacherService teacherService;
    private final ClassroomStudentService classroomStudentService;
    private final ClassroomService classroomService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ClassroomStudentRepository classroomStudentRepository;

    // [교사] 행동 작성
    @Transactional //false
    public BehaviorResponseDto createBehavior( String username,
                                               Integer year, Integer grade, Integer classNum,
                                               Long studentId,
                                               BehaviorRequestDto behaviorRequestDto) {

        Teacher teacher = teacherService.authenticate(username);

        ClassroomStudent classroomStudent = classroomStudentService.checkMyStudents(year, grade,classNum, teacher.getId(), studentId);

        Behavior behavior = behaviorRepository.save(BehaviorRequestDto.of(behaviorRequestDto, classroomStudent));

        // Notification 용
        User user = classroomStudent.getStudent().getUser();
        String content = "행동 특성 및 종합의견이 등록되었습니다.";
        eventPublisher.publishEvent(new SendBehaviorFcmEvent(behavior));
        notificationService.sendNotification(user, content);

        return BehaviorResponseDto.of(behavior);
    }

    // [교사 / 학생 ] 행동 조회
    public BehaviorDetailResponseDto getBehavior(String username, Integer year, Integer grade, Integer classNum, Integer number) {

        ClassroomStudent classroomStudent = checkValidate(username, year, grade, classNum, number);
        Behavior behavior = behaviorRepository.findByClassroomStudentId(classroomStudent.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.BEHAVIOR_NOT_FOUND));

        return BehaviorDetailResponseDto.of(behavior);
    }

    // [교사] 행동 피드백 수정
    @Transactional
    public BehaviorDetailResponseDto updateBehavior(String username, Long behaviorId, BehaviorRequestDto behaviorRequestDto) {
        Teacher teacher = teacherService.authenticate(username);

        Behavior behavior = behaviorRepository.findById(behaviorId).orElseThrow(
                () -> new CustomException(ErrorCode.BEHAVIOR_NOT_FOUND));

        if (behavior.getClassroomStudent().getClassroom().getTeacher() != teacher) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        behavior.update(behaviorRequestDto);

        // Notification 용
        User user = behavior.getClassroomStudent().getStudent().getUser();
        String content = "행동 특성 및 종합의견이 수정되었습니다.";
        eventPublisher.publishEvent(new SendBehaviorFcmEvent(behavior));
        notificationService.sendNotification(user, content);

        return BehaviorDetailResponseDto.of(behavior);
    }

    private ClassroomStudent checkValidate(String username, int year, int grade, int classNum, int number) {
        User user = userService.getUser(username);

        switch (user.getRole()) {
            case STUDENT -> {
                // 본인이 요청한 게 맞는지 확인
                ClassroomStudent cs = classroomStudentRepository.findByStudentUser(user).orElseThrow(
                        () -> new CustomException(ErrorCode.CLASSROOM_NOT_FOUND)
                );
                if (cs.getNumber() != number) {
                    throw new CustomException(HANDLE_ACCESS_DENIED);
                }
                return cs;
            }

            case TEACHER -> {
                Teacher teacher = teacherService.authenticate(username);
                Classroom classroom = classroomService.findClassroom(year, grade, classNum, teacher.getId());

                return classroomStudentRepository.findByClassroomAndNumber(classroom, number)
                        .orElseThrow(() -> new CustomException(HANDLE_ACCESS_DENIED));
            }

            default -> throw new CustomException(HANDLE_ACCESS_DENIED);
        }
    }


}
