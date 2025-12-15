package com.example.backend.controller;

import com.example.backend.entity.ComplaintRecord;
import com.example.backend.service.ComplaintRecordService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    
    @GetMapping("/severity/{severity}")
    public Result<List<ComplaintRecord>> findBySeverity(@PathVariable String severity) {
        return Result.success(complaintRecordService.findBySeverity(severity));
    }
    
    @GetMapping("/type/{type}")
    public Result<List<ComplaintRecord>> findByType(@PathVariable String type) {
        return Result.success(complaintRecordService.findByType(type));
    }
    
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam(required = false) Long salesmanId) {
        return Result.success(complaintRecordService.getStatistics(salesmanId));
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
