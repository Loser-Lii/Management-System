SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sales_record;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sales_record (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_no varchar(50) NOT NULL COMMENT '订单编号 (业务唯一标识)',
  
  -- 核心三要素：人、货、场(客户)
  salesman_id bigint NOT NULL COMMENT '关联销售员ID',
  product_id bigint NOT NULL COMMENT '关联产品ID',
  customer_id bigint NOT NULL COMMENT '关联客户ID',
  
  -- 交易核心数据 (快照机制)
  sale_date date NOT NULL COMMENT '销售日期',
  unit_price decimal(10,2) NOT NULL COMMENT '成交单价 (快照: 记录当时的单价)',
  quantity int NOT NULL DEFAULT 1 COMMENT '销售数量',
  total_amount decimal(10,2) NOT NULL COMMENT '订单总金额 (单价x数量)',
  
  -- 提成核算 (快照机制)
  -- 【修改点】改为 decimal(5,4) 以匹配 salesman 表的精度 (如 0.0600)
  commission_rate decimal(5,4) DEFAULT 0.0000 COMMENT '当时的提成点位',
  commission_amount decimal(10,2) DEFAULT 0.00 COMMENT '计算得出的提成金额',
  
  -- 审核流程控制
  -- 【修改点】draft:草稿, pending:待审核, approved:已通过, rejected:已拒绝, withdrawn:撤回
  status varchar(20) NOT NULL DEFAULT 'draft' COMMENT '状态流转',
  
  -- 审核反馈
  reject_reason varchar(500) DEFAULT NULL COMMENT '拒绝原因 (拒绝时必填)',
  remark varchar(500) DEFAULT NULL COMMENT '订单备注',
  
  -- 审计字段
  created_by varchar(50) DEFAULT NULL COMMENT '创建人(通常是销售员自己)',
  approved_by varchar(50) DEFAULT NULL COMMENT '审核管理员',
  approved_time datetime DEFAULT NULL COMMENT '审核通过时间',
  
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no), -- 订单号必须唯一
  
  -- 索引优化：方便管理员按状态查待办，方便销售员按日期查业绩
  KEY idx_salesman_status (salesman_id, status), 
  KEY idx_status (status),
  KEY idx_sale_date (sale_date),
  
  -- 外键约束
  CONSTRAINT fk_sales_salesman FOREIGN KEY (salesman_id) REFERENCES salesman (id),
  CONSTRAINT fk_sales_product FOREIGN KEY (product_id) REFERENCES product (id),
  CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
  
  -- 【加分项】数据有效性约束
  CONSTRAINT chk_status_flow CHECK (status IN ('draft', 'pending', 'approved', 'rejected', 'withdrawn')),
  CONSTRAINT chk_quantity_valid CHECK (quantity > 0),
  CONSTRAINT chk_amount_valid CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售业绩记录表';

DELIMITER //
CREATE TRIGGER trg_check_reject_reason_before_update
BEFORE UPDATE ON sales_record
FOR EACH ROW
BEGIN
    -- 逻辑：如果是改成 'rejected' 状态，且没有填原因，就报错阻止更新
    IF NEW.status = 'rejected' AND (NEW.reject_reason IS NULL OR TRIM(NEW.reject_reason) = '') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '错误：拒绝订单时，必须填写拒绝原因！';
    END IF;
END;
//
DELIMITER ;

