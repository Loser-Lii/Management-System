SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS complaint_record;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE complaint_record (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  complaint_no varchar(50) NOT NULL COMMENT '投诉单号 (CMP-yyyyMMdd-xxx)',
  
  -- 核心三要素
  customer_id bigint NOT NULL COMMENT '投诉的客户',
  salesman_id bigint NOT NULL COMMENT '负责处理的销售员',
  
  -- 关联信息
  related_order_no varchar(50) DEFAULT NULL COMMENT '关联订单号',
  related_product_id bigint DEFAULT NULL COMMENT '关联产品ID',
  
  -- 投诉定性 (已移除欺诈 fraud)
  complaint_type varchar(20) NOT NULL COMMENT '类型: quality, attitude, logistics, price',
  
  -- 严重等级 (这里的 DEFAULT 只是个占位符，实际逻辑由触发器接管)
  severity varchar(10) NOT NULL DEFAULT 'Normal' COMMENT '严重等级: Low, Normal, High, Critical',
  
  -- 详情
  content varchar(1000) NOT NULL COMMENT '投诉详情描述',
  evidence_image varchar(255) DEFAULT NULL COMMENT '图片凭证路径',
  
  -- 处理闭环
  solution varchar(1000) DEFAULT NULL COMMENT '处理方案',
  customer_feedback varchar(200) DEFAULT NULL COMMENT '客户反馈',
  
  -- 状态与时间
  status varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending, processing, resolved, closed',
  complaint_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolve_time datetime DEFAULT NULL,
  
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (id),
  UNIQUE KEY uk_complaint_no (complaint_no),
  KEY idx_salesman_status (salesman_id, status),
  
  CONSTRAINT fk_cmp_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT fk_cmp_salesman FOREIGN KEY (salesman_id) REFERENCES salesman(id),
  CONSTRAINT fk_cmp_product FOREIGN KEY (related_product_id) REFERENCES product(id),
  
  -- 约束：移除了 fraud
  CONSTRAINT chk_cmp_type CHECK (complaint_type IN ('quality', 'attitude', 'logistics', 'price')),
  CONSTRAINT chk_cmp_severity CHECK (severity IN ('Low', 'Normal', 'High', 'Critical')),
  CONSTRAINT chk_cmp_status CHECK (status IN ('pending', 'processing', 'resolved', 'closed'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户投诉记录表';

DELIMITER //
CREATE TRIGGER trg_set_default_severity_before_insert
BEFORE INSERT ON complaint_record
FOR EACH ROW
BEGIN
    -- 只有当插入语句没有指定 severity (或者填了默认值) 时，才自动判断
    -- 如果前端管理员特意选了一个等级，我们就不覆盖它(可选逻辑)，或者强制覆盖(当前逻辑)
    -- 这里我们采用：强制根据类型设置初始默认值
    
    IF NEW.complaint_type = 'quality' THEN
        SET NEW.severity = 'Critical';
    ELSEIF NEW.complaint_type = 'attitude' THEN
        SET NEW.severity = 'High';
    ELSEIF NEW.complaint_type = 'logistics' THEN
        SET NEW.severity = 'Normal';
    ELSEIF NEW.complaint_type = 'price' THEN
        SET NEW.severity = 'Low';
    END IF;
END;
//
DELIMITER ;

-- 清空旧数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE complaint_record;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入数据 (注意：此处不需要再写 severity 字段，触发器会自动生成)
INSERT INTO complaint_record 
(complaint_no, customer_id, salesman_id, related_order_no, related_product_id, complaint_type, content, solution, status, resolve_time, customer_feedback) VALUES

-- 1) 客户5 / 销售员2：质量投诉（订单 2025-07-18 之后）
('CMP-20250802-001', 5, 2, 'ORD-20250718-001', 10, 'quality',
 '购买的高能源石稳定剂在使用中出现剧烈抖动，险些引发实验室爆炸！要求立即解释！',
 '经工程部检测为批次瑕疵，已全额退款并赔偿实验室损失，召回同批次产品。',
 'resolved', '2025-08-04 10:00:00', '接受赔偿'),

-- 2) 客户13 / 销售员4：态度投诉（订单 2025-11-16 之后也合理，但你这里关联的是 2025-10-03 的订单）
('CMP-20251008-001', 13, 4, 'ORD-20251003-001', NULL, 'attitude',
 '接待人员言语傲慢，一直强调“你们买不起就别问”，严重损害品牌形象。',
 '已对当事员工进行记过处分，并由部门经理登门致歉，赠送一年VIP服务。',
 'processing', NULL, NULL),

-- 3) 客户2 / 销售员1：物流投诉（订单 2025-08-09 之后）
('CMP-20250818-001', 2, 1, 'ORD-20250809-001', 9, 'logistics',
 '收到的止血带外包装严重挤压变形，虽然内部未损坏，但观感极差。',
 '向物流合作方（企鹅物流）发起索赔，并向客户发放50元优惠券。',
 'resolved', '2025-08-19 09:00:00', '满意'),

-- 4) 客户4 / 销售员1：价格异议（关联草稿订单 2025-07-05 之后）
('CMP-20250712-001', 4, 1, 'ORD-20250705-001', 1, 'price',
 '发现隔壁药房的同款抑制剂比你们便宜 5%。',
 '向客户解释了我们的产品纯度和售后服务的差异，客户表示理解。',
 'closed', '2025-07-12 14:00:00', '勉强接受'),

-- 5) 客户11 / 销售员3：质量投诉（订单 2025-10-06 之后）
('CMP-20251022-001', 11, 3, 'ORD-20251006-001', 20, 'quality',
 '溶解剂颜色不对，怀疑过期。',
 NULL,
 'pending', NULL, NULL);

