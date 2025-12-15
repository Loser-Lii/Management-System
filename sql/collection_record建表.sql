SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS collection_record;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 建表：新增 customer_id 字段
CREATE TABLE collection_record (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  collection_no varchar(50) NOT NULL COMMENT '催款单号',
  
  order_no varchar(50) NOT NULL COMMENT '关联销售订单号',
  salesman_id bigint NOT NULL COMMENT '催款人',
  
  -- 【新增列】关联客户ID
  -- 这是一个冗余字段，专门为了方便查询“客户回款情况”，数据由 Trigger 保证一致性
  customer_id bigint NOT NULL COMMENT '关联客户ID (由触发器自动校验同步)',
  
  collection_date date NOT NULL COMMENT '催款日期',
  current_amount decimal(10,2) DEFAULT 0.00 COMMENT '本次回款金额',
  remaining_amount decimal(10,2) NOT NULL COMMENT '剩余待回款金额',
  
  remark varchar(500) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  
  -- 虚拟生成列：自动判断状态
  collection_status varchar(20) GENERATED ALWAYS AS (
    CASE 
      WHEN remaining_amount <= 0 THEN 'completed' 
      ELSE 'pending' 
    END
  ) VIRTUAL COMMENT '状态(自动生成)',
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_collection_no (collection_no),
  KEY idx_order (order_no),
  KEY idx_customer (customer_id), -- 加上索引，查客户回款飞快
  
  CONSTRAINT fk_col_order FOREIGN KEY (order_no) REFERENCES sales_record(order_no),
  CONSTRAINT fk_col_salesman FOREIGN KEY (salesman_id) REFERENCES salesman(id),
  CONSTRAINT fk_col_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售催款记录表';

-- 2. 触发器：自动查订单，把正确的 customer_id 填进去
DELIMITER //
CREATE TRIGGER trg_collection_auto_fill_customer
BEFORE INSERT ON collection_record
FOR EACH ROW
BEGIN
    DECLARE v_real_customer_id BIGINT;
    DECLARE v_order_status VARCHAR(20);
    
    -- 根据订单号，查出【真实的客户ID】和【订单状态】
    SELECT customer_id, status 
    INTO v_real_customer_id, v_order_status
    FROM sales_record 
    WHERE order_no = NEW.order_no;
    
    -- 校验1：关联订单必须存在
    IF v_real_customer_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '错误：关联的销售订单不存在！';
    END IF;

    -- 校验2：订单必须是已确认状态
    IF v_order_status != 'approved' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '错误：只能为【已确认】的订单添加催款记录！';
    END IF;

    -- 【核心逻辑】强制覆盖：不管 Insert 语句里 customer_id 填什么，都用查出来的真实 ID 覆盖它
    SET NEW.customer_id = v_real_customer_id;
END;
//
DELIMITER ;

-- 3. 插入原来的那批数据 (共14条)
-- 注意：我在 VALUES 里加上了 customer_id 这一列。
-- 虽然 Trigger 会自动修正它，但为了符合外键 NOT NULL 的语法检查，我这里填入了正确的对应值。

INSERT INTO collection_record 
(collection_no, order_no, salesman_id, customer_id, collection_date, current_amount, remaining_amount, remark) 
VALUES

-- =======================================================
-- 订单1：客户1 / 销售员1（一次性结清）
-- =======================================================
('COL-20250820-001', 'ORD-20250812-001', 1, 1, '2025-08-20', 21000.00, 0.00, '全额回款，订单已完成'),

-- =======================================================
-- 订单2：客户2 / 销售员1（分两次回款）
-- =======================================================
('COL-20250910-001', 'ORD-20250903-001', 1, 2, '2025-09-10', 500.00, 22000.00, '收到少量定金'),
('COL-20250925-001', 'ORD-20250903-001', 1, 2, '2025-09-25', 20000.00, 2000.00, '收到大笔款项，还剩零头'),

-- =======================================================
-- 订单3：客户7 / 销售员2（首付 + 尾款）
-- =======================================================
('COL-20250801-001', 'ORD-20250725-001', 2, 7, '2025-08-01', 12000.00, 12000.00, '收到首付款 50%'),
('COL-20250820-002', 'ORD-20250725-001', 2, 7, '2025-08-20', 12000.00, 0.00, '收到尾款，订单已完成'),

-- =======================================================
-- 订单4：客户11 / 销售员3（现场结清）
-- =======================================================
('COL-20251008-001', 'ORD-20251006-001', 3, 11, '2025-10-08', 9000.00, 0.00, '现场作业结算，现金付清'),

-- =======================================================
-- 订单5：客户12 / 销售员3（尚未回款）
-- =======================================================
('COL-20250905-001', 'ORD-20250830-001', 3, 12, '2025-09-05', 0.00, 1700.00, '等待医院财务流程审批'),

-- =======================================================
-- 订单6：客户3 / 销售员1（多次催款）
-- =======================================================
('COL-20250725-001', 'ORD-20250718-001', 1, 3, '2025-07-25', 0.00, 5800.00, '电话无人接听，未回款'),
('COL-20250805-001', 'ORD-20250718-001', 1, 3, '2025-08-05', 0.00, 5800.00, '上门催收，客户承诺月底付'),
('COL-20250828-001', 'ORD-20250718-001', 1, 3, '2025-08-28', 2900.00, 2900.00, '终于追回一半欠款'),

-- =======================================================
-- 订单7：客户4 / 销售员1（垫付结清）
-- =======================================================
('COL-20250916-001', 'ORD-20250914-001', 1, 4, '2025-09-16', 4250.00, 0.00, '博士垫付，已结清'),

-- =======================================================
-- 订单8：客户18 / 销售员5（支票结清）
-- =======================================================
('COL-20251105-001', 'ORD-20251102-001', 5, 18, '2025-11-05', 9600.00, 0.00, '黑钢支票到账，订单已完成'),

-- =======================================================
-- 订单9：客户16 / 销售员4（汇款结清）
-- =======================================================
('COL-20251022-001', 'ORD-20251018-001', 4, 16, '2025-10-22', 4500.00, 0.00, '款项已汇出，等待查收'),

-- =======================================================
-- 订单10：客户17 / 销售员5（结清）
-- =======================================================
('COL-20251210-001', 'ORD-20251205-001', 5, 17, '2025-12-10', 4200.00, 0.00, '环境监测费结清，订单已完成');
