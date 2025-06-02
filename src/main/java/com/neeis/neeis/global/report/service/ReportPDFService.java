package com.neeis.neeis.global.report.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.layout.font.FontProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.itextpdf.html2pdf.HtmlConverter;

@Service
public class ReportPDFService {

    public byte[] generateCounselPdf(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 1. Converter 설정
            ConverterProperties properties = new ConverterProperties();

            // 2. FontProvider에 한글 폰트 등록
            FontProvider fontProvider = new FontProvider();
            File fontFile = new ClassPathResource("fonts/NanumGothic.ttf").getFile();
            fontProvider.addFont(fontFile.getAbsolutePath());
            properties.setFontProvider(fontProvider);

            // 3. 기본 폰트 family 설정 (html에 지정된 폰트 이름과 맞춰야 함)
            properties.setCharset("utf-8");

            HtmlConverter.convertToPdf(htmlContent, outputStream, properties);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }
}