-- =============================================
-- Day06 数据库修复脚本（MySQL兼容版）
-- 修复 edit_request 和 family_member 表结构
-- =============================================

-- 注意：请逐条执行，如果报错"Duplicate column name"请忽略该条

-- ==============================
-- 1. edit_request 表修复
-- ==============================

-- 添加 member_name 字段
ALTER TABLE `edit_request` 
ADD COLUMN `member_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '成员姓名' AFTER `applicant_user_id`;

-- 添加 changes_json 字段（主要缺失字段）
ALTER TABLE `edit_request` 
ADD COLUMN `changes_json` TEXT NULL DEFAULT NULL COMMENT '修改内容JSON' AFTER `member_name`;

-- 添加 reject_reason 字段
ALTER TABLE `edit_request` 
ADD COLUMN `reject_reason` VARCHAR(255) NULL DEFAULT NULL COMMENT '拒绝原因' AFTER `status`;

-- ==============================
-- 2. family_member 表修复
-- ==============================

-- 添加 birth_place 字段
ALTER TABLE `family_member` 
ADD COLUMN `birth_place` VARCHAR(255) NULL DEFAULT NULL COMMENT '出生地' AFTER `birth_date`;

-- 添加 death_date 字段
ALTER TABLE `family_member` 
ADD COLUMN `death_date` DATE NULL DEFAULT NULL COMMENT '去世日期' AFTER `birth_place`;

-- ==============================
-- 3. 添加索引
-- ==============================

ALTER TABLE `family_member` ADD INDEX `idx_birth_date` (`birth_date`);
ALTER TABLE `family_member` ADD INDEX `idx_gender` (`gender`);

-- ==============================
-- 执行完成后检查
-- ==============================
-- SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'edit_request';
-- SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'family_member';
