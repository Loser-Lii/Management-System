SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS salesman;
-- 1. 创建销售员表
CREATE TABLE salesman (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  employee_no varchar(50) NOT NULL COMMENT '工号',
  name varchar(50) NOT NULL COMMENT '姓名',
  
  -- 等级与提成
  level varchar(20) DEFAULT 'Junior' COMMENT '等级',
  commission_rate decimal(5,4) DEFAULT 0.0000 COMMENT '提成比例',
  
  -- 联系方式
  phone varchar(20) DEFAULT NULL,
  email varchar(100) DEFAULT NULL,
  wechat varchar(50) DEFAULT NULL,
  qq varchar(50) DEFAULT NULL,
  avatar varchar(255) DEFAULT NULL COMMENT '头像路径',
  
  -- 状态与时间
  status varchar(20) DEFAULT 'active',
  hire_date date DEFAULT NULL,
  resignation_date date DEFAULT NULL,
  remark varchar(500) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_no (employee_no),
  -- 约束：只能填规定的四个等级
  CONSTRAINT chk_level CHECK (level IN ('Junior', 'Intermediate', 'Senior', 'Expert'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售员信息表';

-- 2. 【高分考点】创建触发器：插入或更新前，根据等级自动算提成
DELIMITER //
CREATE TRIGGER trg_calculate_commission_insert BEFORE INSERT ON salesman
FOR EACH ROW
BEGIN
    IF NEW.level = 'Junior' THEN SET NEW.commission_rate = 0.04;
    ELSEIF NEW.level = 'Intermediate' THEN SET NEW.commission_rate = 0.05;
    ELSEIF NEW.level = 'Senior' THEN SET NEW.commission_rate = 0.06;
    ELSEIF NEW.level = 'Expert' THEN SET NEW.commission_rate = 0.08;
    END IF;
END;
//
CREATE TRIGGER trg_calculate_commission_update BEFORE UPDATE ON salesman
FOR EACH ROW
BEGIN
    IF NEW.level = 'Junior' THEN SET NEW.commission_rate = 0.04;
    ELSEIF NEW.level = 'Intermediate' THEN SET NEW.commission_rate = 0.05;
    ELSEIF NEW.level = 'Senior' THEN SET NEW.commission_rate = 0.06;
    ELSEIF NEW.level = 'Expert' THEN SET NEW.commission_rate = 0.08;
    END IF;
END;
//
DELIMITER ;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO salesman (id, create_time, email, employee_no, hire_date, level, name, phone, qq, wechat, avatar, status) VALUES
(1, '2025-12-01 14:39:00', '353830@luodedao.cn', 'EMP1764571140641', '2025-12-01', 'Senior', '凯尔希', '15671635683', '1877581648', 'Kaierxi', '/uploads/avatars/d739a568-40e1-4780-85de-e949edf26796.jpg', 'active'),
(2, '2025-12-01 16:18:11', '353543@whut.edu.cn', 'EMP1764577091001', '2025-12-01', 'Junior', '阿米娅', '13036122820', '2820205662', 'Amiya', '/uploads/avatars/a74fd197-ae3c-42c6-b147-963d3cd9700c.jpg', 'active'),
(3, '2025-12-01 16:51:57', '353854@whut.edu.cn', 'EMP1764579117792', '2025-12-01', 'Junior', '维什戴尔', '19573493808', '1346473883', 'Wiša\'del', '/uploads/avatars/3bb218c6-8939-4884-bfb1-4e569e0c7f75.jpg', 'active'),
(4, '2025-12-01 16:56:39', 'gialinhhoang497@gmail.com', 'EMP1764579399680', '2025-12-01', 'Junior', '银灰', '18815970856', '2835859791', 'Yinghui', '/uploads/avatars/1c67200c-ad5d-490c-9491-41f2af10be96.jpg', 'active'),
(5, '2025-12-01 16:58:28', 'lijinjie23@gmail.com', 'EMP1764579508812', '2025-12-01', 'Junior', '史尔特尔', '15859501183', '1299154732', 'Surtr', '/uploads/avatars/32352ede-349a-4745-a50d-14431d087e5d.jpg', 'active');