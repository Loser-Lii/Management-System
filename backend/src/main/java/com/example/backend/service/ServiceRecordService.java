package com.example.backend.service;

import com.example.backend.entity.ServiceRecord;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ServiceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class ServiceRecordService {

    @Autowired
    private ServiceRecordRepository serviceRecordRepository;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuditLogService auditLogService;

    private String generateServiceNo() {
        LocalDate today = LocalDate.now();
        Long count = serviceRecordRepository.countByDate(today);
        long sequenceNumber = (count == null ? 0 : count) + 1;
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
        return String.format("SRV-%04d%02d%02d-%03d", year, month, day, sequenceNumber);
    }

    public List<ServiceRecord> findAll() {
        return serviceRecordRepository.findAll();
    }

    public ServiceRecord findById(Long id) {
        return serviceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务记录不存在"));
    }

    public List<ServiceRecord> findBySalesman(Long salesmanId) {
        return serviceRecordRepository.findBySalesman_Id(salesmanId);
    }

    public List<ServiceRecord> findByServiceType(String serviceType) {
        return serviceRecordRepository.findByServiceType(serviceType);
    }

    public List<ServiceRecord> findByStatus(String status) {
        return serviceRecordRepository.findByStatus(status);
    }

    public List<ServiceRecord> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return serviceRecordRepository.findByStartTimeBetween(startTime, endTime);
    }

    public Double getAverageSatisfaction(Long salesmanId) {
        Double avg = serviceRecordRepository.getAverageSatisfactionBySalesman(salesmanId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public ServiceRecord create(ServiceRecord record) {
        // 验证必填字段
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            record.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            record.setCustomer(customerService.findById(record.getCustomer().getId()));
        }
        
        if (record.getProduct() != null && record.getProduct().getId() != null) {
            record.setProduct(productService.findById(record.getProduct().getId()));
        }
        
        // 生成服务单号
        if (record.getServiceNo() == null || record.getServiceNo().isEmpty()) {
            record.setServiceNo(generateServiceNo());
        }
        
        // 创建时强制设置为待处理状态，清空不应该有的字段
        record.setStatus("pending");
        record.setEndTime(null);
        record.setSolution(null);
        record.setSatisfaction(null);
        
        ServiceRecord saved = serviceRecordRepository.save(record);
        
        // 记录日志
        auditLogService.log("ServiceRecord", saved.getId(), "CREATE", 
            saved.getSalesman() != null ? saved.getSalesman().getName() : "system", 
            "salesman", 
            null, saved, 
            "创建服务记录: " + saved.getServiceNo());
        
        return saved;
    }

    @Transactional
    public ServiceRecord update(Long id, ServiceRecord record) {
        ServiceRecord existing = findById(id);
        
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        }
        if (record.getProduct() != null && record.getProduct().getId() != null) {
            existing.setProduct(productService.findById(record.getProduct().getId()));
        }
        
        existing.setServiceType(record.getServiceType());
        existing.setStartTime(record.getStartTime());
        existing.setContent(record.getContent());
        
        ServiceRecord updated = serviceRecordRepository.save(existing);
        
        // 记录日志
        auditLogService.log("ServiceRecord", id, "UPDATE",
            updated.getSalesman() != null ? updated.getSalesman().getName() : "system",
            "salesman",
            null, updated,
            "更新服务记录");
        
        return updated;
    }

    /**
     * 销售员处理服务：待处理 -> 处理中
     */
    @Transactional
    public ServiceRecord processService(Long id, String solution) {
        ServiceRecord existing = findById(id);
        
        // 状态校验
        if (!"pending".equals(existing.getStatus())) {
            throw new BusinessException("只有待处理的服务才能开始处理");
        }
        
        // 必须填写处理结果
        if (solution == null || solution.trim().isEmpty()) {
            throw new BusinessException("处理结果不能为空");
        }
        
        existing.setSolution(solution);
        existing.setStatus("processing");
        
        ServiceRecord processed = serviceRecordRepository.save(existing);
        
        // 记录日志
        auditLogService.log("ServiceRecord", id, "PROCESS",
            processed.getSalesman() != null ? processed.getSalesman().getName() : "system",
            "salesman",
            "pending", "processing",
            "销售员处理服务记录");
        
        return processed;
    }

    /**
     * 客户评分（管理员模拟）：处理中 -> 已完成
     */
    @Transactional
    public ServiceRecord rateService(Long id, Integer satisfaction) {
        ServiceRecord existing = findById(id);
        
        // 状态校验
        if (!"processing".equals(existing.getStatus())) {
            throw new BusinessException("只有处理中的服务才能评分");
        }
        
        // 满意度校验
        if (satisfaction == null || satisfaction < 1 || satisfaction > 5) {
            throw new BusinessException("满意度评分必须在1-5之间");
        }
        
        existing.setSatisfaction(satisfaction);
        existing.setEndTime(LocalDateTime.now());
        existing.setStatus("completed");
        
        ServiceRecord rated = serviceRecordRepository.save(existing);
        
        // 记录日志
        auditLogService.log("ServiceRecord", id, "RATE",
            "admin", "admin",
            "processing", "completed",
            "客户评分，满意度: " + satisfaction + "星");
        
        return rated;
    }

    @Transactional
    public void delete(Long id) {
        ServiceRecord existing = findById(id);
        
        // 记录日志（在删除前记录）
        auditLogService.log("ServiceRecord", id, "DELETE",
            existing.getSalesman() != null ? existing.getSalesman().getName() : "system",
            "salesman",
            existing, null,
            "删除服务记录: " + existing.getServiceNo());
        
        serviceRecordRepository.deleteById(id);
    }
}
