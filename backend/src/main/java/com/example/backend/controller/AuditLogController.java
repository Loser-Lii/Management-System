package com.example.backend.controller;

import com.example.backend.entity.AuditLog;
import com.example.backend.service.AuditLogService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "http://localhost:5173")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 查询所有审计日志
     */
    @GetMapping
    public Result<List<AuditLog>> findAll() {
        return Result.success(auditLogService.findAll());
    }

    /**
     * 查询指定实体的审计日志
     */
    @GetMapping("/entity/{entityName}/{entityId}")
    public Result<List<AuditLog>> findByEntity(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        return Result.success(auditLogService.findByEntity(entityName, entityId));
    }

    /**
     * 查询指定实体类型的所有日志
     */
    @GetMapping("/entity-type/{entityName}")
    public Result<List<AuditLog>> findByEntityName(@PathVariable String entityName) {
        return Result.success(auditLogService.findByEntityName(entityName));
    }

    /**
     * 查询指定操作人的日志
     */
    @GetMapping("/operator/{operator}")
    public Result<List<AuditLog>> findByOperator(@PathVariable String operator) {
        return Result.success(auditLogService.findByOperator(operator));
    }
}
