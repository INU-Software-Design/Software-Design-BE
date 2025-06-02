package com.neeis.neeis.global.report.controller;


import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.counsel.service.CounselService;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.service.ScoreSummaryService;
import com.neeis.neeis.global.report.service.ReportPDFService;
import com.neeis.neeis.global.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report/excel")
public class ReportController{

    private final ScoreSummaryService scoreSummaryService;
    private final ReportService reportService;
    private final CounselService counselService;
    private final ReportPDFService reportPDFService;

    @GetMapping("/score")
    @Operation(summary = "성적 분석 보고서 (미완)")
    public void downloadStudentScoreExcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int semester,
            @RequestParam int grade,
            @RequestParam int classNum,
            @RequestParam int number,
            HttpServletResponse response
    ) throws IOException {
        // 한 학생의 과목별 성적 요약
        StudentScoreSummaryDto summaryDto = scoreSummaryService.getStudentSummary(userDetails.getUsername(), year, semester, grade, classNum, number);

        byte[] excelBytes = reportService.generateStudentScoreExcel(summaryDto);

        String fileName = URLEncoder.encode(summaryDto.getStudentName() + "_성적요약.xlsx", StandardCharsets.UTF_8);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/counsel")
    @Operation(summary = "상담 내역 보고서")
    public void CounselExcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long studentId,
            HttpServletResponse response
    ) throws IOException {
        List<CounselDetailDto> counselDetailDtoList = counselService.getCounsels(userDetails.getUsername(), studentId);

        byte[] excelBytes = reportService.generateCounselExcel(counselDetailDtoList);

        String fileName = URLEncoder.encode("상담내역.xlsx", StandardCharsets.UTF_8);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();

    }
}
