-- 为销售记录表添加状态和审计字段

-- 添加状态字段
ALTER TABLE sales_record ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft(草稿)、pending(待审核)、approved(已确认)、rejected(已拒绝)';

-- 添加创建人字段
ALTER TABLE sales_record ADD COLUMN created_by VARCHAR(50) COMMENT '创建人';

-- 添加审核人字段
ALTER TABLE sales_record ADD COLUMN approved_by VARCHAR(50) COMMENT '审核人';

-- 添加审核时间字段
ALTER TABLE sales_record ADD COLUMN approved_time DATETIME COMMENT '审核时间';

-- 添加拒绝原因字段
ALTER TABLE sales_record ADD COLUMN reject_reason VARCHAR(500) COMMENT '拒绝原因';

-- 为现有数据设置默认状态为已确认（避免影响历史数据统计）
UPDATE sales_record SET status = 'approved' WHERE status = 'draft';

-- 添加状态索引以提高查询性能
CREATE INDEX idx_sales_record_status ON sales_record(status);

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    action VARCHAR(20) NOT NULL COMMENT '操作类型',
    operator VARCHAR(50) NOT NULL COMMENT '操作人',
    operator_role VARCHAR(20) COMMENT '操作人角色',
    old_value TEXT COMMENT '修改前的值（JSON）',
    new_value TEXT COMMENT '修改后的值（JSON）',
    description VARCHAR(500) COMMENT '操作描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_operator (operator),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
