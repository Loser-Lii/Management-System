-- ===================================================================
-- 通用操作日志表 (audit_logs)
-- 用于记录所有实体的操作日志：销售记录、服务记录、催款记录、联络记录、投诉记录、销售员、客户、产品
-- ===================================================================

-- 1. 检查表是否已存在（如果存在则跳过创建）
-- 如果需要重建表，请手动执行: DROP TABLE IF EXISTS audit_logs;

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

-- 2. 插入一些示例数据（可选，用于测试）
INSERT INTO audit_logs 
(entity_id, entity_name, operation, operation_time, operator, operator_type, ip_address, description, old_value, new_value) 
VALUES
(1, 'SalesRecord', 'CREATE', '2025-12-12 10:30:15', 'salesman1', 'salesman', '127.0.0.1', '创建销售记录', NULL, '{"orderNo":"ORD-20251212-001","totalAmount":1000}'),
(1, 'SalesRecord', 'SUBMIT', '2025-12-12 10:35:20', 'salesman1', 'salesman', '127.0.0.1', '提交销售记录等待审核', 'draft', 'pending'),
(1, 'SalesRecord', 'APPROVE', '2025-12-12 14:20:30', 'admin', 'admin', '127.0.0.1', '审核通过销售记录', 'pending', 'approved'),
(1, 'SalesRecord', 'STOCK_SIMULATED', '2025-12-12 14:20:32', 'admin', 'system', '127.0.0.1', '模拟入库操作', NULL, '{"stockStatus":"simulated"}'),
(5, 'Customer', 'CREATE', '2025-12-10 09:15:00', 'admin', 'admin', '127.0.0.1', '创建客户', NULL, '{"name":"测试客户","contactPerson":"张三"}'),
(3, 'ServiceRecord', 'PROCESS', '2025-12-13 16:45:00', 'salesman2', 'salesman', '127.0.0.1', '处理服务记录', 'pending', 'processing'),
(3, 'ServiceRecord', 'RATE', '2025-12-13 18:30:00', 'admin', 'admin', '127.0.0.1', '客户评分，满意度: 5星', 'processing', 'completed');

-- 3. 查询验证
SELECT * FROM audit_logs ORDER BY operation_time DESC LIMIT 10;

-- 4. 常用查询示例
-- 查询某个销售记录的所有日志
-- SELECT * FROM audit_logs WHERE entity_name = 'SalesRecord' AND entity_id = 1 ORDER BY operation_time DESC;

-- 查询某个操作人的所有操作
-- SELECT * FROM audit_logs WHERE operator = 'admin' ORDER BY operation_time DESC;

-- 查询最近24小时的所有操作
-- SELECT * FROM audit_logs WHERE operation_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ORDER BY operation_time DESC;

-- 查询所有创建操作
-- SELECT * FROM audit_logs WHERE operation = 'CREATE' ORDER BY operation_time DESC;

-- 5. 统计查询
-- 统计各实体类型的操作次数
-- SELECT entity_name, COUNT(*) as operation_count FROM audit_logs GROUP BY entity_name;

-- 统计各操作类型的次数
-- SELECT operation, COUNT(*) as operation_count FROM audit_logs GROUP BY operation;

-- 统计各操作人的操作次数
-- SELECT operator, operator_type, COUNT(*) as operation_count FROM audit_logs GROUP BY operator, operator_type;
