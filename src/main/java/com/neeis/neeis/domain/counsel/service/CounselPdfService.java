//package com.neeis.neeis.domain.counsel.service;
//
//import com.neeis.neeis.domain.counsel.Counsel;
//import com.neeis.neeis.domain.counsel.CounselRepository;
//import com.neeis.neeis.domain.counsel.dto.pdf.CounselPdfDto;
//import com.neeis.neeis.domain.user.Role;
//import com.neeis.neeis.domain.user.User;
//import com.neeis.neeis.domain.user.service.UserService;
//import com.neeis.neeis.global.exception.CustomException;
//import com.neeis.neeis.global.exception.ErrorCode;
//import com.neeis.neeis.global.report.service.ReportPDFService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 상담 보고서 PDF 생성 서비스
// *
// * 상담 데이터를 기반으로 HTML 템플릿을 렌더링하고 PDF로 변환하는 서비스입니다.
// * TDD 방식으로 개발되어 각 기능별로 철저한 테스트가 보장됩니다.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class CounselPdfService {
//
//    private final CounselRepository counselRepository;
//    private final UserService userService;
//    private final ReportPDFService reportPDFService;
//    private final TemplateEngine templateEngine;
//
//    /**
//     * 개별 상담 PDF 생성
//     *
//     * @param username 요청자 사용자명
//     * @param counselId 상담 ID
//     * @return PDF 바이트 배열
//     */
//    public byte[] generateSingleCounselPdf(String username, Long counselId) {
//        log.info("상담 PDF 생성 요청 - 사용자: {}, 상담ID: {}", username, counselId);
//
//        // 사용자 권한 검증 및 상담 데이터 조회
//        User user = userService.getUser(username);
//        Counsel counsel = getCounselWithAccessValidation(user, counselId);
//
//        // 실제 데이터를 DTO로 변환
//        CounselPdfDto counselDto = CounselPdfDto.fromEntity(counsel);
//
//        // 템플릿 데이터 구성
//        Context context = createSingleCounselContext(counselDto);
//
//        // HTML 렌더링
//        String renderedHtml = templateEngine.process("counsel-report", context);
//
//        // PDF 생성
//        byte[] pdfBytes = reportPDFService.generateCounselPdf(renderedHtml);
//
//        log.info("상담 PDF 생성 완료 - 크기: {} bytes", pdfBytes.length);
//        return pdfBytes;
//    }
//
//
//    /**
//     * 학생별 상담 이력 PDF 생성
//     *
//     * @param username 요청자 사용자명
//     * @param studentId 학생 ID
//     * @return PDF 바이트 배열
//     */
//    public byte[] generateStudentCounselHistoryPdf(String username, Long studentId) {
//        log.info("학생 상담 이력 PDF 생성 요청 - 사용자: {}, 학생ID: {}", username, studentId);
//
//        // 사용자 권한 검증
//        User user = userService.getUser(username);
//        validateUserAccessToStudent(user, studentId);
//
//        // 상담 이력 조회 (최신순 정렬)
//        List<Counsel> counsels = counselRepository.findByStudentId(studentId)
//                .orElseThrow(() -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND));
//
//        if (counsels.isEmpty()) {
//            throw new CustomException(ErrorCode.COUNSEL_NOT_FOUND);
//        }
//
//        // 날짜순으로 정렬 (최신이 먼저)
//        counsels.sort((c1, c2) -> c2.getDateTime().compareTo(c1.getDateTime()));
//
//        // 템플릿 데이터 구성
//        Context context = createStudentCounselHistoryContext(counsels);
//
//        // HTML 렌더링
//        String renderedHtml = templateEngine.process("student-counsel-history", context);
//
//        // PDF 생성
//        byte[] pdfBytes = reportPDFService.generateCounselPdf(renderedHtml);
//
//        log.info("학생 상담 이력 PDF 생성 완료 - 상담 건수: {}, 크기: {} bytes",
//                counsels.size(), pdfBytes.length);
//        return pdfBytes;
//    }
//
//    /**
//     * 상담 조회 및 접근 권한 검증
//     */
//    private Counsel getCounselWithAccessValidation(User user, Long counselId) {
//        Counsel counsel = counselRepository.findById(counselId)
//                .orElseThrow(() -> new CustomException(ErrorCode.COUNSEL_NOT_FOUND));
//
//        validateUserAccessToCounsel(user, counsel);
//        return counsel;
//    }
//
//    /**
//     * 상담 접근 권한 검증
//     */
//    private void validateUserAccessToCounsel(User user, Counsel counsel) {
//        Role userRole = user.getRole();
//
//        switch (userRole) {
//            case TEACHER -> {
//                // 교사는 모든 상담에 접근 가능
//                log.debug("교사 권한으로 상담 접근 허용 - 교사: {}", user.getUsername());
//            }
//            case STUDENT -> {
//                // 학생은 본인 상담만 접근 가능
//                if (!user.getId().equals(counsel.getStudent().getUser().getId())) {
//                    log.warn("학생의 다른 학생 상담 접근 시도 - 요청자: {}, 상담학생: {}",
//                            user.getUsername(), counsel.getStudent().getUser().getUsername());
//                    throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
//                }
//                log.debug("학생 본인 상담 접근 허용 - 학생: {}", user.getUsername());
//            }
//            case PARENT -> {
//                // 부모는 자녀 상담만 접근 가능 (실제 구현에서는 Parent-Student 관계 확인 필요)
//                log.debug("부모 권한으로 상담 접근 허용 - 부모: {}", user.getUsername());
//                // TODO: Parent-Student 관계 검증 로직 추가 필요
//            }
//            default -> {
//                log.warn("허용되지 않은 권한으로 상담 접근 시도 - 사용자: {}, 권한: {}",
//                        user.getUsername(), userRole);
//                throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
//            }
//        }
//    }
//
//    /**
//     * 학생 데이터 접근 권한 검증
//     */
//    private void validateUserAccessToStudent(User user, Long studentId) {
//        Role userRole = user.getRole();
//
//        switch (userRole) {
//            case TEACHER -> {
//                // 교사는 모든 학생 정보에 접근 가능
//                log.debug("교사 권한으로 학생 정보 접근 허용 - 교사: {}, 학생ID: {}",
//                        user.getUsername(), studentId);
//            }
//            case STUDENT -> {
//                // 학생은 본인 정보만 접근 가능 (실제로는 User ID와 Student ID 매핑 확인 필요)
//                log.debug("학생 본인 정보 접근 허용 - 학생: {}, 학생ID: {}",
//                        user.getUsername(), studentId);
//                // TODO: 사용자 ID와 학생 ID 매핑 검증 로직 추가
//            }
//            case PARENT -> {
//                // 부모는 자녀 정보만 접근 가능
//                log.debug("부모 권한으로 자녀 정보 접근 허용 - 부모: {}, 학생ID: {}",
//                        user.getUsername(), studentId);
//                // TODO: Parent-Student 관계 검증 로직 추가
//            }
//            default -> {
//                log.warn("허용되지 않은 권한으로 학생 정보 접근 시도 - 사용자: {}, 권한: {}",
//                        user.getUsername(), userRole);
//                throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
//            }
//        }
//    }
//
//    /**
//     * 개별 상담 템플릿 컨텍스트 생성 (실제 데이터 사용)
//     */
//    private Context createSingleCounselContext(CounselPdfDto counselDto) {
//        Context context = new Context();
//
//        // 실제 데이터 바인딩
//        context.setVariable("studentName", counselDto.getStudentName());
//        context.setVariable("teacherName", counselDto.getTeacherName());
//        context.setVariable("category", getCategoryKoreanName(counselDto.getCategory()));
//        context.setVariable("counselDate", counselDto.getCounselDate());
//        context.setVariable("counselTime", counselDto.getCounselTime());
//        context.setVariable("content", counselDto.getContent());
//        context.setVariable("nextPlan", counselDto.getNextPlan());
//        context.setVariable("generatedDate", counselDto.getGeneratedDate());
//
//        log.debug("개별 상담 템플릿 데이터 설정 완료 - 학생: {}, 교사: {}",
//                counselDto.getStudentName(), counselDto.getTeacherName());
//
//        return context;
//    }
//
//    /**
//     * 학생 상담 이력 템플릿 컨텍스트 생성 (실제 데이터 사용)
//     */
//    private Context createStudentCounselHistoryContext(List<Counsel> counsels) {
//        Context context = new Context();
//
//        // 학생 기본 정보 (첫 번째 상담에서 추출)
//        Counsel firstCounsel = counsels.get(0);
//        String studentName = firstCounsel.getStudent().getName();
//
//        context.setVariable("studentName", studentName);
//        context.setVariable("counsels", counsels);
//        context.setVariable("totalCounselCount", counsels.size());
//
//        // 카테고리별 통계 계산
//        Map<String, Integer> categoryStats = calculateCategoryStatistics(counsels);
//        context.setVariable("categoryStats", categoryStats);
//
//        // 기간 정보 계산
//        String periodStart = counsels.stream()
//                .map(Counsel::getDateTime)
//                .min(Comparator.naturalOrder())
//                .map(dt -> dt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
//                .orElse("");
//
//        String periodEnd = counsels.stream()
//                .map(Counsel::getDateTime)
//                .max(Comparator.naturalOrder())
//                .map(dt -> dt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
//                .orElse("");
//
//        context.setVariable("periodStart", periodStart);
//        context.setVariable("periodEnd", periodEnd);
//
//        // PDF 생성 정보
//        context.setVariable("generatedDate",
//                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")));
//
//        log.debug("학생 상담 이력 템플릿 데이터 설정 완료 - 학생: {}, 상담수: {}, 기간: {} ~ {}",
//                studentName, counsels.size(), periodStart, periodEnd);
//
//        return context;
//    }
//
//    /**
//     * 상담 카테고리별 통계 계산 (실제 데이터 기반)
//     */
//    private Map<String, Integer> calculateCategoryStatistics(List<Counsel> counsels) {
//        Map<String, Integer> stats = new HashMap<>();
//
//        counsels.forEach(counsel -> {
//            String category = getCategoryKoreanName(counsel.getCategory().name());
//            stats.put(category, stats.getOrDefault(category, 0) + 1);
//        });
//
//        log.debug("카테고리별 통계 계산 완료 - {}", stats);
//        return stats;
//    }
//
//    /**
//     * 상담 카테고리 영문을 한글로 변환
//     */
//    private String getCategoryKoreanName(String categoryName) {
//        return switch (categoryName) {
//            case "UNIVERSITY" -> "대학상담";
//            case "CAREER" -> "진로상담";
//            case "ACADEMIC" -> "학업상담";
//            case "PERSONAL" -> "개인상담";
//            case "FAMILY" -> "가정상담";
//            case "OTHER" -> "기타";
//            default -> categoryName;
//        };
//    }
//}