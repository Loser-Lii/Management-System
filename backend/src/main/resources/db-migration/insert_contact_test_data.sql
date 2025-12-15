-- 插入联络记录测试数据（修正版）
-- 执行日期: 2025-12-08

-- 1. 凯尔希 上门拜访 龙门近卫局(陈)
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251205001', 1, 1, '2025-12-05 10:00:00', '上门拜访', 
'就本季度医疗物资采购协议进行最终确认，对方对源石抑制剂的报价仍有疑虑，已出示详细临床数据。会面地点：龙门近卫局总部大楼会议室A', 
'陈警官表示需要再向上级汇报', '感兴趣', 'A', '2025-12-10', NOW(), NOW());

-- 2. 阿米娅 电话回访 企鹅物流(大帝)
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251206001', 2, 2, '2025-12-06 14:30:00', '电话', 
'询问上批急救包的使用反馈。对方表示物流途中损耗率较低，很满意，但希望下次能送点赠品。', 
'整体满意，期待优惠活动', '考虑中', 'B', '2025-12-20', NOW(), NOW());

-- 3. 银灰 饭局 喀兰贸易(讯使)
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251207001', 4, 5, '2025-12-07 19:00:00', '上门拜访', 
'商务晚宴。讨论了谢拉格地区的独家代理权问题。会面地点：汐斯塔大酒店VIP包厢', 
'已达成合作意向，准备签约', '感兴趣', 'S', NULL, NOW(), NOW());

-- 4. 维什戴尔 微信联系 莱茵生命(塞雷娅)
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251208001', 3, 3, '2025-12-08 11:20:00', '微信', 
'发了新产品宣传册过去，介绍了最新的医疗设备', 
'暂未回复', '未接通', 'C', '2025-12-09', NOW(), NOW());

-- 5. 史尔特尔 上门拜访 熔岩温泉药局
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251209001', 5, 11, '2025-12-09 15:00:00', '上门拜访', 
'尝试推销新品。会面地点：熔岩温泉前台', 
'对方嫌弃送货太慢，表示暂不考虑', '已拒绝', 'C', NULL, NOW(), NOW());

-- 6. 阿米娅 邮件跟进 维克汉姆社区医院
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251210001', 2, 5, '2025-12-10 09:00:00', '邮件', 
'发送了修正后的报价单，等待回复。', 
'收到邮件，正在评估', '考虑中', 'B', '2025-12-13', NOW(), NOW());

-- 7. 凯尔希 再次拜访 龙门近卫局
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251210002', 1, 1, '2025-12-10 10:00:00', '上门拜访', 
'解决了报价疑虑，陈警官表示可以走签约流程。会面地点：龙门近卫局总部', 
'确认采购，准备签约', '感兴趣', 'S', NULL, NOW(), NOW());

-- 8. 银灰 电话 维多利亚广场
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_time, contact_method, summary, feedback, followup_result, intention_level, next_followup_date, create_time, update_time) 
VALUES ('CR20251211001', 4, 13, '2025-12-11 16:45:00', '电话', 
'了解康复中心扩建计划，推荐了新的骨科耗材。', 
'对产品感兴趣，约定实地考察', '感兴趣', 'A', '2025-12-15', NOW(), NOW());
