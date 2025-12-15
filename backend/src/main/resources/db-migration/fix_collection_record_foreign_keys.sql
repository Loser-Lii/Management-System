-- ===================================================================
-- 修复 collection_record 表的外键约束问题
-- ===================================================================

-- 1. 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 删除有问题的表
DROP TABLE IF EXISTS collection_record;

-- 3. 重新创建 collection_record 表（与实体匹配）
CREATE TABLE collection_record (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  collection_no varchar(50) NOT NULL COMMENT '催款单号',
  order_no varchar(50) NOT NULL COMMENT '关联销售订单号',
  
  -- 核心关联（使用 ID 作为外键）
  customer_id bigint NOT NULL COMMENT '客户ID',
  sales_record_id bigint DEFAULT NULL COMMENT '销售记录ID',
  salesman_id bigint NOT NULL COMMENT '销售员ID',
  
  -- 业务数据
  collection_date date NOT NULL COMMENT '催款日期',
  amount_due decimal(10,2) DEFAULT NULL COMMENT '应收金额',
  amount_received decimal(10,2) DEFAULT NULL COMMENT '已收金额',
  current_amount decimal(10,2) DEFAULT 0.00 COMMENT '本次回款金额',
  remaining_amount decimal(10,2) NOT NULL COMMENT '剩余待回款金额',
  collection_status varchar(20) DEFAULT 'pending' COMMENT '状态: completed/pending',
  collection_method varchar(20) DEFAULT NULL COMMENT '回款方式',
  due_date date DEFAULT NULL COMMENT '应收日期',
  
  remark varchar(500) DEFAULT NULL,
  create_time datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  update_time datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_collection_no (collection_no),
  KEY idx_order (order_no),
  KEY FK4wx6yrl2w0w5qo0o95kijae08 (customer_id),
  KEY FKe2j3a9l59kaseb4284oa0a7vs (sales_record_id),
  KEY FKh0qbiuo09ms1p3q9km9w47k4m (salesman_id),
  
  -- 【正确的外键约束】使用 ID 作为外键
  CONSTRAINT FK4wx6yrl2w0w5qo0o95kijae08 FOREIGN KEY (customer_id) REFERENCES customer (id),
  CONSTRAINT FKe2j3a9l59kaseb4284oa0a7vs FOREIGN KEY (sales_record_id) REFERENCES sales_record (id),
  CONSTRAINT FKh0qbiuo09ms1p3q9km9w47k4m FOREIGN KEY (salesman_id) REFERENCES salesman (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='销售催款记录表';

-- 4. 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
