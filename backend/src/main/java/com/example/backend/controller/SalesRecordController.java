package com.example.backend.controller;

import com.example.backend.entity.SalesRecord;
import com.example.backend.service.SalesRecordService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:5173")
public class SalesRecordController {

    @Autowired
    private SalesRecordService salesRecordService;

    @GetMapping
    public Result<List<SalesRecord>> findAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId) {
        // 如果是销售员角色，只返回该销售员的销售记录
        if ("salesman".equals(role) && salesmanId != null) {
            return Result.success(salesRecordService.findBySalesman(salesmanId));
        }
        // 管理员可以选择性地按销售员筛选
        if (salesmanId != null) {
            return Result.success(salesRecordService.findBySalesman(salesmanId));
        }
        return Result.success(salesRecordService.findAll());
    }

    @GetMapping("/{id}")
    public Result<SalesRecord> findById(@PathVariable Long id) {
        return Result.success(salesRecordService.findById(id));
    }

    @GetMapping("/order/{orderNo}")
    public Result<SalesRecord> findByOrderNo(@PathVariable String orderNo) {
        return Result.success(salesRecordService.findByOrderNo(orderNo));
    }

    @GetMapping("/salesman/{salesmanId}")
    public Result<List<SalesRecord>> findBySalesman(@PathVariable Long salesmanId) {
        return Result.success(salesRecordService.findBySalesman(salesmanId));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<SalesRecord>> findByCustomer(@PathVariable Long customerId) {
        return Result.success(salesRecordService.findByCustomer(customerId));
    }

    @GetMapping("/products/by-salesman-customer")
    public Result<List<com.example.backend.entity.Product>> findProductsBySalesmanAndCustomer(
            @RequestParam Long salesmanId,
            @RequestParam Long customerId) {
        return Result.success(salesRecordService.findProductsBySalesmanAndCustomer(salesmanId, customerId));
    }

    @GetMapping("/date-range")
    public Result<List<SalesRecord>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(salesRecordService.findByDateRange(startDate, endDate));
    }

    @GetMapping("/salesman/{salesmanId}/date-range")
    public Result<List<SalesRecord>> findBySalesmanAndDateRange(
            @PathVariable Long salesmanId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(salesRecordService.findBySalesmanAndDateRange(salesmanId, startDate, endDate));
    }

    @GetMapping("/stats/total-sales/{salesmanId}")
    public Result<BigDecimal> getTotalSales(@PathVariable Long salesmanId) {
        return Result.success(salesRecordService.getTotalSalesBySalesman(salesmanId));
    }

    @GetMapping("/stats/total-commission/{salesmanId}")
    public Result<BigDecimal> getTotalCommission(@PathVariable Long salesmanId) {
        return Result.success(salesRecordService.getTotalCommissionBySalesman(salesmanId));
    }

    @PostMapping
    public Result<SalesRecord> create(@RequestBody SalesRecord record) {
        return Result.success("创建成功", salesRecordService.create(record));
    }

    @PutMapping("/{id}")
    public Result<SalesRecord> update(@PathVariable Long id, @RequestBody SalesRecord record) {
        return Result.success("更新成功", salesRecordService.updateById(id, record));
    }

    @PutMapping("/order/{orderNo}")
    public Result<SalesRecord> updateByOrderNo(@PathVariable String orderNo, @RequestBody SalesRecord record) {
        return Result.success("更新成功", salesRecordService.updateByOrderNo(orderNo, record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        salesRecordService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/order/{orderNo}")
    public Result<Void> deleteByOrderNo(@PathVariable String orderNo) {
        salesRecordService.deleteByOrderNo(orderNo);
        return Result.success("删除成功", null);
    }

    // ==================== 状态管理接口 ====================

    @GetMapping("/status/{status}")
    public Result<List<SalesRecord>> findByStatus(@PathVariable String status) {
        return Result.success(salesRecordService.findByStatus(status));
    }

    @GetMapping("/salesman/{salesmanId}/status/{status}")
    public Result<List<SalesRecord>> findBySalesmanAndStatus(
            @PathVariable Long salesmanId, 
            @PathVariable String status) {
        return Result.success(salesRecordService.findBySalesmanAndStatus(salesmanId, status));
    }

    @PostMapping("/{id}/submit")
    public Result<SalesRecord> submitForApproval(
            @PathVariable Long id,
            @RequestParam String operator) {
        return Result.success("提交审核成功", salesRecordService.submitForApprovalById(id, operator));
    }

    @PostMapping("/order/{orderNo}/submit")
    public Result<SalesRecord> submitForApprovalByOrderNo(
            @PathVariable String orderNo,
            @RequestParam String operator) {
        return Result.success("提交审核成功", salesRecordService.submitForApprovalByOrderNo(orderNo, operator));
    }

    @PostMapping("/{id}/approve")
    public Result<SalesRecord> approve(
            @PathVariable Long id,
            @RequestParam String approver) {
        return Result.success("审核通过", salesRecordService.approveById(id, approver));
    }

    @PostMapping("/order/{orderNo}/approve")
    public Result<SalesRecord> approveByOrderNo(
            @PathVariable String orderNo,
            @RequestParam String approver) {
        return Result.success("审核通过", salesRecordService.approveByOrderNo(orderNo, approver));
    }

    @PostMapping("/{id}/reject")
    public Result<SalesRecord> reject(
            @PathVariable Long id,
            @RequestParam String approver,
            @RequestParam String reason) {
        return Result.success("审核拒绝", salesRecordService.rejectById(id, approver, reason));
    }

    @PostMapping("/order/{orderNo}/reject")
    public Result<SalesRecord> rejectByOrderNo(
            @PathVariable String orderNo,
            @RequestParam String approver,
            @RequestParam String reason) {
        return Result.success("审核拒绝", salesRecordService.rejectByOrderNo(orderNo, approver, reason));
    }

    @PostMapping("/{id}/withdraw")
    public Result<SalesRecord> withdraw(
            @PathVariable Long id,
            @RequestParam String operator) {
        return Result.success("撤回成功", salesRecordService.withdrawById(id, operator));
    }

    @PostMapping("/order/{orderNo}/withdraw")
    public Result<SalesRecord> withdrawByOrderNo(
            @PathVariable String orderNo,
            @RequestParam String operator) {
        return Result.success("撤回成功", salesRecordService.withdrawByOrderNo(orderNo, operator));
    }
}
