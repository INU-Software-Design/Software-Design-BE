package com.neeis.neeis.domain.counsel.controller;

import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.service.CounselService;
import com.neeis.neeis.domain.student.service.StudentService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_COUNSEL;
import static com.neeis.neeis.global.common.StatusCode.SUCCESS_POST_COUNSEL;

@RestController
@RequiredArgsConstructor
@RequestMapping("/counsel")
public class CounselController {
    private final CounselService counselService;
    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "상담 작성", description = "교사가 학생과의 상담 기록을 작성합니다." +
            "상담 종류:  UNIVERSITY(대학), CAREER(취업), FAMILY(가정), ACADEMIC(학업), PERSONAL(개인), OTHER(기타) 가 존재합니다." )
    public ResponseEntity<CommonResponse<Object>> postCounsel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "studentId") Long studentId,
            @RequestBody CounselRequestDto requestDto){

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_COUNSEL.getMessage(),  counselService.createCounsel(userDetails.getUsername(), studentId, requestDto)));
    }

    @GetMapping("/{counselId}")
    @Operation(summary = "상담 개별 조회", description = "권한을 가진 교사가 상담 기록을 조회합니다." +
            "상담이 비공개 처리가 되었을 시, isPublic이 false 로 조회됩니다. ")
    public ResponseEntity<CommonResponse<CounselDetailDto>> getCounsel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long counselId){
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_COUNSEL.getMessage(), counselService.getCounsel( userDetails.getUsername(), counselId)));
    }

    @GetMapping
    @Operation(summary = "학생별 상담 기록 조회", description = "권한을 가진 교사가 상담 기록을 조회합니다." +
            "상담이 비공개 처리가 되었을 시, isPublic이 false 로 조회됩니다. ")
    public ResponseEntity<CommonResponse<List<CounselDetailDto>>> getCounsels(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value="studentId") Long studentId){
        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_COUNSEL.getMessage(), counselService.getCounsels(userDetails.getUsername(), studentId)));
    }

}
