package com.example.backend.service;

import com.example.backend.entity.ComplaintRecord;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ComplaintRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComplaintRecordService {

    @Autowired
    private ComplaintRecordRepository complaintRecordRepository;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ProductService productService;

    @SuppressWarnings("null")
    public List<ComplaintRecord> findAll() {
        return complaintRecordRepository.findAll();
    }

    public ComplaintRecord findById(Long id) {
        return complaintRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("投诉记录不存在"));
    }

    public List<ComplaintRecord> findBySalesman(Long salesmanId) {
        return complaintRecordRepository.findBySalesman_Id(salesmanId);
    }

    public List<ComplaintRecord> findByStatus(String status) {
        return complaintRecordRepository.findByStatus(status);
    }
    
    public List<ComplaintRecord> findBySeverity(String severity) {
        return complaintRecordRepository.findBySeverity(severity);
    }
    
    public List<ComplaintRecord> findByType(String complaintType) {
        return complaintRecordRepository.findByComplaintType(complaintType);
    }

    public Long countBySalesman(Long salesmanId) {
        return complaintRecordRepository.countBySalesman(salesmanId);
    }
    
    /**
     * 获取投诉统计仪表盘数据
     * @param salesmanId 销售员ID（可选），为null时返回全部统计
     */
    public Map<String, Object> getStatistics(Long salesmanId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<ComplaintRecord> records;
        if (salesmanId != null) {
            records = complaintRecordRepository.findBySalesman_Id(salesmanId);
        } else {
            records = complaintRecordRepository.findAll();
        }
        
        long totalCount = records.size();

        // 处理状态统计
        long pendingCount = records.stream().filter(r -> "pending".equals(r.getStatus())).count();
        long processingCount = records.stream().filter(r -> "processing".equals(r.getStatus())).count();
        long resolvedCount = records.stream().filter(r -> "resolved".equals(r.getStatus())).count();
        long closedCount = records.stream().filter(r -> "closed".equals(r.getStatus())).count();
        
        // 计算解决率（只包含已解决，不包含已撤销）
        long effectiveTotal = totalCount - closedCount;
        
        // 计算解决率：(已解决 / 有效投诉总数) * 100
        double resolveRate = effectiveTotal > 0 
            ? ((double)resolvedCount / effectiveTotal) * 100 
            : 0.0;
        
        // 处理状态统计
        stats.put("pendingCount", pendingCount);
        stats.put("processingCount", processingCount);
        stats.put("resolvedCount", resolvedCount);
        stats.put("resolveRate", String.format("%.1f", resolveRate));
        
        return stats;
    }
    
    /**
     * 自动生成投诉单号: CMP-yyyyMMdd-xxx
     */
    private String generateComplaintNo() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CMP-" + datePrefix + "-";
        
        List<ComplaintRecord> todayRecords = complaintRecordRepository.findAll().stream()
            .filter(r -> r.getComplaintNo() != null && r.getComplaintNo().startsWith(prefix))
            .toList();
        
        int nextNum = todayRecords.size() + 1;
        return prefix + String.format("%03d", nextNum);
    }

    @Transactional
    public ComplaintRecord create(ComplaintRecord record) {
        // 自动生成投诉单号
        record.setComplaintNo(generateComplaintNo());
        
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            record.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            record.setCustomer(customerService.findById(record.getCustomer().getId()));
        }
        
        if (record.getRelatedProduct() != null && record.getRelatedProduct().getId() != null) {
            record.setRelatedProduct(productService.findById(record.getRelatedProduct().getId()));
        }
        
        return complaintRecordRepository.save(record);
    }

    @Transactional
    public ComplaintRecord update(Long id, ComplaintRecord record) {
        ComplaintRecord existing = findById(id);
        
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        }
        if (record.getRelatedProduct() != null && record.getRelatedProduct().getId() != null) {
            existing.setRelatedProduct(productService.findById(record.getRelatedProduct().getId()));
        }
        
        if (record.getRelatedOrderNo() != null) existing.setRelatedOrderNo(record.getRelatedOrderNo());
        if (record.getComplaintType() != null && !record.getComplaintType().isEmpty()) {
            existing.setComplaintType(record.getComplaintType());
        }
        if (record.getSeverity() != null && !record.getSeverity().isEmpty()) {
            existing.setSeverity(record.getSeverity());
        }
        if (record.getContent() != null && !record.getContent().isEmpty()) {
            existing.setContent(record.getContent());
        }
        if (record.getEvidenceImage() != null) existing.setEvidenceImage(record.getEvidenceImage());
        if (record.getSolution() != null) existing.setSolution(record.getSolution());
        if (record.getCustomerFeedback() != null) existing.setCustomerFeedback(record.getCustomerFeedback());
        if (record.getStatus() != null) {
            existing.setStatus(record.getStatus());
            // 如果状态变为resolved或closed，记录解决时间
            if (("resolved".equals(record.getStatus()) || "closed".equals(record.getStatus())) 
                && existing.getResolveTime() == null) {
                existing.setResolveTime(LocalDateTime.now());
            }
        }
        
        return complaintRecordRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!complaintRecordRepository.existsById(id)) {
            throw new BusinessException("投诉记录不存在");
        }
        complaintRecordRepository.deleteById(id);
    }
}
