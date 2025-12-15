package com.example.backend.service;

import com.example.backend.dto.CustomerDTO;
import com.example.backend.entity.CollectionRecord;
import com.example.backend.entity.ComplaintRecord;
import com.example.backend.entity.Customer;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    @Autowired
    private CollectionRecordRepository collectionRecordRepository;

    @Autowired
    private ComplaintRecordRepository complaintRecordRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Customer> findAll() {
        return customerRepository.findAll().stream()
            .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
            .collect(Collectors.toList());
    }

    public List<CustomerDTO> findAllWithSalesman() {
        return customerRepository.findAll().stream()
            .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // 根据销售员ID查询客户（销售员只能看到自己的客户）
    public List<Customer> findBySalesmanId(Long salesmanId) {
        return customerRepository.findBySalesmanId(salesmanId).stream()
                .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));
    }

    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return customerRepository.searchByKeyword(keyword).stream()
                .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    // 根据销售员ID和关键词搜索
    public List<Customer> searchBySalesman(Long salesmanId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findBySalesmanId(salesmanId);
        }
        return customerRepository.searchBySalesmanIdAndKeyword(salesmanId, keyword).stream()
                .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Customer> findByType(String customerType) {
        return customerRepository.findByCustomerType(customerType).stream()
                .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Customer> findByLevel(String level) {
        return customerRepository.findByLevel(level).stream()
                .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    // 查询无负责人的客户
    public List<Customer> findWithoutSalesman() {
        return customerRepository.findBySalesmanIdIsNull().stream()
                .filter(c -> c.getStatus() == null || "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    // 生成客户编号（C开头的序列）
    private String generateCustomerNo() {
        // 查询最新的客户编号
        Customer latestCustomer = customerRepository.findFirstByOrderByIdDesc();
        long nextId = 1;
        
        if (latestCustomer != null && latestCustomer.getId() != null) {
            nextId = latestCustomer.getId() + 1;
        }
        
        return String.format("C%06d", nextId);
    }

    @Transactional
    public Customer create(Customer customer) {
        // 自动生成客户编号
        if (customer.getCustomerNo() == null || customer.getCustomerNo().isEmpty()) {
            customer.setCustomerNo(generateCustomerNo());
        }
        Customer result = customerRepository.save(customer);
        
        // 记录审计日志
        try {
            Map<String, Object> customerInfo = new HashMap<>();
            customerInfo.put("customerNo", result.getCustomerNo());
            customerInfo.put("name", result.getName());
            customerInfo.put("contactPerson", result.getContactPerson());
            customerInfo.put("phone", result.getPhone());
            customerInfo.put("customerType", result.getCustomerType());
            customerInfo.put("level", result.getLevel());
            customerInfo.put("salesmanId", result.getSalesmanId());
            String customerJson = objectMapper.writeValueAsString(customerInfo);
            String operator = result.getSalesmanId() != null ? 
                salesmanRepository.findById(result.getSalesmanId()).map(s -> s.getName()).orElse("admin") : "admin";
            String operatorType = result.getSalesmanId() != null ? "salesman" : "admin";
            String description = String.format("新增客户: %s (编号: %s)", result.getName(), result.getCustomerNo());
            auditLogService.log("Customer", result.getId(), "CREATE", 
                operator, operatorType, 
                null, customerJson, description);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
        
        return result;
    }

    @Transactional
    public Customer update(Long id, Customer customer) {
        Customer existing = findById(id);
        
        // 记录修改前的值
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        if (!existing.getName().equals(customer.getName())) {
            oldValues.put("name", existing.getName());
            newValues.put("name", customer.getName());
        }
        if (!java.util.Objects.equals(existing.getContactPerson(), customer.getContactPerson())) {
            oldValues.put("contactPerson", existing.getContactPerson());
            newValues.put("contactPerson", customer.getContactPerson());
        }
        if (!java.util.Objects.equals(existing.getPhone(), customer.getPhone())) {
            oldValues.put("phone", existing.getPhone());
            newValues.put("phone", customer.getPhone());
        }
        if (!java.util.Objects.equals(existing.getEmail(), customer.getEmail())) {
            oldValues.put("email", existing.getEmail());
            newValues.put("email", customer.getEmail());
        }
        if (!java.util.Objects.equals(existing.getAddress(), customer.getAddress())) {
            oldValues.put("address", existing.getAddress());
            newValues.put("address", customer.getAddress());
        }
        if (!java.util.Objects.equals(existing.getCustomerType(), customer.getCustomerType())) {
            oldValues.put("customerType", existing.getCustomerType());
            newValues.put("customerType", customer.getCustomerType());
        }
        if (!java.util.Objects.equals(existing.getLevel(), customer.getLevel())) {
            oldValues.put("level", existing.getLevel());
            newValues.put("level", customer.getLevel());
        }
        if (!java.util.Objects.equals(existing.getSalesmanId(), customer.getSalesmanId())) {
            oldValues.put("salesmanId", existing.getSalesmanId());
            newValues.put("salesmanId", customer.getSalesmanId());
        }
        
        existing.setName(customer.getName());
        existing.setContactPerson(customer.getContactPerson());
        existing.setPhone(customer.getPhone());
        existing.setEmail(customer.getEmail());
        existing.setAddress(customer.getAddress());
        existing.setCustomerType(customer.getCustomerType());
        existing.setLevel(customer.getLevel());
        existing.setSalesmanId(customer.getSalesmanId());
        existing.setRemark(customer.getRemark());
        Customer result = customerRepository.save(existing);
        
        // 记录审计日志
        if (!oldValues.isEmpty()) {
            try {
                String oldValuesJson = objectMapper.writeValueAsString(oldValues);
                String newValuesJson = objectMapper.writeValueAsString(newValues);
                String description = String.format("修改客户 %s 的信息: %s", 
                    result.getName(), String.join("、", newValues.keySet()));
                auditLogService.log("Customer", id, "UPDATE", 
                    "admin", "admin", 
                    oldValuesJson, newValuesJson, description);
            } catch (Exception e) {
                // 日志记录失败不影响主业务
            }
        }
        
        return result;
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findById(id);  // 检查客户是否存在
        
        // 一次性检查所有未完成的事项（复用解绑客户和销售员离职的逻辑）
        List<String> issues = new ArrayList<>();
        
        // 检查是否有未完成的待审核销售记录
        List<SalesRecord> pendingSalesRecords = salesRecordRepository.findByCustomer_IdAndStatusOrderBySaleDateDesc(id, "pending");
        if (!pendingSalesRecords.isEmpty()) {
            issues.add(pendingSalesRecords.size() + " 条待审核的销售记录");
        }
        
        // 检查是否有未完成的催款记录
        List<CollectionRecord> pendingCollections = collectionRecordRepository.findByCustomer_Id(id).stream()
                .filter(c -> "pending".equals(c.getCollectionStatus()))
                .collect(Collectors.toList());
        if (!pendingCollections.isEmpty()) {
            issues.add(pendingCollections.size() + " 条未完成的催款记录");
        }
        
        // 检查是否有未结束的投诉记录
        List<ComplaintRecord> activeComplaints = complaintRecordRepository.findByCustomer_Id(id).stream()
                .filter(c -> "pending".equals(c.getStatus()) || "processing".equals(c.getStatus()))
                .collect(Collectors.toList());
        if (!activeComplaints.isEmpty()) {
            issues.add(activeComplaints.size() + " 条未结束的投诉记录");
        }
        
        // 如果有任何未完成的事项，一次性抛出
        if (!issues.isEmpty()) {
            String message = "该客户还有以下未完成的事项，请先处理完成：\n" + String.join("、", issues);
            throw new BusinessException(message);
        }
        
        // 软删除：标记停用
        customer.setStatus("inactive");
        customerRepository.save(customer);

        // 记录删除前的客户信息
        try {
            Map<String, Object> customerInfo = new HashMap<>();
            customerInfo.put("customerNo", customer.getCustomerNo());
            customerInfo.put("name", customer.getName());
            customerInfo.put("phone", customer.getPhone());
            customerInfo.put("status", customer.getStatus());
            String customerJson = objectMapper.writeValueAsString(customerInfo);
            String description = String.format("停用客户: %s (编号: %s)", customer.getName(), customer.getCustomerNo());
            auditLogService.log("Customer", id, "DEACTIVATE", 
                "admin", "admin", 
                null, customerJson, description);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
    }

    // 解绑客户（将负责销售员设为空）
    @Transactional
    public Customer unbindSalesman(Long id) {
        Customer customer = findById(id);
        
        if (customer.getSalesmanId() == null) {
            throw new BusinessException("该客户未分配销售员，无需解绑");
        }
        
        Long oldSalesmanId = customer.getSalesmanId();
        String oldSalesmanName = salesmanRepository.findById(oldSalesmanId)
            .map(s -> s.getName()).orElse("未知");
        
        // 一次性检查所有未完成的事项（复用离职检查逻辑）
        List<String> issues = new ArrayList<>();
        
        // 检查是否有未完成的待审核销售记录
        List<SalesRecord> pendingSalesRecords = salesRecordRepository.findByCustomer_IdAndStatusOrderBySaleDateDesc(id, "pending");
        if (!pendingSalesRecords.isEmpty()) {
            issues.add(pendingSalesRecords.size() + " 条待审核的销售记录");
        }
        
        // 检查是否有未完成的催款记录
        List<CollectionRecord> pendingCollections = collectionRecordRepository.findByCustomer_Id(id).stream()
                .filter(c -> "pending".equals(c.getCollectionStatus()))
                .collect(Collectors.toList());
        if (!pendingCollections.isEmpty()) {
            issues.add(pendingCollections.size() + " 条未完成的催款记录");
        }
        
        // 检查是否有未结束的投诉记录
        List<ComplaintRecord> activeComplaints = complaintRecordRepository.findByCustomer_Id(id).stream()
                .filter(c -> "pending".equals(c.getStatus()) || "processing".equals(c.getStatus()))
                .collect(Collectors.toList());
        if (!activeComplaints.isEmpty()) {
            issues.add(activeComplaints.size() + " 条未结束的投诉记录");
        }
        
        // 如果有任何未完成的事项，一次性抛出
        if (!issues.isEmpty()) {
            String message = "该客户还有以下未完成的事项，请先处理完成：\n" + String.join("、", issues);
            throw new BusinessException(message);
        }
        
        // 解绑销售员
        customer.setSalesmanId(null);
        Customer result = customerRepository.save(customer);
        
        // 记录审计日志
        String description = String.format("将客户 %s (编号: %s) 从销售员 %s 处解绑", 
            customer.getName(), customer.getCustomerNo(), oldSalesmanName);
        auditLogService.log("Customer", id, "UNBIND", 
            "admin", "admin", 
            String.valueOf(oldSalesmanId), null, description);
        
        return result;
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setCustomerNo(customer.getCustomerNo());
        dto.setName(customer.getName());
        dto.setContactPerson(customer.getContactPerson());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setCustomerType(customer.getCustomerType());
        dto.setLevel(customer.getLevel());
        dto.setSalesmanId(customer.getSalesmanId());
        dto.setRemark(customer.getRemark());
        dto.setCreateTime(customer.getCreateTime());
        dto.setUpdateTime(customer.getUpdateTime());
        
        // 获取销售员名称
        if (customer.getSalesmanId() != null) {
            salesmanRepository.findById(customer.getSalesmanId())
                    .ifPresent(salesman -> dto.setSalesmanName(salesman.getName()));
        }
        
        return dto;
    }
}
