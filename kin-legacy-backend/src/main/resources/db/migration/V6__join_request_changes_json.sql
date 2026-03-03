-- 为 join_request 表添加 changes_json 字段，用于存储录入子嗣/追溯先祖的成员信息
ALTER TABLE family_tree.join_request 
ADD COLUMN changes_json TEXT NULL COMMENT '成员信息JSON' AFTER join_type;
