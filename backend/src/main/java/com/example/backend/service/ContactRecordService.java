package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ContactRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactRecordService {

    @Autowired
    private ContactRecordRepository contactRecordRepository;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private CustomerService customerService;

    public List<ContactRecord> findAll() {
        return contactRecordRepository.findAll();
    }

    public ContactRecord findById(Long id) {
        return contactRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("联络记录不存在"));
    }

    public List<ContactRecord> findBySalesman(Long salesmanId) {
        return contactRecordRepository.findBySalesmanId(salesmanId);
    }

    public List<ContactRecord> findByCustomer(Long customerId) {
        return contactRecordRepository.findByCustomerId(customerId);
    }

    public List<ContactRecord> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return contactRecordRepository.findByContactTimeBetween(startTime, endTime);
    }

    @Transactional
    public ContactRecord create(ContactRecord record) {
        record.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        record.setCustomer(customerService.findById(record.getCustomer().getId()));
        return contactRecordRepository.save(record);
    }

    @Transactional
    public ContactRecord update(Long id, ContactRecord record) {
        ContactRecord existing = findById(id);
        if (record.getSalesman() != null) existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        if (record.getCustomer() != null) existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        existing.setContactTime(record.getContactTime());
        existing.setContactMethod(record.getContactMethod());
        existing.setSummary(record.getSummary());
        existing.setFeedback(record.getFeedback());
        return contactRecordRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!contactRecordRepository.existsById(id)) {
            throw new BusinessException("联络记录不存在");
        }
        contactRecordRepository.deleteById(id);
    }
}
