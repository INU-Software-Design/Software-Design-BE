package com.neeis.neeis.domain.counsel.dto.pdf;

import com.neeis.neeis.domain.counsel.Counsel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 상담 PDF 생성용 DTO
 */
@Getter
@Builder
public class CounselPdfDto {
    private final String studentName;
    private final String teacherName;
    private final String category;
    private final String counselDate;
    private final String counselTime;
    private final String content;
    private final String nextPlan;
    private final String generatedDate;


    /**
     * Counsel 엔티티를 PDF DTO로 변환
     */
    public static CounselPdfDto fromEntity(Counsel counsel) {

        return CounselPdfDto.builder()
                .studentName(counsel.getStudent().getName())
                .teacherName(counsel.getTeacher().getName())
                .category(getCategoryKoreanName(counsel.getCategory().name()))
                .counselDate(counsel.getDateTime().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
                .content(counsel.getContent())
                .nextPlan(counsel.getNextPlan())
                .generatedDate(counsel.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")))
                .build();
    }

    /**
     * 상담 카테고리 영문을 한글로 변환
     */
    private static String getCategoryKoreanName(String categoryName) {
        return switch (categoryName) {
            case "UNIVERSITY" -> "대학상담";
            case "CAREER" -> "진로상담";
            case "ACADEMIC" -> "학업상담";
            case "PERSONAL" -> "개인상담";
            case "FAMILY" -> "가정상담";
            case "OTHER" -> "기타";
            default -> categoryName;
        };
    }
}