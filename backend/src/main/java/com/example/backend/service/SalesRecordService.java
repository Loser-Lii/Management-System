package com.example.backend.service;

import com.example.backend.entity.SalesRecord;
import com.example.backend.entity.Customer;
import com.example.backend.entity.Product;
import com.example.backend.entity.Salesman;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.SalesRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SalesRecordService {

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuditLogService auditLogService;

    // 生成订单号：ORD-yyyyMMdd-序号（当天第几个记录）
    private String generateOrderNo() {
        LocalDate today = LocalDate.now();
        // 统计今天已有的记录数
        Long count = salesRecordRepository.countByDate(today);
        // 序号从1开始
        int sequenceNumber = (int) (count + 1);
        // 格式：ORD-20251220-001
        return String.format("ORD-%04d%02d%02d-%03d", 
            today.getYear(), 
            today.getMonthValue(), 
            today.getDayOfMonth(), 
            sequenceNumber);
    }

    public List<SalesRecord> findAll() {
        return salesRecordRepository.findAllByOrderBySaleDateDesc();
    }

    public SalesRecord findLatestBySaleDate() {
        return salesRecordRepository.findTopByOrderBySaleDateDesc();
    }

    public SalesRecord findById(Long id) {
        return salesRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售记录不存在"));
    }

    public SalesRecord findByOrderNo(String orderNo) {
        return salesRecordRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("销售记录不存在"));
    }

    public List<SalesRecord> findBySalesman(Long salesmanId) {
        return salesRecordRepository.findBySalesman_IdOrderBySaleDateDesc(salesmanId);
    }

    public List<SalesRecord> findByCustomer(Long customerId) {
        return salesRecordRepository.findByCustomer_Id(customerId);
    }

    public List<Product> findProductsBySalesmanAndCustomer(Long salesmanId, Long customerId) {
        return salesRecordRepository.findDistinctProductsBySalesmanAndCustomer(salesmanId, customerId);
    }

    public List<SalesRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findBySaleDateBetween(startDate, endDate);
    }

    public List<SalesRecord> findBySalesmanAndDateRange(Long salesmanId, LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findBySalesman_IdAndSaleDateBetween(salesmanId, startDate, endDate);
    }

    @Transactional
    public SalesRecord create(SalesRecord record) {
        // 自动生成订单号
        if (record.getOrderNo() == null || record.getOrderNo().isEmpty()) {
            record.setOrderNo(generateOrderNo());
        }

        // 设置初始状态为草稿
        if (record.getStatus() == null || record.getStatus().isEmpty()) {
            record.setStatus("draft");
        }

        // 如果前端直接以 pending 创建，则需要验证必填字段
        if ("pending".equals(record.getStatus())) {
            validateForSubmission(record);
        }

        // 验证关联实体
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            Salesman salesman = salesmanService.findById(record.getSalesman().getId());
            record.setSalesman(salesman);
            // 如果没有设置提成比例，使用销售员的基础提成比例
            if (record.getCommissionRate() == null && salesman.getCommissionRate() != null) {
                record.setCommissionRate(salesman.getCommissionRate());
            }
        }
        
        if (record.getProduct() != null && record.getProduct().getId() != null) {
            Product product = productService.findById(record.getProduct().getId());
            record.setProduct(product);
        }
        
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            Customer customer = customerService.findById(record.getCustomer().getId());
            record.setCustomer(customer);
        }

        // 计算总金额
        if (record.getTotalAmount() == null) {
            BigDecimal totalAmount = record.getUnitPrice().multiply(BigDecimal.valueOf(record.getQuantity()));
            record.setTotalAmount(totalAmount);
        }

        SalesRecord saved = salesRecordRepository.save(record);
        
        // 记录审计日志
        if (record.getCreatedBy() != null) {
            auditLogService.log("SalesRecord", saved.getId(), "CREATE", 
                record.getCreatedBy(), "salesman", null, saved, "创建销售记录");
        }
        
        return saved;
    }

    @Transactional
    public SalesRecord updateById(Long id, SalesRecord record) {
        SalesRecord existing = findById(id);
        
        // 只有草稿和已拒绝状态的记录可以编辑
        if (!"draft".equals(existing.getStatus()) && !"rejected".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿或已拒绝状态的记录可以编辑");
        }

        SalesRecord oldRecord = new SalesRecord();
        oldRecord.setQuantity(existing.getQuantity());
        oldRecord.setUnitPrice(existing.getUnitPrice());
        oldRecord.setTotalAmount(existing.getTotalAmount());

        // 更新关联实体
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        }
        if (record.getProduct() != null && record.getProduct().getId() != null) {
            existing.setProduct(productService.findById(record.getProduct().getId()));
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        }

        existing.setQuantity(record.getQuantity());
        existing.setUnitPrice(record.getUnitPrice());
        existing.setTotalAmount(record.getTotalAmount());
        existing.setCommissionRate(record.getCommissionRate());
        existing.setSaleDate(record.getSaleDate());
        existing.setRemark(record.getRemark());

        SalesRecord saved = salesRecordRepository.save(existing);
        
        // 记录审计日志
        if (record.getCreatedBy() != null) {
            auditLogService.log("SalesRecord", id, "UPDATE", 
                record.getCreatedBy(), "salesman", oldRecord, saved, "更新销售记录");
        }

        return saved;
    }

    @Transactional
    public SalesRecord updateByOrderNo(String orderNo, SalesRecord record) {
        SalesRecord existing = findByOrderNo(orderNo);
        
        // 只有草稿和已拒绝状态的记录可以编辑
        if (!"draft".equals(existing.getStatus()) && !"rejected".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿或已拒绝状态的记录可以编辑");
        }

        SalesRecord oldRecord = new SalesRecord();
        oldRecord.setQuantity(existing.getQuantity());
        oldRecord.setUnitPrice(existing.getUnitPrice());
        oldRecord.setTotalAmount(existing.getTotalAmount());

        // 更新关联实体
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        }
        if (record.getProduct() != null && record.getProduct().getId() != null) {
            existing.setProduct(productService.findById(record.getProduct().getId()));
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        }

        existing.setQuantity(record.getQuantity());
        existing.setUnitPrice(record.getUnitPrice());
        existing.setTotalAmount(record.getTotalAmount());
        existing.setCommissionRate(record.getCommissionRate());
        existing.setSaleDate(record.getSaleDate());
        existing.setRemark(record.getRemark());

        SalesRecord saved = salesRecordRepository.save(existing);
        
        // 记录审计日志
        if (record.getCreatedBy() != null) {
            auditLogService.log("SalesRecord", existing.getId(), "UPDATE", 
                record.getCreatedBy(), "salesman", oldRecord, saved, "更新销售记录");
        }

        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!salesRecordRepository.existsById(id)) {
            throw new BusinessException("销售记录不存在");
        }
        salesRecordRepository.deleteById(id);
    }

    @Transactional
    public void deleteByOrderNo(String orderNo) {
        SalesRecord record = findByOrderNo(orderNo);
        salesRecordRepository.delete(record);
    }

    /**
     * 验证提交审核时必须填写的字段
     */
    private void validateForSubmission(SalesRecord record) {
        if (record.getSalesman() == null || record.getSalesman().getId() == null) {
            throw new BusinessException("销售员不能为空");
        }
        if (record.getProduct() == null || record.getProduct().getId() == null) {
            throw new BusinessException("产品不能为空");
        }
        if (record.getCustomer() == null || record.getCustomer().getId() == null) {
            throw new BusinessException("客户不能为空");
        }
        if (record.getQuantity() == null || record.getQuantity() <= 0) {
            throw new BusinessException("销售数量必须大于0");
        }
        if (record.getUnitPrice() == null || record.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("单价必须大于0");
        }
        if (record.getSaleDate() == null) {
            throw new BusinessException("销售日期不能为空");
        }
    }

    // 统计方法
    public BigDecimal getTotalSalesBySalesman(Long salesmanId) {
        BigDecimal total = salesRecordRepository.sumTotalAmountBySalesman(salesmanId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalCommissionBySalesman(Long salesmanId) {
        BigDecimal total = salesRecordRepository.sumCommissionAmountBySalesman(salesmanId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // ==================== 状态管理方法 ====================

    /**
     * 根据状态查询销售记录
     */
    public List<SalesRecord> findByStatus(String status) {
        return salesRecordRepository.findByStatusOrderBySaleDateDesc(status);
    }

    /**
     * 根据销售员和状态查询
     */
    public List<SalesRecord> findBySalesmanAndStatus(Long salesmanId, String status) {
        return salesRecordRepository.findBySalesman_IdAndStatusOrderBySaleDateDesc(salesmanId, status);
    }

    /**
     * 提交审核（草稿 -> 待审核）- 通过ID
     */
    @Transactional
    public SalesRecord submitForApprovalById(Long id, String operator) {
        SalesRecord record = findById(id);
        
        if (!"draft".equals(record.getStatus())) {
            throw new BusinessException("只有草稿状态的记录可以提交审核");
        }
        // 验证必填字段
        validateForSubmission(record);

        String oldStatus = record.getStatus();
        record.setStatus("pending");
        SalesRecord saved = salesRecordRepository.save(record);

        // 记录审计日志
        auditLogService.log("SalesRecord", id, "SUBMIT", operator, "salesman", 
            oldStatus, "pending", "提交销售记录审核");

        return saved;
    }

    /**
     * 提交审核（草稿 -> 待审核）- 通过订单号
     */
    @Transactional
    public SalesRecord submitForApprovalByOrderNo(String orderNo, String operator) {
        SalesRecord record = findByOrderNo(orderNo);
        
        if (!"draft".equals(record.getStatus()) && !"rejected".equals(record.getStatus())) {
            throw new BusinessException("只有草稿或已拒绝状态的记录可以提交审核");
        }
        // 验证必填字段
        validateForSubmission(record);

        String oldStatus = record.getStatus();
        record.setStatus("pending");
        record.setRejectReason(null); // 清空拒绝原因
        SalesRecord saved = salesRecordRepository.save(record);

        // 记录审计日志
        auditLogService.log("SalesRecord", record.getId(), "SUBMIT", operator, "salesman", 
            oldStatus, "pending", "提交销售记录审核");

        return saved;
    }

    /**
     * 审核通过（待审核 -> 已确认）- 通过ID
     */
    @Transactional
    public SalesRecord approveById(Long id, String approver) {
        SalesRecord record = findById(id);
        
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("只有待审核状态的记录可以审核");
        }
        
        String oldStatus = record.getStatus();
        record.setStatus("approved");
        record.setApprovedBy(approver);
        record.setApprovedTime(java.time.LocalDateTime.now());
        SalesRecord saved = salesRecordRepository.save(record);
        
        // 记录审计日志
        auditLogService.log("SalesRecord", id, "APPROVE", approver, "admin", 
            oldStatus, "approved", "审核通过销售记录");
        // 模拟库存扣减（仅记录审计，不做真实扣减）
        auditLogService.log("SalesRecord", id, "STOCK_SIMULATED", approver, "system",
            null, null, String.format("模拟扣减库存: productId=%s, qty=%d", record.getProduct() != null ? record.getProduct().getId() : "-", record.getQuantity() != null ? record.getQuantity() : 0));
        
        return saved;
    }

    /**
     * 审核通过（待审核 -> 已确认）- 通过订单号
     */
    @Transactional
    public SalesRecord approveByOrderNo(String orderNo, String approver) {
        SalesRecord record = findByOrderNo(orderNo);
        
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("只有待审核状态的记录可以审核");
        }
        
        String oldStatus = record.getStatus();
        record.setStatus("approved");
        record.setApprovedBy(approver);
        record.setApprovedTime(java.time.LocalDateTime.now());
        SalesRecord saved = salesRecordRepository.save(record);
        
        // 记录审计日志
        auditLogService.log("SalesRecord", record.getId(), "APPROVE", approver, "admin", 
            oldStatus, "approved", "审核通过销售记录");
        // 模拟库存扣减（仅记录审计，不做真实扣减）
        auditLogService.log("SalesRecord", record.getId(), "STOCK_SIMULATED", approver, "system",
            null, null, String.format("模拟扣减库存: productId=%s, qty=%d", record.getProduct() != null ? record.getProduct().getId() : "-", record.getQuantity() != null ? record.getQuantity() : 0));
        
        return saved;
    }

    /**
     * 审核拒绝（待审核 -> 已拒绝）- 通过ID
     */
    @Transactional
    public SalesRecord rejectById(Long id, String approver, String reason) {
        SalesRecord record = findById(id);
        
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("只有待审核状态的记录可以拒绝");
        }
        
        String oldStatus = record.getStatus();
        record.setStatus("rejected");
        record.setApprovedBy(approver);
        record.setApprovedTime(java.time.LocalDateTime.now());
        record.setRejectReason(reason);
        SalesRecord saved = salesRecordRepository.save(record);
        
        // 记录审计日志
        auditLogService.log("SalesRecord", id, "REJECT", approver, "admin", 
            oldStatus, "rejected", "拒绝销售记录：" + reason);
        
        return saved;
    }

    /**
     * 审核拒绝（待审核 -> 已拒绝）- 通过订单号
     */
    @Transactional
    public SalesRecord rejectByOrderNo(String orderNo, String approver, String reason) {
        SalesRecord record = findByOrderNo(orderNo);
        
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("只有待审核状态的记录可以拒绝");
        }
        
        String oldStatus = record.getStatus();
        record.setStatus("rejected");
        record.setApprovedBy(approver);
        record.setApprovedTime(java.time.LocalDateTime.now());
        record.setRejectReason(reason);
        SalesRecord saved = salesRecordRepository.save(record);
        
        // 记录审计日志
        auditLogService.log("SalesRecord", record.getId(), "REJECT", approver, "admin", 
            oldStatus, "rejected", "拒绝销售记录：" + reason);
        
        return saved;
    }

    /**
     * 撤回审核（待审核 -> 草稿）- 通过ID
     */
    @Transactional
    public SalesRecord withdrawById(Long id, String operator) {
        SalesRecord record = findById(id);
        
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("只有待审核状态的记录可以撤回");
        }
        
        String oldStatus = record.getStatus();
        record.setStatus("draft");
        record.setFromWithdrawn(true);
        SalesRecord saved = salesRecordRepository.save(record);
        
        // 记录审计日志
        auditLogService.log("SalesRecord", id, "WITHDRAW", operator, "salesman", 
            oldStatus, "draft", "撤回销售记录审核");
        
        return saved;
    }

    /**
     * 撤回审核（待审核 -> 草稿）- 通过订单号
     */
    @Transactional
    public SalesRecord withdrawByOrderNo(String orderNo, String operator) {
        SalesRecord record = findByOrderNo(orderNo);
        
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("只有待审核状态的记录可以撤回");
        }
        
        String oldStatus = record.getStatus();
        record.setStatus("draft");
        record.setFromWithdrawn(true);
        SalesRecord saved = salesRecordRepository.save(record);
        
        // 记录审计日志
        auditLogService.log("SalesRecord", record.getId(), "WITHDRAW", operator, "salesman", 
            oldStatus, "draft", "撤回销售记录审核");
        
        return saved;
    }
}
