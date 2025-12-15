package com.example.backend.service;

import com.example.backend.entity.Product;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Product> findAll() {
        return productRepository.findAll().stream()
            .filter(this::isVisible)
            .toList();
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
        return productRepository.searchByKeyword(keyword).stream()
                .filter(this::isVisible)
                .toList();
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category).stream()
            .filter(this::isVisible)
            .toList();
    }

    // 生成产品编号（RH-开头，基于ID）
    private String generateProductNo() {
        Product latestProduct = productRepository.findFirstByOrderByIdDesc();
        long nextId = 1;
        
        if (latestProduct != null && latestProduct.getId() != null) {
            nextId = latestProduct.getId() + 1;
        }
        
        return String.format("RH-%03d", nextId);
    }

    // 获取下一个产品编号（供前端显示）
    public String getNextProductNo() {
        return generateProductNo();
    }

    @Transactional
    public Product create(Product product) {
        // 自动生成产品编号
        if (product.getProductNo() == null || product.getProductNo().isEmpty()) {
            product.setProductNo(generateProductNo());
        } else if (productRepository.existsByProductNo(product.getProductNo())) {
            throw new BusinessException("产品编号已存在");
        }
        Product result = productRepository.save(product);
        
        // 记录审计日志
        try {
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("productNo", result.getProductNo());
            productInfo.put("name", result.getName());
            productInfo.put("category", result.getCategory());
            productInfo.put("specification", result.getSpecification());
            productInfo.put("price", result.getPrice());
            productInfo.put("status", result.getStatus());
            String productJson = objectMapper.writeValueAsString(productInfo);
            String description = String.format("创建产品: %s (编号: %s, 类别: %s)", 
                result.getName(), result.getProductNo(), result.getCategory());
            auditLogService.log("Product", result.getId(), "CREATE", 
                "admin", "admin", 
                null, productJson, description);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
        
        return result;
    }

    @Transactional
    public Product update(Long id, Product product) {
        Product existing = findById(id);
        
        if (!existing.getProductNo().equals(product.getProductNo()) 
            && productRepository.existsByProductNo(product.getProductNo())) {
            throw new BusinessException("产品编号已被使用");
        }
        
        // 记录修改前的值
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        if (!existing.getProductNo().equals(product.getProductNo())) {
            oldValues.put("productNo", existing.getProductNo());
            newValues.put("productNo", product.getProductNo());
        }
        if (!existing.getName().equals(product.getName())) {
            oldValues.put("name", existing.getName());
            newValues.put("name", product.getName());
        }
        if (!java.util.Objects.equals(existing.getCategory(), product.getCategory())) {
            oldValues.put("category", existing.getCategory());
            newValues.put("category", product.getCategory());
        }
        if (!java.util.Objects.equals(existing.getSpecification(), product.getSpecification())) {
            oldValues.put("specification", existing.getSpecification());
            newValues.put("specification", product.getSpecification());
        }
        if (!java.util.Objects.equals(existing.getUnit(), product.getUnit())) {
            oldValues.put("unit", existing.getUnit());
            newValues.put("unit", product.getUnit());
        }
        if (!java.util.Objects.equals(existing.getPrice(), product.getPrice())) {
            oldValues.put("price", existing.getPrice());
            newValues.put("price", product.getPrice());
        }
        if (!java.util.Objects.equals(existing.getStatus(), product.getStatus())) {
            oldValues.put("status", existing.getStatus());
            newValues.put("status", product.getStatus());
            // 上架/下架操作特别记录
            if ("active".equals(product.getStatus())) {
                auditLogService.log("Product", id, "ACTIVATE", 
                    "admin", "admin", 
                    "inactive", "active", 
                    String.format("产品 %s 上架", product.getName()));
            } else if ("inactive".equals(product.getStatus())) {
                auditLogService.log("Product", id, "DEACTIVATE", 
                    "admin", "admin", 
                    "active", "inactive", 
                    String.format("产品 %s 下架", product.getName()));
            }
        }
        
        existing.setProductNo(product.getProductNo());
        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setSpecification(product.getSpecification());
        existing.setUnit(product.getUnit());
        existing.setPrice(product.getPrice());
        existing.setStatus(product.getStatus());
        existing.setDescription(product.getDescription());
        
        Product result = productRepository.save(existing);
        
        // 记录审计日志（除status外的修改）
        if (!oldValues.isEmpty() && !oldValues.containsKey("status") || oldValues.size() > 1) {
            try {
                String oldValuesJson = objectMapper.writeValueAsString(oldValues);
                String newValuesJson = objectMapper.writeValueAsString(newValues);
                String description = String.format("修改产品 %s 的信息: %s", 
                    result.getName(), String.join("、", newValues.keySet()));
                auditLogService.log("Product", id, "UPDATE", 
                    "admin", "admin", 
                    oldValuesJson, newValuesJson, description);
            } catch (Exception e) {
                // 日志记录失败不影响主业务
            }
        }
        
        return result;
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);

        // 改为软删除：标记删除
        product.setStatus("deleted");
        productRepository.save(product);

        // 记录日志
        try {
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("productNo", product.getProductNo());
            productInfo.put("name", product.getName());
            productInfo.put("category", product.getCategory());
            productInfo.put("status", product.getStatus());
            String productJson = objectMapper.writeValueAsString(productInfo);
            String description = String.format("标记产品删除: %s (编号: %s)", 
                product.getName(), product.getProductNo());
            auditLogService.log("Product", id, "DELETE_SOFT", 
                "admin", "admin", 
                null, productJson, description);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
    }

    private boolean isVisible(Product p) {
        String s = p.getStatus();
        return s == null || (!"deleted".equalsIgnoreCase(s) && !"discontinued".equalsIgnoreCase(s));
    }
}
