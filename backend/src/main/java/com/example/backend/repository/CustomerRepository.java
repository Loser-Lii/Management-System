package com.example.backend.repository;

import com.example.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 根据客户名称模糊查询
    List<Customer> findByNameContaining(String name);

    // 根据客户类型查询
    List<Customer> findByCustomerType(String customerType);

    // 根据客户等级查询
    List<Customer> findByLevel(String level);

    // 根据销售员ID查询客户
    List<Customer> findBySalesmanId(Long salesmanId);

    // 综合搜索
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.contactPerson LIKE %:keyword% OR c.phone LIKE %:keyword%")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);

    // 根据销售员ID和关键词搜索
    @Query("SELECT c FROM Customer c WHERE c.salesmanId = :salesmanId AND (c.name LIKE %:keyword% OR c.contactPerson LIKE %:keyword% OR c.phone LIKE %:keyword%)")
    List<Customer> searchBySalesmanIdAndKeyword(@Param("salesmanId") Long salesmanId, @Param("keyword") String keyword);
}
