-- sample_seed.sql
-- 为 Management-System 项目生成测试数据
-- 假设：数据库为 MySQL，已存在表结构（由 JPA 自动建表），并且已有 5 个销售员（保留原有数据，不删除）
-- 若要执行：
-- Windows PowerShell (在项目根目录下):
-- mysql -u root -p salesman_performance_management_system < backend/src/main/resources/data/sample_seed.sql
-- 输入密码后回车即可

-- 注意：
-- 1. 本脚本只插入新数据，不会删除或修改现有销售员数据
-- 2. 如果产品/客户已存在，可能会因唯一约束报错，可忽略或先清空相关表（但保留销售员表）
-- 3. 执行前建议备份数据库

-- ====================
-- 1) 插入 20 个医疗产品（产品表为 `product`）
-- 使用 INSERT IGNORE 避免重复插入导致的错误（如果 product_no 已存在则跳过）
-- ====================

INSERT IGNORE INTO product (product_no, name, category, specification, unit, price, stock, description, status) VALUES
('RI-001', 'Amiya Serum', '药品', '10ml/支', '支', 120.00, 200, 'Rhodes Island 研发的基础治疗血清，用于急救。', '在售'),
('RI-002', 'Exusiai Injector', '器械', '一次性注射器', '个', 8.50, 1500, '轻量注射器，适配多种给药方案。', '在售'),
('RI-003', 'SilverAsh Bandage', '消耗品', '5cm x 5m', '卷', 15.00, 500, '高强度创口绷带。', '在售'),
('RI-004', 'Lungmen Antiseptic', '药品', '100ml', '瓶', 25.00, 300, '用于消毒与伤口清洁。', '在售'),
('RI-005', 'Rhodes Mask', '防护', '一次性口罩', '个', 1.50, 5000, '医用防护口罩，过滤效率高。', '在售'),
('RI-006', 'Nearl FFP', '防护', 'FFP2 型', '个', 10.00, 800, '高级呼吸防护器具。', '在售'),
('RI-007', 'Skadi Infusion', '药品', '250ml', '袋', 45.00, 400, '标配补液输注包。', '在售'),
('RI-008', 'Projekt Red Ointment', '药品', '20g', '支', 30.00, 350, '外用修复药膏。', '在售'),
('RI-009', 'Caster Gel', '消耗品', '50g', '支', 18.00, 600, '用于局部冷敷/护理。', '在售'),
('RI-010', 'Ursus Stent', '器械', '小号/中号/大号', '套', 450.00, 40, '血管支架（模型用途）。', '在售'),
('RI-011', 'BlackSteel Scalpel', '器械', '手术刀（单支）', '把', 22.00, 300, '一次性手术刀，锋利耐用。', '在售'),
('RI-012', 'Originium Test Kit', '试剂', '单次/盒 (50)', '盒', 320.00, 120, '快速检测试剂盒，用于常见感染检测。', '在售'),
('RI-013', 'Ptilopsis Serum', '药品', '5ml/支', '支', 200.00, 180, '高效型修复血清。', '在售'),
('RI-014', 'Ifrit Heater Pad', '器械', '20x30cm', '片', 12.00, 600, '急救保温垫片。', '在售'),
('RI-015', 'Texas Sutures', '器械', '可吸收缝合线', '包', 60.00, 220, '医用缝合线（可吸收）。', '在售'),
('RI-016', 'Istina Drops', '药品', '10ml', '瓶', 28.00, 250, '滴眼药水类产品。', '在售'),
('RI-017', 'Ch'en Splint', '器械', '通用夹板', '个', 35.00, 150, '骨折固定夹板。', '在售'),
('RI-018', 'Lappland Saline', '药品', '500ml', '袋', 22.00, 400, '生理盐水补液。', '在售'),
('RI-019', 'Hoshiguma Shield', '防护', '医用护臂', '套', 18.00, 500, '防护用护臂，耐磨。', '在售'),
('RI-020', 'Saria Vaccine Mock', '疫苗', '单剂', '支', 480.00, 60, '示范用疫苗（模拟）', '在售');

-- ====================
-- 2) 为 5 个已存在的销售员各创建 4 个客户（共 20 个客户）
-- 注意：本脚本假设你的销售员ID为 1,2,3,4,5，如果不是请手动调整 salesman_id
-- 使用 INSERT IGNORE 避免手机号/邮箱等唯一字段冲突
-- ====================

