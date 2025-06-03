package com.neeis.neeis.domain.student.service;

import com.neeis.neeis.domain.classroom.Classroom;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudent;
import com.neeis.neeis.domain.classroomStudent.ClassroomStudentRepository;
import com.neeis.neeis.domain.parent.Parent;
import com.neeis.neeis.domain.parent.ParentService;
import com.neeis.neeis.domain.student.Student;
import com.neeis.neeis.domain.student.StudentRepository;
import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentUpdateRequestDto;
import com.neeis.neeis.domain.student.dto.res.PasswordResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentDetailResDto;
import com.neeis.neeis.domain.student.dto.res.StudentResponseDto;
import com.neeis.neeis.domain.student.dto.res.StudentSaveResponseDto;
import com.neeis.neeis.domain.user.Role;
import com.neeis.neeis.domain.user.User;
import com.neeis.neeis.domain.user.UserRepository;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final UserRepository userRepository;
    private final ParentService parentService;
    private final PasswordEncoder passwordEncoder;

    @Value("${image.path}")
    private String uploadPath;

    // 아이디 찾기
    public StudentResponseDto findUsername(FindIdRequestDto findIdRequestDto) {
        Student student = studentRepository.findByPhone(findIdRequestDto.getPhone()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!student.getName().equals(findIdRequestDto.getName()) ||
        !student.getUser().getSchool().equals(findIdRequestDto.getSchool())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return StudentResponseDto.of(student);
    }

    // 비밀번호 찾기
    public PasswordResponseDto findPassword(PasswordRequestDto passwordRequestDto) {
        Student student = studentRepository.findByPhone(passwordRequestDto.getPhone()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!student.getName().equals(passwordRequestDto.getName()) ||
                !student.getUser().getSchool().equals(passwordRequestDto.getSchool()) ||
        !student.getSsn().equals(passwordRequestDto.getSsn())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return PasswordResponseDto.of(student);
    }

    // 학생 상세 정보 조회
    public StudentDetailResDto getStudentDetails(Long studentId, int year) {
        Student student = getStudent(studentId);

        ClassroomStudent classroomStudent = classroomStudentRepository.findByStudentAndClassroomYear(student.getId(), year).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        Classroom classroom = classroomStudent.getClassroom();

        List<Parent> parents = parentService.getParents(student);

        Parent father = parents.stream()
                .filter(p -> "부".equalsIgnoreCase(p.getRelationShip()))
                .findFirst().orElse(null);
        Parent mother = parents.stream()
                .filter(p -> "모".equalsIgnoreCase(p.getRelationShip()))
                .findFirst().orElse(null);

        return StudentDetailResDto.of(student, father, mother, classroom, classroomStudent);
    }

    @Transactional
    public StudentSaveResponseDto saveStudent(String username, StudentRequestDto requestDto, MultipartFile imageFile) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.TEACHER) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String imagePath = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            imagePath = saveImage(imageFile);
        }

        // username : 입학년도 + user.getId
        // password: 핸드폰 뒷자리 4자리
        int admissionYear = requestDto.getAdmissionDate().getYear();
        String rawPassword = getLast4Digits(requestDto.getPhone());
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User newUser = userRepository.save(User.builder()
                .role(Role.valueOf(requestDto.getRole()))
                .school(requestDto.getSchool())
                .username("temp") // 나중에 update -> 임시저장
                .password(encodedPassword)
                .build());

        // username: 입학년도 + user.getId()
        String newUsername = admissionYear + String.valueOf(newUser.getId());
        newUser.updateUsername(newUsername);

        userRepository.save(newUser);

        Student student = StudentRequestDto.of(requestDto, imagePath, newUser );
        studentRepository.save(student);

        return StudentSaveResponseDto.toDto(student, rawPassword);
    }

    @Transactional
    public void updateStudent(String username, Long studentId,
                                            StudentUpdateRequestDto requestDto,
                                            MultipartFile imageFile) {

        User updater = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (updater.getRole() != Role.ADMIN && updater.getRole() != Role.TEACHER) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        Student student = getStudent(studentId);

        // 이미지 변경 처리
        String newImagePath = student.getImage();
        if (imageFile != null && !imageFile.isEmpty()) {
            newImagePath = saveImage(imageFile);
        }

        // 학생 정보 수정
        student.updateInfo(
                requestDto.getName(),
                requestDto.getAddress(),
                requestDto.getPhone(),
                newImagePath
        );

        // 부모 정보 수정
        List<Parent> parents = parentService.getParents(student);

        parents.stream()
                .filter(p -> "부".equalsIgnoreCase(p.getRelationShip()))
                .findFirst()
                .ifPresent(p -> {
                    if (requestDto.getFatherName() != null) p.updateName(requestDto.getFatherName());
                    if (requestDto.getFatherPhone() != null) p.updatePhone(requestDto.getFatherPhone());
                });

        parents.stream()
                .filter(p -> "모".equalsIgnoreCase(p.getRelationShip()))
                .findFirst()
                .ifPresent(p -> {
                    if (requestDto.getMotherName() != null) p.updateName(requestDto.getMotherName());
                    if (requestDto.getMotherPhone() != null) p.updatePhone(requestDto.getMotherPhone());
                });

    }

    private String saveImage(MultipartFile file) {
        String fileName = UUID.randomUUID().toString().replace("-","") + "_" + file.getOriginalFilename();

        Path savePath = Paths.get(uploadPath).resolve(fileName);
        log.info(">>> uploadPath: {}", uploadPath);
        log.info(">>> savePath: {}", savePath);

        try{
            Files.createDirectories(savePath.getParent());
            file.transferTo(savePath.toFile());
        }catch(IOException e){
            throw new CustomException(ErrorCode.IMAGE_SAVE_ERROR);
        }
        return fileName;
    }

    private String getLast4Digits(String phone) {
        if (phone == null || phone.length() < 4) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return phone.substring(phone.length() - 4);
    }

    public Student getStudent(Long studentId) {
        log.info(">>> getStudent: {}", studentId);
        return studentRepository.findById(studentId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
    }

    /**
     * User로 Student 조회
     */
    public Student findByUser(User user) {
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
