-- 社区模块升级：点赞去重 + 评论回复功能
-- 执行时间：2026-04-23

-- 1. 帖子点赞记录表（防重复点赞）
CREATE TABLE IF NOT EXISTS `community_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB COMMENT='帖子点赞记录表';

-- 2. 扩展评论表：添加回复相关字段
ALTER TABLE `community_reply`
  ADD COLUMN `root_id` bigint NOT NULL DEFAULT 0 COMMENT '一级评论ID(0=一级评论)' AFTER `post_id`,
  ADD COLUMN `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '直接回复的评论ID' AFTER `root_id`,
  ADD COLUMN `reply_to_id` bigint NOT NULL DEFAULT 0 COMMENT '被回复的评论ID' AFTER `parent_id`,
  ADD COLUMN `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复的用户ID' AFTER `reply_to_id`,
  ADD COLUMN `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `reply_to_user_id`,
  ADD COLUMN `reply_count` int NOT NULL DEFAULT 0 COMMENT '回复数(仅一级评论维护)' AFTER `like_count`,
  ADD COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-正常 3-已删除' AFTER `reply_count`;

-- 3. 添加索引
ALTER TABLE `community_reply`
  ADD INDEX `idx_root_id` (`root_id`, `status`, `create_time`),
  ADD INDEX `idx_post_root` (`post_id`, `root_id`, `status`, `create_time`);

-- 4. 评论点赞记录表
CREATE TABLE IF NOT EXISTS `community_comment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB COMMENT='评论点赞记录表';