INSERT IGNORE INTO customer (name, contact_person, phone, email, address, customer_type, level, salesman_id, remark) VALUES
('Rhodes General Hospital', 'Amiya', '021-10001001', 'amiya@rhodes.example', 'Rhodes Island Port, Near Homeland', 'enterprise', 'A', 1, 'Rhodes Island 直属合作医院'),
('Lungmen Clinic #7', 'Doctor Martha', '021-20002002', 'martha@lungmen.example', 'Lungmen District 7', 'enterprise', 'B', 1, '社区诊所'),
('Ursus Pharmacy - Ortus', 'SilverWolf', '031-30003003', 'sw@ursus.example', 'Ursus Capital Market', 'enterprise', 'B', 1, '连锁药房'),
('Chernobog Care Center', 'Doc. Kal'tsit', '041-40004004', 'kaltsit@chernobog.example', 'Chernobog Outskirts', 'enterprise', 'A', 1, '大型疗养院'),

('Wickham Community Hospital', 'Nearl', '051-50005005', 'nearl@wickham.example', 'Wickham East', 'enterprise', 'B', 2, '区域医院'),
('Tundra Mobile Clinic', 'Ifrit', '061-60006006', 'ifrit@tundra.example', 'Tundra Route 3', 'enterprise', 'C', 2, '流动医疗点'),
('Kjerag Veterinary & Med', 'Doc. Skadi', '071-70007007', 'skadi@kjerag.example', 'Kjerag Hill', 'enterprise', 'C', 2, '兼顾兽医的综合诊所'),
('Lungmen Research Lab', 'Exusiai', '081-80008008', 'exu@lungmenlab.example', 'Lungmen Science Park', 'enterprise', 'A', 2, '科研单位'),

('Heathrow Clinic', 'Saria', '091-90009009', 'saria@heathrow.example', 'Heathrow Center', 'enterprise', 'B', 3, '社区医疗中心'),
('Kazimierz Care', 'SilverAsh', '101-10010010', 'sa@kazimierz.example', 'Kazimierz Old Town', 'enterprise', 'A', 3, '高端门诊'),
('Lava Springs Dispensary', 'Ifrit', '111-11011011', 'ifrit2@lava.example', 'Lava Springs', 'enterprise', 'C', 3, '小型药局'),
('Penguin Logistics Medical', 'Ptilopsis', '121-12012012', 'pt@penguin.example', 'Penguin Hub', 'enterprise', 'B', 3, '企业自建医务室'),

('Victoria Yard Clinic', 'Sussurro', '131-13013013', 'sussurro@victoria.example', 'Victoria Yard', 'enterprise', 'B', 4, '社区诊所'),
('Gaia Rehabilitation', 'Nearl II', '141-14014014', 'nearl2@gaia.example', 'Gaia Rehab Park', 'enterprise', 'A', 4, '康复中心'),
('Mountain Pass Hospital', 'Lappland', '151-15015015', 'lappland@mp.example', 'Mountain Pass', 'enterprise', 'B', 4, '山区医院'),
('Rhodes Field Lab', 'Saria II', '161-16016016', 'saria2@rhodesfield.example', 'Rhodes Field Station', 'enterprise', 'C', 4, '野外检测点'),

('Contingency Clinic Alpha', 'Amiya II', '171-17017017', 'amiya2@contingency.example', 'Contingency Sector', 'enterprise', 'B', 5, '快速响应诊疗点'),
('Blacksteel Outpost', 'Ch'en', '181-18018018', 'chen@blacksteel.example', 'Blacksteel Fort', 'enterprise', 'A', 5, '军用医疗保障'),
('Second Storm Pharmacy', 'Texas', '191-19019019', 'texas@sstorm.example', 'Second Storm District', 'enterprise', 'C', 5, '私人药房'),
('Southern Isle Clinic', 'Sideroca', '201-20020020', 'sideroca@si.example', 'Southern Isle Port', 'enterprise', 'B', 5, '港口医院');

-- ====================
-- 3) 生成记录：销售记录 (sales_record)、联络记录 (contact_record)、服务记录 (service_record)、催款记录 (collection_record)、投诉记录 (complaint_record)
-- 使用子查询动态获取 product_id 和 customer_id，避免硬编码ID导致的外键错误
-- ====================

-- 销售记录：使用子查询根据 product_no 和 customer.name 查找ID
INSERT INTO sales_record (salesman_id, product_id, customer_id, quantity, unit_price, total_amount, commission_rate, commission_amount, sale_date, remark)
VALUES
-- 客户 Rhodes General Hospital (salesman 1)
(1, (SELECT id FROM product WHERE product_no='RI-001'), (SELECT id FROM customer WHERE name='Rhodes General Hospital'), 10, 120.00, 1200.00, 0.04, 48.00, '2025-02-15', '首次大单'),
(1, (SELECT id FROM product WHERE product_no='RI-002'), (SELECT id FROM customer WHERE name='Rhodes General Hospital'), 50, 8.50, 425.00, 0.04, 17.00, '2025-03-01', '补货'),

-- 客户 Lungmen Clinic #7 (salesman 1)
(1, (SELECT id FROM product WHERE product_no='RI-004'), (SELECT id FROM customer WHERE name='Lungmen Clinic #7'), 30, 25.00, 750.00, 0.04, 30.00, '2025-04-07', '常规供货'),

-- 客户 Ursus Pharmacy - Ortus (salesman 1)
(1, (SELECT id FROM product WHERE product_no='RI-003'), (SELECT id FROM customer WHERE name='Ursus Pharmacy - Ortus'), 100, 15.00, 1500.00, 0.04, 60.00, '2025-05-12', '创口绷带补给'),

-- 客户 Chernobog Care Center (salesman 1)
(1, (SELECT id FROM product WHERE product_no='RI-012'), (SELECT id FROM customer WHERE name='Chernobog Care Center'), 2, 320.00, 640.00, 0.04, 25.60, '2025-06-20', '检测试剂采购'),

-- 客户 Wickham Community Hospital (salesman 2)
(2, (SELECT id FROM product WHERE product_no='RI-007'), (SELECT id FROM customer WHERE name='Wickham Community Hospital'), 20, 45.00, 900.00, 0.04, 36.00, '2025-02-22', ''),
(2, (SELECT id FROM product WHERE product_no='RI-001'), (SELECT id FROM customer WHERE name='Wickham Community Hospital'), 5, 120.00, 600.00, 0.04, 24.00, '2025-03-05', ''),

-- 客户 Tundra Mobile Clinic (salesman 2)
(2, (SELECT id FROM product WHERE product_no='RI-008'), (SELECT id FROM customer WHERE name='Tundra Mobile Clinic'), 10, 30.00, 300.00, 0.04, 12.00, '2025-04-10', ''),

-- 客户 Kjerag Veterinary & Med (salesman 2)
(2, (SELECT id FROM product WHERE product_no='RI-011'), (SELECT id FROM customer WHERE name='Kjerag Veterinary & Med'), 15, 22.00, 330.00, 0.04, 13.20, '2025-05-16', ''),

-- 客户 Lungmen Research Lab (salesman 2)
(2, (SELECT id FROM product WHERE product_no='RI-012'), (SELECT id FROM customer WHERE name='Lungmen Research Lab'), 3, 320.00, 960.00, 0.04, 38.40, '2025-06-30', ''),

-- 客户 Heathrow Clinic (salesman 3)
(3, (SELECT id FROM product WHERE product_no='RI-001'), (SELECT id FROM customer WHERE name='Heathrow Clinic'), 8, 120.00, 960.00, 0.04, 38.40, '2025-03-03', ''),

-- 客户 Kazimierz Care (salesman 3)
(3, (SELECT id FROM product WHERE product_no='RI-005'), (SELECT id FROM customer WHERE name='Kazimierz Care'), 200, 1.50, 300.00, 0.04, 12.00, '2025-04-04', ''),

-- 客户 Lava Springs Dispensary (salesman 3)
(3, (SELECT id FROM product WHERE product_no='RI-002'), (SELECT id FROM customer WHERE name='Lava Springs Dispensary'), 60, 8.50, 510.00, 0.04, 20.40, '2025-05-14', ''),

-- 客户 Penguin Logistics Medical (salesman 3)
(3, (SELECT id FROM product WHERE product_no='RI-013'), (SELECT id FROM customer WHERE name='Penguin Logistics Medical'), 4, 200.00, 800.00, 0.04, 32.00, '2025-06-18', ''),

-- 客户 Victoria Yard Clinic (salesman 4)
(4, (SELECT id FROM product WHERE product_no='RI-015'), (SELECT id FROM customer WHERE name='Victoria Yard Clinic'), 20, 60.00, 1200.00, 0.04, 48.00, '2025-03-12', ''),

-- 客户 Gaia Rehabilitation (salesman 4)
(4, (SELECT id FROM product WHERE product_no='RI-018'), (SELECT id FROM customer WHERE name='Gaia Rehabilitation'), 10, 22.00, 220.00, 0.04, 8.80, '2025-04-20', ''),

-- 客户 Mountain Pass Hospital (salesman 4)
(4, (SELECT id FROM product WHERE product_no='RI-009'), (SELECT id FROM customer WHERE name='Mountain Pass Hospital'), 30, 18.00, 540.00, 0.04, 21.60, '2025-05-25', ''),

-- 客户 Rhodes Field Lab (salesman 4)
(4, (SELECT id FROM product WHERE product_no='RI-003'), (SELECT id FROM customer WHERE name='Rhodes Field Lab'), 50, 15.00, 750.00, 0.04, 30.00, '2025-06-28', ''),

-- 客户 Contingency Clinic Alpha (salesman 5)
(5, (SELECT id FROM product WHERE product_no='RI-006'), (SELECT id FROM customer WHERE name='Contingency Clinic Alpha'), 40, 10.00, 400.00, 0.04, 16.00, '2025-02-28', ''),

-- 客户 Blacksteel Outpost (salesman 5)
(5, (SELECT id FROM product WHERE product_no='RI-017'), (SELECT id FROM customer WHERE name='Blacksteel Outpost'), 12, 35.00, 420.00, 0.04, 16.80, '2025-04-11', ''),

-- 客户 Second Storm Pharmacy (salesman 5)
(5, (SELECT id FROM product WHERE product_no='RI-014'), (SELECT id FROM customer WHERE name='Second Storm Pharmacy'), 6, 12.00, 72.00, 0.04, 2.88, '2025-05-08', ''),

-- 客户 Southern Isle Clinic (salesman 5)
(5, (SELECT id FROM product WHERE product_no='RI-020'), (SELECT id FROM customer WHERE name='Southern Isle Clinic'), 2, 480.00, 960.00, 0.04, 38.40, '2025-06-05', '高价产品示例');

-- 联络记录 (contact_record)：使用子查询动态获取customer_id
-- 联络记录 (contact_record)：使用子查询动态获取customer_id
INSERT INTO contact_record (salesman_id, customer_id, contact_time, contact_method, summary, feedback)
VALUES
(1,(SELECT id FROM customer WHERE name='Rhodes General Hospital'),'2025-02-10 10:30:00','电话','确认交货时间','要求缩短交货期'),
(1,(SELECT id FROM customer WHERE name='Rhodes General Hospital'),'2025-02-14 14:00:00','上门拜访','签订供货合同','询问后续产品'),
(1,(SELECT id FROM customer WHERE name='Lungmen Clinic #7'),'2025-03-02 09:15:00','邮件','报价发送','等待审批'),
(1,(SELECT id FROM customer WHERE name='Ursus Pharmacy - Ortus'),'2025-05-10 11:00:00','电话','复盘库存','需补货'),
(1,(SELECT id FROM customer WHERE name='Chernobog Care Center'),'2025-06-15 16:20:00','面谈','检测需求沟通','同意采购'),

(2,(SELECT id FROM customer WHERE name='Wickham Community Hospital'),'2025-02-20 10:00:00','电话','初次联络','关注到新项目'),
(2,(SELECT id FROM customer WHERE name='Wickham Community Hospital'),'2025-03-02 10:30:00','微信','确认发货','已安排物流'),
(2,(SELECT id FROM customer WHERE name='Tundra Mobile Clinic'),'2025-04-08 09:00:00','电话','产品介绍','客户感兴趣'),
(2,(SELECT id FROM customer WHERE name='Kjerag Veterinary & Med'),'2025-05-18 10:40:00','面谈','设备维护方案','讨论频次'),
(2,(SELECT id FROM customer WHERE name='Lungmen Research Lab'),'2025-07-01 08:30:00','电话','售后跟进','满意'),

(3,(SELECT id FROM customer WHERE name='Heathrow Clinic'),'2025-03-04 13:00:00','电话','价格确认','同意采购'),
(3,(SELECT id FROM customer WHERE name='Kazimierz Care'),'2025-04-05 15:15:00','邮件','促销信息','有意向'),
(3,(SELECT id FROM customer WHERE name='Lava Springs Dispensary'),'2025-05-15 09:45:00','电话','发货确认','收到货款'),
(3,(SELECT id FROM customer WHERE name='Penguin Logistics Medical'),'2025-06-20 11:30:00','微信','售后回访','需要技术支持'),

(4,(SELECT id FROM customer WHERE name='Victoria Yard Clinic'),'2025-03-15 10:20:00','电话','疗养项目沟通','希望试用'),
(4,(SELECT id FROM customer WHERE name='Gaia Rehabilitation'),'2025-04-22 14:10:00','面谈','合作方式讨论','细节待确认'),
(4,(SELECT id FROM customer WHERE name='Mountain Pass Hospital'),'2025-05-28 09:10:00','电话','补货提醒','同意'),
(4,(SELECT id FROM customer WHERE name='Rhodes Field Lab'),'2025-06-30 17:00:00','邮件','验收反馈','无异常'),

(5,(SELECT id FROM customer WHERE name='Contingency Clinic Alpha'),'2025-03-01 10:00:00','电话','初次介绍','记录需求'),
(5,(SELECT id FROM customer WHERE name='Blacksteel Outpost'),'2025-04-12 13:40:00','上门拜访','签订意向书','正在审批'),
(5,(SELECT id FROM customer WHERE name='Second Storm Pharmacy'),'2025-05-09 11:11:00','微信','小额补货','已完成'),
(5,(SELECT id FROM customer WHERE name='Southern Isle Clinic'),'2025-06-07 16:25:00','电话','高价疫苗交付沟通','需冷链支持');

-- 服务记录 (service_record)：使用子查询动态获取customer_id
-- 服务记录 (service_record)：使用子查询动态获取customer_id
INSERT INTO service_record (salesman_id, customer_id, service_date, service_type, content, satisfaction_rating, customer_feedback, remark)
VALUES
(1,(SELECT id FROM customer WHERE name='Rhodes General Hospital'),'2025-02-18','设备维护','对输液泵进行巡检',5,'非常满意','现场维护'),
(1,(SELECT id FROM customer WHERE name='Ursus Pharmacy - Ortus'),'2025-05-13','药品培训','药品使用培训 2 小时',4,'培训有帮助','培训记录存档'),
(2,(SELECT id FROM customer WHERE name='Wickham Community Hospital'),'2025-03-06','系统调试','配送系统对接',5,'对接顺利','已完成对接'),
(3,(SELECT id FROM customer WHERE name='Lava Springs Dispensary'),'2025-05-17','设备安装','安装手术器械',5,'安装完成并测试通过','安装报告'),
(4,(SELECT id FROM customer WHERE name='Victoria Yard Clinic'),'2025-03-20','康复指导','康复项目方案制定',4,'愿意试点','后续跟进'),
(5,(SELECT id FROM customer WHERE name='Southern Isle Clinic'),'2025-06-10','疫苗接种支持','提供冷链与现场协助',5,'支持充分','已出示合规文件');

-- 催款记录 (collection_record)：使用子查询动态获取customer_id和sales_record_id
-- 注意：sales_record_id 需要根据实际插入的销售记录ID来匹配，这里使用子查询
INSERT INTO collection_record (salesman_id, customer_id, sales_record_id, collection_date, collection_method, amount_due, amount_received, status, due_date, remark)
VALUES
(1,(SELECT id FROM customer WHERE name='Rhodes General Hospital'),(SELECT id FROM sales_record WHERE customer_id=(SELECT id FROM customer WHERE name='Rhodes General Hospital') AND sale_date='2025-02-15' LIMIT 1),'2025-03-20','银行转账',1200.00,1200.00,'completed','2025-03-30','已结清'),
(1,(SELECT id FROM customer WHERE name='Lungmen Clinic #7'),(SELECT id FROM sales_record WHERE customer_id=(SELECT id FROM customer WHERE name='Lungmen Clinic #7') LIMIT 1),'2025-04-10','微信',750.00,0.00,'pending','2025-04-20','等待付款'),
(3,(SELECT id FROM customer WHERE name='Lava Springs Dispensary'),(SELECT id FROM sales_record WHERE customer_id=(SELECT id FROM customer WHERE name='Lava Springs Dispensary') LIMIT 1),'2025-06-01','银行转账',510.00,510.00,'completed','2025-06-10',''),
(5,(SELECT id FROM customer WHERE name='Southern Isle Clinic'),(SELECT id FROM sales_record WHERE customer_id=(SELECT id FROM customer WHERE name='Southern Isle Clinic') LIMIT 1),'2025-06-25','线下支付',960.00,500.00,'partial','2025-07-05','部分回款，剩余460');

-- 投诉记录 (complaint_record)：使用子查询动态获取customer_id
-- 投诉记录 (complaint_record)：使用子查询动态获取customer_id
INSERT INTO complaint_record (customer_id, salesman_id, complaint_date, complaint_type, content, handler, status, result, handle_date, remark)
VALUES
((SELECT id FROM customer WHERE name='Tundra Mobile Clinic'),2,'2025-04-15','服务质量','现场服务人员迟到','经理处理中','processing','','2025-04-16','待进一步沟通'),
((SELECT id FROM customer WHERE name='Penguin Logistics Medical'),3,'2025-06-22','产品质量','试剂出现批次问题','归厂处理','resolved','更换批次并赔付','2025-06-25','客户接受解决方案');

-- 脚本结束
COMMIT;
