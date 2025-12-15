-- 为产品表添加状态字段
-- 执行日期: 2025-12-08

-- 添加状态字段，默认为 'on_sale' (在售)
ALTER TABLE product ADD COLUMN status VARCHAR(20) DEFAULT 'on_sale';

-- 更新所有现有产品的状态为 'on_sale'
UPDATE product SET status = 'on_sale' WHERE status IS NULL;

-- 添加注释
ALTER TABLE product MODIFY COLUMN status VARCHAR(20) DEFAULT 'on_sale' COMMENT '状态：on_sale(在售)/discontinued(停售)';
