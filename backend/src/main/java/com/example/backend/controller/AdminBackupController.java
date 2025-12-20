package com.example.backend.controller;

import com.example.backend.service.BackupService;
import com.example.backend.service.BackupService.BackupInfo;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminBackupController {

    @Autowired
    private BackupService backupService;

    @GetMapping("/backups")
    public Result<List<BackupInfo>> listBackups() {
        return Result.success(backupService.listBackups());
    }
}
