-- 更新产品类别数据
-- 执行日期: 2025-12-07
-- 注意：这个脚本会将现有产品的类别更新为新的三类

-- 将"医疗耗材"更新为"防护用品"
UPDATE product SET category = '防护用品' WHERE category = '医疗耗材';

-- 将"医用试剂"更新为"药品"
UPDATE product SET category = '药品' WHERE category = '医用试剂';

-- 将"其他"类别的产品，根据实际情况手动调整为三类之一
-- UPDATE product SET category = '医疗设备' WHERE category = '其他';

-- 查看更新后的类别分布
SELECT category, COUNT(*) as count FROM product GROUP BY category;
