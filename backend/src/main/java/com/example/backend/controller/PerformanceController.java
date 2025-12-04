package com.example.backend.controller;

import com.example.backend.dto.PerformanceStatsDTO;
import com.example.backend.service.PerformanceService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 绩效统计控制器
 */
@RestController
@RequestMapping("/api/performance")
@CrossOrigin(origins = "http://localhost:5173")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    // 获取所有销售员的绩效统计
    @GetMapping("/all")
    public Result<List<PerformanceStatsDTO>> getAllPerformance() {
        return Result.success(performanceService.getAllSalesmenPerformance());
    }

    // 获取单个销售员的综合绩效
    @GetMapping("/salesman/{salesmanId}")
    public Result<PerformanceStatsDTO> getSalesmanPerformance(@PathVariable Long salesmanId) {
        return Result.success(performanceService.getSalesmanPerformance(salesmanId));
    }

    // 获取指定时间段的销售员绩效
    @GetMapping("/salesman/{salesmanId}/date-range")
    public Result<PerformanceStatsDTO> getSalesmanPerformanceByDateRange(
            @PathVariable Long salesmanId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(performanceService.getSalesmanPerformanceByDateRange(salesmanId, startDate, endDate));
    }

    // 获取活跃销售员数量（近三个月内有销售记录）
    @GetMapping("/active-salesmen-count")
    public Result<Long> getActiveSalesmenCount() {
        return Result.success(performanceService.getActiveSalesmenCount());
    }
}
