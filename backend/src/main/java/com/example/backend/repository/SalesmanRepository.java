package com.example.backend.repository;

import com.example.backend.entity.Salesman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 销售员数据访问层
 */
@Repository
public interface SalesmanRepository extends JpaRepository<Salesman, Long> {

    // 根据员工编号查询
    Optional<Salesman> findByEmployeeNo(String employeeNo);

    // 根据姓名模糊查询
    List<Salesman> findByNameContaining(String name);

    // 根据员工编号模糊查询
    List<Salesman> findByEmployeeNoContaining(String employeeNo);


    // 组合查询：姓名或编号模糊匹配
    @Query("SELECT s FROM Salesman s WHERE s.name LIKE %:keyword% OR s.employeeNo LIKE %:keyword%")
    List<Salesman> searchByKeyword(@Param("keyword") String keyword);

    // 检查员工编号是否已存在
    boolean existsByEmployeeNo(String employeeNo);
}
