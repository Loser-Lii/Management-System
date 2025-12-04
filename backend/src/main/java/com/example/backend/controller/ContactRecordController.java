package com.example.backend.controller;

import com.example.backend.entity.ContactRecord;
import com.example.backend.service.ContactRecordService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "http://localhost:5173")
public class ContactRecordController {

    @Autowired
    private ContactRecordService contactRecordService;

    @GetMapping
    public Result<List<ContactRecord>> findAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId) {
        // 如果是销售员角色，只返回该销售员的联络记录
        if ("salesman".equals(role) && salesmanId != null) {
            return Result.success(contactRecordService.findBySalesman(salesmanId));
        }
        // 管理员可以选择性地按销售员筛选
        if (salesmanId != null) {
            return Result.success(contactRecordService.findBySalesman(salesmanId));
        }
        return Result.success(contactRecordService.findAll());
    }

    @GetMapping("/{id}")
    public Result<ContactRecord> findById(@PathVariable Long id) {
        return Result.success(contactRecordService.findById(id));
    }

    @GetMapping("/salesman/{salesmanId}")
    public Result<List<ContactRecord>> findBySalesman(@PathVariable Long salesmanId) {
        return Result.success(contactRecordService.findBySalesman(salesmanId));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<ContactRecord>> findByCustomer(@PathVariable Long customerId) {
        return Result.success(contactRecordService.findByCustomer(customerId));
    }

    @GetMapping("/date-range")
    public Result<List<ContactRecord>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(contactRecordService.findByDateRange(startTime, endTime));
    }

    @PostMapping
    public Result<ContactRecord> create(@RequestBody ContactRecord record) {
        return Result.success("创建成功", contactRecordService.create(record));
    }

    @PutMapping("/{id}")
    public Result<ContactRecord> update(@PathVariable Long id, @RequestBody ContactRecord record) {
        return Result.success("更新成功", contactRecordService.update(id, record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contactRecordService.delete(id);
        return Result.success("删除成功", null);
    }
}
