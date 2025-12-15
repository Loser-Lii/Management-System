SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `users` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT,
  `username`        VARCHAR(50)  NOT NULL COMMENT '登录用户名',
  `password`        VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  `role`            VARCHAR(20)  NOT NULL COMMENT '角色: admin 或 salesman',
  `salesman_id`     BIGINT DEFAULT NULL COMMENT '关联的销售员ID, 管理员为NULL',
  `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active_token`    VARCHAR(100) DEFAULT NULL COMMENT '当前会话Token',
  `is_logged_in`    BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否已登录',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  CONSTRAINT `chk_role_value` CHECK (`role` IN ('admin', 'salesman'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

INSERT INTO `users` (id, create_time, password, role, salesman_id, update_time, username)
VALUES
  (1, '2025-12-01 14:37:31', '123', 'admin',    NULL, '2025-12-01 14:37:31', 'admin'),
  (2, '2025-12-01 14:39:00', '123', 'salesman', 1,    '2025-12-01 14:39:00', 'salesman1'),
  (3, '2025-12-01 16:18:11', '123', 'salesman', 2,    '2025-12-01 16:18:11', 'salesman2'),
  (4, '2025-12-01 16:51:57', '123', 'salesman', 3,    '2025-12-01 16:51:57', 'salesman3'),
  (5, '2025-12-01 16:56:39', '123', 'salesman', 4,    '2025-12-01 16:56:39', 'salesman4'),
  (6, '2025-12-01 16:58:28', '123', 'salesman', 5,    '2025-12-01 16:58:28', 'salesman5');