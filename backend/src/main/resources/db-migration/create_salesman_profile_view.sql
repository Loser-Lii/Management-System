-- 创建销售员档案视图
-- 聚合销售员基本信息和关联的登录账号信息

CREATE OR REPLACE VIEW view_salesman_profile AS
SELECT 
    s.id,
    s.employee_no,
    s.name,
    s.avatar,
    s.level,
    s.commission_rate,
    s.hire_date,
    s.status,
    s.phone,
    s.email,
    s.wechat,
    s.qq,
    s.remark,
    u.username AS login_username
FROM salesman s
LEFT JOIN users u ON u.salesman_id = s.id;
