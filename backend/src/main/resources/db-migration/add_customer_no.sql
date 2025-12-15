-- 为客户表添加编号字段
-- 执行日期: 2025-12-07

-- 添加客户编号字段（C开头的序列）
ALTER TABLE customer ADD COLUMN IF NOT EXISTS customer_no VARCHAR(50) UNIQUE;

-- 为现有客户生成编号（如果还没有的话）
-- 这个SQL需要根据数据库类型调整
-- MySQL版本：
UPDATE customer SET customer_no = CONCAT('C', LPAD(id, 6, '0')) WHERE customer_no IS NULL;