INSERT INTO sales_record 
(order_no, salesman_id, product_id, customer_id, sale_date, unit_price, quantity, total_amount, commission_rate, commission_amount, status, created_by, remark, reject_reason) VALUES
-- =======================================================
-- 场景 A：已批准 (Approved)
-- =======================================================
('ORD-20250812-001', 1, 1, 1, '2025-08-12', 210.00, 100, 21000.00, 0.0600, 1260.00, 'approved', 'Kaltsit', '罗德岛本月常规采购', NULL),
('ORD-20250903-001', 1, 2, 2, '2025-09-03', 450.00, 50, 22500.00, 0.0600, 1350.00, 'approved', 'Amiya', '贫民区紧急支援', NULL),
('ORD-20250725-001', 2, 4, 7, '2025-07-25', 120.00, 200, 24000.00, 0.0400, 960.00, 'approved', 'SilverAsh', '喀兰贸易专供', NULL),
('ORD-20251006-001', 3, 20, 11, '2025-10-06', 1800.00, 5, 9000.00, 0.0400, 360.00, 'approved', 'Surtr', '特殊作业需求', NULL),
('ORD-20250830-001', 3, 3, 12, '2025-08-30', 850.00, 2, 1700.00, 0.0400, 68.00, 'approved', 'Kaltsit', '血库设备更新', NULL),
('ORD-20250718-001', 1, 10, 3, '2025-07-18', 580.00, 10, 5800.00, 0.0600, 348.00, 'approved', 'W', NULL, NULL),
('ORD-20250914-001', 1, 5, 4, '2025-09-14', 85.00, 50, 4250.00, 0.0600, 255.00, 'approved', 'Amiya', '基础防护物资', NULL),
('ORD-20251102-001', 5, 7, 18, '2025-11-02', 320.00, 30, 9600.00, 0.0400, 384.00, 'approved', 'SilverAsh', '黑钢安保补给', NULL),
('ORD-20251018-001', 4, 16, 16, '2025-10-18', 45.00, 100, 4500.00, 0.0400, 180.00, 'approved', 'Kaltsit', '精神科药物储备', NULL),
('ORD-20251205-001', 5, 13, 17, '2025-12-05', 420.00, 10, 4200.00, 0.0400, 168.00, 'approved', 'Surtr', NULL, NULL),

-- =======================================================
-- 场景 B：待审核 (Pending)
-- =======================================================
('ORD-20251111-001', 1, 14, 1, '2025-11-11', 2400.00, 2, 4800.00, 0.0600, 288.00, 'pending', 'W', '急诊室加急设备', NULL),
('ORD-20250922-001', 4, 17, 15, '2025-09-22', 1200.00, 5, 6000.00, 0.0400, 240.00, 'pending', 'Kaltsit', '山口要塞骨科耗材', NULL),
('ORD-20251003-001', 5, 8, 19, '2025-10-03', 1100.00, 3, 3300.00, 0.0400, 132.00, 'pending', 'SilverAsh', '救援队储备', NULL),
('ORD-20250809-001', 2, 9, 6, '2025-08-09', 35.00, 500, 17500.00, 0.0400, 700.00, 'pending', 'Amiya', '苔原大量采购', NULL),
('ORD-20251128-001', 3, 19, 10, '2025-11-28', 240.00, 20, 4800.00, 0.0400, 192.00, 'pending', 'Surtr', '卡西米尔医疗中心', NULL),
('ORD-20250731-001', 2, 6, 8, '2025-07-31', 165.00, 10, 1650.00, 0.0400, 66.00, 'pending', 'W', NULL, NULL),
('ORD-20251212-001', 5, 15, 20, '2025-12-12', 680.00, 10, 6800.00, 0.0400, 272.00, 'pending', 'Kaltsit', '赫默实验室定制', NULL),
('ORD-20250907-001', 2, 18, 5, '2025-09-07', 980.00, 2, 1960.00, 0.0400, 78.40, 'pending', 'SilverAsh', '特殊作业', NULL),
('ORD-20251025-001', 3, 11, 9, '2025-10-25', 75.00, 20, 1500.00, 0.0400, 60.00, 'pending', 'Amiya', NULL, NULL),
('ORD-20251116-001', 4, 12, 13, '2025-11-16', 1500.00, 1, 1500.00, 0.0400, 60.00, 'pending', 'Surtr', '环境监测', NULL),

