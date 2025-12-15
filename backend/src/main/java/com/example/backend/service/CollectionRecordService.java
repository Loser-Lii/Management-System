package com.example.backend.service;

import com.example.backend.entity.CollectionRecord;
import com.example.backend.entity.SalesRecord;
import com.example.backend.entity.Salesman;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.CollectionRecordRepository;
import com.example.backend.repository.SalesRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CollectionRecordService {

    @Autowired
    private CollectionRecordRepository collectionRecordRepository;

    @Autowired
    private SalesmanService salesmanService;
    
    @Autowired
    private SalesRecordRepository salesRecordRepository;

    // ... (Find 查询方法保持不变) ...
    public List<CollectionRecord> findAll() { return collectionRecordRepository.findAll(); }
    public CollectionRecord findById(Long id) {
        return collectionRecordRepository.findById(id).orElseThrow(() -> new BusinessException("催款记录不存在"));
    }
    public List<CollectionRecord> findBySalesman(Long salesmanId) { return collectionRecordRepository.findBySalesman_Id(salesmanId); }
    public List<CollectionRecord> findByStatus(String status) { return collectionRecordRepository.findByCollectionStatus(status); }
    public List<CollectionRecord> findByDateRange(LocalDate start, LocalDate end) { return collectionRecordRepository.findByCollectionDateBetween(start, end); }
    public List<CollectionRecord> findByOrderNo(String orderNo) { return collectionRecordRepository.findByOrderNo(orderNo); }

    // 按日期生成催款单号（COL-yyyyMMdd-NNN）
    private String generateCollectionNo() {
        LocalDate today = LocalDate.now();
        Long count = collectionRecordRepository.countByDate(today);
        long sequenceNumber = (count == null ? 0 : count) + 1;
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
        return String.format("COL-%04d%02d%02d-%03d", year, month, day, sequenceNumber);
    }

    @Transactional
    public CollectionRecord create(CollectionRecord record) {
        // 1. 强校验：订单必须存在且已确认
        SalesRecord salesRecord = salesRecordRepository.findByOrderNo(record.getOrderNo())
                .orElseThrow(() -> new BusinessException("关联订单不存在"));
        
        if (!"approved".equals(salesRecord.getStatus())) {
            throw new BusinessException("只能为【已确认】的订单添加催款记录");
        }
        
        // 2. 逻辑校验：日期
        if (record.getCollectionDate() != null && record.getCollectionDate().isBefore(salesRecord.getSaleDate())) {
            throw new BusinessException("催款日期不能早于销售日期(" + salesRecord.getSaleDate() + ")");
        }
        
        // 3. 数据一致性：强制使用订单关联的客户 (忽略前端传的 Customer)
        record.setCustomer(salesRecord.getCustomer());
        
        // 4. 设置负责人 (Salesman)
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            Salesman salesman = salesmanService.findById(record.getSalesman().getId());
            record.setSalesman(salesman);
        } else {
            // 如果前端没传负责人，默认使用该订单的销售员？(可选逻辑)
            // record.setSalesman(salesRecord.getSalesman());
            throw new BusinessException("必须指定催款负责人");
        }
        
        // 5. 生成单号
        if (record.getCollectionNo() == null || record.getCollectionNo().isEmpty()) {
            record.setCollectionNo(generateCollectionNo());
        }
        
        return collectionRecordRepository.save(record);
    }

    @Transactional
    public CollectionRecord update(Long id, CollectionRecord record) {
        CollectionRecord existing = findById(id);
        
        // 1. 更新简单字段
        if (record.getCurrentAmount() != null) existing.setCurrentAmount(record.getCurrentAmount());
        if (record.getRemainingAmount() != null) existing.setRemainingAmount(record.getRemainingAmount());
        if (record.getCollectionStatus() != null) existing.setCollectionStatus(record.getCollectionStatus());
        if (record.getRemark() != null) existing.setRemark(record.getRemark());

        // 2. 更新日期 (带校验)
        if (record.getCollectionDate() != null) {
            // 重新查订单以获取销售日期进行对比
            SalesRecord salesRecord = salesRecordRepository.findByOrderNo(existing.getOrderNo())
                    .orElseThrow(() -> new BusinessException("关联订单数据异常"));
            
            if (record.getCollectionDate().isBefore(salesRecord.getSaleDate())) {
                throw new BusinessException("催款日期不能早于销售日期");
            }
            existing.setCollectionDate(record.getCollectionDate());
        }

        // 3. 【关键修复】更新负责人 (Salesman)
        // 之前你的代码漏掉了这一段，导致修改负责人无效
        if (record.getSalesman() != null && record.getSalesman().getId() != null) {
            // 如果 ID 变了才查数据库，减少查询
            if (!record.getSalesman().getId().equals(existing.getSalesman().getId())) {
                Salesman newSalesman = salesmanService.findById(record.getSalesman().getId());
                existing.setSalesman(newSalesman);
            }
        }

        return collectionRecordRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!collectionRecordRepository.existsById(id)) {
            throw new BusinessException("催款记录不存在");
        }
        collectionRecordRepository.deleteById(id);
    }

    public BigDecimal getTotalAmount(Long salesmanId) {
        BigDecimal total = collectionRecordRepository.sumCurrentAmountBySalesman(salesmanId);
        return total != null ? total : BigDecimal.ZERO;
    }
}