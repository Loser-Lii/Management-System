SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS customer;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE customer (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  customer_no varchar(50) NOT NULL COMMENT '客户编号',
  name varchar(100) NOT NULL COMMENT '客户名称',

  customer_type varchar(50) NOT NULL COMMENT '类型: enterprise/individual',
  level varchar(20) DEFAULT 'C' COMMENT '等级: A/B/C',

  contact_person varchar(20) DEFAULT NULL COMMENT '联系人',
  phone varchar(20) DEFAULT NULL,
  email varchar(100) DEFAULT NULL,
  address varchar(200) DEFAULT NULL,

  salesman_id bigint DEFAULT NULL COMMENT '归属销售员ID',
  remark varchar(500) DEFAULT NULL,

  status varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态: active(启用), inactive(停用/软删)',
  last_contact_time datetime DEFAULT NULL COMMENT '最近一次被联系的时间',

  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_customer_no (customer_no),

  CONSTRAINT chk_cust_level CHECK (level IN ('A', 'B', 'C')),
  CONSTRAINT chk_cust_type CHECK (customer_type IN ('enterprise', 'individual')),
  CONSTRAINT fk_customer_salesman FOREIGN KEY (salesman_id) REFERENCES salesman(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE customer;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO customer (id, customer_no, name, contact_person, customer_type, level, phone, email, address, salesman_id, remark, status, create_time, last_contact_time) VALUES
(1, 'C000001', '罗德岛总医院', '逻各斯', 'enterprise', 'A', '021-10001001', 'amiya@rhodes.example', '罗德岛港区近地', 1, '罗德岛直属合作医院', 'active', '2025-12-01 18:04:33', '2025-12-10 10:30:00'),
(2, 'C000002', '龙门第七诊所', '玛苏医生', 'enterprise', 'B', '021-20002002', 'martha@lungmen.example', '龙门第七区', 1, '社区诊所', 'active', '2025-12-01 18:04:33', '2025-12-08 14:20:00'),
(3, 'C000003', '乌萨斯东方药房', '银狼', 'enterprise', 'B', '031-30003003', 'sw@ursus.example', '乌萨斯首都市场', 1, '连锁药房', 'active', '2025-12-01 18:04:33', '2025-12-05 09:15:00'),
(4, 'C000004', '博士', '博士', 'individual', 'A', '021-88888888', 'doctor@personal.example', '罗德岛宿舍区', 1, '个人客户，长期采购医疗用品', 'active', '2025-12-01 18:04:33', '2025-12-11 20:00:00'),
(5, 'C000005', '维克汉姆社区医院', '临光', 'enterprise', 'B', '051-50005005', 'nearl@wickham.example', '维克汉姆东区', 2, '区域医院', 'active', '2025-12-01 18:04:33', '2025-12-10 09:00:00'),
(6, 'C000006', '苔原流动医疗站', '伊芙利特', 'enterprise', 'C', '061-60006006', 'ifrit@tundra.example', '苔原3号路', 2, '流动医疗点', 'active', '2025-12-01 18:04:33', '2025-12-02 16:45:00'),
(7, 'C000007', '谢拉格综合诊所', '斯卡蒂医生', 'enterprise', 'C', '071-70007007', 'skadi@kjerag.example', '谢拉格山区', 2, '兼顾兽医的综合诊所', 'active', '2025-12-01 18:04:33', '2025-12-09 11:30:00'),
(8, 'C000008', '乌尔比安', '乌尔比安', 'individual', 'A', '021-99999999', 'kaltsit@personal.example', '罗德岛医疗部', 2, '个人采购，医疗研究用', 'active', '2025-12-01 18:04:33', '2025-12-11 15:00:00'),
(9, 'C000009', '希斯洛诊所', '塞雷娅', 'enterprise', 'B', '091-90009009', 'saria@heathrow.example', '希斯洛中心区', 3, '社区医疗中心', 'active', '2025-12-01 18:04:34', '2025-12-08 11:20:00'),
(10, 'C000010', '卡西米尔医疗中心', '玛恩纳', 'enterprise', 'A', '101-10010010', 'sa@kazimierz.example', '卡西米尔老城', 3, '高端门诊', 'active', '2025-12-01 18:04:34', NULL),
(11, 'C000011', '熔岩温泉药局', '熔泉', 'enterprise', 'C', '111-11011011', 'ifrit2@lava.example', '熔岩温泉区', 3, '小型药局', 'active', '2025-12-01 18:04:34', '2025-12-06 18:00:00'),
(12, 'C000012', '华法琳', '华法琳', 'individual', 'B', '091-77777777', 'warfarin@personal.example', '罗德岛血库', 3, '个人客户，医疗耗材采购', 'active', '2025-12-01 18:04:34', '2025-12-07 23:00:00'),
(13, 'C000013', '维多利亚广场诊所', '苏苏洛', 'enterprise', 'B', '131-13013013', 'sussurro@victoria.example', '维多利亚广场', 4, '社区诊所', 'active', '2025-12-01 18:04:34', '2025-12-11 16:45:00'),
(14, 'C000014', '盖亚康复中心', '瑕光', 'enterprise', 'A', '141-14014014', 'nearl2@gaia.example', '盖亚康复公园', 4, '康复中心', 'active', '2025-12-01 18:04:34', '2025-12-09 10:00:00'),
(15, 'C000015', '山口医院', '拉普兰德', 'enterprise', 'B', '151-15015015', 'lappland@mp.example', '山口要道', 4, '山区医院', 'active', '2025-12-01 18:04:34', '2025-12-05 13:00:00'),
(16, 'C000016', '闪灵', '闪灵', 'individual', 'A', '131-66666666', 'shining@personal.example', '圣堂医疗室', 4, '个人客户，高端医疗器械', 'active', '2025-12-01 18:04:34', '2025-12-03 09:30:00'),
(17, 'C000017', '应急响应医疗点', '杜宾', 'enterprise', 'B', '171-17017017', 'amiya2@contingency.example', '应急区域', 5, '快速响应诊疗点', 'active', '2025-12-01 18:04:34', '2025-12-04 15:00:00'),
(18, 'C000018', '黑钢前哨站', '陈', 'enterprise', 'A', '181-18018018', 'chen@blacksteel.example', '黑钢要塞', 5, '军用医疗保障', 'active', '2025-12-01 18:04:34', '2025-12-10 14:00:00'),
(19, 'C000019', '第二风暴药房', '德克萨斯', 'enterprise', 'C', '191-19019019', 'texas@sstorm.example', '第二风暴区', 5, '私人药房', 'active', '2025-12-01 18:04:34', '2025-12-09 15:30:00'),
(20, 'C000020', '赫默', '赫默', 'individual', 'B', '171-55555555', 'silence@personal.example', '罗德岛研发部', 5, '个人客户，实验器材采购', 'active', '2025-12-01 18:04:34', NULL);