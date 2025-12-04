package com.example.backend.service;

import com.example.backend.entity.ComplaintRecord;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ComplaintRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ComplaintRecordService {

    @Autowired
    private ComplaintRecordRepository complaintRecordRepository;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private CustomerService customerService;

    public List<ComplaintRecord> findAll() {
        return complaintRecordRepository.findAll();
    }

    public ComplaintRecord findById(Long id) {
        return complaintRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("投诉记录不存在"));
    }

    public List<ComplaintRecord> findBySalesman(Long salesmanId) {
        return complaintRecordRepository.findBySalesmanId(salesmanId);
    }

    public List<ComplaintRecord> findByStatus(String status) {
        return complaintRecordRepository.findByStatus(status);
    }

    public List<ComplaintRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return complaintRecordRepository.findByComplaintDateBetween(startDate, endDate);
    }

    public Long countBySalesman(Long salesmanId) {
        return complaintRecordRepository.countBySalesman(salesmanId);
    }

    @Transactional
    public ComplaintRecord create(ComplaintRecord record) {
        record.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        record.setCustomer(customerService.findById(record.getCustomer().getId()));
        return complaintRecordRepository.save(record);
    }

    @Transactional
    public ComplaintRecord update(Long id, ComplaintRecord record) {
        ComplaintRecord existing = findById(id);
        if (record.getSalesman() != null) existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        if (record.getCustomer() != null) existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        existing.setComplaintDate(record.getComplaintDate());
        existing.setComplaintType(record.getComplaintType());
        existing.setContent(record.getContent());
        existing.setHandler(record.getHandler());
        existing.setStatus(record.getStatus());
        existing.setResult(record.getResult());
        existing.setHandleDate(record.getHandleDate());
        existing.setRemark(record.getRemark());
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
