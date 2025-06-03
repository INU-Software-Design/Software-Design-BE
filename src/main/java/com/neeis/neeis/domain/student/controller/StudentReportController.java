package com.neeis.neeis.domain.student.controller;

import com.neeis.neeis.domain.student.dto.req.StudentReportRequestDto;
import com.neeis.neeis.domain.student.dto.res.StudentReportResponseDto;
import com.neeis.neeis.domain.student.service.StudentReportService;
import com.neeis.neeis.global.report.service.PdfGeneratorService;
import com.neeis.neeis.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.neeis.neeis.global.common.StatusCode.SUCCESS_GET_PDF;

@Tag(name = "학생 보고서", description = "학생 생활기록부 조회 및 PDF 생성 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class StudentReportController {

    private final StudentReportService studentReportService;
    private final PdfGeneratorService pdfGeneratorService;

    /**
     * 학생 본인의 보고서 조회 (인증된 사용자)
     */
    @Operation(
            summary = "본인 학생 보고서 데이터 조회",
            description = """
            로그인한 학생이 본인의 생활기록부 데이터를 조회합니다.
            
            **기능:**
            - 학생 기본 정보
            - 성적 정보 (옵션)
            - 출결 정보 (옵션)
            - 상담 기록 (옵션)
            - 행동평가 (옵션)
            
            **권한:** 학생 본인만 조회 가능
            """,
            tags = {"학생 보고서"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보고서 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = StudentReportResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 - 학생만 접근 가능"),
            @ApiResponse(responseCode = "404", description = "학생 정보를 찾을 수 없음")
    })
    @PostMapping("/my/data")
    public ResponseEntity<CommonResponse<StudentReportResponseDto>> getMyStudentReportData(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "보고서 생성 요청 정보", required = true)
            @RequestBody StudentReportRequestDto requestDto) {

        log.info("본인 학생 보고서 데이터 조회 요청 - 사용자: {}", userDetails.getUsername());

        StudentReportResponseDto reportData = studentReportService.generateMyStudentReport(userDetails.getUsername(), requestDto);

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_PDF.getMessage(), reportData));
    }

    /**
     * 학생 본인의 보고서 PDF 다운로드 (인증된 사용자)
     */
    @Operation(
            summary = "본인 학생 보고서 PDF 다운로드",
            description = """
            로그인한 학생이 본인의 생활기록부를 PDF 파일로 다운로드합니다.
            
            **기능:**
            - 학생 생활기록부 PDF 생성
            - 파일명: {학생이름}_생활기록부_{yyyyMMdd_HHmmss}.pdf
            - Content-Disposition: attachment로 다운로드 형태
            
            **포함 데이터:**
            - 학생 기본 정보 (필수)
            - 성적, 출결, 상담, 행동평가 (선택 옵션에 따라)
            
            **권한:** 학생 본인만 다운로드 가능
            """,
            tags = {"학생 보고서"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF 다운로드 성공",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 - 학생만 접근 가능"),
            @ApiResponse(responseCode = "404", description = "학생 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "PDF 생성 중 오류 발생")
    })
    @PostMapping("/my/pdf")
    public ResponseEntity<byte[]> downloadMyStudentReportPdf(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "보고서 생성 요청 정보", required = true)
            @RequestBody StudentReportRequestDto requestDto) {

        log.info("본인 학생 보고서 PDF 다운로드 요청 - 사용자: {}", userDetails.getUsername());

        try {
            // 보고서 데이터 생성
            StudentReportResponseDto reportData = studentReportService.generateMyStudentReport(userDetails.getUsername(), requestDto);

            // PDF 생성
            byte[] pdfBytes = pdfGeneratorService.generateStudentReportPdf(reportData, userDetails.getUsername());

            // 파일명 생성
            String fileName = generateFileName(reportData.getStudentInfo().getName());
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("본인 PDF 생성 중 오류 발생 - 사용자: {}", userDetails.getUsername(), e);
            throw e;
        }
    }

    /**
     * 교사용 특정 학생 보고서 조회
     */
    @Operation(
            summary = "교사용 학생 보고서 데이터 조회",
            description = """
            교사가 담당 학생의 생활기록부 데이터를 조회합니다.
            
            **기능:**
            - 특정 학생의 생활기록부 데이터 조회
            - 교사 권한 확인 후 접근 허용
            - 담당 학급/과목 학생에 대한 접근 제어
            
            **포함 데이터:**
            - 학생 기본 정보, 부모 정보
            - 성적, 출결, 상담, 행동평가 (옵션 선택에 따라)
            
            **권한:** 교사 또는 관리자만 접근 가능
            """,
            tags = {"학생 보고서"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보고서 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = StudentReportResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 - 교사만 접근 가능"),
            @ApiResponse(responseCode = "404", description = "학생 정보를 찾을 수 없음")
    })
    @PostMapping("/teacher/data")
    public ResponseEntity<CommonResponse<StudentReportResponseDto>> getStudentReportDataByTeacher(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "조회할 학생의 보고서 요청 정보 (studentId 필수)", required = true)
            @RequestBody StudentReportRequestDto requestDto) {

        log.info("교사의 학생 보고서 데이터 조회 요청 - 교사: {}, 대상학생ID: {}",
                userDetails.getUsername(), requestDto.getStudentId());

        StudentReportResponseDto reportData = studentReportService.generateStudentReportByTeacher(userDetails.getUsername(), requestDto);

        return ResponseEntity.ok(CommonResponse.from(SUCCESS_GET_PDF.getMessage(), reportData));
    }

    /**
     * 교사용 특정 학생 보고서 PDF 다운로드
     */
    @Operation(
            summary = "교사용 학생 보고서 PDF 다운로드",
            description = """
            교사가 담당 학생의 생활기록부를 PDF 파일로 다운로드합니다.
            
            **기능:**
            - 특정 학생의 생활기록부 PDF 생성 및 다운로드
            - 교사 권한 확인 및 담당 학생 여부 검증
            - 파일명: {학생이름}_생활기록부_{yyyyMMdd_HHmmss}.pdf
          
            **보안:**
            - 교사 인증 필수
            - 담당 학급/과목 학생에 대해서만 접근 가능
            - 다운로드 로그 기록
            
            **권한:** 교사 또는 관리자만 접근 가능
            """,
            tags = {"학생 보고서"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF 다운로드 성공",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 - 교사만 접근 가능"),
            @ApiResponse(responseCode = "404", description = "학생 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "PDF 생성 중 오류 발생")
    })
    @PostMapping("/teacher/pdf")
    public ResponseEntity<byte[]> downloadStudentReportPdfByTeacher(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "다운로드할 학생의 보고서 요청 정보 (studentId 필수)", required = true)
            @RequestBody StudentReportRequestDto requestDto) {

        log.info("교사의 학생 보고서 PDF 다운로드 요청 - 교사: {}, 대상 학생 ID: {}",
                userDetails.getUsername(), requestDto.getStudentId());

        try {
            StudentReportResponseDto reportData = studentReportService.generateStudentReportByTeacher(userDetails.getUsername(), requestDto);

            byte[] pdfBytes = pdfGeneratorService.generateStudentReportPdf(reportData, userDetails.getUsername());

            String fileName = generateFileName(reportData.getStudentInfo().getName());
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("교사용 PDF 생성 중 오류 발생 - 교사: {}", userDetails.getUsername(), e);
            throw e;
        }
    }

    @GetMapping("/password-info")
    public ResponseEntity<Map<String, String>> getPasswordInfo(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String username = userDetails.getUsername();
            String userRole = pdfGeneratorService.getUserRoleByUsername(username);
            Map<String, String> response = new HashMap<>();

            switch (userRole.toUpperCase()) {
                case "STUDENT":
                    response.put("message", "PDF 문서의 암호는 본인의 생년월일 6자리입니다.");
                    response.put("format", "YYMMDD (예: 051225)");
                    response.put("example", "2005년 12월 25일생 → 051225");
                    break;

                case "PARENT":
                    response.put("message", "PDF 문서의 암호는 자녀의 생년월일 6자리입니다.");
                    response.put("format", "YYMMDD (예: 051225)");
                    response.put("example", "자녀가 2005년 12월 25일생 → 051225");
                    break;

                case "TEACHER":
                    response.put("message", "PDF 문서의 암호는 선생님의 핸드폰 번호 뒷자리 4자리입니다.");
                    response.put("format", "핸드폰 뒷자리 4자리 (예: 1234)");
                    response.put("example", "010-1234-5678 → 5678");
                    break;

                default:
                    response.put("message", "패스워드 정보를 확인할 수 없습니다.");
                    response.put("format", "관리자에게 문의하세요.");
            }

            response.put("role", userRole);
            response.put("username", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "패스워드 정보를 조회할 수 없습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 파일명 생성 유틸리티
     * @param studentName 학생 이름
     * @return 생성된 파일명 (학생이름_생활기록부_yyyyMMdd_HHmmss.pdf)
     */
    private String generateFileName(String studentName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_생활기록부_%s.pdf", studentName, timestamp);
    }
}