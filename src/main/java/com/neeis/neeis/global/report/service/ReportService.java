package com.neeis.neeis.global.report.service;

import com.neeis.neeis.domain.counsel.dto.res.CounselDetailDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.StudentScoreSummaryDto;
import com.neeis.neeis.domain.scoreSummary.dto.res.SubjectScoreDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {
    public byte[] generateStudentScoreExcel(StudentScoreSummaryDto dto) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("성적 요약");

            Row header = sheet.createRow(0);
            String[] headers = {"출석번호", "이름", "과목", "총점", "원점수", "등수", "등급", "성취도", "평균", "표준편차", "수강자수", "피드백"};

            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (SubjectScoreDto subject : dto.getSubjects()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getNumber());
                row.createCell(1).setCellValue(dto.getStudentName());
                row.createCell(2).setCellValue(subject.getSubjectName());
                row.createCell(3).setCellValue(subject.getRawTotal());
                row.createCell(4).setCellValue(subject.getWeightedTotal());
                row.createCell(5).setCellValue(subject.getRank());
                row.createCell(6).setCellValue(subject.getGrade());
                row.createCell(7).setCellValue(subject.getAchievementLevel());
                row.createCell(8).setCellValue(subject.getAverage());
                row.createCell(9).setCellValue(subject.getStdDev());
                row.createCell(10).setCellValue(subject.getTotalStudentCount());
                row.createCell(11).setCellValue(subject.getFeedback());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Excel 생성 실패", e);
        }
    }

    public byte[] generateCounselExcel(List<CounselDetailDto> counselDetailDtos){

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("상담 내역");

            Row header = sheet.createRow(0);
            String[] headers = { "카테고리", "담당 교사", "내용", "다음 상담 계획", "입력 날짜", "공개여부"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

            int rowIdx = 1;
            for (CounselDetailDto dto : counselDetailDtos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getCategory().getDisplayName());
                row.createCell(1).setCellValue(dto.getTeacher());
                row.createCell(2).setCellValue(dto.getContent());
                row.createCell(3).setCellValue(dto.getNextPlan());
                row.createCell(4).setCellValue(dto.getDateTime().format(formatter));
                row.createCell(5).setCellValue(dto.getIsPublic());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Excel 생성 실패", e);
        }
    }
}
