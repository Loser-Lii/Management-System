CREATE OR REPLACE VIEW view_salesman_profile AS
SELECT 
    s.id,
    s.employee_no,
    s.name,
    s.avatar,
    
    -- 职业信息 (对应前端的职业信息栏)
    s.level,
    s.commission_rate,
    s.hire_date,
    s.status,
    
    -- 联系方式 (对应前端的联系方式表单)
    s.phone,
    s.email,
    s.wechat,
    s.qq,
    
    -- 备注
    s.remark,
    
    -- 补充信息 (通过关联 users 表获取，虽然前端暂时没展示，但后端最好备着)
    u.id AS linked_user_id,
    u.username AS login_username,
    s.create_time,
    s.update_time

FROM salesman s
LEFT JOIN users u ON u.salesman_id = s.id;