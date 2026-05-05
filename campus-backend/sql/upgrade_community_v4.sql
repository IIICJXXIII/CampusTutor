-- =============================================
-- 社区模块 v4 升级脚本
-- 添加社区互动通知表
-- =============================================

CREATE TABLE IF NOT EXISTS `community_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `type` tinyint NOT NULL COMMENT '通知类型: 1-帖子收到新评论, 2-评论收到新回复',
  `post_id` bigint NOT NULL COMMENT '相关帖子ID',
  `reply_id` bigint DEFAULT NULL COMMENT '相关评论ID',
  `from_user_id` bigint NOT NULL COMMENT '触发通知的用户ID',
  `content_summary` varchar(200) DEFAULT NULL COMMENT '内容摘要',
  `is_read` tinyint DEFAULT '0' COMMENT '0-未读, 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区互动通知表';
