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

    public List<SalesRecord> findAll() {
        return salesRecordRepository.findAll();
    }

    public SalesRecord findById(Long id) {
        return salesRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售记录不存在"));
    }

    public List<SalesRecord> findBySalesman(Long salesmanId) {
        return salesRecordRepository.findBySalesmanId(salesmanId);
    }

    public List<SalesRecord> findByCustomer(Long customerId) {
        return salesRecordRepository.findByCustomerId(customerId);
    }

    public List<SalesRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findBySaleDateBetween(startDate, endDate);
    }

    public List<SalesRecord> findBySalesmanAndDateRange(Long salesmanId, LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findBySalesmanIdAndSaleDateBetween(salesmanId, startDate, endDate);
    }

    @Transactional
    public SalesRecord create(SalesRecord record) {
        // 验证关联实体
        Salesman salesman = salesmanService.findById(record.getSalesman().getId());
        Product product = productService.findById(record.getProduct().getId());
        Customer customer = customerService.findById(record.getCustomer().getId());

        record.setSalesman(salesman);
        record.setProduct(product);
        record.setCustomer(customer);

        // 计算总金额
        if (record.getTotalAmount() == null) {
            BigDecimal totalAmount = record.getUnitPrice().multiply(BigDecimal.valueOf(record.getQuantity()));
            record.setTotalAmount(totalAmount);
        }

        // 如果没有设置提成比例，使用销售员的基础提成比例
        if (record.getCommissionRate() == null && salesman.getCommissionRate() != null) {
            record.setCommissionRate(salesman.getCommissionRate());
        }

        return salesRecordRepository.save(record);
    }

    @Transactional
    public SalesRecord update(Long id, SalesRecord record) {
        SalesRecord existing = findById(id);

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

        return salesRecordRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!salesRecordRepository.existsById(id)) {
            throw new BusinessException("销售记录不存在");
        }
        salesRecordRepository.deleteById(id);
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
}
