-- 从服务记录表中删除服务费用相关字段
-- 执行前请确保已备份数据

-- 删除 service_fee 字段
ALTER TABLE `service_record` DROP COLUMN IF EXISTS `service_fee`;

-- 删除 is_paid 字段（如果存在）
ALTER TABLE `service_record` DROP COLUMN IF EXISTS `is_paid`;
