package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ContactRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContactRecordService {

    @Autowired
    private ContactRecordRepository contactRecordRepository;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateContactNo() {
        String datePrefix = java.time.LocalDate.now().toString().replace("-", "");
        ContactRecord latest = contactRecordRepository.findAll().stream()
                .filter(r -> r.getContactNo() != null && r.getContactNo().startsWith("CNT-" + datePrefix))
                .max((a, b) -> a.getContactNo().compareTo(b.getContactNo()))
                .orElse(null);
        int nextSeq = 1;
        if (latest != null) {
            String lastNo = latest.getContactNo();
            String[] parts = lastNo.split("-");
            if (parts.length == 3) {
                nextSeq = Integer.parseInt(parts[2]) + 1;
            }
        }
        return String.format("CNT-%s-%03d", datePrefix, nextSeq);
    }

    public List<ContactRecord> findAll() {
        return contactRecordRepository.findAll();
    }

    public ContactRecord findById(Long id) {
        return contactRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("联络记录不存在"));
    }

    public List<ContactRecord> findBySalesman(Long salesmanId) {
        return contactRecordRepository.findBySalesman_Id(salesmanId);
    }

    public List<ContactRecord> findByCustomer(Long customerId) {
        return contactRecordRepository.findByCustomer_Id(customerId);
    }

    public List<ContactRecord> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return contactRecordRepository.findByContactDateBetween(startTime, endTime);
    }

    @Transactional
    public ContactRecord create(ContactRecord record) {
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            record.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            record.setCustomer(customerService.findById(record.getCustomer().getId()));
        }
        if (record.getContactNo() == null || record.getContactNo().isEmpty()) {
            record.setContactNo(generateContactNo());
        }
        ContactRecord result = contactRecordRepository.save(record);
        
        // 记录审计日志
        try {
            Map<String, Object> recordInfo = new HashMap<>();
            recordInfo.put("contactNo", result.getContactNo());
            recordInfo.put("salesmanName", result.getSalesman() != null ? result.getSalesman().getName() : null);
            recordInfo.put("customerName", result.getCustomer() != null ? result.getCustomer().getName() : null);
            recordInfo.put("contactDate", result.getContactDate());
            recordInfo.put("contactWay", result.getContactWay());
            recordInfo.put("content", result.getContent());
            recordInfo.put("outcome", result.getOutcome());
            String recordJson = objectMapper.writeValueAsString(recordInfo);
            String salesmanName = result.getSalesman() != null ? result.getSalesman().getName() : "未知";
            String customerName = result.getCustomer() != null ? result.getCustomer().getName() : "未知";
            String description = String.format("销售员 %s 上传联络记录 (客户: %s, 方式: %s)", 
                salesmanName, customerName, result.getContactWay());
            auditLogService.log("ContactRecord", result.getId(), "CREATE", 
                salesmanName, "salesman", 
                null, recordJson, description);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
        
        return result;
    }

    @Transactional
    public ContactRecord update(Long id, ContactRecord record) {
        ContactRecord existing = findById(id);
        
        // 记录修改前的值
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            Salesman newSalesman = salesmanService.findById(record.getSalesman().getId());
            if (!newSalesman.getId().equals(existing.getSalesman().getId())) {
                oldValues.put("salesman", existing.getSalesman().getName());
                newValues.put("salesman", newSalesman.getName());
            }
            existing.setSalesman(newSalesman);
        }
        if (record.getCustomer() != null && record.getCustomer().getId() != null) {
            Customer newCustomer = customerService.findById(record.getCustomer().getId());
            if (!newCustomer.getId().equals(existing.getCustomer().getId())) {
                oldValues.put("customer", existing.getCustomer().getName());
                newValues.put("customer", newCustomer.getName());
            }
            existing.setCustomer(newCustomer);
        }
        if (!existing.getContactDate().equals(record.getContactDate())) {
            oldValues.put("contactDate", existing.getContactDate());
            newValues.put("contactDate", record.getContactDate());
        }
        if (!java.util.Objects.equals(existing.getContactWay(), record.getContactWay())) {
            oldValues.put("contactWay", existing.getContactWay());
            newValues.put("contactWay", record.getContactWay());
        }
        if (!java.util.Objects.equals(existing.getContent(), record.getContent())) {
            oldValues.put("content", existing.getContent());
            newValues.put("content", record.getContent());
        }
        if (!java.util.Objects.equals(existing.getOutcome(), record.getOutcome())) {
            oldValues.put("outcome", existing.getOutcome());
            newValues.put("outcome", record.getOutcome());
        }
        
        existing.setContactDate(record.getContactDate());
        existing.setContactWay(record.getContactWay());
        existing.setContent(record.getContent());
        existing.setOutcome(record.getOutcome());
        existing.setLocation(record.getLocation());
        existing.setFilePath(record.getFilePath());
        ContactRecord result = contactRecordRepository.save(existing);
        
        // 记录审计日志
        if (!oldValues.isEmpty()) {
            try {
                String oldValuesJson = objectMapper.writeValueAsString(oldValues);
                String newValuesJson = objectMapper.writeValueAsString(newValues);
                String salesmanName = result.getSalesman() != null ? result.getSalesman().getName() : "未知";
                String description = String.format("修改联络记录 %s 的信息: %s", 
                    result.getContactNo(), String.join("、", newValues.keySet()));
                auditLogService.log("ContactRecord", id, "UPDATE", 
                    salesmanName, "salesman", 
                    oldValuesJson, newValuesJson, description);
            } catch (Exception e) {
                // 日志记录失败不影响主业务
            }
        }
        
        return result;
    }

    @Transactional
    public void delete(Long id) {
        ContactRecord record = findById(id);
        
        // 记录删除前的信息
        try {
            Map<String, Object> recordInfo = new HashMap<>();
            recordInfo.put("contactNo", record.getContactNo());
            recordInfo.put("salesmanName", record.getSalesman() != null ? record.getSalesman().getName() : null);
            recordInfo.put("customerName", record.getCustomer() != null ? record.getCustomer().getName() : null);
            String recordJson = objectMapper.writeValueAsString(recordInfo);
            String salesmanName = record.getSalesman() != null ? record.getSalesman().getName() : "未知";
            String description = String.format("删除联络记录: %s", record.getContactNo());
            auditLogService.log("ContactRecord", id, "DELETE", 
                salesmanName, "salesman", 
                recordJson, null, description);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
        
        contactRecordRepository.deleteById(id);
    }
}
