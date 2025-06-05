package com.neeis.neeis.global.backup;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class BackupStatus {
    private final LocalDateTime lastBackupTime;
    private final int backupCount;
    private final long totalSize;

    public String getTotalSizeFormatted() {
        if (totalSize < 1024) {
            return totalSize + " B";
        } else if (totalSize < 1024 * 1024) {
            return String.format("%.1f KB", totalSize / 1024.0);
        } else if (totalSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", totalSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
}