-- =======================================================
-- 场景 C：草稿 (Draft)
-- =======================================================
('ORD-20250705-001', 1, 1, 2, '2025-07-05', 210.00, 5, 1050.00, 0.0600, 63.00, 'draft', 'Kaltsit', '草稿-龙门补货', NULL),
('ORD-20250709-001', 1, 2, 3, '2025-07-09', 450.00, 2, 900.00, 0.0600, 54.00, 'draft', 'Amiya', '草稿-乌萨斯', NULL),
('ORD-20250801-001', 1, 3, 4, '2025-08-01', 850.00, 1, 850.00, 0.0600, 51.00, 'draft', 'W', '待确认型号', NULL),
('ORD-20250817-001', 2, 4, 5, '2025-08-17', 120.00, 10, 1200.00, 0.0400, 48.00, 'draft', 'SilverAsh', NULL, NULL),
('ORD-20250901-001', 2, 5, 6, '2025-09-01', 85.00, 100, 8500.00, 0.0400, 340.00, 'draft', 'Surtr', '大批量意向', NULL),
('ORD-20250918-001', 2, 6, 7, '2025-09-18', 165.00, 4, 660.00, 0.0400, 26.40, 'draft', 'Kaltsit', NULL, NULL),
('ORD-20251002-001', 2, 7, 8, '2025-10-02', 320.00, 5, 1600.00, 0.0400, 64.00, 'draft', 'Amiya', NULL, NULL),
('ORD-20251014-001', 3, 8, 9, '2025-10-14', 1100.00, 1, 1100.00, 0.0400, 44.00, 'draft', 'W', NULL, NULL),
('ORD-20251104-001', 3, 9, 10, '2025-11-04', 35.00, 20, 700.00, 0.0400, 28.00, 'draft', 'SilverAsh', NULL, NULL),
('ORD-20251109-001', 3, 10, 11, '2025-11-09', 580.00, 2, 1160.00, 0.0400, 46.40, 'draft', 'Surtr', NULL, NULL),

-- =======================================================
-- 场景 D：已拒绝 (Rejected)
-- =======================================================
('ORD-20251201-001', 4, 1, 14, '2025-12-01', 100.00, 1, 100.00, 0.0400, 4.00, 'rejected', 'Kaltsit', '特价申请', '价格严重低于成本价'),
('ORD-20250822-001', 2, 1, 5, '2025-08-22', 210.00, 10000, 2100000.00, 0.0400, 84000.00, 'rejected', 'W', '大单', '库存不足，请分批下单'),
('ORD-20250926-001', 5, 5, 18, '2025-09-26', 85.00, 100, 8500.00, 0.0400, 340.00, 'rejected', 'SilverAsh', NULL, '该客户信用额度已超'),
('ORD-20251007-001', 1, 11, 2, '2025-10-07', 75.00, 1, 75.00, 0.0600, 4.50, 'rejected', 'Surtr', NULL, '该区域物流暂停'),
('ORD-20251121-001', 1, 2, 1, '2025-11-21', 450.00, 10, 4500.00, 0.2000, 900.00, 'rejected', 'Amiya', NULL, '系统bug导致，提成比例错误'),
('ORD-20251203-001', 1, 3, 4, '2025-12-03', 850.00, 1, 850.00, 0.0600, 51.00, 'rejected', 'system', '系统拦截', '系统自动风控拦截'),
('ORD-20250811-001', 2, 4, 5, '2025-08-11', 120.00, 1, 120.00, 0.0400, 4.80, 'rejected', 'system', '重复', '重复订单'),
('ORD-20250912-001', 2, 5, 6, '2025-09-12', 85.00, 1, 85.00, 0.0400, 3.40, 'rejected', 'SilverAsh', '清仓', '产品已下架'),
('ORD-20251020-001', 2, 6, 7, '2025-10-20', 165.00, 1, 165.00, 0.0400, 6.60, 'rejected', 'Surtr', '测试', '测试数据'),
('ORD-20251215-001', 2, 7, 8, '2025-12-15', 320.00, 1, 320.00, 0.0400, 12.80, 'rejected', 'Amiya', '缺项', '信息填写不完整');


