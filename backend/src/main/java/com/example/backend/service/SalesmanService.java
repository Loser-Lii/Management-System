package com.example.backend.service;

import com.example.backend.dto.PerformanceSuggestionDTO;
import com.example.backend.entity.Salesman;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.SalesRecordRepository;
import com.example.backend.repository.SalesmanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售员业务逻辑层
 */
@Service
public class SalesmanService {

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    // 查询所有销售员
    public List<Salesman> findAll() {
        return salesmanRepository.findAll();
    }

    // 根据ID查询
    public Salesman findById(Long id) {
        return salesmanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售员不存在"));
    }

    // 根据员工编号查询
    public Salesman findByEmployeeNo(String employeeNo) {
        return salesmanRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new BusinessException("销售员不存在"));
    }

    // 模糊查询
    public List<Salesman> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return salesmanRepository.searchByKeyword(keyword);
    }

    // 根据状态查询
    
    // 升职：按等级顺序晋升并更新提成比例
    @Transactional
    public Salesman promote(Long id) {
        Salesman s = findById(id);
        String level = s.getLevel();
        if (level == null) level = "junior";
        switch (level) {
            case "junior":
                s.setLevel("intermediate");
                s.setCommissionRate(getCommissionByLevel("intermediate"));
                break;
            case "intermediate":
                s.setLevel("senior");
                s.setCommissionRate(getCommissionByLevel("senior"));
                break;
            case "senior":
                s.setLevel("expert");
                s.setCommissionRate(getCommissionByLevel("expert"));
                break;
            case "expert":
                throw new BusinessException("已是最高级别，无法继续升职");
            default:
                s.setLevel("intermediate");
                s.setCommissionRate(getCommissionByLevel("intermediate"));
        }
        return salesmanRepository.save(s);
    }

    // 降职：按等级顺序降级并更新提成比例
    @Transactional
    public Salesman demote(Long id) {
        Salesman s = findById(id);
        String level = s.getLevel();
        if (level == null) level = "junior";
        switch (level) {
            case "expert":
                s.setLevel("senior");
                s.setCommissionRate(getCommissionByLevel("senior"));
                break;
            case "senior":
                s.setLevel("intermediate");
                s.setCommissionRate(getCommissionByLevel("intermediate"));
                break;
            case "intermediate":
                s.setLevel("junior");
                s.setCommissionRate(getCommissionByLevel("junior"));
                break;
            case "junior":
                throw new BusinessException("已是最低级别，无法继续降职");
            default:
                s.setLevel("junior");
                s.setCommissionRate(getCommissionByLevel("junior"));
        }
        return salesmanRepository.save(s);
    }

    // 创建销售员
    @Transactional
    public Salesman create(Salesman salesman) {
        // 检查员工编号是否已存在
        if (salesmanRepository.existsByEmployeeNo(salesman.getEmployeeNo())) {
            throw new BusinessException("员工编号已存在");
        }
        
        // 默认等级与提成设置
        if (salesman.getLevel() == null) {
            salesman.setLevel("junior");
        }
        salesman.setCommissionRate(getCommissionByLevel(salesman.getLevel()));
        return salesmanRepository.save(salesman);
    }

    // 更新销售员
    @Transactional
    public Salesman update(Long id, Salesman salesman) {
        Salesman existing = findById(id);
        
        // 检查员工编号是否被其他人使用
        if (!existing.getEmployeeNo().equals(salesman.getEmployeeNo()) 
            && salesmanRepository.existsByEmployeeNo(salesman.getEmployeeNo())) {
            throw new BusinessException("员工编号已被使用");
        }
        
        // 更新字段
        existing.setName(salesman.getName());
        existing.setEmployeeNo(salesman.getEmployeeNo());
        existing.setPhone(salesman.getPhone());
        existing.setEmail(salesman.getEmail());
        existing.setHireDate(salesman.getHireDate());
        existing.setCommissionRate(salesman.getCommissionRate());
        // 如果前端传了等级，则同步设置提成比例
        if (salesman.getLevel() != null) {
            existing.setLevel(salesman.getLevel());
            existing.setCommissionRate(getCommissionByLevel(salesman.getLevel()));
        } else {
            existing.setCommissionRate(salesman.getCommissionRate());
        }
        existing.setRemark(salesman.getRemark());
        
        return salesmanRepository.save(existing);
    }

    private java.math.BigDecimal getCommissionByLevel(String level) {
        switch (level) {
            case "junior":
                return java.math.BigDecimal.valueOf(0.04);
            case "intermediate":
                return java.math.BigDecimal.valueOf(0.05);
            case "senior":
                return java.math.BigDecimal.valueOf(0.06);
            case "expert":
                return java.math.BigDecimal.valueOf(0.08);
            default:
                return java.math.BigDecimal.valueOf(0.04);
        }
    }

    // 删除销售员
    @Transactional
    public void delete(Long id) {
        if (!salesmanRepository.existsById(id)) {
            throw new BusinessException("销售员不存在");
        }
        salesmanRepository.deleteById(id);
    }

    // 获取职位变化建议（近三个月业绩分析）
    public PerformanceSuggestionDTO getPerformanceSuggestion() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);
        
        List<Object[]> performanceData = salesRecordRepository.getPerformanceRanking(startDate, endDate);
        
        PerformanceSuggestionDTO suggestion = new PerformanceSuggestionDTO();
        List<PerformanceSuggestionDTO.SalesmanPerformance> topPerformers = new ArrayList<>();
        List<PerformanceSuggestionDTO.SalesmanPerformance> bottomPerformers = new ArrayList<>();
        
        // 获取前2名
        int count = Math.min(2, performanceData.size());
        for (int i = 0; i < count; i++) {
            Object[] data = performanceData.get(i);
            topPerformers.add(new PerformanceSuggestionDTO.SalesmanPerformance(
                ((Number) data[0]).longValue(),
                (String) data[1],
                (String) data[2],
                (java.math.BigDecimal) data[3]
            ));
        }
        
        // 获取后2名
        int size = performanceData.size();
        count = Math.min(2, size);
        for (int i = size - count; i < size; i++) {
            Object[] data = performanceData.get(i);
            bottomPerformers.add(new PerformanceSuggestionDTO.SalesmanPerformance(
                ((Number) data[0]).longValue(),
                (String) data[1],
                (String) data[2],
                (java.math.BigDecimal) data[3]
            ));
        }
        
        suggestion.setTopPerformers(topPerformers);
        suggestion.setBottomPerformers(bottomPerformers);
        
        return suggestion;
    }
}
