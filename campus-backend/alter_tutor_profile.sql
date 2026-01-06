-- 修复 tutor_profile 表结构
-- 使其与后端 TutorProfile 实体类字段一致
-- 执行前请备份数据！

USE `campus_tutor_db`;

-- 方案1: 如果表中没有重要数据，直接删除重建
DROP TABLE IF EXISTS `tutor_profile`;

CREATE TABLE `tutor_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `real_name` varchar(32) DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号(加密存储)',
  `id_card_front_url` varchar(255) DEFAULT NULL COMMENT '身份证正面照URL',
  `id_card_back_url` varchar(255) DEFAULT NULL COMMENT '身份证背面照URL',
  `university_name` varchar(64) DEFAULT NULL COMMENT '学校名称',
  `major` varchar(64) DEFAULT NULL COMMENT '专业',
  `education` tinyint DEFAULT NULL COMMENT '学历：1本科在读 2本科毕业 3硕士在读 4硕士毕业 5博士',
  `enroll_year` int DEFAULT NULL COMMENT '入学年份',
  `student_card_url` varchar(255) DEFAULT NULL COMMENT '学生证照片URL',
  `certificate_urls` text DEFAULT NULL COMMENT '资质证书URLs(JSON数组)',
  `teach_subjects` text DEFAULT NULL COMMENT '可授科目(JSON数组)',
  `teach_grades` text DEFAULT NULL COMMENT '可授年级(JSON数组)',
  `teach_style` varchar(255) DEFAULT NULL COMMENT '教学风格',
  `introduction` text COMMENT '自我介绍',
  `expect_price` decimal(10,2) DEFAULT NULL COMMENT '期望时薪(元)',
  `can_visit` tinyint DEFAULT '1' COMMENT '可上门：0否 1是',
  `can_online` tinyint DEFAULT '1' COMMENT '可网课：0否 1是',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `cert_status` tinyint DEFAULT '0' COMMENT '认证状态：0待提交 1待审核 2已通过 3已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核拒绝原因',
  `rating` decimal(2,1) DEFAULT '5.0' COMMENT '综合评分(1-5星)',
  `order_count` int DEFAULT '0' COMMENT '完成订单数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='教员档案认证表';

-- 方案2 (可选): 如果需要保留数据，使用 ALTER TABLE 逐步修改
-- 注意：这需要更复杂的数据迁移逻辑，建议在开发阶段使用方案1
