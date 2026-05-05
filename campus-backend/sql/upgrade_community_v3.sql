-- =============================================
-- 社区模块 v3 升级脚本
-- 添加帖子标签(tags)字段、帖子状态(status)字段
-- 依赖: upgrade_community_v2.sql
-- 注意: 本脚本仅需执行一次，重复执行会报错（列已存在）
-- =============================================

-- 添加标签字段（逗号分隔多个标签）
ALTER TABLE community_post
    ADD COLUMN `tags` varchar(256) DEFAULT NULL COMMENT '标签，逗号分隔，如: 学习经验,考试技巧';

-- 添加帖子状态字段（1-正常, 0-已删除/隐藏）
ALTER TABLE community_post
    ADD COLUMN `status` tinyint DEFAULT '1' COMMENT '状态: 1-正常, 0-已隐藏';

-- 为状态字段添加索引
ALTER TABLE community_post
    ADD INDEX `idx_status` (`status`);
