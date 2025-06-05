package com.neeis.neeis.global.backup;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/backup")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "backup.enabled", havingValue = "true")
public class BackupController {

    private final DatabaseBackupService backupService;

    @PostMapping("/manual")
    public ResponseEntity<String> manualBackup() {
        boolean success = backupService.performBackup();
        return success ?
                ResponseEntity.ok("백업이 성공적으로 완료되었습니다.") :
                ResponseEntity.internalServerError().body("백업 실행 중 오류가 발생했습니다.");
    }

    @GetMapping("/status")
    public ResponseEntity<BackupStatus> getBackupStatus() {
        return ResponseEntity.ok(backupService.getBackupStatus());
    }
}