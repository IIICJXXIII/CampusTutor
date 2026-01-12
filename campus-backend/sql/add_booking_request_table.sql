-- Add booking_request table
use campus_tutor_db;
CREATE TABLE `booking_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL COMMENT '家长用户ID',
  `tutor_id` bigint NOT NULL COMMENT '教师用户ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `subject` varchar(50) NOT NULL COMMENT '科目',
  `grade` varchar(50) NOT NULL COMMENT '年级',
  `booking_date` datetime NOT NULL COMMENT '预约日期',
  `start_time` varchar(10) NOT NULL COMMENT '开始时间',
  `end_time` varchar(10) NOT NULL COMMENT '结束时间',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待教师确认, 1-教师已确认, 2-教师已拒绝, 3-家长已取消',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_tutor` (`tutor_id`)
) ENGINE=InnoDB COMMENT='预约请求表';