package com.neeis.neeis.global.backup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@ConditionalOnProperty(name = "backup.enabled", havingValue = "true")
public class DatabaseBackupService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${backup.storage-path:./database-backups}")
    private String backupPath;

    @Value("${backup.retention-days:30}")
    private int retentionDays;

    @Value("${backup.compress:true}")
    private boolean compressBackup;

    @Value("${backup.database-name:neels_db}")
    private String databaseName;

    public DatabaseBackupService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 정기 백업 스케줄러 - 매일 새벽 2시 실행
     */
    @Scheduled(cron = "${backup.schedule:0 0 2 * * *}")
    public void scheduledBackup() {
        log.info("정기 데이터베이스 백업 시작");
        performBackup();
        cleanOldBackups();
        log.info("정기 데이터베이스 백업 완료");
    }

    /**
     * 수동 백업 실행 (JDBC 방식)
     */
    public boolean performBackup() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = String.format("neels_backup_%s.sql", timestamp);

        try {
            // 백업 디렉토리 생성
            Path backupDir = Paths.get(backupPath);
            Files.createDirectories(backupDir);

            Path backupFile = backupDir.resolve(backupFileName);

            // JDBC를 통한 백업 실행
            boolean success = executeJdbcBackup(backupFile);

            if (success && compressBackup) {
                // 백업 파일 압축
                compressBackupFile(backupFile);
            }

            return success;

        } catch (Exception e) {
            log.error("데이터베이스 백업 실패", e);
            return false;
        }
    }

    /**
     * JDBC를 통한 백업 실행
     */
    private boolean executeJdbcBackup(Path backupFile) throws Exception {
        try (FileWriter writer = new FileWriter(backupFile.toFile())) {

            // SQL 헤더 작성
            writer.write("-- Neels Database Backup\n");
            writer.write("-- Generated: " + LocalDateTime.now() + "\n");
            writer.write("-- Database: " + databaseName + "\n\n");
            writer.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            // 모든 테이블 목록 가져오기
            List<String> tableNames = getTableNames();

            for (String tableName : tableNames) {
                log.info("백업 중: {}", tableName);

                // 테이블 구조 백업
                backupTableStructure(writer, tableName);

                // 테이블 데이터 백업
                backupTableData(writer, tableName);
            }

            writer.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");

            long fileSize = Files.size(backupFile);
            log.info("데이터베이스 백업 성공: {} (크기: {} MB)",
                    backupFile.getFileName(), fileSize / 1024 / 1024);

            return true;

        } catch (Exception e) {
            log.error("JDBC 백업 실행 실패", e);
            // 실패한 백업 파일 삭제
            Files.deleteIfExists(backupFile);
            return false;
        }
    }

    /**
     * 모든 테이블 이름 가져오기
     */
    private List<String> getTableNames() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'";

        return jdbcTemplate.queryForList(sql, String.class, databaseName);
    }

    /**
     * 테이블 구조 백업
     */
    private void backupTableStructure(FileWriter writer, String tableName) throws IOException {
        try {
            String createTableSql = jdbcTemplate.queryForObject(
                    "SHOW CREATE TABLE " + tableName,
                    (rs, rowNum) -> rs.getString(2)
            );

            writer.write("-- Table structure for " + tableName + "\n");
            writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");
            writer.write(createTableSql + ";\n\n");

        } catch (Exception e) {
            log.warn("테이블 구조 백업 실패: {}", tableName, e);
            writer.write("-- 테이블 구조 백업 실패: " + tableName + "\n\n");
        }
    }

    /**
     * 테이블 데이터 백업
     */
    private void backupTableData(FileWriter writer, String tableName) throws IOException {
        try {
            // 테이블의 모든 데이터 가져오기
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName);

            if (rows.isEmpty()) {
                writer.write("-- 데이터가 없는 테이블: " + tableName + "\n\n");
                return;
            }

            writer.write("-- Data for table " + tableName + "\n");
            writer.write("INSERT INTO `" + tableName + "` VALUES\n");

            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> row = rows.get(i);

                writer.write("(");

                boolean first = true;
                for (Object value : row.values()) {
                    if (!first) {
                        writer.write(", ");
                    }

                    if (value == null) {
                        writer.write("NULL");
                    } else if (value instanceof String) {
                        // SQL 인젝션 방지를 위한 이스케이프
                        String escapedValue = value.toString()
                                .replace("\\", "\\\\")
                                .replace("'", "\\'")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r");
                        writer.write("'" + escapedValue + "'");
                    } else if (value instanceof LocalDateTime) {
                        writer.write("'" + value.toString() + "'");
                    } else {
                        writer.write(value.toString());
                    }

                    first = false;
                }

                writer.write(")");

                if (i < rows.size() - 1) {
                    writer.write(",\n");
                } else {
                    writer.write(";\n\n");
                }
            }

        } catch (Exception e) {
            log.warn("테이블 데이터 백업 실패: {}", tableName, e);
            writer.write("-- 테이블 데이터 백업 실패: " + tableName + "\n\n");
        }
    }

    /**
     * 백업 파일 압축
     */
    private void compressBackupFile(Path backupFile) throws IOException {
        Path compressedFile = Paths.get(backupFile.toString() + ".gz");

        try (FileInputStream fis = new FileInputStream(backupFile.toFile());
             FileOutputStream fos = new FileOutputStream(compressedFile.toFile());
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzos.write(buffer, 0, length);
            }
        }

        // 원본 파일 삭제
        Files.delete(backupFile);

        long compressedSize = Files.size(compressedFile);
        log.info("백업 파일 압축 완료: {} (크기: {} MB)",
                compressedFile.getFileName(),
                compressedSize / 1024 / 1024);
    }

    /**
     * 오래된 백업 파일 정리
     */
    private void cleanOldBackups() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            Path backupDir = Paths.get(backupPath);

            if (!Files.exists(backupDir)) {
                return;
            }

            Files.list(backupDir)
                    .filter(path -> path.toString().matches(".*neels_backup_\\d{8}_\\d{6}\\.(sql|sql\\.gz)$"))
                    .filter(path -> {
                        try {
                            FileTime fileTime = Files.getLastModifiedTime(path);
                            return fileTime.toInstant()
                                    .isBefore(cutoffDate.toInstant(ZoneOffset.UTC));
                        } catch (IOException e) {
                            log.warn("파일 시간 확인 실패: {}", path, e);
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("오래된 백업 파일 삭제: {} ({}일 이상)",
                                    path.getFileName(), retentionDays);
                        } catch (IOException e) {
                            log.error("백업 파일 삭제 실패: {}", path, e);
                        }
                    });

        } catch (IOException e) {
            log.error("오래된 백업 파일 정리 중 오류", e);
        }
    }

    /**
     * 백업 상태 확인
     */
    public BackupStatus getBackupStatus() {
        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                return BackupStatus.builder()
                        .lastBackupTime(null)
                        .backupCount(0)
                        .totalSize(0L)
                        .build();
            }

            var backupFiles = Files.list(backupDir)
                    .filter(path -> path.toString().matches(".*neels_backup_\\d{8}_\\d{6}\\.(sql|sql\\.gz)$"))
                    .toList();

            LocalDateTime lastBackupTime = backupFiles.stream()
                    .map(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(instant -> instant != null)
                    .max(java.time.Instant::compareTo)
                    .map(instant -> LocalDateTime.ofInstant(instant, ZoneOffset.UTC))
                    .orElse(null);

            long totalSize = backupFiles.stream()
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();

            return BackupStatus.builder()
                    .lastBackupTime(lastBackupTime)
                    .backupCount(backupFiles.size())
                    .totalSize(totalSize)
                    .build();

        } catch (IOException e) {
            log.error("백업 상태 확인 실패", e);
            return BackupStatus.builder()
                    .lastBackupTime(null)
                    .backupCount(0)
                    .totalSize(0L)
                    .build();
        }
    }
}