-- 销售员绩效视图：销售额/提成/订单数 + 平均满意度 + 态度投诉罚款
DROP VIEW IF EXISTS vw_salesman_performance;
CREATE VIEW vw_salesman_performance AS
SELECT
    sm.id                    AS salesman_id,
    sm.name                  AS salesman_name,
    COALESCE(SUM(sr.total_amount), 0)          AS total_sales,
    COALESCE(SUM(sr.commission_amount), 0)     AS total_commission,
    COUNT(DISTINCT sr.id)                      AS sales_count,
    -- 平均满意度（只统计有评分的服务）
    (SELECT AVG(srv.satisfaction) FROM service_record srv WHERE srv.salesman_id = sm.id AND srv.satisfaction IS NOT NULL)
        AS avg_satisfaction,
    -- 投诉统计（仅态度类）
    SUM(CASE WHEN cr.complaint_type = 'attitude' THEN 1 ELSE 0 END) AS attitude_complaints,
    SUM(CASE WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'critical' THEN 1 ELSE 0 END) AS attitude_critical,
    SUM(CASE WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'high' THEN 1 ELSE 0 END)     AS attitude_high,
    SUM(CASE WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'normal' THEN 1 ELSE 0 END)   AS attitude_normal,
    SUM(CASE WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'low' THEN 1 ELSE 0 END)      AS attitude_low,
    -- 态度罚款：Critical 2000 / High 1000 / Normal 500 / Low 200
    COALESCE(SUM(
        CASE 
          WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'critical' THEN 2000
          WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'high'     THEN 1000
          WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'normal'   THEN 500
          WHEN cr.complaint_type = 'attitude' AND LOWER(cr.severity) = 'low'      THEN 200
          ELSE 0
        END
    ), 0) AS attitude_penalty
FROM salesman sm
LEFT JOIN sales_record sr       ON sr.salesman_id = sm.id
LEFT JOIN complaint_record cr   ON cr.salesman_id = sm.id
GROUP BY sm.id, sm.name;

-- 销售漏斗视图：按销售员统计各状态订单数与金额
DROP VIEW IF EXISTS vw_sales_funnel;
CREATE VIEW vw_sales_funnel AS
SELECT
    sm.id AS salesman_id,
    sm.name AS salesman_name,
    SUM(CASE WHEN sr.status = 'draft'    THEN 1 ELSE 0 END) AS draft_count,
    SUM(CASE WHEN sr.status = 'pending'  THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN sr.status = 'approved' THEN 1 ELSE 0 END) AS approved_count,
    SUM(CASE WHEN sr.status = 'rejected' THEN 1 ELSE 0 END) AS rejected_count,
    SUM(CASE WHEN sr.status = 'pending'  THEN sr.total_amount ELSE 0 END) AS pending_amount,
    SUM(CASE WHEN sr.status = 'approved' THEN sr.total_amount ELSE 0 END) AS approved_amount
FROM salesman sm
LEFT JOIN sales_record sr ON sr.salesman_id = sm.id
GROUP BY sm.id, sm.name;

-- 客户 360 视图：基础信息 + 销售、服务满意度、投诉
DROP VIEW IF EXISTS vw_customer_360;
CREATE VIEW vw_customer_360 AS
SELECT
    c.id            AS customer_id,
    c.name          AS customer_name,
    c.salesman_id,
    (SELECT SUM(sr.total_amount) FROM sales_record sr WHERE sr.customer_id = c.id) AS total_sales,
    (SELECT COUNT(*) FROM sales_record sr WHERE sr.customer_id = c.id)             AS order_count,
    (SELECT MAX(sr.sale_date) FROM sales_record sr WHERE sr.customer_id = c.id)    AS last_sale_date,
    (SELECT AVG(srv.satisfaction) FROM service_record srv WHERE srv.customer_id = c.id AND srv.satisfaction IS NOT NULL)
        AS avg_satisfaction,
    (SELECT COUNT(*) FROM complaint_record cr WHERE cr.customer_id = c.id)         AS complaint_count,
    (SELECT MAX(cr.complaint_time) FROM complaint_record cr WHERE cr.customer_id = c.id)
        AS last_complaint_time
FROM customer c;

-- 投诉风险视图：态度类投诉明细 + 罚款金额
DROP VIEW IF EXISTS vw_complaint_attitude_risk;
CREATE VIEW vw_complaint_attitude_risk AS
SELECT
    cr.id,
    cr.complaint_no,
    cr.salesman_id,
    cr.customer_id,
    cr.related_product_id,
    cr.related_order_no,
    cr.severity,
    cr.status,
    cr.complaint_time,
    CASE 
      WHEN LOWER(cr.severity) = 'critical' THEN 2000
      WHEN LOWER(cr.severity) = 'high'     THEN 1000
      WHEN LOWER(cr.severity) = 'normal'   THEN 500
      WHEN LOWER(cr.severity) = 'low'      THEN 200
      ELSE 0
    END AS penalty
FROM complaint_record cr
WHERE cr.complaint_type = 'attitude';