-- 视图：销售员绩效汇总（与 PerformanceService 的 vw_salesman_performance 查询对应）
DROP VIEW IF EXISTS vw_salesman_performance;
CREATE VIEW vw_salesman_performance AS
SELECT
    s.id   AS salesman_id,
    s.name AS salesman_name,
    COALESCE(sr.total_sales, 0)        AS total_sales,
    COALESCE(sr.total_commission, 0)   AS total_commission,
    COALESCE(sr.sales_count, 0)        AS sales_count,
    COALESCE(sv.avg_satisfaction, 0)   AS avg_satisfaction,
    COALESCE(ca.attitude_complaints, 0)    AS attitude_complaints,
    COALESCE(ca.attitude_critical, 0)      AS attitude_critical,
    COALESCE(ca.attitude_high, 0)          AS attitude_high,
    COALESCE(ca.attitude_normal, 0)        AS attitude_normal,
    COALESCE(ca.attitude_low, 0)           AS attitude_low,
    COALESCE(ca.attitude_penalty, 0)       AS attitude_penalty
FROM salesman s
LEFT JOIN (
    SELECT
        salesman_id,
        SUM(CASE WHEN status = 'approved' THEN total_amount ELSE 0 END)      AS total_sales,
        SUM(CASE WHEN status = 'approved' THEN commission_amount ELSE 0 END) AS total_commission,
        SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END)                 AS sales_count
    FROM sales_record
    GROUP BY salesman_id
) sr ON sr.salesman_id = s.id
LEFT JOIN (
    SELECT salesman_id, AVG(satisfaction) AS avg_satisfaction
    FROM service_record
    WHERE satisfaction IS NOT NULL
    GROUP BY salesman_id
) sv ON sv.salesman_id = s.id
LEFT JOIN (
    SELECT
        salesman_id,
        SUM(CASE WHEN complaint_type = 'attitude' THEN 1 ELSE 0 END) AS attitude_complaints,
        SUM(CASE WHEN complaint_type = 'attitude' AND LOWER(severity) = 'critical' THEN 1 ELSE 0 END) AS attitude_critical,
        SUM(CASE WHEN complaint_type = 'attitude' AND LOWER(severity) = 'high' THEN 1 ELSE 0 END)     AS attitude_high,
        SUM(CASE WHEN complaint_type = 'attitude' AND LOWER(severity) = 'normal' THEN 1 ELSE 0 END)   AS attitude_normal,
        SUM(CASE WHEN complaint_type = 'attitude' AND LOWER(severity) = 'low' THEN 1 ELSE 0 END)      AS attitude_low,
        SUM(CASE WHEN complaint_type = 'attitude' THEN
            CASE LOWER(severity)
                WHEN 'critical' THEN 2000
                WHEN 'high' THEN 1000
                WHEN 'normal' THEN 500
                WHEN 'low' THEN 200
                ELSE 0
            END
        ELSE 0 END) AS attitude_penalty
    FROM complaint_record
    GROUP BY salesman_id
) ca ON ca.salesman_id = s.id
WHERE s.status IS NULL OR s.status = 'active';
