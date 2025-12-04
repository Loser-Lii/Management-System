package com.example.backend.controller;

import com.example.backend.entity.ServiceRecord;
import com.example.backend.service.ServiceRecordService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
        // 如果是销售员角色，只返回该销售员的服务记录
        if ("salesman".equals(role) && salesmanId != null) {
            return Result.success(serviceRecordService.findBySalesman(salesmanId));
        }
        // 管理员可以选择性地按销售员筛选
        if (salesmanId != null) {
            return Result.success(serviceRecordService.findBySalesman(salesmanId));
        }
        return Result.success(serviceRecordService.findAll());
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(serviceRecordService.findByDateRange(startDate, endDate));
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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceRecordService.delete(id);
        return Result.success("删除成功", null);
    }
}
