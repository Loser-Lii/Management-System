package com.example.backend.controller;

import com.example.backend.entity.ServiceRecord;
import com.example.backend.service.ServiceRecordService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceRecordController {

    @Autowired
    private ServiceRecordService serviceRecordService;

    @GetMapping
    public Result<List<ServiceRecord>> findAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId) {
        System.out.println("=== ServiceRecordController.findAll called ===");
        System.out.println("role: " + role);
        System.out.println("salesmanId: " + salesmanId);
        System.out.println("role == 'salesman': " + "salesman".equals(role));
        System.out.println("salesmanId != null: " + (salesmanId != null));
        
        // 如果是销售员角色，只返回该销售员的服务记录
        if ("salesman".equals(role) && salesmanId != null) {
            System.out.println("Executing: findBySalesman(" + salesmanId + ")");
            List<ServiceRecord> records = serviceRecordService.findBySalesman(salesmanId);
            System.out.println("Found " + records.size() + " records");
            for (ServiceRecord r : records) {
                System.out.println("  - " + r.getServiceNo() + " (salesman: " + (r.getSalesman() != null ? r.getSalesman().getId() : "null") + ")");
            }
            return Result.success(records);
        }
        // 管理员可以选择性地按销售员筛选
        if (salesmanId != null) {
            System.out.println("Executing: findBySalesman(" + salesmanId + ") - Admin filter");
            List<ServiceRecord> records = serviceRecordService.findBySalesman(salesmanId);
            System.out.println("Found " + records.size() + " records");
            return Result.success(records);
        }
        System.out.println("Executing: findAll()");
        List<ServiceRecord> records = serviceRecordService.findAll();
        System.out.println("Found " + records.size() + " total records");
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<ServiceRecord> findById(@PathVariable Long id) {
        return Result.success(serviceRecordService.findById(id));
    }

    @GetMapping("/salesman/{salesmanId}")
    public Result<List<ServiceRecord>> findBySalesman(@PathVariable Long salesmanId) {
        return Result.success(serviceRecordService.findBySalesman(salesmanId));
    }

    @GetMapping("/date-range")
    public Result<List<ServiceRecord>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(serviceRecordService.findByDateRange(startTime, endTime));
    }

    @GetMapping("/satisfaction/{salesmanId}")
    public Result<Double> getAverageSatisfaction(@PathVariable Long salesmanId) {
        return Result.success(serviceRecordService.getAverageSatisfaction(salesmanId));
    }

    @PostMapping
    public Result<ServiceRecord> create(@RequestBody ServiceRecord record) {
        return Result.success("创建成功", serviceRecordService.create(record));
    }

    @PutMapping("/{id}")
    public Result<ServiceRecord> update(@PathVariable Long id, @RequestBody ServiceRecord record) {
        return Result.success("更新成功", serviceRecordService.update(id, record));
    }

    /**
     * 销售员处理服务：待处理 -> 处理中
     */
    @PutMapping("/{id}/process")
    public Result<ServiceRecord> processService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String solution = (String) payload.get("solution");
        return Result.success("服务处理成功", serviceRecordService.processService(id, solution));
    }

    /**
     * 客户评分（管理员模拟）：处理中 -> 已完成
     */
    @PutMapping("/{id}/rate")
    public Result<ServiceRecord> rateService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Integer satisfaction = (Integer) payload.get("satisfaction");
        return Result.success("评分成功", serviceRecordService.rateService(id, satisfaction));
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceRecordService.delete(id);
        return Result.success("删除成功", null);
    }
}
