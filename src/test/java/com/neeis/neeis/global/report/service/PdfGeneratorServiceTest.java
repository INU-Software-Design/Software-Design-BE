package com.neeis.neeis.global.report.service;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorServiceTest {

    @Test
    public void testKoreanFont() {
        try {
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            System.out.println("한글 폰트 로드 성공!");
        } catch (Exception e) {
            System.out.println("한글 폰트 로드 실패: " + e.getMessage());
        }
    }
}