-- =============================================
-- Day05 数据库迁移脚本
-- 接口安全加固 + 成员管理优化
-- =============================================

-- ----------------------------
-- 1. 成员表扩展字段（出生地、去世日期）
-- ----------------------------
ALTER TABLE `family_member` 
ADD COLUMN `birth_place` VARCHAR(255) NULL DEFAULT NULL COMMENT '出生地' AFTER `birth_date`,
ADD COLUMN `death_date` DATE NULL DEFAULT NULL COMMENT '去世日期' AFTER `birth_place`;

-- ----------------------------
-- 2. 添加索引优化查询性能
-- ----------------------------
ALTER TABLE `family_member` ADD INDEX `idx_birth_date` (`birth_date`);
ALTER TABLE `family_member` ADD INDEX `idx_gender` (`gender`);
