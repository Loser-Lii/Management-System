package com.example.backend.service;

import com.example.backend.entity.CollectionRecord;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.CollectionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class CollectionRecordService {

    @Autowired
    private CollectionRecordRepository collectionRecordRepository;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private CustomerService customerService;

    public List<CollectionRecord> findAll() {
        return collectionRecordRepository.findAll();
    }

    public CollectionRecord findById(Long id) {
        return collectionRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("催款记录不存在"));
    }

    public List<CollectionRecord> findBySalesman(Long salesmanId) {
        return collectionRecordRepository.findBySalesmanId(salesmanId);
    }

    public List<CollectionRecord> findByStatus(String status) {
        return collectionRecordRepository.findByStatus(status);
    }

    public List<CollectionRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return collectionRecordRepository.findByCollectionDateBetween(startDate, endDate);
    }

    // 计算回款率
    public BigDecimal getCollectionRate(Long salesmanId) {
        BigDecimal received = collectionRecordRepository.sumReceivedAmountBySalesman(salesmanId);
        BigDecimal due = collectionRecordRepository.sumDueAmountBySalesman(salesmanId);
        
        if (received == null) received = BigDecimal.ZERO;
        if (due == null || due.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        return received.divide(due, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    @Transactional
    public CollectionRecord create(CollectionRecord record) {
        record.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        record.setCustomer(customerService.findById(record.getCustomer().getId()));
        return collectionRecordRepository.save(record);
    }

    @Transactional
    public CollectionRecord update(Long id, CollectionRecord record) {
        CollectionRecord existing = findById(id);
        if (record.getSalesman() != null) existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        if (record.getCustomer() != null) existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        existing.setCollectionDate(record.getCollectionDate());
        existing.setCollectionMethod(record.getCollectionMethod());
        existing.setAmountDue(record.getAmountDue());
        existing.setAmountReceived(record.getAmountReceived());
        existing.setStatus(record.getStatus());
        existing.setDueDate(record.getDueDate());
        existing.setRemark(record.getRemark());
        return collectionRecordRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!collectionRecordRepository.existsById(id)) {
            throw new BusinessException("催款记录不存在");
        }
        collectionRecordRepository.deleteById(id);
    }
}
