SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS contact_record;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE contact_record (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  contact_no varchar(50) NOT NULL COMMENT '联络单号 (唯一标识)',
  
  -- 关联信息
  salesman_id bigint NOT NULL COMMENT '跟进的销售员',
  customer_id bigint NOT NULL COMMENT '跟进的客户',
  
  -- 联络细节
  contact_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '联络时间',
  contact_way varchar(20) NOT NULL COMMENT '方式: phone, wechat, visit, email, dinner',
  
  -- 沟通内容
  content varchar(500) NOT NULL COMMENT '沟通内容纪要',
  outcome varchar(20) NOT NULL COMMENT '结果: interested, thinking, rejected, no_answer, signed',
  
  -- (已移除销售漏斗字段: next_contact_time, intention_level)
  
  -- 审计与凭证
  location varchar(100) DEFAULT NULL COMMENT '定位地址',
  file_path varchar(255) DEFAULT NULL COMMENT '附件凭证',
  
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_contact_no (contact_no),
  
  -- 索引
  KEY idx_salesman_date (salesman_id, contact_date),
  KEY idx_customer (customer_id),
  
  -- 外键
  CONSTRAINT fk_contact_salesman FOREIGN KEY (salesman_id) REFERENCES salesman(id),
  CONSTRAINT fk_contact_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  
  -- 检查约束
  CONSTRAINT chk_contact_way CHECK (contact_way IN ('phone', 'wechat', 'visit', 'email', 'dinner', 'other')),
  CONSTRAINT chk_outcome CHECK (outcome IN ('signed', 'interested', 'thinking', 'rejected', 'no_answer'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联络记录表';

-- 插入测试数据 (已移除 下次跟进时间 和 意向等级 数据)
INSERT INTO contact_record (contact_no, salesman_id, customer_id, contact_date, contact_way, content, outcome, location) VALUES
('CNT-20250805-001', 1, 1, '2025-08-05 10:00:00', 'visit',   '就本季度医疗物资采购协议进行最终确认，对方对源石抑制剂的报价仍有疑虑。', 'interested', '龙门近卫局总部大楼会议室A'),
('CNT-20250912-001', 1, 2, '2025-09-12 14:30:00', 'phone',   '询问上批急救包的使用反馈。对方表示物流途中损耗率较低，很满意。', 'thinking', NULL),
('CNT-20250828-001', 2, 5, '2025-08-28 19:00:00', 'dinner',  '商务晚宴。讨论了谢拉格地区的独家代理权问题。', 'signed', '汐斯塔大酒店VIP包厢'),
('CNT-20250708-001', 1, 3, '2025-07-08 11:20:00', 'wechat',  '发了新产品宣传册过去。', 'no_answer', NULL),
('CNT-20251020-001', 3, 11,'2025-10-20 15:00:00', 'visit',   '对方嫌弃我们送货太慢，不想谈。', 'rejected', '熔岩温泉前台'),
('CNT-20250903-001', 2, 5, '2025-09-03 09:00:00', 'email',   '发送了修正后的报价单，等待回复。', 'thinking', NULL),
('CNT-20250811-001', 1, 1, '2025-08-11 10:00:00', 'visit',   '解决了报价疑虑，陈警官表示可以走签约流程。', 'signed', '龙门近卫局总部'),
('CNT-20251110-001', 4, 13,'2025-11-10 16:45:00', 'phone',   '了解康复中心扩建计划，推荐了新的骨科耗材。', 'interested', NULL);

