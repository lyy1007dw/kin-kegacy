-- =============================================
-- Day04 数据库迁移脚本
-- 权限控制完善 + 操作日志 + 登录日志
-- =============================================

-- ----------------------------
-- 1. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) NULL COMMENT '操作用户名',
    `module` VARCHAR(50) NOT NULL COMMENT '模块',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作',
    `method` VARCHAR(200) NOT NULL COMMENT '请求方法',
    `request_url` VARCHAR(500) NOT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    `request_params` TEXT NULL COMMENT '请求参数',
    `response_result` TEXT NULL COMMENT '响应结果',
    `ip_address` VARCHAR(50) NULL COMMENT 'IP地址',
    `location` VARCHAR(100) NULL COMMENT '操作地点',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0失败 1成功',
    `error_msg` TEXT NULL COMMENT '错误信息',
    `duration` BIGINT NULL COMMENT '执行时长(ms)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ----------------------------
-- 2. 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `login_type` VARCHAR(20) NOT NULL COMMENT '登录类型: PASSWORD-账号密码 WECHAT-微信',
    `ip_address` VARCHAR(50) NULL COMMENT 'IP地址',
    `location` VARCHAR(100) NULL COMMENT '登录地点',
    `device` VARCHAR(100) NULL COMMENT '设备信息',
    `user_agent` VARCHAR(500) NULL COMMENT '浏览器User-Agent',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0失败 1成功',
    `error_msg` VARCHAR(255) NULL COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ----------------------------
-- 3. 索引优化（可选，提升查询性能）
-- ----------------------------
ALTER TABLE `user_genealogy` ADD INDEX `idx_role` (`role`);
