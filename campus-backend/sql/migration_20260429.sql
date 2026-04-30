-- =====================================================
-- 数据库迁移脚本 - 2026-04-29
-- 包含5大需求的表结构变更
-- =====================================================

-- 1. sys_user 添加地址字段 (需求2: 用户地址获取)
ALTER TABLE `sys_user`
  ADD COLUMN `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度' AFTER `gender`,
  ADD COLUMN `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度' AFTER `longitude`,
  ADD COLUMN `address` VARCHAR(255) DEFAULT NULL COMMENT '结构化地址' AFTER `latitude`;

-- 2. 创建接单申请表 (需求3: 教师申请接单机制)
CREATE TABLE IF NOT EXISTS `tutor_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `demand_id` BIGINT NOT NULL COMMENT '需求ID',
  `tutor_id` BIGINT NOT NULL COMMENT '教师用户ID',
  `tutor_profile_id` BIGINT DEFAULT NULL COMMENT '教师档案ID',
  `total_hours` INT DEFAULT 10 COMMENT '计划课时数',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '申请备注',
  `status` TINYINT DEFAULT 0 COMMENT '0-待审核 1-已接受 2-已拒绝',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_tutor_id` (`tutor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师接单申请表';

-- 3. course_order 添加上课流程模式字段 (需求4: 支付与开课关联机制)
ALTER TABLE `course_order`
  ADD COLUMN `course_flow_mode` TINYINT DEFAULT 1 COMMENT '上课流程模式: 1-先支付后上课 2-先上课后支付' AFTER `paid_hours`;

-- 4. community_post 和 community_reply 表已存在于 schema.sql 中
-- 确保表存在 (如果之前被注释掉，需要重新创建)
CREATE TABLE IF NOT EXISTS `community_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '发帖用户ID',
  `topic_type` TINYINT DEFAULT 1 COMMENT '1-经验分享, 2-难题求助',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` TEXT DEFAULT NULL COMMENT '内容',
  `images` JSON DEFAULT NULL COMMENT '图片列表',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

CREATE TABLE IF NOT EXISTS `community_reply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '回复用户ID',
  `content` VARCHAR(512) DEFAULT NULL COMMENT '回复内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区评论表';
