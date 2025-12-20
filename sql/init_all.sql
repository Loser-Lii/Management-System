-- 销售员业绩管理系统：数据库一键初始化脚本（建库 + 表/触发器 + 视图 + 演示数据）
-- 适用：MySQL Workbench / mysql 客户端（支持 SOURCE 命令）

-- 0) 建库（如你已手动建库，可保留但不会影响）
CREATE DATABASE IF NOT EXISTS salesman_performance_management_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE salesman_performance_management_system;

-- 1) 先删视图（避免视图依赖导致的“表被引用”问题）
DROP VIEW IF EXISTS view_salesman_profile;

-- 2) 按依赖顺序执行各建表脚本
-- 注意：以下路径按“当前脚本所在目录”为基准（建议把 init_all.sql 放在 sql/ 目录下）

SOURCE ./salesman建表.sql;
SOURCE ./users建表.sql;
SOURCE ./product建表.sql;
SOURCE ./customer建表.sql;

SOURCE ./sales_record建表.sql;
SOURCE ./collection_record建表.sql;
SOURCE ./complaint_record建表.sql;
SOURCE ./contact_record建表.sql;
SOURCE ./service_record建表.sql;

-- 日志表
SOURCE ./audit_logs建表.sql;

-- 3) 视图放最后
SOURCE ./view_salesman_profile.sql;
SOURCE ./View.sql;