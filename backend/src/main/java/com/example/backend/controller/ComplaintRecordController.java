package com.example.backend.controller;

import com.example.backend.entity.ComplaintRecord;
import com.example.backend.service.ComplaintRecordService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "http://localhost:5173")
public class ComplaintRecordController {

    @Autowired
    private ComplaintRecordService complaintRecordService;

    @GetMapping
    public Result<List<ComplaintRecord>> findAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId) {
        // 如果是销售员角色，只返回该销售员的投诉记录
        if ("salesman".equals(role) && salesmanId != null) {
            return Result.success(complaintRecordService.findBySalesman(salesmanId));
        }
        // 管理员可以选择性地按销售员筛选
        if (salesmanId != null) {
            return Result.success(complaintRecordService.findBySalesman(salesmanId));
        }
        return Result.success(complaintRecordService.findAll());
    }

    @GetMapping("/{id}")
    public Result<ComplaintRecord> findById(@PathVariable Long id) {
        return Result.success(complaintRecordService.findById(id));
    }

    @GetMapping("/salesman/{salesmanId}")
    public Result<List<ComplaintRecord>> findBySalesman(@PathVariable Long salesmanId) {
        return Result.success(complaintRecordService.findBySalesman(salesmanId));
    }

    @GetMapping("/status/{status}")
    public Result<List<ComplaintRecord>> findByStatus(@PathVariable String status) {
        return Result.success(complaintRecordService.findByStatus(status));
    }

    @GetMapping("/date-range")
    public Result<List<ComplaintRecord>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(complaintRecordService.findByDateRange(startDate, endDate));
    }

    @GetMapping("/count/{salesmanId}")
    public Result<Long> countBySalesman(@PathVariable Long salesmanId) {
        return Result.success(complaintRecordService.countBySalesman(salesmanId));
    }

    @PostMapping
    public Result<ComplaintRecord> create(@RequestBody ComplaintRecord record) {
        return Result.success("创建成功", complaintRecordService.create(record));
    }

    @PutMapping("/{id}")
    public Result<ComplaintRecord> update(@PathVariable Long id, @RequestBody ComplaintRecord record) {
        return Result.success("更新成功", complaintRecordService.update(id, record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        complaintRecordService.delete(id);
        return Result.success("删除成功", null);
    }
}
