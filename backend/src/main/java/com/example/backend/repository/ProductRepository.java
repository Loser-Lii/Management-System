package com.example.backend.repository;

import com.example.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 根据产品编号查询
    Optional<Product> findByProductNo(String productNo);

    // 根据产品名称模糊查询
    List<Product> findByNameContaining(String name);

    // 根据类别查询
    List<Product> findByCategory(String category);

    // 检查产品编号是否存在
    boolean existsByProductNo(String productNo);

    // 综合搜索
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.productNo LIKE %:keyword% OR p.category LIKE %:keyword%")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
}
