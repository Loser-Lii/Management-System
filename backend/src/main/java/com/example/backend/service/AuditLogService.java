package com.example.backend.service;

import com.example.backend.entity.AuditLog;
import com.example.backend.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 记录审计日志
     * @param entityName 实体名称
     * @param entityId 实体ID
     * @param operation 操作类型
     * @param operator 操作人
     * @param operatorType 操作人类型
     * @param oldValue 旧值
     * @param newValue 新值
     * @param description 描述
     */
    @Transactional
    public void log(String entityName, Long entityId, String operation, 
                   String operator, String operatorType, 
                   Object oldValue, Object newValue, String description) {
        AuditLog log = new AuditLog();
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setOperation(operation);
        log.setOperator(operator);
        log.setOperatorType(operatorType);
        log.setDescription(description);
        
        // 将对象转换为JSON字符串
        try {
            if (oldValue != null) {
                if (oldValue instanceof String) {
                    log.setOldValue((String) oldValue);
                } else {
                    log.setOldValue(objectMapper.writeValueAsString(oldValue));
                }
            }
            if (newValue != null) {
                if (newValue instanceof String) {
                    log.setNewValue((String) newValue);
                } else {
                    log.setNewValue(objectMapper.writeValueAsString(newValue));
                }
            }
        } catch (Exception e) {
            // 如果转换失败，使用toString
            if (oldValue != null) {
                log.setOldValue(oldValue.toString());
            }
            if (newValue != null) {
                log.setNewValue(newValue.toString());
            }
        }
        
        auditLogRepository.save(log);
    }

    /**
     * 查询所有审计日志
     */
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByOperationTimeDesc();
    }

    /**
     * 根据实体查询审计日志
     */
    public List<AuditLog> findByEntity(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByOperationTimeDesc(entityName, entityId);
    }

    /**
     * 根据实体类型查询审计日志
     */
    public List<AuditLog> findByEntityName(String entityName) {
        return auditLogRepository.findByEntityNameOrderByOperationTimeDesc(entityName);
    }

    /**
     * 根据操作人查询审计日志
     */
    public List<AuditLog> findByOperator(String operator) {
        return auditLogRepository.findByOperatorOrderByOperationTimeDesc(operator);
    }

    /**
     * 根据操作类型查询审计日志
     */
    public List<AuditLog> findByOperation(String operation) {
        return auditLogRepository.findByOperationOrderByOperationTimeDesc(operation);
    }

    /**
     * 根据时间范围查询审计日志
     */
    public List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByOperationTimeBetweenOrderByOperationTimeDesc(startTime, endTime);
    }

    /**
     * 根据ID查询审计日志
     */
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }
}
