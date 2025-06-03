package com.neeis.neeis.global.report.service;

// iText 7 imports
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.io.font.constants.StandardFonts;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

// Spring imports
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.neeis.neeis.domain.student.dto.report.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// 프로젝트 imports
import com.neeis.neeis.domain.student.dto.res.*;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;

// Java imports
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    @Value("${pdf.font.path:#{null}}")
    private String fontPath;

    public byte[] generateStudentReportPdf(StudentReportResponseDto report) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            // 한글 폰트 설정
            PdfFont koreanFont = getKoreanFont();

            // 제목
            Paragraph title = new Paragraph("학생 생활기록부")
                    .setFont(koreanFont)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // 생성 일시
            Paragraph generatedDate = new Paragraph(
                    "생성일시: " + report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
                    .setFont(koreanFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(20);
            document.add(generatedDate);

            // 학적 정보 (필수)
            addStudentInfoSection(document, report.getStudentInfo(), koreanFont);

            // 출결 정보
            if (report.getAttendance() != null) {
                addAttendanceSection(document, report.getAttendance(), koreanFont);
            }

            // 성적 정보
            if (report.getGrades() != null) {
                addGradesSection(document, report.getGrades(), koreanFont);
            }

            // 상담 정보
            if (report.getCounseling() != null) {
                addCounselingSection(document, report.getCounseling(), koreanFont);
            }

            // 행동 및 종합 의견
            if (report.getBehavior() != null) {
                addBehaviorSection(document, report.getBehavior(), koreanFont);
            }

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.PDF_GENERATION_ERROR);
        }
    }

    private PdfFont getKoreanFont() {
        try {
            // 시스템에 설치된 한글 폰트 사용
            if (fontPath != null && !fontPath.isEmpty() && new File(fontPath).exists()) {
                return PdfFontFactory.createFont(fontPath, "Identity-H");
            }

            // 기본 한글 폰트 경로들
            String[] fontPaths = {
                    "c:/windows/fonts/malgun.ttf",  // Windows
                    "/System/Library/Fonts/AppleGothic.ttf",  // macOS
                    "/usr/share/fonts/truetype/nanum/NanumGothic.ttf"  // Linux
            };

            for (String path : fontPaths) {
                if (new File(path).exists()) {
                    return PdfFontFactory.createFont(path, "Identity-H");
                }
            }

            // 폰트를 찾지 못한 경우 기본 폰트 사용
            return PdfFontFactory.createFont(StandardFonts.HELVETICA);

        } catch (Exception e) {
            try {
                // 폰트 로딩 실패시 기본 폰트 사용
                return PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException ioException) {
                throw new CustomException(ErrorCode.PDF_GENERATION_ERROR);
            }
        }
    }

    private void addStudentInfoSection(Document document, StudentDetailResDto studentInfo, PdfFont font) {
        // 섹션 제목
        Paragraph sectionTitle = new Paragraph("1. 학적 정보")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(new DeviceRgb(52, 152, 219))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // 학생 정보 테이블
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        // 학생 기본 정보 (기존 DTO 필드명 사용)
        addTableRow(table, "이름", studentInfo.getName(), "담임교사", studentInfo.getTeacherName(), font);
        addTableRow(table, "성별", studentInfo.getGender(), "주민등록번호",
                studentInfo.getSsn() != null ? maskSsn(studentInfo.getSsn()) : "-", font);
        addTableRow(table, "전화번호",
                studentInfo.getPhone() != null ? studentInfo.getPhone() : "-",
                "주소",
                studentInfo.getAddress() != null ? studentInfo.getAddress() : "-", font);
        addTableRow(table, "학년", String.valueOf(studentInfo.getGrade()), "반",
                String.valueOf(studentInfo.getClassroom()), font);
        addTableRow(table, "번호", String.valueOf(studentInfo.getNumber()), "입학일자",
                studentInfo.getAdmissionDate() != null ? studentInfo.getAdmissionDate().toString() : "-", font);

        // 부모 정보
        if (studentInfo.getFatherName() != null || studentInfo.getMotherName() != null) {
            String fatherInfo = (studentInfo.getFatherName() != null ? studentInfo.getFatherName() : "-") +
                    (studentInfo.getFatherNum() != null ? " (" + studentInfo.getFatherNum() + ")" : "");
            String motherInfo = (studentInfo.getMotherName() != null ? studentInfo.getMotherName() : "-") +
                    (studentInfo.getMotherNum() != null ? " (" + studentInfo.getMotherNum() + ")" : "");
            addTableRow(table, "아버지", fatherInfo, "어머니", motherInfo, font);
        }

        document.add(table);
    }

    private void addAttendanceSection(Document document, AttendanceReportDto attendance, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("2. 출결 현황")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(new DeviceRgb(52, 152, 219))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // 출결 통계 테이블
        Table table = new Table(6).useAllAvailableWidth().setMarginBottom(15);

        // 테이블 헤더
        String[] headers = {"총 수업일수", "출석", "결석", "지각", "조퇴", "출석률"};
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setFont(font).setFontSize(12))
                    .setBackgroundColor(new DeviceRgb(236, 240, 241))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            table.addCell(cell);
        }

        // 데이터 행
        table.addCell(createDataCell(String.valueOf(attendance.getTotalDays()), font));
        table.addCell(createDataCell(String.valueOf(attendance.getPresentDays()), font));
        table.addCell(createDataCell(String.valueOf(attendance.getAbsentDays()), font));
        table.addCell(createDataCell(String.valueOf(attendance.getLateDays()), font));
        table.addCell(createDataCell(String.valueOf(attendance.getEarlyLeaveDays()), font));
        table.addCell(createDataCell(attendance.getAttendanceRate() + "%", font));

        document.add(table);
    }

    private void addGradesSection(Document document, GradesReportDto grades, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("3. 성적 현황")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(new DeviceRgb(52, 152, 219))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // 성적 요약
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        addTableRow(summaryTable, "평균 성적", String.format("%.2f", grades.getGpa()),
                "총 이수학점", String.valueOf(grades.getTotalCredits()), font);
        addTableRow(summaryTable, "반 석차", grades.getClassRank() > 0 ? grades.getClassRank() + "등" : "-",
                "학년 석차", grades.getGradeRank() > 0 ? grades.getGradeRank() + "등" : "-", font);

        document.add(summaryTable);

        // 과목별 성적 (있는 경우)
        if (grades.getSubjects() != null && !grades.getSubjects().isEmpty()) {
            Paragraph subjectsTitle = new Paragraph("과목별 성적")
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginTop(10)
                    .setMarginBottom(5);
            document.add(subjectsTitle);

            Table subjectsTable = new Table(7).useAllAvailableWidth().setMarginBottom(15);

            // 헤더
            String[] subjectHeaders = {"과목명", "원점수", "학점", "석차", "등급", "성취도", "수강자수"};
            for (String header : subjectHeaders) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setFont(font).setFontSize(12))
                        .setBackgroundColor(new DeviceRgb(236, 240, 241))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8);
                subjectsTable.addCell(cell);
            }

            // 과목 데이터
            for (SubjectGradeDto subject : grades.getSubjects()) {
                subjectsTable.addCell(createDataCell(subject.getSubjectName(), font));
                subjectsTable.addCell(createDataCell(String.format("%.0f", subject.getScore()), font));
                subjectsTable.addCell(createDataCell(String.valueOf(subject.getCredits()), font));
                subjectsTable.addCell(createDataCell(subject.getRank() + "/" + subject.getTotalStudents(), font));
                subjectsTable.addCell(createDataCell(subject.getGrade(), font));
                subjectsTable.addCell(createDataCell(subject.getAchievementLevel(), font));
                subjectsTable.addCell(createDataCell(String.valueOf(subject.getTotalStudents()), font));
            }

            document.add(subjectsTable);
        }
    }

    private void addCounselingSection(Document document, CounselingReportDto counseling, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("4. 상담 기록")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(new DeviceRgb(52, 152, 219))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Paragraph summary = new Paragraph("총 상담 횟수: " + counseling.getTotalSessions() + "회")
                .setFont(font)
                .setFontSize(12)
                .setMarginBottom(10);
        document.add(summary);

        if (counseling.getRecords() != null && !counseling.getRecords().isEmpty()) {
            Table table = new Table(4).useAllAvailableWidth().setMarginBottom(15);

            // 헤더
            String[] headers = {"상담일자", "상담 유형", "상담 내용", "상담교사"};
            for (String header : headers) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setFont(font).setFontSize(12))
                        .setBackgroundColor(new DeviceRgb(236, 240, 241))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8);
                table.addCell(cell);
            }

            // 상담 기록 데이터
            for (CounselingRecordDto record : counseling.getRecords()) {
                table.addCell(createDataCell(record.getCounselingDate().toString(), font));
                table.addCell(createDataCell(record.getCounselingType(), font));
                table.addCell(createDataCell(record.getContent(), font));
                table.addCell(createDataCell(record.getCounselorName(), font));
            }

            document.add(table);
        }
    }

    private void addBehaviorSection(Document document, BehaviorReportDto behavior, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("5. 행동 및 종합 의견")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(new DeviceRgb(52, 152, 219))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // 행동 등급
        if (behavior.getBehaviorGrade() != null && !behavior.getBehaviorGrade().isEmpty()) {
            Paragraph behaviorGrade = new Paragraph("행동 등급: " + behavior.getBehaviorGrade())
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(behaviorGrade);
        }

        // 종합 의견
        if (behavior.getComprehensiveOpinion() != null && !behavior.getComprehensiveOpinion().isEmpty()) {
            Paragraph opinion = new Paragraph("종합 의견:")
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(5);
            document.add(opinion);

            Paragraph opinionContent = new Paragraph(behavior.getComprehensiveOpinion())
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginLeft(20)
                    .setMarginBottom(15);
            document.add(opinionContent);
        }

        // 특별 활동
        if (behavior.getSpecialActivities() != null && !behavior.getSpecialActivities().isEmpty()) {
            Paragraph activitiesTitle = new Paragraph("특별 활동:")
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(5);
            document.add(activitiesTitle);

            for (String activity : behavior.getSpecialActivities()) {
                Paragraph activityItem = new Paragraph("• " + activity)
                        .setFont(font)
                        .setFontSize(12)
                        .setMarginLeft(20)
                        .setMarginBottom(3);
                document.add(activityItem);
            }
            document.add(new Paragraph(" ").setMarginBottom(10)); // 여백
        }

        // 수상 기록
        if (behavior.getAwards() != null && !behavior.getAwards().isEmpty()) {
            Paragraph awardsTitle = new Paragraph("수상 기록:")
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(5);
            document.add(awardsTitle);

            for (String award : behavior.getAwards()) {
                Paragraph awardItem = new Paragraph("• " + award)
                        .setFont(font)
                        .setFontSize(12)
                        .setMarginLeft(20)
                        .setMarginBottom(3);
                document.add(awardItem);
            }
        }
    }

    private void addTableRow(Table table, String label1, String value1,
                             String label2, String value2, PdfFont font) {
        table.addCell(createLabelCell(label1, font));
        table.addCell(createDataCell(value1, font));
        table.addCell(createLabelCell(label2, font));
        table.addCell(createDataCell(value2, font));
    }

    private Cell createLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(12))
                .setBackgroundColor(new DeviceRgb(245, 245, 245))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private Cell createDataCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(12))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    // 주민등록번호 마스킹 처리
    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 8) {
            return ssn;
        }
        return ssn.substring(0, 6) + "-" + ssn.substring(6, 7) + "******";
    }
}