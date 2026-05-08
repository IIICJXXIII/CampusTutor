-- ========================================
-- 上课流程完善 v2.0 - 数据库迁移
-- 包含：状态码迁移、新字段、课表排期、结算字段
-- ========================================

-- ========================================
-- 第一部分：TeachingRecord 状态码迁移（旧→新）
-- 旧: 0=待确认, 1=已确认, 2=申诉中
-- 新: 0=待上课, 1=上课中, 2=待确认, 3=已确认, 4=申诉中, 5=已解决, 6=已过期
-- ========================================

-- 有GPS打卡但无结课内容 → 上课中(1)
UPDATE teaching_record SET status = 1
WHERE status = 0
  AND clock_in_lat IS NOT NULL
  AND (content_summary IS NULL OR end_time IS NULL);

-- 有GPS打卡且有结课内容 → 待确认(2)
UPDATE teaching_record SET status = 2
WHERE status = 0
  AND clock_in_lat IS NOT NULL
  AND content_summary IS NOT NULL
  AND end_time IS NOT NULL;

-- 已确认(旧1) → 已确认(新3)
UPDATE teaching_record SET status = 3 WHERE status = 1;

-- 申诉中(旧2) → 申诉中(新4)
UPDATE teaching_record SET status = 4 WHERE status = 2;

-- ========================================
-- 第三部分：CourseOrder 新增字段（paid_hours 如已存在则跳过）
-- ========================================

-- 使用存储过程安全添加列（避免重复列名错误）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN tbl VARCHAR(64), IN col VARCHAR(64), IN def VARCHAR(256)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', col, ' ', def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL add_column_if_not_exists('course_order', 'paid_hours', 'INT DEFAULT 0 COMMENT ''已支付课时数'' AFTER used_hours');
CALL add_column_if_not_exists('course_order', 'confirmed_hours', 'INT DEFAULT 0 COMMENT ''已确认课时数'' AFTER paid_hours');
CALL add_column_if_not_exists('teaching_record', 'scheduled_start_time', 'DATETIME NULL COMMENT ''预定上课时间'' AFTER end_time');
CALL add_column_if_not_exists('teaching_record', 'scheduled_end_time', 'DATETIME NULL COMMENT ''预定下课时间'' AFTER scheduled_start_time');

DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- ========================================
-- 第四部分：新增 电子协议签署记录表
-- ========================================

CREATE TABLE IF NOT EXISTS `electronic_agreement` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `agreement_no` varchar(64) NOT NULL COMMENT '协议编号',
    `content` text NOT NULL COMMENT '协议正文',
    `version` int DEFAULT 1 COMMENT '协议版本',
    `parent_signed` tinyint DEFAULT 0 COMMENT '家长签署:0否1是',
    `parent_signed_time` datetime DEFAULT NULL COMMENT '家长签署时间',
    `tutor_signed` tinyint DEFAULT 0 COMMENT '教员签署:0否1是',
    `tutor_signed_time` datetime DEFAULT NULL COMMENT '教员签署时间',
    `status` tinyint DEFAULT 0 COMMENT '0-待签署, 1-已签署, 2-已失效',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order` (`order_id`),
    UNIQUE KEY `uk_agreement_no` (`agreement_no`)
) ENGINE=InnoDB COMMENT='电子协议签署记录';

-- ========================================
-- 第五部分：新增 系统通知表
-- ========================================

CREATE TABLE IF NOT EXISTS `system_notification` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '接收用户ID',
    `type` tinyint NOT NULL COMMENT '类型:1-上课提醒,2-确认提醒,3-结算通知,4-申诉通知,5-退款通知,6-系统消息',
    `title` varchar(128) NOT NULL,
    `content` varchar(512) DEFAULT NULL,
    `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
    `related_type` varchar(32) DEFAULT NULL COMMENT '关联业务类型:order/record/dispute',
    `is_read` tinyint DEFAULT 0,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_read_time` (`user_id`, `is_read`, `create_time`)
) ENGINE=InnoDB COMMENT='系统通知表';

-- ========================================
-- 第六部分：新增 课时反馈评价表
-- ========================================

CREATE TABLE IF NOT EXISTS `teaching_feedback` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `record_id` bigint NOT NULL COMMENT '关联TeachingRecord ID',
    `order_id` bigint NOT NULL COMMENT '关联订单ID',
    `from_user_id` bigint NOT NULL COMMENT '评价人ID',
    `rating` tinyint DEFAULT 5 COMMENT '评分1-5',
    `tags` varchar(255) DEFAULT NULL COMMENT '评价标签逗号分隔',
    `content` varchar(512) DEFAULT NULL COMMENT '评价内容',
    `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名:0否1是',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_user` (`record_id`, `from_user_id`),
    KEY `idx_order` (`order_id`)
) ENGINE=InnoDB COMMENT='课时反馈评价表';
