-- 为销售员表添加状态和离职日期字段
-- 执行日期: 2025-12-07

-- 添加状态字段，默认为 'active' (在职)
ALTER TABLE salesman ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'active';

-- 添加离职日期字段
ALTER TABLE salesman ADD COLUMN IF NOT EXISTS resignation_date DATE;

-- 更新所有现有销售员的状态为 'active'
UPDATE salesman SET status = 'active' WHERE status IS NULL;

-- 添加注释
COMMENT ON COLUMN salesman.status IS '状态：active(在职)/resigned(离职)';
COMMENT ON COLUMN salesman.resignation_date IS '离职日期';
