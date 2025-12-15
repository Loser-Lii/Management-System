package com.example.backend.controller;

import com.example.backend.entity.CollectionRecord;
import com.example.backend.service.CollectionRecordService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins = "http://localhost:5173")
public class CollectionRecordController {

    @Autowired
    private CollectionRecordService collectionRecordService;

    @GetMapping
    public Result<List<CollectionRecord>> findAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId) {
        // 如果是销售员角色，只返回该销售员的催款记录
        if ("salesman".equals(role) && salesmanId != null) {
            return Result.success(collectionRecordService.findBySalesman(salesmanId));
        }
        // 管理员可以选择性地按销售员筛选
        if (salesmanId != null) {
            return Result.success(collectionRecordService.findBySalesman(salesmanId));
        }
        return Result.success(collectionRecordService.findAll());
    }

    @GetMapping("/{id}")
    public Result<CollectionRecord> findById(@PathVariable Long id) {
        return Result.success(collectionRecordService.findById(id));
    }

    @GetMapping("/salesman/{salesmanId}")
    public Result<List<CollectionRecord>> findBySalesman(@PathVariable Long salesmanId) {
        return Result.success(collectionRecordService.findBySalesman(salesmanId));
    }

    @GetMapping("/order/{orderNo}")
    public Result<List<CollectionRecord>> findByOrderNo(@PathVariable String orderNo) {
        return Result.success(collectionRecordService.findByOrderNo(orderNo));
    }

    @GetMapping("/status/{status}")
    public Result<List<CollectionRecord>> findByStatus(@PathVariable String status) {
        return Result.success(collectionRecordService.findByStatus(status));
    }

    @GetMapping("/date-range")
    public Result<List<CollectionRecord>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(collectionRecordService.findByDateRange(startDate, endDate));
    }

    @GetMapping("/total/{salesmanId}")
    public Result<BigDecimal> getTotalAmount(@PathVariable Long salesmanId) {
        return Result.success(collectionRecordService.getTotalAmount(salesmanId));
    }

    @PostMapping
    public Result<CollectionRecord> create(
            @RequestParam(required = false) String role,
            @RequestBody CollectionRecord record) {
        System.out.println("=== CollectionRecord CREATE ===");
        System.out.println("Received record: " + record);
        System.out.println("salesmanId from virtual field: " + record.getSalesmanId());
        System.out.println("salesman object: " + record.getSalesman());
        System.out.println("orderNo: " + record.getOrderNo());
        
        // 权限检查：只有销售员可以新增催款记录
        if ("admin".equals(role)) {
            return Result.error("权限不足：管理员不能新增催款记录");
        }
        
        return Result.success("创建成功", collectionRecordService.create(record));
    }

    @PutMapping("/{id}")
    public Result<CollectionRecord> update(@PathVariable Long id, @RequestBody CollectionRecord record) {
        System.out.println("=== CollectionRecord UPDATE ===");
        System.out.println("Received record: " + record);
        System.out.println("salesmanId from virtual field: " + record.getSalesmanId());
        System.out.println("salesman object: " + record.getSalesman());
        return Result.success("更新成功", collectionRecordService.update(id, record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        collectionRecordService.delete(id);
        return Result.success("删除成功", null);
    }
}
