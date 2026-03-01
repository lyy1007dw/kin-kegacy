-- =============================================
-- Day06 数据库迁移脚本（修复版）
-- 成员详情扩展 + 申请修改审批
-- =============================================

-- ----------------------------
-- 1. 添加缺失的新字段（MySQL版本）
-- 注意：IF NOT EXISTS 在 MySQL 8.0+ 可用
-- ----------------------------
-- 添加 member_name 字段（如果不存在）
ALTER TABLE `edit_request` 
ADD COLUMN `member_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '成员姓名' AFTER `applicant_user_id`;

-- 添加 changes_json 字段（如果不存在）
ALTER TABLE `edit_request` 
ADD COLUMN `changes_json` TEXT NULL DEFAULT NULL COMMENT '修改内容JSON' AFTER `member_name`;

-- 添加 reject_reason 字段（如果不存在）
ALTER TABLE `edit_request` 
ADD COLUMN `reject_reason` VARCHAR(255) NULL DEFAULT NULL COMMENT '拒绝原因' AFTER `status`;
