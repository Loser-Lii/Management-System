package com.example.backend.service;

import com.example.backend.entity.Product;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("产品不存在"));
    }

    public Product findByProductNo(String productNo) {
        return productRepository.findByProductNo(productNo)
                .orElseThrow(() -> new BusinessException("产品不存在"));
    }

    public List<Product> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return productRepository.searchByKeyword(keyword);
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Transactional
    public Product create(Product product) {
        if (productRepository.existsByProductNo(product.getProductNo())) {
            throw new BusinessException("产品编号已存在");
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, Product product) {
        Product existing = findById(id);
        
        if (!existing.getProductNo().equals(product.getProductNo()) 
            && productRepository.existsByProductNo(product.getProductNo())) {
            throw new BusinessException("产品编号已被使用");
        }
        
        existing.setProductNo(product.getProductNo());
        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setSpecification(product.getSpecification());
        existing.setUnit(product.getUnit());
        existing.setPrice(product.getPrice());
        existing.setDescription(product.getDescription());
        
        return productRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException("产品不存在");
        }
        productRepository.deleteById(id);
    }
}
