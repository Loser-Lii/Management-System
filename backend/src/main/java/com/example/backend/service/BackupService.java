package com.example.backend.service;

import com.example.backend.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackupService {

    @Value("${backup.list-dir:E:/Projects/Management-System/db_backups}")
    private String backupDir;

    public List<BackupInfo> listBackups() {
        try {
            Path dir = Paths.get(backupDir);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                throw new BusinessException("备份目录不存在: " + backupDir);
            }
            return Files.list(dir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".sql"))
                    .map(this::toInfo)
                    .sorted(Comparator.comparingLong(BackupInfo::getLastModified).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new BusinessException("读取备份目录失败: " + e.getMessage());
        }
    }

    private BackupInfo toInfo(Path p) {
        try {
            return new BackupInfo(
                    p.getFileName().toString(),
                    Files.size(p),
                    Files.getLastModifiedTime(p).toMillis()
            );
        } catch (IOException e) {
            return new BackupInfo(p.getFileName().toString(), 0L, 0L);
        }
    }

    @Data
    @AllArgsConstructor
    public static class BackupInfo {
        private String name;
        private long size;
        private long lastModified;
    }
}
