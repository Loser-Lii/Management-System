package com.example.backend.service;

import com.example.backend.dto.CustomerDTO;
import com.example.backend.entity.Customer;
import com.example.backend.entity.Salesman;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.CustomerRepository;
import com.example.backend.repository.SalesmanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesmanRepository salesmanRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public List<CustomerDTO> findAllWithSalesman() {
        return customerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 根据销售员ID查询客户（销售员只能看到自己的客户）
    public List<Customer> findBySalesmanId(Long salesmanId) {
        return customerRepository.findBySalesmanId(salesmanId);
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));
    }

    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return customerRepository.searchByKeyword(keyword);
    }

    // 根据销售员ID和关键词搜索
    public List<Customer> searchBySalesman(Long salesmanId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findBySalesmanId(salesmanId);
        }
        return customerRepository.searchBySalesmanIdAndKeyword(salesmanId, keyword);
    }

    public List<Customer> findByType(String customerType) {
        return customerRepository.findByCustomerType(customerType);
    }

    public List<Customer> findByLevel(String level) {
        return customerRepository.findByLevel(level);
    }

    @Transactional
    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, Customer customer) {
        Customer existing = findById(id);
        existing.setName(customer.getName());
        existing.setContactPerson(customer.getContactPerson());
        existing.setPhone(customer.getPhone());
        existing.setEmail(customer.getEmail());
        existing.setAddress(customer.getAddress());
        existing.setCustomerType(customer.getCustomerType());
        existing.setLevel(customer.getLevel());
        existing.setSalesmanId(customer.getSalesmanId());
        existing.setRemark(customer.getRemark());
        return customerRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new BusinessException("客户不存在");
        }
        customerRepository.deleteById(id);
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
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
