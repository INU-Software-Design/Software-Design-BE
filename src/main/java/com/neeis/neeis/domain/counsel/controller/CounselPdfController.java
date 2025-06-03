//package com.neeis.neeis.domain.counsel.controller;
//
//import com.neeis.neeis.domain.counsel.service.CounselPdfService;
//import com.neeis.neeis.domain.counsel.service.CounselService;
//import com.neeis.neeis.global.jwt.CustomUserDetails;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
///**
// * 상담 보고서 PDF API 컨트롤러
// *
// * 기존 CounselService를 통해 실제 데이터베이스의 상담 데이터를 PDF로 생성합니다.
// */
//@Slf4j
//@RestController
//@RequestMapping("/reports/counsels")
//@RequiredArgsConstructor
//@Tag(name = "상담 PDF", description = "상담 보고서 PDF 생성 API")
//public class CounselPdfController {
//
//    // ✅ CounselService 사용 (CounselPdfService 대신)
//    private final CounselPdfService counselPdfService;
//
//    @GetMapping("/{counselId}/pdf")
//    @Operation(summary = "개별 상담 PDF 생성", description = "특정 상담에 대한 PDF 보고서를 생성합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "PDF 생성 성공"),
//            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
//            @ApiResponse(responseCode = "404", description = "상담을 찾을 수 없음")
//    })
//    public ResponseEntity<byte[]> generateSingleCounselPdf(
//            @Parameter(description = "상담 ID", required = true)
//            @PathVariable Long counselId,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("개별 상담 PDF 생성 요청 - 사용자: {}, 상담ID: {}", userDetails.getUsername(), counselId);
//
//        try {
//            // CounselService를 통해 PDF 생성 (실제 데이터 사용)
//            byte[] pdfBytes = counselPdfService.generateSingleCounselPdf(userDetails.getUsername(), counselId);
//
//            // 파일명 생성 (상담ID_날짜.pdf)
//            String filename = String.format("counsel_report_%d_%s.pdf",
//                    counselId,
//                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
//
//            // HTTP 응답 헤더 설정
//            HttpHeaders headers = createPdfHeaders(filename, pdfBytes.length);
//
//            log.info("개별 상담 PDF 생성 완료 - 파일명: {}, 크기: {} bytes", filename, pdfBytes.length);
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(pdfBytes);
//
//        } catch (Exception e) {
//            log.error("개별 상담 PDF 생성 실패 - 상담ID: {}, 오류: {}", counselId, e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    @GetMapping("/students/{studentId}/history/pdf")
//    @Operation(summary = "학생별 상담 이력 PDF 생성", description = "특정 학생의 모든 상담 이력을 PDF로 생성합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "PDF 생성 성공"),
//            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
//            @ApiResponse(responseCode = "404", description = "상담 이력을 찾을 수 없음")
//    })
//    public ResponseEntity<byte[]> generateStudentCounselHistoryPdf(
//            @Parameter(description = "학생 ID", required = true)
//            @PathVariable Long studentId,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("학생 상담 이력 PDF 생성 요청 - 사용자: {}, 학생ID: {}", userDetails.getUsername(), studentId);
//
//        try {
//            // CounselService를 통해 PDF 생성 (실제 데이터 사용)
//            byte[] pdfBytes = counselPdfService.generateStudentCounselHistoryPdf(userDetails.getUsername(), studentId);
//
//            // 파일명 생성 (student_counsel_history_학생ID_날짜.pdf)
//            String filename = String.format("student_counsel_history_%d_%s.pdf",
//                    studentId,
//                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
//
//            // HTTP 응답 헤더 설정
//            HttpHeaders headers = createPdfHeaders(filename, pdfBytes.length);
//
//            log.info("학생 상담 이력 PDF 생성 완료 - 파일명: {}, 크기: {} bytes", filename, pdfBytes.length);
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(pdfBytes);
//
//        } catch (Exception e) {
//            log.error("학생 상담 이력 PDF 생성 실패 - 학생ID: {}, 오류: {}", studentId, e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    /**
//     * PDF 응답을 위한 HTTP 헤더 생성
//     */
//    private HttpHeaders createPdfHeaders(String filename, int contentLength) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDispositionFormData("attachment", filename);
//        headers.setContentLength(contentLength);
//        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//        headers.add("Pragma", "no-cache");
//        headers.add("Expires", "0");
//        return headers;
//    }
//}