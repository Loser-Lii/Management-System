-- 清理 collection_record 表中的无效数据
-- 删除 customer_id = 0 或在 customer 表中不存在的记录

-- 1. 查看有多少条无效数据
SELECT COUNT(*) as invalid_count
FROM collection_record cr
WHERE cr.customer_id = 0 
   OR (cr.customer_id IS NOT NULL AND cr.customer_id NOT IN (SELECT id FROM customer));

-- 2. 删除这些无效数据
DELETE FROM collection_record
WHERE customer_id = 0 
   OR (customer_id IS NOT NULL AND customer_id NOT IN (SELECT id FROM customer));

-- 3. 验证删除后的数据
SELECT COUNT(*) as total_count FROM collection_record;
SELECT COUNT(DISTINCT customer_id) as unique_customers FROM collection_record WHERE customer_id IS NOT NULL;

-- 4. 检查现在的数据是否都有效
SELECT cr.id, cr.collection_no, cr.customer_id, c.name
FROM collection_record cr
LEFT JOIN customer c ON cr.customer_id = c.id
WHERE cr.customer_id IS NOT NULL;
