-- ===================================================================
-- 通用操作日志表 (audit_logs)
-- 用于记录所有实体的操作日志：销售记录、服务记录、催款记录、联络记录、投诉记录、销售员、客户、产品
-- ===================================================================

-- 1. 删除旧的销售记录审核日志表（如果存在）
DROP TABLE IF EXISTS audit_logs;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  
  -- 实体信息
  entity_id BIGINT COMMENT '关联实体ID（如销售记录ID、客户ID等）',
  entity_name VARCHAR(100) NOT NULL COMMENT '实体类型：SalesRecord/ServiceRecord/CollectionRecord/ContactRecord/ComplaintRecord/Salesman/Customer/Product',
  
  -- 操作信息
  operation VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/WITHDRAW/STOCK_SIMULATED/PROCESS/RATE',
  operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  
  -- 操作人信息
  operator VARCHAR(100) COMMENT '操作人用户名',
  operator_type VARCHAR(50) COMMENT '操作人类型：admin/salesman/customer/system',
  
  -- IP地址（可选，用于安全审计）
  ip_address VARCHAR(50) COMMENT 'IP地址',
  
  -- 变更详情
  description VARCHAR(500) COMMENT '操作描述',
  old_value TEXT COMMENT '旧值（JSON格式，存储修改前的数据）',
  new_value TEXT COMMENT '新值（JSON格式，存储修改后的数据）',
  
  PRIMARY KEY (id),
  INDEX idx_entity (entity_name, entity_id),
  INDEX idx_operation_time (operation_time DESC),
  INDEX idx_operator (operator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用操作日志表';
