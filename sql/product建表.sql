SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS product;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE product (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  product_no varchar(50) NOT NULL COMMENT '商品编号',
  name varchar(100) NOT NULL COMMENT '商品名称',

  price decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价 (计算业绩的基础)',

  category varchar(50) DEFAULT 'General' COMMENT '分类',
  unit varchar(20) DEFAULT '个' COMMENT '单位',
  specification varchar(50) DEFAULT NULL COMMENT '规格',
  description varchar(500) DEFAULT NULL COMMENT '描述',

  -- 状态：在售 / 下架 / 删除(软删)
  status varchar(20) NOT NULL DEFAULT 'on_sale' COMMENT '状态: on_sale(在售), discontinued(下架), deleted(删除)',

  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_product_no (product_no),
  CONSTRAINT chk_product_status CHECK (status IN ('on_sale', 'discontinued', 'deleted'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品目录表';

INSERT INTO product (id, product_no, category, name, price, specification, unit, status, description) VALUES
(1, 'RH-001', '药品', '急性源石感染抑制剂', 210.00, '10ml/支', '支', 'on_sale', NULL),
(2, 'RH-002', '药品', '广谱抗矿石病血清', 450.00, '2ml/安瓿', '支', 'on_sale', NULL),
(3, 'RH-003', '医疗设备', '便携式神经递质检测仪', 850.00, '手持数字式', '台', 'on_sale', NULL),
(4, 'RH-004', '药品', '战场应急复合止血胶', 120.00, '50ml/喷雾', '瓶', 'on_sale', NULL),
(5, 'RH-005', '防护用品', '高压环境活性过滤面罩', 85.00, '活性碳/多层', '个', 'on_sale', NULL),
(6, 'RH-006', '药品', '组织再生刺激软膏', 165.00, '120g/罐', '罐', 'on_sale', NULL),
(7, 'RH-007', '药品', '战术生理调节激素', 320.00, '5ml/针', '支', 'on_sale', NULL),
(8, 'RH-008', '医疗设备', '合金战地折叠式担架', 1100.00, '重250kg', '副', 'on_sale', NULL),
(9, 'RH-009', '防护用品', '自锁式静脉止血带', 35.00, '快速紧固型', '条', 'on_sale', NULL),
(10, 'RH-010', '药品', '高能源石能量稳定剂', 580.00, '20ml/瓶', '瓶', 'on_sale', NULL),
(11, 'RH-011', '药品', '低温创面速效冷却膏', 75.00, '60g/支', '支', 'on_sale', NULL),
(12, 'RH-012', '医疗设备', '区域环境矿石密度监测仪', 1500.00, '展开式', '台', 'on_sale', NULL),
(13, 'RH-013', '防护用品', '强光滤过型战术护目镜', 420.00, '偏振防雾', '副', 'on_sale', NULL),
(14, 'RH-014', '医疗设备', '精密便携式血液透析仪', 2400.00, '移动式', '台', 'on_sale', NULL),
(15, 'RH-015', '医疗设备', '多维生命特征监控手环', 680.00, '防水抗冲击', '个', 'on_sale', NULL),
(16, 'RH-016', '药品', '广谱镇静抗焦虑片', 45.00, '30片/盒', '盒', 'on_sale', NULL),
(17, 'RH-017', '医疗设备', '能量传导型医用骨钉', 1200.00, '钛合金/能量', '套', 'on_sale', NULL),
(18, 'RH-018', '防护用品', '源石粉尘防护全身套装', 980.00, '密封型/B级', '套', 'on_sale', NULL),
(19, 'RH-019', '医疗设备', '手术用能量手术刀刃', 240.00, '振动波式', '把', 'on_sale', NULL),
(20, 'RH-020', '药品', '活性源石结晶溶解剂', 1800.00, '5ml/支', '支', 'on_sale', NULL);