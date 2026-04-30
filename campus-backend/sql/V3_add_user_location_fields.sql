-- ============================================================
-- CampusTutor 数据库迁移脚本
-- 版本: V3__add_user_location_fields.sql
-- 日期: 2026-04-28
-- 描述: 为 sys_user 表添加地理位置和联系方式字段
-- ============================================================

-- 添加微信字段
ALTER TABLE `sys_user` 
ADD COLUMN `wechat` varchar(64) DEFAULT NULL COMMENT '微信号' AFTER `gender`;

-- 添加地区字段
ALTER TABLE `sys_user` 
ADD COLUMN `region` varchar(128) DEFAULT NULL COMMENT '所在地区' AFTER `wechat`;

-- 添加详细地址字段
ALTER TABLE `sys_user` 
ADD COLUMN `address` varchar(255) DEFAULT NULL COMMENT '详细地址' AFTER `region`;

-- 添加经度字段
ALTER TABLE `sys_user` 
ADD COLUMN `longitude` decimal(10, 7) DEFAULT NULL COMMENT '经度' AFTER `address`;

-- 添加纬度字段
ALTER TABLE `sys_user` 
ADD COLUMN `latitude` decimal(10, 7) DEFAULT NULL COMMENT '纬度' AFTER `longitude`;

-- 验证修改
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'campus_tutor_db' AND TABLE_NAME = 'sys_user'
ORDER BY ORDINAL_POSITION;
