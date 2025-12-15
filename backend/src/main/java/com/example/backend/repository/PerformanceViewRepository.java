package com.example.backend.repository;

import java.util.List;

/**
 * 只读视图：vw_salesman_performance
 */
public interface PerformanceViewRepository {

    List<PerformanceViewRow> findAllView();

    PerformanceViewRow findBySalesmanId(Long salesmanId);
}
