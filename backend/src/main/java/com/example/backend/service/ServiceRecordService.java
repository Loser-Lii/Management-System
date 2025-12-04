package com.example.backend.service;

import com.example.backend.entity.ServiceRecord;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ServiceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<ServiceRecord> findAll() {
        return serviceRecordRepository.findAll();
    }

    public ServiceRecord findById(Long id) {
        return serviceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务记录不存在"));
    }

    public List<ServiceRecord> findBySalesman(Long salesmanId) {
        return serviceRecordRepository.findBySalesmanId(salesmanId);
    }

    public List<ServiceRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return serviceRecordRepository.findByServiceDateBetween(startDate, endDate);
    }

    public Double getAverageSatisfaction(Long salesmanId) {
        Double avg = serviceRecordRepository.getAverageSatisfactionBySalesman(salesmanId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public ServiceRecord create(ServiceRecord record) {
        record.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        record.setCustomer(customerService.findById(record.getCustomer().getId()));
        return serviceRecordRepository.save(record);
    }

    @Transactional
    public ServiceRecord update(Long id, ServiceRecord record) {
        ServiceRecord existing = findById(id);
        if (record.getSalesman() != null) existing.setSalesman(salesmanService.findById(record.getSalesman().getId()));
        if (record.getCustomer() != null) existing.setCustomer(customerService.findById(record.getCustomer().getId()));
        existing.setServiceDate(record.getServiceDate());
        existing.setServiceType(record.getServiceType());
        existing.setContent(record.getContent());
        existing.setSatisfactionRating(record.getSatisfactionRating());
        existing.setCustomerFeedback(record.getCustomerFeedback());
        existing.setRemark(record.getRemark());
        return serviceRecordRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!serviceRecordRepository.existsById(id)) {
            throw new BusinessException("服务记录不存在");
        }
        serviceRecordRepository.deleteById(id);
    }
}
