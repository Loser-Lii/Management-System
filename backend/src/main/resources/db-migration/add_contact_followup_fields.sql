-- 为联络记录表添加跟进相关字段
-- 执行日期: 2025-12-08

-- 添加跟进结果字段
ALTER TABLE contact_record ADD COLUMN followup_result VARCHAR(20) DEFAULT NULL COMMENT '跟进结果：感兴趣/考虑中/已拒绝/未接通';

-- 添加意向等级字段
ALTER TABLE contact_record ADD COLUMN intention_level VARCHAR(2) DEFAULT NULL COMMENT '意向等级：S/A/B/C';

-- 添加下次跟进日期字段
ALTER TABLE contact_record ADD COLUMN next_followup_date DATE DEFAULT NULL COMMENT '下次跟进日期';

-- 插入模拟数据（凯尔希上门拜访龙门近卫局）
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time)
SELECT 
    CONCAT('CR', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(IFNULL(MAX(CAST(SUBSTRING(contact_no, 11) AS UNSIGNED)), 0) + 1, 3, '0')) AS contact_no,
    1 AS salesman_id,
    (SELECT id FROM customer LIMIT 1) AS customer_id,
    DATE_SUB(NOW(), INTERVAL 1 DAY) AS contact_time,
    '上门拜访' AS contact_method,
    '实地考察龙门近卫局需求，讨论了医疗设备采购计划，客户对我们的高端医疗设备表现出浓厚兴趣，特别是移动医疗舱方案' AS summary,
    '客户表示非常感兴趣，需要内部讨论预算后再决定' AS feedback,
    '感兴趣' AS followup_result,
    'A' AS intention_level,
    DATE_ADD(CURDATE(), INTERVAL 3 DAY) AS next_followup_date,
    NOW() AS create_time,
    NOW() AS update_time
FROM contact_record
WHERE contact_no LIKE CONCAT('CR', DATE_FORMAT(NOW(), '%Y%m%d'), '%');

-- 插入模拟数据（阿米娅电话联系企鹅物流）
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time)
SELECT 
    CONCAT('CR', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(IFNULL(MAX(CAST(SUBSTRING(contact_no, 11) AS UNSIGNED)), 0) + 1, 3, '0')) AS contact_no,
    2 AS salesman_id,
    (SELECT id FROM customer LIMIT 1 OFFSET 1) AS customer_id,
    DATE_SUB(NOW(), INTERVAL 2 HOUR) AS contact_time,
    '电话' AS contact_method,
    '电话跟进企鹅物流的防护用品订单需求，介绍了最新的N95口罩和防护服产品线' AS summary,
    '客户表示需要再比较几家供应商的报价' AS feedback,
    '考虑中' AS followup_result,
    'B' AS intention_level,
    DATE_ADD(CURDATE(), INTERVAL 7 DAY) AS next_followup_date,
    NOW() AS create_time,
    NOW() AS update_time
FROM contact_record
WHERE contact_no LIKE CONCAT('CR', DATE_FORMAT(NOW(), '%Y%m%d'), '%');
