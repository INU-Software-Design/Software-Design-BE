package com.neeis.neeis.domain.student.controller;

import com.neeis.neeis.domain.student.dto.req.FindIdRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentRequestDto;
import com.neeis.neeis.domain.student.dto.req.StudentUpdateRequestDto;
import com.neeis.neeis.domain.student.dto.res.*;
import com.neeis.neeis.domain.user.dto.LoginRequestDto;
import com.neeis.neeis.domain.student.dto.req.PasswordRequestDto;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.domain.user.service.UserService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.neeis.neeis.global.common.StatusCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/id")
    @Operation(summary = "아이디 찾기")
    public ResponseEntity<CommonResponse<StudentResponseDto>> findUsername(
            @Valid @RequestBody FindIdRequestDto findIdRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_USERNAME.getMessage(), studentService.findUsername(findIdRequestDto)));
    }

    @PostMapping("/password")
    @Operation(summary = "비밀번호 찾기")
    public ResponseEntity<CommonResponse<PasswordResponseDto>> findPassword(
            @Valid @RequestBody PasswordRequestDto passwordRequestDto) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_PASSWORD.getMessage(), studentService.findPassword(passwordRequestDto)));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "[관리자 및 교사 전용] 학생 등록", description =
            "role | 회원 역할 (STUDENT만 허용) <br>" +
            "school | 학생이 소속된 학교 이름 <br>" +
            "gender | 성별 (남 또는 여) <br>" +
            "ssn | 주민등록번호 앞 6자리+뒷자리 7자리 형식 <br>" +
            "phone | 010-0000-0000형식 (가운데 '-' 넣어주세요) 학생 연락처 <br>" +
            "admissionDate | 입학일 (yyyy-MM-dd 형식).")
    public ResponseEntity<CommonResponse<StudentSaveResponseDto>> registerStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("info") @Valid StudentRequestDto studentRequestDto,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_STUDENTS.getMessage(), studentService.saveStudent(userDetails.getUsername(),studentRequestDto, image)));
    }

    @PutMapping(value = "/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "[관리자 및 교사 전용] 학생 정보 수정" )
    public ResponseEntity<CommonResponse<Object>> updateStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId, // 출석번호
            @RequestPart("info") @Valid StudentUpdateRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        studentService.updateStudent(userDetails.getUsername(), studentId, requestDto, image);
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_UPDATE_STUDENTS.getMessage()));
    }
}
