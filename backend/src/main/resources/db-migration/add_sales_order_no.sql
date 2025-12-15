-- 生成55条销售记录（2023年至今）
-- 执行日期: 2025-12-07
-- 注意：执行前确保已有销售员、客户、产品数据

-- 首先添加order_no字段
ALTER TABLE sales_record ADD COLUMN IF NOT EXISTS order_no VARCHAR(50) UNIQUE;

-- 为现有记录生成订单号
UPDATE sales_record SET order_no = CONCAT('O', LPAD(id, 8, '0')) WHERE order_no IS NULL;

-- 清空现有销售记录（如果需要重新生成）
-- TRUNCATE TABLE sales_record;

-- 生成55条销售记录
-- 这里提供SQL模板，需要根据实际的salesman_id, customer_id, product_id来调整

-- 示例数据生成脚本（需要根据实际数据库中的ID调整）
/*
INSERT INTO sales_record (order_no, salesman_id, customer_id, product_id, quantity, unit_price, total_amount, commission_rate, commission_amount, sale_date, create_time, update_time) VALUES
('O00000001', 1, 1, 1, 10, 1500.00, 15000.00, 0.05, 750.00, '2023-01-15', NOW(), NOW()),
('O00000002', 2, 2, 2, 5, 3200.00, 16000.00, 0.04, 640.00, '2023-02-20', NOW(), NOW()),
('O00000003', 1, 3, 3, 20, 450.00, 9000.00, 0.05, 450.00, '2023-03-10', NOW(), NOW()),
...
继续添加更多记录
*/

-- 建议：使用DataInitializer类在Java代码中生成数据会更灵活
-- 请查看backend/src/main/java/com/example/backend/config/DataInitializer.java
