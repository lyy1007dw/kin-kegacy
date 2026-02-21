-- =============================================
-- 家谱管理系统 - 数据库建表SQL
-- =============================================

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(64) NULL COMMENT '微信openid',
    `username` VARCHAR(64) NULL COMMENT '用户名(管理后台登录)',
    `password` VARCHAR(128) NULL COMMENT '密码',
    `nickname` VARCHAR(64) NULL COMMENT '昵称',
    `avatar` VARCHAR(512) NULL COMMENT '头像URL',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin-管理员 user-普通用户',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认管理员用户 (密码: 123456)
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `role`, `create_time`, `update_time`) 
VALUES (1, 'admin', '123456', '管理员', 'admin', NOW(), NOW());

-- ----------------------------
-- 2. 家谱表
-- ----------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '家谱ID',
    `name` VARCHAR(128) NOT NULL COMMENT '家谱名称',
    `code` VARCHAR(6) NOT NULL COMMENT '6位家谱码',
    `avatar` VARCHAR(512) NULL COMMENT '家谱头像',
    `description` TEXT NULL COMMENT '家谱简介',
    `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `member_count` INT NOT NULL DEFAULT 1 COMMENT '成员数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家谱表';

-- ----------------------------
-- 3. 家谱成员表
-- ----------------------------
DROP TABLE IF EXISTS `family_member`;
CREATE TABLE `family_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员ID',
    `family_id` BIGINT NOT NULL COMMENT '家谱ID',
    `user_id` BIGINT NULL COMMENT '关联用户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '姓名',
    `gender` VARCHAR(10) NOT NULL COMMENT '性别: male-男 female-女',
    `avatar` VARCHAR(512) NULL COMMENT '头像URL',
    `birth_date` DATE NULL COMMENT '出生日期',
    `bio` TEXT NULL COMMENT '个人简介',
    `is_creator` TINYINT NOT NULL DEFAULT 0 COMMENT '是否创建者: 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家谱成员表';

-- ----------------------------
-- 4. 成员关系表
-- ----------------------------
DROP TABLE IF EXISTS `member_relation`;
CREATE TABLE `member_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `family_id` BIGINT NOT NULL COMMENT '家谱ID',
    `from_member_id` BIGINT NOT NULL COMMENT '成员ID',
    `to_member_id` BIGINT NOT NULL COMMENT '关联成员ID',
    `relation_type` VARCHAR(20) NOT NULL COMMENT '关系类型: father_son-父子 mother_son-母女 husband_wife-夫妻 sibling-兄弟姐妹',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_from_member_id` (`from_member_id`),
    KEY `idx_to_member_id` (`to_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员关系表';

-- ----------------------------
-- 5. 加入申请表
-- ----------------------------
DROP TABLE IF EXISTS `join_request`;
CREATE TABLE `join_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `family_id` BIGINT NOT NULL COMMENT '家谱ID',
    `applicant_user_id` BIGINT NOT NULL COMMENT '申请人用户ID',
    `applicant_name` VARCHAR(64) NOT NULL COMMENT '申请人姓名',
    `relation_desc` VARCHAR(64) NULL COMMENT '与家谱成员的关系描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待审批 approved-已同意 rejected-已拒绝',
    `reviewer_id` BIGINT NULL COMMENT '审批人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `reviewed_at` DATETIME NULL COMMENT '审批时间',
    PRIMARY KEY (`id`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_applicant_user_id` (`applicant_user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='加入申请表';

-- ----------------------------
-- 6. 修改申请表
-- ----------------------------
DROP TABLE IF EXISTS `edit_request`;
CREATE TABLE `edit_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `family_id` BIGINT NOT NULL COMMENT '家谱ID',
    `member_id` BIGINT NOT NULL COMMENT '被修改的成员ID',
    `applicant_user_id` BIGINT NOT NULL COMMENT '申请人ID',
    `field_name` VARCHAR(32) NOT NULL COMMENT '修改字段',
    `old_value` TEXT NULL COMMENT '原值',
    `new_value` TEXT NULL COMMENT '新值',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待审批 approved-已同意 rejected-已拒绝',
    `reviewer_id` BIGINT NULL COMMENT '审批人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `reviewed_at` DATETIME NULL COMMENT '审批时间',
    PRIMARY KEY (`id`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_applicant_user_id` (`applicant_user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='修改申请表';
