package com.neeis.neeis.global.report.service;

// iText 7 imports
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.io.font.constants.StandardFonts;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.neeis.neeis.domain.attendance.dto.res.AttendanceFeedbackResDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import com.neeis.neeis.domain.student.dto.report.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.neeis.neeis.domain.student.dto.res.*;
import com.neeis.neeis.global.exception.CustomException;
import com.neeis.neeis.global.exception.ErrorCode;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    @Value("${image.path}")
    private String uploadPath;

    // 폰트 경로만 캐싱하고, 실제 PdfFont 객체는 매번 새로 생성
    private String cachedFontPath = null;
    private Boolean isKoreanFontSupported = null;

    public byte[] generateStudentReportPdf(StudentReportResponseDto report) {
        ByteArrayOutputStream outputStream = null;
        Document document = null;

        try {
            outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            document = new Document(pdfDocument, PageSize.A4);

            // 매번 새로운 폰트 객체 생성 (경로만 캐싱)
            PdfFont font = createNewFont();
            boolean isKorean = checkKoreanFontSupport(font);

            log.info("PDF 생성 시작 - 한글 폰트 지원: {}", isKorean);

            // 제목
            addTitle(document, font, isKorean);

            // 생성 일시
            addGeneratedDate(document, report, font, isKorean);

            // 섹션 번호를 위한 카운터 초기화
            AtomicInteger sectionNumber = new AtomicInteger(1);

            // 학적 정보 (필수 - 항상 포함)
            addStudentInfoSection(document, report.getStudentInfo(), font, isKorean, sectionNumber.getAndIncrement());

            // 출결 정보 (선택적)
            if (report.getAttendance() != null) {
                addAttendanceSection(document, report.getAttendance(), font, isKorean, sectionNumber.getAndIncrement());
            }

            // 성적 정보 (선택적)
            if (report.getGrades() != null) {
                addGradesSection(document, report.getGrades(), font, isKorean, sectionNumber.getAndIncrement());
            }

            // 상담 정보 (선택적)
            if (report.getCounseling() != null) {
                addCounselingSection(document, report.getCounseling(), font, isKorean, sectionNumber.getAndIncrement());
            }

            // 행동 및 종합 의견 (선택적)
            if (report.getBehavior() != null) {
                addBehaviorSection(document, report.getBehavior(), font, isKorean, sectionNumber.getAndIncrement());
            }

            if (report.getScoreFeedbacks() != null) {
                addScoreFeedbackSection(document, report.getScoreFeedbacks(), font, isKorean, sectionNumber.getAndIncrement());
            }

            if (report.getAttendanceFeedback() != null) {
                addAttendanceFeedbackSection(document, report.getAttendanceFeedback(), font, isKorean, sectionNumber.getAndIncrement());
            }

            // Document 닫기
            document.close();
            document = null; // null로 설정하여 finally에서 중복 close 방지

            byte[] pdfData = outputStream.toByteArray();

            // PDF 데이터 검증
            validatePdfData(pdfData);

            log.info("PDF 생성 완료 - 크기: {} bytes", pdfData.length);
            return pdfData;

        } catch (Exception e) {
            log.error("PDF 생성 중 오류", e);
            throw new CustomException(ErrorCode.PDF_GENERATION_ERROR);
        } finally {
            // 리소스 정리
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    log.warn("Document 닫기 실패", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.warn("OutputStream 닫기 실패", e);
                }
            }
        }
    }

    private void addTitle(Document document, PdfFont font, boolean isKorean) {
        try {
            String title = isKorean ? "학생 생활기록부" : "Student Life Record";

            Paragraph titleParagraph = new Paragraph(title)
                    .setFont(font)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titleParagraph);
        } catch (Exception e) {
            log.error("제목 추가 실패", e);
            // 기본 제목으로 폴백
            try {
                Paragraph fallback = new Paragraph("Student Report")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        .setFontSize(18)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(fallback);
            } catch (Exception ex) {
                log.error("폴백 제목도 실패", ex);
            }
        }
    }

    private void addGeneratedDate(Document document, StudentReportResponseDto report, PdfFont font, boolean isKorean) {
        try {
            String dateText = isKorean ?
                    "생성일시: " + report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                    "Generated: " + report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Paragraph dateParagraph = new Paragraph(dateText)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10);
            document.add(dateParagraph);
        } catch (Exception e) {
            log.error("생성일시 추가 실패", e);
        }
    }

    private void addStudentInfoSection(Document document, StudentDetailResDto studentInfo, PdfFont font, boolean isKorean, int sectionNumber) {
        try {
            String sectionTitle = isKorean ?
                    sectionNumber + ". 학적 정보" :
                    sectionNumber + ". Student Information";

            Paragraph title = new Paragraph(sectionTitle)
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(5)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            // 메인 테이블: 3열 (사진, 라벨+값, 라벨+값)
            Table mainTable = new Table(UnitValue.createPercentArray(new float[]{2f, 3f, 3f}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);

            // 학생 사진 (4행 병합)
            Cell photoCell = addStudentPhoto(studentInfo, font, isKorean);
            mainTable.addCell(photoCell);

            // 첫 번째 행: 이름
            Cell nameCell = new Cell(1, 2)  // 2열 병합
                    .add(createInfoPair(isKorean ? "이름 " : "Name",
                            sanitizeText(studentInfo.getName()), font))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(8);
            mainTable.addCell(nameCell);

            Cell gradeLabelCell = createLabelCell(isKorean ? "학년/반" : "Grade/Class", font);
            Cell gradeValueCell = createDataCell(studentInfo.getGrade() + "학년 " + studentInfo.getClassroom() + "반", font);
            mainTable.addCell(gradeLabelCell);
            mainTable.addCell(gradeValueCell);

            Cell numberLabelCell = createLabelCell(isKorean ? "번호" : "Number", font);
            Cell numberValueCell = createDataCell(String.valueOf(studentInfo.getNumber()), font);
            mainTable.addCell(numberLabelCell);
            mainTable.addCell(numberValueCell);

            Cell teacherLabelCell = createLabelCell(isKorean ? "담임교사" : "Teacher", font);
            Cell teacherValueCell = createDataCell(sanitizeText(studentInfo.getTeacherName()), font);
            mainTable.addCell(teacherLabelCell);
            mainTable.addCell(teacherValueCell);

            document.add(mainTable);

            // 별도 테이블: 담임교사, 입학일
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);

            addTableRow(infoTable,
                    isKorean ? "성별" : "Gender",
                    sanitizeText(studentInfo.getGender()),
                    isKorean ? "입학일" : "Admission Date",
                    studentInfo.getAdmissionDate() != null ? studentInfo.getAdmissionDate().toString() : "",
                    font);

            addTableRow(infoTable,
                    isKorean ? "주민등록번호" : "ID Number",
                    studentInfo.getSsn() != null ? maskSsn(studentInfo.getSsn()) : "-",
                    isKorean ? "전화번호" : "Phone",
                    sanitizeText(studentInfo.getPhone()),
                    font);

            document.add(infoTable);

            // 주소 테이블
            if (studentInfo.getAddress() != null && !studentInfo.getAddress().trim().isEmpty()) {
                Table addressTable = new Table(UnitValue.createPercentArray(new float[]{1, 4}))
                        .useAllAvailableWidth()
                        .setMarginBottom(5);

                addressTable.addCell(createLabelCell(isKorean ? "주소" : "Address", font));
                addressTable.addCell(createDataCell(sanitizeText(studentInfo.getAddress()), font));

                document.add(addressTable);
            }

            // 부모 정보 섹션
            addParentInfoSubSection(document, studentInfo, font, isKorean);

        } catch (Exception e) {
            log.error("학생 정보 섹션 추가 실패", e);
        }
    }

    // 헬퍼 메서드 추가
    private Paragraph createInfoPair(String label, String value, PdfFont font) {
        return new Paragraph()
                .add(new Text(label + ": ").setFont(font).setFontSize(12).setBold())
                .add(new Text(value).setFont(font).setFontSize(12));
    }

    private void addParentInfoSubSection(Document document, StudentDetailResDto studentInfo, PdfFont font, boolean isKorean) {
        try {
            // 부모 정보 소제목
            String parentSubTitle = isKorean ? "▶ 보호자 정보" : "▶ Parent Information";
            Paragraph parentTitle = new Paragraph(parentSubTitle)
                    .setFont(font)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(8);
            document.add(parentTitle);

            // 부모 정보 테이블
            Table parentTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            // 아버지 정보
            addTableRow(parentTable,
                    isKorean ? "부" : "Father",
                    sanitizeText(studentInfo.getFatherName()),
                    isKorean ? "부 연락처" : "Father Phone",
                    sanitizeText(studentInfo.getFatherNum()),
                    font);

            // 어머니 정보
            addTableRow(parentTable,
                    isKorean ? "모" : "Mother",
                    sanitizeText(studentInfo.getMotherName()),
                    isKorean ? "모 연락처" : "Mother Phone",
                    sanitizeText(studentInfo.getMotherNum()),
                    font);

            document.add(parentTable);
        } catch (Exception e) {
            log.error("부모 정보 하위 섹션 추가 실패", e);
        }
    }

    private void addAttendanceSection(Document document, AttendanceReportDto attendance, PdfFont font, boolean isKorean, int sectionNumber) {
        try {
            String sectionTitle = isKorean ?
                    sectionNumber + ". 출결 현황" :
                    sectionNumber + ". Attendance";

            Paragraph title = new Paragraph(sectionTitle)
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(10)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            Table table = new Table(6).useAllAvailableWidth().setMarginBottom(15);

            String[] headers = isKorean ?
                    new String[]{"총 수업일수", "출석", "결석", "지각", "조퇴", "출석률"} :
                    new String[]{"Total Days", "Present", "Absent", "Late", "Early Leave", "Rate"};

            for (String header : headers) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setFont(font).setFontSize(12))
                        .setBackgroundColor(new DeviceRgb(236, 240, 241))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8);
                table.addCell(cell);
            }

            table.addCell(createDataCell(String.valueOf(attendance.getTotalDays()), font));
            table.addCell(createDataCell(String.valueOf(attendance.getPresentDays()), font));
            table.addCell(createDataCell(String.valueOf(attendance.getAbsentDays()), font));
            table.addCell(createDataCell(String.valueOf(attendance.getLateDays()), font));
            table.addCell(createDataCell(String.valueOf(attendance.getEarlyLeaveDays()), font));
            table.addCell(createDataCell(attendance.getAttendanceRate() + "%", font));

            document.add(table);
        } catch (Exception e) {
            log.error("출결 섹션 추가 실패", e);
        }
    }

    private void addGradesSection(Document document, GradesReportDto grades, PdfFont font, boolean isKorean, int sectionNumber) {
        try {
            String sectionTitle = isKorean ?
                    sectionNumber + ". 성적 현황" :
                    sectionNumber + ". Academic Performance";

            Paragraph title = new Paragraph(sectionTitle)
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(10)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            // 과목별 상세 성적표 테이블
            if (grades.getSubjects() != null && !grades.getSubjects().isEmpty()) {
                addDetailedGradesTable(document, grades.getSubjects(), font, isKorean);
            }

            // 성적 요약 정보
            addGradesSummary(document, grades, font, isKorean);

            // 성적 레이더 차트 추가
            addGradesRadarChart(document, grades, font, isKorean);

        } catch (Exception e) {
            log.error("성적 섹션 추가 실패", e);
        }
    }

    private void addDetailedGradesTable(Document document,
                                        java.util.List subjects,
                                        PdfFont font, boolean isKorean) {
        try {
            // 간소화된 성적표 테이블 - 9개 컬럼
            float[] columnWidths = new float[]{1.6f, 1.0f, 1.2f, 1.0f, 1.2f, 1.6f, 1.2f, 1.0f, 0.8f}; // 전체적으로 약간 축소

            Table gradesTable = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth()
                    .setFontSize(11)
                    .setMarginBottom(15);

            // 헤더 행 - 9개 컬럼
            String[] headers = isKorean ?
                    new String[]{"과목", "만점", "합계 점수", "평균", "원점수", "과목평균(표준편차)", "석차등급", "성취도", "석차"} :
                    new String[]{"Subject", "Full Score", "Score", "Average", "Raw Score", "Subj Avg(Std Dev)", "Grade Level", "Achievement", "Rank"};

            for (String header : headers) {
                gradesTable.addCell(createHeaderCell(header, font));
            }

            // 각 과목별 데이터 행 추가
            for (Object subjectObj : subjects) {
                SubjectScoreDto subject = (SubjectScoreDto) subjectObj;

                // 1. 과목명
                gradesTable.addCell(createDataCell(subject.getSubjectName(), font));

                // 2. 만점 (일반적으로 100점)
                gradesTable.addCell(createDataCell("100", font));

                // 3. 획득점수 (가중합계)
                gradesTable.addCell(createDataCell(String.valueOf((int)subject.getWeightedTotal()), font));

                // 4. 평균 (학생 개인 평균)
                gradesTable.addCell(createDataCell(String.format("%.1f", subject.getAverage()), font));

                // 5. 원점수 (가중합계와 동일하거나 다른 값일 수 있음)
                gradesTable.addCell(createDataCell(String.valueOf((int)subject.getWeightedTotal()), font));

                // 6. 과목평균(표준편차) - 예: "85.2(12.5)"
                String avgStdText = String.format("%.1f(%.1f)",
                        subject.getAverage(),
                        subject.getStdDev());
                gradesTable.addCell(createDataCell(avgStdText, font));

                // 7. 석차등급 - 예: "2등급"
                String gradeText = subject.getGrade() + "등급";
                gradesTable.addCell(createDataCell(gradeText, font));

                // 8. 성취도
                gradesTable.addCell(createDataCell(subject.getAchievementLevel(), font));

                // 9. 석차 - 예: "5등"
                String rankText = subject.getRank() + "등";
                gradesTable.addCell(createDataCell(rankText, font));
            }

            document.add(gradesTable);

        } catch (Exception e) {
            log.error("상세 성적표 테이블 추가 실패", e);
        }
    }


    private void addGradesSummary(Document document, GradesReportDto grades, PdfFont font, boolean isKorean) {
        try {
            // 요약 정보 테이블 - 3개 항목만
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 2f, 1.8f, 1.5f, 1.5f, 1.7f}))
                    .useAllAvailableWidth()
                    .setMarginTop(5)
                    .setMarginBottom(5);

            // 전체평균
            summaryTable.addCell(createLabelCell(isKorean ? "전체평균" : "Overall Average", font));
            summaryTable.addCell(createDataCell(String.format("%.2f", grades.getGpa()), font));

            // 총 이수과목
            summaryTable.addCell(createLabelCell(isKorean ? "총 이수과목" : "Total Subjects", font));
            summaryTable.addCell(createDataCell(String.valueOf(grades.getTotalSubjects()), font));

            // 학급 순위
            summaryTable.addCell(createLabelCell(isKorean ? "학급 순위" : "Class Rank", font));
            summaryTable.addCell(createDataCell(grades.getClassRank() + "/" + grades.getTotalStudents(), font));

            document.add(summaryTable);

            // 성적 분석 차트 공간 (텍스트로 대체)
            if (isKorean) {
                Paragraph chartNote = new Paragraph("※ 상세 성적 분석 차트는 온라인에서 확인 가능합니다.")
                        .setFont(font)
                        .setFontSize(10)
                        .setItalic()
                        .setMarginTop(5);
                document.add(chartNote);
            }

        } catch (Exception e) {
            log.error("성적 요약 정보 추가 실패", e);
        }
    }

    private Cell createHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(new DeviceRgb(135, 206, 250))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4);
    }


    /**
     * 성적 레이더 차트 추가
     */
    private void addGradesRadarChart(Document document, GradesReportDto grades, PdfFont font, boolean isKorean) {
        try {
            // 차트 제목
            String chartTitle = isKorean ? "성적 레이더 차트" : "Grade Radar Chart";
            Paragraph title = new Paragraph(chartTitle)
                    .setFont(font)
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5);
            document.add(title);

            // 레이더 차트 이미지 생성
            BufferedImage chartImage = createRadarChart(grades, isKorean);

            if (chartImage != null) {
                // BufferedImage를 바이트 배열로 변환
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(chartImage, "PNG", baos);
                byte[] imageBytes = baos.toByteArray();

                // PDF에 이미지 추가
                Image pdfImage = new Image(ImageDataFactory.create(imageBytes));
                pdfImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                pdfImage.scaleToFit(350, 250); // 적절한 크기로 조정
                document.add(pdfImage);

                // 차트 설명
                String chartNote = isKorean ?
                        "※ 차트 바깥쪽 1등급, 안쪽으로 갈수록 9등급을 의미합니다" :
                        "※ Outer edge represents Grade 1, inner represents Grade 9";
                Paragraph note = new Paragraph(chartNote)
                        .setFont(font)
                        .setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(5);
                document.add(note);
            }

        } catch (Exception e) {
            log.error("성적 레이더 차트 추가 실패", e);
        }
    }



    private void addCounselingSection(Document document, CounselingReportDto counseling, PdfFont font, boolean isKorean, int sectionNumber) {
        try {
            String sectionTitle = isKorean ?
                    sectionNumber + ". 상담 기록" :
                    sectionNumber + ". Counseling";

            Paragraph title = new Paragraph(sectionTitle)
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(10)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            // 상담 횟수 요약
            String summaryText = isKorean ?
                    "총 상담 횟수: " + counseling.getTotalSessions() + "회" :
                    "Total Sessions: " + counseling.getTotalSessions();

            Paragraph summary = new Paragraph(summaryText)
                    .setFont(font)
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(10);
            document.add(summary);

            // 상담 기록이 있는 경우 세부 내용 표시
            if (counseling.getRecords() != null && !counseling.getRecords().isEmpty()) {
                addCounselingDetails(document, counseling.getRecords(), font, isKorean);
            } else {
                // 상담 기록이 없는 경우
                String noRecordsText = isKorean ?
                        "등록된 상담 기록이 없습니다." :
                        "No counseling records found.";

                Paragraph noRecords = new Paragraph(noRecordsText)
                        .setFont(font)
                        .setFontSize(11)
                        .setItalic()
                        .setMarginBottom(10);
                document.add(noRecords);
            }

        } catch (Exception e) {
            log.error("상담 섹션 추가 실패", e);
        }
    }

    /**
     * 상담 세부 내용 추가
     */
    private void addCounselingDetails(Document document, java.util.List<CounselingRecordDto> records, PdfFont font, boolean isKorean) {
        try {
            for (int i = 0; i < records.size(); i++) {
                // 상담 기록을 CounselDetailDto로 캐스팅 (실제 타입에 맞게 조정)
                Object recordObj = records.get(i);

                // 각 상담 기록을 박스 형태로 표시
                addCounselingRecordBox(document, recordObj, i + 1, font, isKorean);
            }
        } catch (Exception e) {
            log.error("상담 세부 내용 추가 실패", e);
        }
    }

    /**
     * 개별 상담 기록 박스 추가
     */
    private void addCounselingRecordBox(Document document, Object recordObj, int recordNumber, PdfFont font, boolean isKorean) {
        try {
            // 실제 CounselDetailDto의 구조에 맞게 조정 필요
            // 여기서는 일반적인 구조를 가정하고 작성

            // 상담 기록 제목
            String recordTitle = isKorean ?
                    "상담 기록 " + recordNumber :
                    "Counseling Record " + recordNumber;

            Paragraph titlePara = new Paragraph(recordTitle)
                    .setFont(font)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8);
            document.add(titlePara);

            // 상담 기록 테이블 생성
            Table recordTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 3.5f}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            // 상담 일자 (recordObj에서 실제 데이터 추출 필요)
            addCounselingRow(recordTable,
                    isKorean ? "상담 일자" : "Date",
                    "2024-12-01", // 실제 데이터로 교체 필요
                    font);

            // 상담 분류
            addCounselingRow(recordTable,
                    isKorean ? "상담 분류" : "Category",
                    "진로 상담", // 실제 데이터로 교체 필요
                    font);

            // 담당 교사
            addCounselingRow(recordTable,
                    isKorean ? "담당 교사" : "Teacher",
                    "홍길동", // 실제 데이터로 교체 필요
                    font);

            document.add(recordTable);

            // 상담 내용 박스
            addCounselingContentBox(document,
                    isKorean ? "상담 내용" : "Counseling Content",
                    "학생의 진로에 대한 고민을 상담하였습니다. 학생은 공학 분야에 관심이 많으며...", // 실제 데이터로 교체 필요
                    font, isKorean);

            // 다음 계획 박스
            addCounselingContentBox(document,
                    isKorean ? "다음 계획" : "Next Plan",
                    "다음 주에 진로 체험 활동 참여 예정", // 실제 데이터로 교체 필요
                    font, isKorean);

        } catch (Exception e) {
            log.error("상담 기록 박스 추가 실패 - 기록 번호: {}", recordNumber, e);
        }
    }

    /**
     * 상담 테이블 행 추가
     */
    private void addCounselingRow(Table table, String label, String value, PdfFont font) {
        // 라벨 셀
        Cell labelCell = new Cell()
                .add(new Paragraph(label)
                        .setFont(font)
                        .setFontSize(10)
                        .setBold())
                .setBackgroundColor(new DeviceRgb(240, 240, 240))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);
        table.addCell(labelCell);

        // 값 셀
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "")
                        .setFont(font)
                        .setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(6);
        table.addCell(valueCell);
    }

    /**
     * 상담 내용/계획 박스 추가
     */
    private void addCounselingContentBox(Document document, String title, String content, PdfFont font, boolean isKorean) {
        try {
            // 제목
            Paragraph titlePara = new Paragraph(title)
                    .setFont(font)
                    .setFontSize(11)
                    .setBold()
                    .setMarginTop(8)
                    .setMarginBottom(5);
            document.add(titlePara);

            // 내용이 없으면 기본 텍스트
            if (content == null || content.trim().isEmpty()) {
                content = isKorean ? "내용이 없습니다." : "No content available.";
            }

            // 내용 길이에 따른 박스 높이 계산
            int contentLength = content.length();
            float boxHeight = Math.max(50, Math.min(100, 50 + (contentLength / 60) * 15));

            // 테이블로 박스 생성
            Table contentBox = new Table(1)
                    .useAllAvailableWidth()
                    .setMarginBottom(10);

            Cell contentCell = new Cell()
                    .add(new Paragraph(content)
                            .setFont(font)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.LEFT)
                            .setMarginTop(3)
                            .setMarginBottom(3))
                    .setBorder(new com.itextpdf.layout.borders.SolidBorder(new DeviceRgb(180, 180, 180), 1))
                    .setPadding(8)
                    .setHeight(boxHeight)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);

            contentBox.addCell(contentCell);
            document.add(contentBox);

        } catch (Exception e) {
            log.error("상담 내용 박스 추가 실패 - 제목: {}", title, e);
        }
    }


    private void addBehaviorSection(Document document, BehaviorReportDto behavior, PdfFont font, boolean isKorean, int sectionNumber) {
        try {
            String sectionTitle = isKorean ?
                    sectionNumber + ". 행동특성 및 종합 의견" :
                    sectionNumber + ". Behavior & Comments";

            Paragraph title = new Paragraph(sectionTitle)
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(10)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            // 행동특성 박스 (항상 표시)
            String behaviorContent = (behavior.getBehaviorGrade() != null && !behavior.getBehaviorGrade().isEmpty())
                    ? sanitizeText(behavior.getBehaviorGrade())
                    : (isKorean ? "활기차고 명랑한 성격" : "Bright and cheerful personality");

            addBehaviorBox(document,
                    isKorean ? "행동특성" : "Behavior Characteristics",
                    behaviorContent,
                    font, isKorean);

            // 종합의견 박스 (항상 표시)
            String opinionContent = (behavior.getComprehensiveOpinion() != null && !behavior.getComprehensiveOpinion().isEmpty())
                    ? sanitizeText(behavior.getComprehensiveOpinion())
                    : (isKorean ? "교우 관계가 원만하며 친구들에게 긍정적인 영향을 끼칩니다. 단체활동에서 중심 역할을 맡을 수 있는 가능성이 엿보입니다."
                    : "Has good relationships with friends and has a positive influence on them. Shows potential to take a central role in group activities.");

            addBehaviorBox(document,
                    isKorean ? "종합의견" : "Comprehensive Opinion",
                    opinionContent,
                    font, isKorean);

        } catch (Exception e) {
            log.error("행동 섹션 추가 실패", e);
        }
    }

    /**
     * 행동특성/종합의견 박스 추가
     */
    private void addBehaviorBox(Document document, String title, String content, PdfFont font, boolean isKorean) {
        try {
            // 제목 라벨
            Paragraph titlePara = new Paragraph(title)
                    .setFont(font)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(5)
                    .setMarginBottom(5);
            document.add(titlePara);

            // 내용 길이에 따른 박스 높이 계산
            int contentLength = content.length();
            float boxHeight = Math.max(60, Math.min(120, 60 + (contentLength / 50) * 20));

            // 테이블로 박스 생성
            Table contentBox = new Table(1)
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            Cell contentCell = new Cell()
                    .add(new Paragraph(content)
                            .setFont(font)
                            .setFontSize(11)
                            .setTextAlignment(TextAlignment.LEFT)
                            .setMarginTop(5)
                            .setMarginBottom(5))
                    .setBorder(new com.itextpdf.layout.borders.SolidBorder(new DeviceRgb(200, 200, 200), 1))
                    .setPadding(8)
                    .setHeight(boxHeight)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);

            contentBox.addCell(contentCell);
            document.add(contentBox);

        } catch (Exception e) {
            log.error("행동 박스 추가 실패 - 제목: {}", title, e);
        }
    }

    /*
    * 성적 피드백
    * */
    private void addScoreFeedbackSection(Document document,
                                         List<SubjectFeedbackDto> feedbacks,
                                         PdfFont font, boolean isKorean, int sectionNumber) {
        try {
            String sectionTitle = isKorean ?
                    sectionNumber + ". 과목별 성적 피드백" :
                    sectionNumber + ". Subject Feedback";

            Paragraph title = new Paragraph(sectionTitle)
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(8)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            if (feedbacks == null || feedbacks.isEmpty()) {
                String noDataText = isKorean ?
                        "등록된 피드백이 없습니다." :
                        "No feedback available.";
                document.add(new Paragraph(noDataText).setFont(font).setFontSize(11).setItalic());
                return;
            }

            // 테이블: 2열 (과목명 | 피드백 내용)
            Table table = new Table(UnitValue.createPercentArray(new float[]{1f, 3f}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            // 헤더
            String subjectHeader = isKorean ? "과목명" : "Subject";
            String feedbackHeader = isKorean ? "피드백 내용" : "Feedback";
            table.addHeaderCell(createHeaderCell(subjectHeader, font));
            table.addHeaderCell(createHeaderCell(feedbackHeader, font));

            // 데이터 행
            for (SubjectFeedbackDto dto : feedbacks) {
                table.addCell(createDataCell(dto.getSubjectName(), font));
                String feedbackText = dto.getFeedback();
                if (feedbackText == null || feedbackText.trim().isEmpty()) {
                    feedbackText = "-";
                    }
                table.addCell(createDataCell(feedbackText, font));
            }

            document.add(table);

        } catch (Exception e) {
            log.error("과목별 성적 피드백 섹션 추가 실패", e);
        }
    }

    /*
    * 출결 피드백
    * */
    private void addAttendanceFeedbackSection(Document document,
                                              AttendanceFeedbackResDto attendanceFeedback,
                                              PdfFont font, boolean isKorean, int sectionNumber) {
        try {
            String sectionTitle = isKorean ?
                    sectionNumber + ". 출결 피드백" :
                    sectionNumber + ". Attendance Feedback";

            Paragraph title = new Paragraph(sectionTitle)
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(8)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            if (attendanceFeedback == null || attendanceFeedback.getFeedback() == null || attendanceFeedback.getFeedback().isEmpty()) {
                String noDataText = isKorean ?
                        "등록된 출결 피드백이 없습니다." :
                        "No attendance feedback available.";
                document.add(new Paragraph(noDataText).setFont(font).setFontSize(11).setItalic());
                return;
            }

            // 피드백 내용을 박스 형태로 표시
            addBehaviorBox(document,
                    isKorean ? "출결 피드백 내용" : "Feedback Content",
                    attendanceFeedback.getFeedback(),
                    font, isKorean);

        } catch (Exception e) {
            log.error("출결 피드백 섹션 추가 실패", e);
        }
    }

    private void addTableRow(Table table, String label1, String value1, String label2, String value2, PdfFont font) {
        table.addCell(createLabelCell(sanitizeText(label1), font));
        table.addCell(createDataCell(sanitizeText(value1), font));
        table.addCell(createLabelCell(sanitizeText(label2), font));
        table.addCell(createDataCell(sanitizeText(value2), font));
    }

    private Cell createLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(12))
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

    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 8) {
            return ssn;
        }
        return ssn.substring(0, 6) + "-" + ssn.substring(6, 7) + "******";
    }

    /**
     * TODO: 레이더 차트 생성
     * 레이더 차트 생성
     */
    private BufferedImage createRadarChart(GradesReportDto grades, boolean isKorean) {
        try {
            int width = 500;
            int height = 400;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            // 안티앨리어싱 설정
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 배경색 설정
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 차트 중심점과 반지름
            int centerX = width / 2;
            int centerY = height / 2;
            int maxRadius = Math.min(width, height) / 3;

            // 과목 데이터 준비 (최대 8개 과목)
            java.util.List<SubjectData> subjectDataList = prepareSubjectData(grades, isKorean);

            if (subjectDataList.isEmpty()) {
                return null;
            }

            // 배경 그리드 그리기 (동심원)
            drawRadarBackground(g2d, centerX, centerY, maxRadius, subjectDataList.size());

            // 축 라벨 그리기
            drawAxisLabels(g2d, centerX, centerY, maxRadius, subjectDataList);

            // 데이터 폴리곤 그리기
            drawDataPolygon(g2d, centerX, centerY, maxRadius, subjectDataList);

            g2d.dispose();
            return image;

        } catch (Exception e) {
            log.error("레이더 차트 생성 실패", e);
            return null;
        }
    }

    /**
     * 과목 데이터 준비
     */
    private java.util.List<SubjectData> prepareSubjectData(GradesReportDto grades, boolean isKorean) {
        java.util.List<SubjectData> dataList = new java.util.ArrayList<>();

        if (grades.getSubjects() != null && !grades.getSubjects().isEmpty()) {
            // 실제 과목 데이터 사용 (최대 8개)
            int count = 0;
            for (Object subjectObj : grades.getSubjects()) {
                if (count >= 8) break; // 최대 8개 과목만

                SubjectScoreDto subject = (SubjectScoreDto) subjectObj;

                // 등급을 반지름 비율로 변환 (1등급=1.0, 9등급=0.1)
                double gradeValue = Math.max(0.1, (10.0 - subject.getGrade()) / 9.0);

                dataList.add(new SubjectData(subject.getSubjectName(), gradeValue));
                count++;
            }
        } else {
            // 샘플 데이터 (테스트용)
            String[] sampleSubjects = isKorean ?
                    new String[]{"국어", "영어", "수학", "과학", "사회", "체육"} :
                    new String[]{"Korean", "English", "Math", "Science", "Social", "PE"};

            for (String subjectName : sampleSubjects) {
                // 임의의 등급 생성 (1-5등급)
                double gradeValue = 0.6 + (Math.random() * 0.4); // 0.6~1.0 사이
                dataList.add(new SubjectData(subjectName, gradeValue));
            }
        }

        return dataList;
    }

    /**
     * 레이더 차트 배경 그리기
     */
    private void drawRadarBackground(Graphics2D g2d, int centerX, int centerY, int maxRadius, int numAxes) {
        g2d.setColor(new Color(230, 230, 230));
        g2d.setStroke(new BasicStroke(1.0f));

        // 동심원 그리기 (5개 레벨)
        for (int i = 1; i <= 5; i++) {
            int radius = maxRadius * i / 5;
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }

        // 축 라인 그리기
        for (int i = 0; i < numAxes; i++) {
            double angle = 2 * Math.PI * i / numAxes - Math.PI / 2; // -90도부터 시작
            int x = (int) (centerX + maxRadius * Math.cos(angle));
            int y = (int) (centerY + maxRadius * Math.sin(angle));
            g2d.drawLine(centerX, centerY, x, y);
        }
    }

    /**
     * 축 라벨 그리기
     */
    private void drawAxisLabels(Graphics2D g2d, int centerX, int centerY, int maxRadius,
                                java.util.List<SubjectData> subjectDataList) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();

        for (int i = 0; i < subjectDataList.size(); i++) {
            double angle = 2 * Math.PI * i / subjectDataList.size() - Math.PI / 2;

            // 라벨 위치 (축보다 조금 더 바깥쪽)
            int labelX = (int) (centerX + (maxRadius + 20) * Math.cos(angle));
            int labelY = (int) (centerY + (maxRadius + 20) * Math.sin(angle));

            String label = subjectDataList.get(i).name;
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();

            // 텍스트 중앙 정렬
            g2d.drawString(label, labelX - labelWidth / 2, labelY + labelHeight / 4);
        }
    }

    /**
     * 데이터 폴리곤 그리기
     */
    private void drawDataPolygon(Graphics2D g2d, int centerX, int centerY, int maxRadius,
                                 java.util.List<SubjectData> subjectDataList) {
        if (subjectDataList.isEmpty()) return;

        // 폴리곤 포인트 계산
        int[] xPoints = new int[subjectDataList.size()];
        int[] yPoints = new int[subjectDataList.size()];

        for (int i = 0; i < subjectDataList.size(); i++) {
            double angle = 2 * Math.PI * i / subjectDataList.size() - Math.PI / 2;
            double value = subjectDataList.get(i).value;

            xPoints[i] = (int) (centerX + maxRadius * value * Math.cos(angle));
            yPoints[i] = (int) (centerY + maxRadius * value * Math.sin(angle));
        }

        // 폴리곤 채우기 (반투명)
        g2d.setColor(new Color(100, 150, 255, 100));
        g2d.fillPolygon(xPoints, yPoints, subjectDataList.size());

        // 폴리곤 테두리
        g2d.setColor(new Color(50, 100, 200));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawPolygon(xPoints, yPoints, subjectDataList.size());

        // 데이터 포인트 그리기
        g2d.setColor(new Color(200, 50, 50));
        for (int i = 0; i < subjectDataList.size(); i++) {
            g2d.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
        }
    }

    /**
     * 과목 데이터 클래스
     */
    private static class SubjectData {
        String name;
        double value; // 0.0 ~ 1.0 (0.0 = 최악, 1.0 = 최고)

        SubjectData(String name, double value) {
            this.name = name;
            this.value = value;
        }
    }


    // TODO: FONT 생성 관련 메서드
    private PdfFont createNewFont() {
        try {
            // 캐싱된 폰트 경로가 있으면 사용
            if (cachedFontPath != null) {
                if (cachedFontPath.equals("HELVETICA")) {
                    return PdfFontFactory.createFont(StandardFonts.HELVETICA);
                } else {
                    return createFontFromPath(cachedFontPath);
                }
            }

            // 한글 폰트 경로 찾기
            String koreanFontPath = findKoreanFontPath();
            if (koreanFontPath != null) {
                cachedFontPath = koreanFontPath;
                log.info("한글 폰트 경로 캐싱: {}", koreanFontPath);
                return createFontFromPath(koreanFontPath);
            }

            // 기본 폰트 사용
            cachedFontPath = "HELVETICA";
            log.info("기본 폰트 사용");
            return PdfFontFactory.createFont(StandardFonts.HELVETICA);

        } catch (Exception e) {
            log.error("폰트 생성 실패", e);
            throw new CustomException(ErrorCode.PDF_GENERATION_ERROR);
        }
    }

    private PdfFont createFontFromPath(String fontPath) throws IOException {
        if (fontPath.startsWith("classpath:")) {
            // 리소스 폰트
            String resourcePath = fontPath.substring("classpath:".length());
            ClassPathResource fontResource = new ClassPathResource(resourcePath);
            try (InputStream fontStream = fontResource.getInputStream()) {
                byte[] fontBytes = fontStream.readAllBytes();
                return PdfFontFactory.createFont(fontBytes, "Identity-H");
            }
        } else {
            // 파일 시스템 폰트
            return PdfFontFactory.createFont(fontPath, "Identity-H");
        }
    }

    private String findKoreanFontPath() {
        // 리소스 폰트 먼저 시도
        try {
            ClassPathResource fontResource = new ClassPathResource("fonts/NanumGothic.ttf");
            if (fontResource.exists()) {
                return "classpath:fonts/NanumGothic.ttf";
            }
        } catch (Exception e) {
            log.debug("리소스 폰트 확인 실패", e);
        }

        // 시스템 폰트 시도
        String[] fontPaths = {
                "c:/windows/fonts/malgun.ttf",
                "/System/Library/Fonts/AppleSDGothicNeo.ttc",
                "/usr/share/fonts/truetype/nanum/NanumGothic.ttf"
        };

        for (String path : fontPaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    return path;
                }
            } catch (Exception e) {
                log.debug("시스템 폰트 확인 실패: {}", path);
            }
        }

        return null;
    }

    private boolean checkKoreanFontSupport(PdfFont font) {
        if (isKoreanFontSupported != null) {
            return isKoreanFontSupported; // 캐시된 값 사용
        }

        try {
            font.getWidth("가", 12);
            isKoreanFontSupported = true;
            log.info("한글 폰트 지원 확인됨");
        } catch (Exception e) {
            isKoreanFontSupported = false;
            log.info("한글 폰트 지원되지 않음, 영어 모드 사용");
        }

        return isKoreanFontSupported;
    }

    private void validatePdfData(byte[] pdfData) {
        if (pdfData == null || pdfData.length == 0) {
            throw new RuntimeException("생성된 PDF 데이터가 비어있습니다");
        }

        // PDF 헤더 검증
        if (pdfData.length < 4) {
            throw new RuntimeException("PDF 데이터가 너무 작습니다");
        }

        String header = new String(pdfData, 0, 4, StandardCharsets.UTF_8);
        if (!header.equals("%PDF")) {
            throw new RuntimeException("유효하지 않은 PDF 형식입니다");
        }
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replaceAll("[\uFFFE\uFFFF]", "");
    }

    /**
     * 이미지 파일에서 Image 객체 생성
     */
    private Image createStudentImage(String imageFileName) {
        try {
            if (imageFileName == null || imageFileName.trim().isEmpty()) {
                return null;
            }

            // 파일 시스템에서 직접 읽기 (HTTP 요청 없음)
            String fullPath = uploadPath + "/" + imageFileName.trim();
            File imageFile = new File(fullPath);

            if (!imageFile.exists()) {
                log.warn("이미지 파일이 존재하지 않음: {}", fullPath);
                return null;
            }

            log.info("이미지 파일 로드: {}", fullPath);

            // 파일에서 직접 이미지 생성
            Image image = new Image(ImageDataFactory.create(fullPath));
            image.scaleToFit(80, 100);
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);

            return image;

        } catch (Exception e) {
            log.warn("이미지 생성 실패 - 파일명: {}, 에러: {}", imageFileName, e.getMessage());
            return null;
        }
    }

    /**
     * 학생 사진을 포함하는 셀 생성
     */
    private Cell addStudentPhoto(StudentDetailResDto studentInfo, PdfFont font, boolean isKorean) {
        try {
            Cell photoCell;

            if (studentInfo.getImage() != null && !studentInfo.getImage().trim().isEmpty()) {
                // 사진이 있는 경우
                Image studentImage = createStudentImage(studentInfo.getImage());
                if (studentImage != null) {
                    photoCell = new Cell(4, 1)  // 4행 1열 병합을 생성자에서 지정
                            .add(studentImage)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(8)
                            .setHeight(100)
                            .setWidth(80);
                } else {
                    // 이미지 로딩 실패 시 기본 셀
                    photoCell = createDefaultPhotoCell(font, isKorean);
                }
            } else {
                // 사진이 없는 경우 기본 셀
                photoCell = createDefaultPhotoCell(font, isKorean);
            }

            return photoCell;

        } catch (Exception e) {
            log.warn("학생 사진 추가 실패: {}", e.getMessage());
            return createDefaultPhotoCell(font, isKorean);
        }
    }


    /**
     * 사진이 없을 때 기본 셀 생성
    */
    private Cell createDefaultPhotoCell(PdfFont font, boolean isKorean) {
        String noPhotoText = isKorean ? "사진없음" : "No Photo";

        return new Cell(4, 1)  // 여기도 4행 1열 병합으로 수정
                .add(new Paragraph(noPhotoText)
                        .setFont(font)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER))
                .setHeight(100)
                .setWidth(80)
                .setBackgroundColor(new DeviceRgb(245, 245, 245))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

}