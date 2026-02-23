-- =============================================
-- Day03 + Day03.5 数据库迁移脚本
-- 密码加密 + 用户表改造 + 用户-家谱关联 + 姓名一致性
-- =============================================

-- ----------------------------
-- 1. 用户表改造
-- ----------------------------

-- 重命名 role 为 global_role，并扩展角色类型
ALTER TABLE `user` CHANGE COLUMN `role` `global_role` VARCHAR(20) NOT NULL DEFAULT 'NORMAL_USER' 
COMMENT '全局角色: SUPER_ADMIN-超级管理员 GENEALOGY_ADMIN-家谱管理员 NORMAL_USER-普通用户';

-- 新增字段
ALTER TABLE `user` ADD COLUMN `last_login_time` DATETIME NULL COMMENT '最后登录时间' AFTER `global_role`;
ALTER TABLE `user` ADD COLUMN `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数' AFTER `last_login_time`;
ALTER TABLE `user` ADD COLUMN `lock_time` DATETIME NULL COMMENT '锁定时间' AFTER `login_fail_count`;
ALTER TABLE `user` ADD COLUMN `name` VARCHAR(50) NULL COMMENT '真实姓名（全局统一）' AFTER `phone`;

-- 添加索引
ALTER TABLE `user` ADD INDEX `idx_global_role` (`global_role`);

-- 迁移现有管理员角色
UPDATE `user` SET `global_role` = 'SUPER_ADMIN' WHERE `global_role` = 'admin';
UPDATE `user` SET `global_role` = 'NORMAL_USER' WHERE `global_role` = 'user';

-- ----------------------------
-- 2. 用户-家谱关联表（新建）
-- ----------------------------
DROP TABLE IF EXISTS `user_genealogy`;
CREATE TABLE `user_genealogy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `genealogy_id` BIGINT NOT NULL COMMENT '家谱ID',
    `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: ADMIN-家谱管理员 MEMBER-普通成员',
    `family_member_id` BIGINT NULL COMMENT '关联的家族成员ID',
    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `created_by` BIGINT NULL COMMENT '操作人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_genealogy` (`user_id`, `genealogy_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_genealogy_id` (`genealogy_id`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-家谱关联表';

-- 迁移现有家谱创建者到关联表
INSERT INTO `user_genealogy` (`user_id`, `genealogy_id`, `role`, `family_member_id`, `joined_at`, `created_by`)
SELECT f.`creator_id`, f.`id`, 'ADMIN', fm.`id`, f.`create_time`, f.`creator_id`
FROM `family` f
LEFT JOIN `family_member` fm ON fm.`family_id` = f.`id` AND fm.`user_id` = f.`creator_id`
WHERE f.`creator_id` IS NOT NULL
ON DUPLICATE KEY UPDATE `role` = 'ADMIN';

-- 更新拥有家谱管理员身份的用户全局角色
UPDATE `user` u
SET `global_role` = 'GENEALOGY_ADMIN'
WHERE EXISTS (
    SELECT 1 FROM `user_genealogy` ug
    WHERE ug.`user_id` = u.`id` AND ug.`role` = 'ADMIN'
) AND u.`global_role` = 'NORMAL_USER';

-- ----------------------------
-- 3. 姓名数据迁移（Day03.5）
-- ----------------------------

-- 将成员姓名同步到关联的用户表
UPDATE `user` u
INNER JOIN `family_member` fm ON fm.`user_id` = u.`id`
SET u.`name` = fm.`name`
WHERE u.`name` IS NULL OR u.`name` = '';
