package com.campus.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void runMigrations() {
        log.info("[数据库迁移] 开始执行迁移检查...");

        addColumnIfNotExists("sys_user", "longitude",
                "DECIMAL(10,6) DEFAULT NULL COMMENT '经度' AFTER `gender`");
        addColumnIfNotExists("sys_user", "latitude",
                "DECIMAL(10,6) DEFAULT NULL COMMENT '纬度' AFTER `longitude`");
        addColumnIfNotExists("sys_user", "address",
                "VARCHAR(255) DEFAULT NULL COMMENT '结构化地址' AFTER `latitude`");
        addColumnIfNotExists("course_order", "course_flow_mode",
                "TINYINT DEFAULT 1 COMMENT '上课流程模式: 1-先支付后上课 2-先上课后支付' AFTER `paid_hours`");

        createTableIfNotExists("tutor_application",
                "CREATE TABLE IF NOT EXISTS `tutor_application` (" +
                        "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                        "`demand_id` BIGINT NOT NULL COMMENT '需求ID'," +
                        "`tutor_id` BIGINT NOT NULL COMMENT '教师用户ID'," +
                        "`tutor_profile_id` BIGINT DEFAULT NULL COMMENT '教师档案ID'," +
                        "`total_hours` INT DEFAULT 10 COMMENT '计划课时数'," +
                        "`remark` VARCHAR(512) DEFAULT NULL COMMENT '申请备注'," +
                        "`status` TINYINT DEFAULT 0 COMMENT '0-待审核 1-已接受 2-已拒绝'," +
                        "`reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因'," +
                        "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                        "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                        "PRIMARY KEY (`id`)," +
                        "KEY `idx_demand_id` (`demand_id`)," +
                        "KEY `idx_tutor_id` (`tutor_id`)," +
                        "KEY `idx_status` (`status`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师接单申请表'");

        createTableIfNotExists("community_post",
                "CREATE TABLE IF NOT EXISTS `community_post` (" +
                        "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                        "`user_id` BIGINT NOT NULL COMMENT '发帖用户ID'," +
                        "`topic_type` TINYINT DEFAULT 1 COMMENT '1-经验分享, 2-难题求助'," +
                        "`title` VARCHAR(128) NOT NULL COMMENT '标题'," +
                        "`content` TEXT DEFAULT NULL COMMENT '内容'," +
                        "`images` JSON DEFAULT NULL COMMENT '图片列表'," +
                        "`view_count` INT DEFAULT 0 COMMENT '浏览量'," +
                        "`like_count` INT DEFAULT 0 COMMENT '点赞量'," +
                        "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                        "PRIMARY KEY (`id`)," +
                        "KEY `idx_user` (`user_id`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表'");

        createTableIfNotExists("community_reply",
                "CREATE TABLE IF NOT EXISTS `community_reply` (" +
                        "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                        "`post_id` BIGINT NOT NULL COMMENT '帖子ID'," +
                        "`user_id` BIGINT NOT NULL COMMENT '回复用户ID'," +
                        "`content` VARCHAR(512) DEFAULT NULL COMMENT '回复内容'," +
                        "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                        "PRIMARY KEY (`id`)," +
                        "KEY `idx_post` (`post_id`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区评论表'");

        log.info("[数据库迁移] 迁移检查完成");
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class, tableName, columnName);
            if (count != null && count == 0) {
                String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnDefinition;
                jdbcTemplate.execute(sql);
                log.info("[数据库迁移] 添加列: {}.{}", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("[数据库迁移] 添加列 {}.{} 失败: {}", tableName, columnName, e.getMessage());
        }
    }

    private void createTableIfNotExists(String tableName, String createSql) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                    Integer.class, tableName);
            if (count != null && count == 0) {
                jdbcTemplate.execute(createSql);
                log.info("[数据库迁移] 创建表: {}", tableName);
            }
        } catch (Exception e) {
            log.warn("[数据库迁移] 创建表 {} 失败: {}", tableName, e.getMessage());
        }
    }
}
