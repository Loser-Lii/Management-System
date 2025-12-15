SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS service_record;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE service_record (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  service_no varchar(50) NOT NULL COMMENT '服务单号 (SVC-xxx)',
  
  -- 执行者与对象
  salesman_id bigint NOT NULL COMMENT '执行服务人员',
  customer_id bigint NOT NULL COMMENT '客户ID',
  
  -- 关联业务
  product_id bigint DEFAULT NULL COMMENT '关联产品',
  related_order_no varchar(50) DEFAULT NULL COMMENT '关联销售单',
  
  -- 纯服务类型
  service_type varchar(20) NOT NULL COMMENT '类型: repair(维修), maintenance(保养), training(培训), consult(咨询), installation(安装)',
  
  -- 执行细节
  start_time datetime NOT NULL,
  end_time datetime DEFAULT NULL,
  content varchar(500) NOT NULL COMMENT '服务内容/故障现象',
  solution varchar(500) DEFAULT NULL COMMENT '处理结果',
  
  -- 评价 (已移除 service_fee 字段)
  satisfaction int DEFAULT NULL COMMENT '评分1-5',
  
  -- 状态
  status varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending, processing, completed, cancelled',
  
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_service_no (service_no),
  KEY idx_salesman (salesman_id),
  KEY idx_customer (customer_id),
  
  CONSTRAINT fk_svc_salesman FOREIGN KEY (salesman_id) REFERENCES salesman(id),
  CONSTRAINT fk_svc_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT fk_svc_product FOREIGN KEY (product_id) REFERENCES product(id),
  
  CONSTRAINT chk_svc_type CHECK (service_type IN ('repair', 'maintenance', 'training', 'consult', 'installation')),
  CONSTRAINT chk_svc_status CHECK (status IN ('pending', 'processing', 'completed', 'cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后服务记录表';

-- 1. 为了保证数据干净，先清空服务表
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE service_record;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. 插入纯技术服务数据 (已移除 service_fee 数据)
INSERT INTO service_record 
(service_no, salesman_id, customer_id, product_id, related_order_no, service_type, start_time, end_time, content, solution, satisfaction, status) VALUES
('SVC-20251118-001', 1, 1, 14, 'ORD-20251111-001', 'repair',
 '2025-11-18 14:00:00', '2025-11-18 18:00:00',
 '血液透析仪核心组件过热报警，影响正常透析作业',
 '更换了散热模块，并重新校准了源石供能参数，测试运行正常。', 5, 'completed'),

('SVC-20250915-001', 5, 18, 5, 'ORD-20250907-001', 'maintenance',
 '2025-09-15 10:00:00', '2025-09-15 12:00:00',
 '黑钢前哨站防护服季度例行检查',
 '检查了所有面罩的气密性和过滤层寿命，全部合格，无需更换。', 5, 'completed'),

('SVC-20250813-001', 2, 6, 9, 'ORD-20250809-001', 'training',
 '2025-08-13 13:00:00', '2025-08-13 16:00:00',
 '苔原医疗站急救人员培训：自锁式止血带的快速使用',
 '进行了现场演示和全员实操考核，确保人人会用。', 5, 'completed'),

('SVC-20251120-001', 3, 11, 12, 'ORD-20251116-001', 'repair',
 '2025-11-20 09:00:00', NULL,
 '区域监测仪读数出现异常跳动，数值不稳定',
 '初步判断是源石粉尘干扰了传感器，正在拆机清理中。', NULL, 'processing'),

('SVC-20251022-001', 1, 4, NULL, NULL, 'consult',
 '2025-10-22 20:00:00', '2025-10-22 21:00:00',
 '博士关于矿石病抑制剂下一阶段疗程的用药咨询',
 '根据最新体检报告，制定了新的理智液配方和作息建议。', 5, 'completed'),

('SVC-20250722-001', 1, 3, 10, 'ORD-20250718-001', 'installation',
 '2025-07-22 10:30:00', '2025-07-22 11:30:00',
 '上门安装高能源石能量稳定剂存放柜',
 '已固定在墙面并接通警报电源，交付钥匙。', 4, 'completed'),

('SVC-20251112-001', 4, 13, NULL, NULL, 'maintenance',
 '2025-11-12 09:00:00', NULL,
 '维多利亚广场诊所例行巡检',
 '客户临时有急诊手术，要求改期，本次服务单取消。', NULL, 'cancelled');
