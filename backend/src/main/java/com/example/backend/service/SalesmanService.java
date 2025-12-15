package com.example.backend.service;

import com.example.backend.dto.PerformanceSuggestionDTO;
import com.example.backend.dto.SalesmanProfileDTO;
import com.example.backend.dto.SalesmanUpdateForm;
import com.example.backend.entity.CollectionRecord;
import com.example.backend.entity.ComplaintRecord;
import com.example.backend.entity.Customer;
import com.example.backend.entity.Salesman;
import com.example.backend.entity.SalesRecord;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.CollectionRecordRepository;
import com.example.backend.repository.ComplaintRecordRepository;
import com.example.backend.repository.CustomerRepository;
import com.example.backend.repository.SalesRecordRepository;
import com.example.backend.repository.SalesmanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售员业务逻辑层
 */
@Service
public class SalesmanService {

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CollectionRecordRepository collectionRecordRepository;

    @Autowired
    private ComplaintRecordRepository complaintRecordRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    // 查询所有销售员（包括离职）
    public List<Salesman> findAll() {
        return salesmanRepository.findAll();
    }

    // 查询所有在职销售员
    public List<Salesman> findAllActive() {
        return salesmanRepository.findAll().stream()
                .filter(s -> s.getStatus() == null || "active".equals(s.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    // 查询所有销售员（包括离职）- 废弃，使用findAll()
    public List<Salesman> findAllIncludingResigned() {
        return salesmanRepository.findAll();
    }

    // 根据ID查询
    public Salesman findById(Long id) {
        return salesmanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售员不存在"));
    }

    /**
     * 获取销售员档案信息（从视图查询 - READ）
     * 使用 view_salesman_profile 获取聚合数据
     * 这是只读操作，从视图中查询
     */
    public SalesmanProfileDTO getProfileById(Long id) {
        return salesmanRepository.findProfileById(id)
                .orElseThrow(() -> new BusinessException("销售员档案不存在"));
    }

    /**
     * 更新销售员档案信息（写入实体 - WRITE）
     * 更新操作直接操作 salesman 表的实体
     * 不操作视图，保持读写分离
     */
    @Transactional
    public SalesmanProfileDTO updateProfile(Long id, SalesmanUpdateForm form) {
        // 1. 从实际的 salesman 表查询实体
        Salesman salesman = salesmanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售员不存在"));
        
        // 记录修改前的值
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        // 2. 更新可编辑字段并记录变化
        if (form.getPhone() != null && !form.getPhone().equals(salesman.getPhone())) {
            oldValues.put("phone", salesman.getPhone());
            newValues.put("phone", form.getPhone());
            salesman.setPhone(form.getPhone());
        }
        if (form.getEmail() != null && !form.getEmail().equals(salesman.getEmail())) {
            oldValues.put("email", salesman.getEmail());
            newValues.put("email", form.getEmail());
            salesman.setEmail(form.getEmail());
        }
        if (form.getWechat() != null && !form.getWechat().equals(salesman.getWechat())) {
            oldValues.put("wechat", salesman.getWechat());
            newValues.put("wechat", form.getWechat());
            salesman.setWechat(form.getWechat());
        }
        if (form.getQq() != null && !form.getQq().equals(salesman.getQq())) {
            oldValues.put("qq", salesman.getQq());
            newValues.put("qq", form.getQq());
            salesman.setQq(form.getQq());
        }
        if (form.getAvatar() != null && !form.getAvatar().equals(salesman.getAvatar())) {
            oldValues.put("avatar", salesman.getAvatar());
            newValues.put("avatar", form.getAvatar());
            salesman.setAvatar(form.getAvatar());
        }
        if (form.getRemark() != null && !form.getRemark().equals(salesman.getRemark())) {
            oldValues.put("remark", salesman.getRemark());
            newValues.put("remark", form.getRemark());
            salesman.setRemark(form.getRemark());
        }
        
        // 3. 保存实体到 salesman 表
        salesmanRepository.save(salesman);
        
        // 4. 记录审计日志
        if (!oldValues.isEmpty()) {
            try {
                String oldValuesJson = objectMapper.writeValueAsString(oldValues);
                String newValuesJson = objectMapper.writeValueAsString(newValues);
                String description = String.format("销售员 %s 更新了个人信息: %s", 
                    salesman.getName(), String.join("、", newValues.keySet()));
                auditLogService.log("Salesman", id, "UPDATE_PROFILE", 
                    salesman.getName(), "salesman", 
                    oldValuesJson, newValuesJson, description);
            } catch (Exception e) {
                // 日志记录失败不影响主业务
            }
        }
        
        // 5. 从视图重新查询并返回最新的档案信息
        return getProfileById(id);
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
        String oldLevel = s.getLevel();
        if (oldLevel == null) oldLevel = "Junior";
        String normalizedLevel = oldLevel.toLowerCase();
        String newLevel;
        switch (normalizedLevel) {
            case "junior":
                newLevel = "Intermediate";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
                break;
            case "intermediate":
                newLevel = "Senior";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
                break;
            case "senior":
                newLevel = "Expert";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
                break;
            case "expert":
                throw new BusinessException("已是最高级别，无法继续升职");
            default:
                newLevel = "Intermediate";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
        }
        Salesman result = salesmanRepository.save(s);
        
        // 记录审计日志
        String description = String.format("销售员 %s 从 %s 升职到 %s", s.getName(), oldLevel, newLevel);
        auditLogService.log("Salesman", id, "PROMOTE", 
            "admin", "admin", 
            oldLevel, newLevel, description);
        
        return result;
    }

    // 降职：按等级顺序降级并更新提成比例
    @Transactional
    public Salesman demote(Long id) {
        Salesman s = findById(id);
        String oldLevel = s.getLevel();
        if (oldLevel == null) oldLevel = "Junior";
        String normalizedLevel = oldLevel.toLowerCase();
        String newLevel;
        switch (normalizedLevel) {
            case "expert":
                newLevel = "Senior";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
                break;
            case "senior":
                newLevel = "Intermediate";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
                break;
            case "intermediate":
                newLevel = "Junior";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
                break;
            case "junior":
                throw new BusinessException("已是最低级别，无法继续降职");
            default:
                newLevel = "Junior";
                s.setLevel(newLevel);
                s.setCommissionRate(getCommissionByLevel(newLevel));
        }
        Salesman result = salesmanRepository.save(s);
        
        // 记录审计日志
        String description = String.format("销售员 %s 从 %s 降职到 %s", s.getName(), oldLevel, newLevel);
        auditLogService.log("Salesman", id, "DEMOTE", 
            "admin", "admin", 
            oldLevel, newLevel, description);
        
        return result;
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
            salesman.setLevel("Junior");
        }
        salesman.setCommissionRate(getCommissionByLevel(salesman.getLevel()));
        Salesman result = salesmanRepository.save(salesman);
        
        // 记录审计日志
        try {
            Map<String, Object> salesmanInfo = new HashMap<>();
            salesmanInfo.put("name", result.getName());
            salesmanInfo.put("employeeNo", result.getEmployeeNo());
            salesmanInfo.put("phone", result.getPhone());
            salesmanInfo.put("email", result.getEmail());
            salesmanInfo.put("level", result.getLevel());
            salesmanInfo.put("hireDate", result.getHireDate());
            String salesmanJson = objectMapper.writeValueAsString(salesmanInfo);
            String description = String.format("创建销售员账号: %s (工号: %s)", result.getName(), result.getEmployeeNo());
            auditLogService.log("Salesman", result.getId(), "CREATE", 
                "admin", "admin", 
                null, salesmanJson, description);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
        
        return result;
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
        
        // 记录修改前的值
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        // 更新字段并记录变化
        if (!existing.getName().equals(salesman.getName())) {
            oldValues.put("name", existing.getName());
            newValues.put("name", salesman.getName());
        }
        if (!existing.getEmployeeNo().equals(salesman.getEmployeeNo())) {
            oldValues.put("employeeNo", existing.getEmployeeNo());
            newValues.put("employeeNo", salesman.getEmployeeNo());
        }
        if ((existing.getPhone() == null && salesman.getPhone() != null) || 
            (existing.getPhone() != null && !existing.getPhone().equals(salesman.getPhone()))) {
            oldValues.put("phone", existing.getPhone());
            newValues.put("phone", salesman.getPhone());
        }
        if ((existing.getEmail() == null && salesman.getEmail() != null) || 
            (existing.getEmail() != null && !existing.getEmail().equals(salesman.getEmail()))) {
            oldValues.put("email", existing.getEmail());
            newValues.put("email", salesman.getEmail());
        }
        if ((existing.getRemark() == null && salesman.getRemark() != null) || 
            (existing.getRemark() != null && !existing.getRemark().equals(salesman.getRemark()))) {
            oldValues.put("remark", existing.getRemark());
            newValues.put("remark", salesman.getRemark());
        }
        
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
        
        Salesman result = salesmanRepository.save(existing);
        
        // 记录审计日志
        if (!oldValues.isEmpty()) {
            try {
                String oldValuesJson = objectMapper.writeValueAsString(oldValues);
                String newValuesJson = objectMapper.writeValueAsString(newValues);
                String description = String.format("销售员 %s 更新了个人信息: %s", 
                    result.getName(), String.join("、", newValues.keySet()));
                auditLogService.log("Salesman", id, "UPDATE", 
                    result.getName(), "salesman", 
                    oldValuesJson, newValuesJson, description);
            } catch (Exception e) {
                // 日志记录失败不影响主业务
            }
        }
        
        return result;
    }

    private java.math.BigDecimal getCommissionByLevel(String level) {
        if (level == null) return java.math.BigDecimal.valueOf(0.04);
        String normalizedLevel = level.toLowerCase();
        switch (normalizedLevel) {
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

    // 办理离职
    @Transactional
    public Long resign(Long id) {
        Salesman salesman = findById(id);
        if ("resigned".equals(salesman.getStatus())) {
            throw new BusinessException("该销售员已离职");
        }
        
        // 一次性检查所有未完成的事项
        List<String> issues = new ArrayList<>();
        
        // 检查是否有未完成的待审核销售记录
        List<SalesRecord> pendingSalesRecords = salesRecordRepository.findBySalesman_IdAndStatusOrderBySaleDateDesc(id, "pending");
        if (!pendingSalesRecords.isEmpty()) {
            issues.add(pendingSalesRecords.size() + " 条待审核的销售记录");
        }
        
        // 检查是否有未完成的催款记录
        List<CollectionRecord> pendingCollections = collectionRecordRepository.findBySalesman_Id(id).stream()
                .filter(c -> "pending".equals(c.getCollectionStatus()))
                .collect(java.util.stream.Collectors.toList());
        if (!pendingCollections.isEmpty()) {
            issues.add(pendingCollections.size() + " 条未完成的催款记录");
        }
        
        // 检查是否有未结束的投诉记录
        List<ComplaintRecord> activeComplaints = complaintRecordRepository.findBySalesman_Id(id).stream()
                .filter(c -> "pending".equals(c.getStatus()) || "processing".equals(c.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        if (!activeComplaints.isEmpty()) {
            issues.add(activeComplaints.size() + " 条未结束的投诉记录");
        }
        
        // 如果有任何未完成的事项，一次性抛出
        if (!issues.isEmpty()) {
            String message = "该销售员还有以下未完成的事项，请先处理完成：\n" + String.join("、", issues);
            throw new BusinessException(message);
        }
        
        // 标记为离职状态
        salesman.setStatus("resigned");
        salesman.setResignationDate(LocalDate.now());
        salesmanRepository.save(salesman);
        
        // 查找该销售员负责的客户并清空负责人
        List<Customer> customers = customerRepository.findBySalesmanId(id);
        int customerCount = customers.size();
        if (!customers.isEmpty()) {
            for (Customer customer : customers) {
                customer.setSalesmanId(null);
            }
            customerRepository.saveAll(customers);
        }
        
        // 记录审计日志
        String description = String.format("销售员 %s (工号: %s) 办理离职，解绑了 %d 个客户", 
            salesman.getName(), salesman.getEmployeeNo(), customerCount);
        auditLogService.log("Salesman", id, "RESIGN", 
            "admin", "admin", 
            "active", "resigned", description);
        
        // 返回受影响的客户数量
        return (long) customerCount;
    }

    // 删除销售员（保留用于管理员强制删除）
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
                (String) data[3],
                (java.math.BigDecimal) data[4]
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
                (String) data[3],
                (java.math.BigDecimal) data[4]
            ));
        }
        
        suggestion.setTopPerformers(topPerformers);
        suggestion.setBottomPerformers(bottomPerformers);
        
        return suggestion;
    }
}
