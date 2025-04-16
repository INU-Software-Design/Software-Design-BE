package com.neeis.neeis.domain.counsel.controller;

import com.neeis.neeis.domain.counsel.dto.req.CounselRequestDto;
import com.neeis.neeis.domain.counsel.service.CounselService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_POST_COUNSEL;

@RestController
@RequiredArgsConstructor
@RequestMapping("/counsel")
public class CounselController {
    private final CounselService counselService;

    @PostMapping
    @Operation(summary = "상담 작성", description = "교사가 학생과의 상담 기록을 작성합니다.")
    public ResponseEntity<CommonResponse<Object>> postCounsel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "studentId") Long studentId,
            @RequestBody CounselRequestDto requestDto){

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_POST_COUNSEL.getMessage(),  counselService.createCounsel(userDetails.getUsername(), studentId, requestDto)));
    }




}
