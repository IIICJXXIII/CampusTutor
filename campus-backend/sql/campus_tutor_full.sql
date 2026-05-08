/*
 * CampusTutor 校园智教平台 - 完整数据库初始化脚本（整合版）
 * Version: 3.0.0
 * Date: 2026-05-01
 * Description:
 *   整合了 schema.sql + data.sql + 社区扩展 + 协同过滤缓存 + RAG基础表
 *   直接运行此文件即可完成所有建表和数据初始化
 *
 * 整合来源：
 *   - schema.sql                   核心DDL（含所有业务表）
 *   - upgrade_community_v2.sql     社区扩展（点赞去重 + 评论回复）
 *   - V2__add_user_similarity_cache.sql  协同过滤缓存表
 *   - final_rag_setup.sql          RAG知识库/Prompt模板/用户画像表
 *   - data.sql                     基础演示数据
 *
 * 密码统一为: test123456 (MD5: 47ec2dd791e31e2ef2076caf64ed9b3d)
 */

-- ========================================
-- 0. 数据库初始化
-- ========================================
DROP DATABASE IF EXISTS `campus_tutor_db`;
CREATE DATABASE `campus_tutor_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `campus_tutor_db`;


-- ========================================
-- 1. 用户与权限模块 (User & Auth)
-- ========================================

-- 1.1 系统用户表 (所有角色的登录凭证)
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '用户名/手机号',
  `password` varchar(128) NOT NULL COMMENT '加密密码',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT NULL COMMENT '性别: 1-男, 2-女',
  `role` tinyint NOT NULL COMMENT '角色: 0-管理员, 1-教员, 2-家长',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-正常, 0-禁用',
  `wechat` varchar(64) DEFAULT NULL COMMENT '微信号',
  `region` varchar(128) DEFAULT NULL COMMENT '所在区域',
  `address` varchar(256) DEFAULT NULL COMMENT '详细地址',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='系统用户表';

-- 1.2 钱包表 (资金托管与结算)
CREATE TABLE `sys_wallet` (
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '可用余额',
  `frozen_amount` decimal(10,2) DEFAULT '0.00' COMMENT '冻结金额(担保交易中)',
  `pay_password` varchar(128) DEFAULT NULL COMMENT '支付密码(加密)',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB COMMENT='用户钱包表';

-- 1.3 资金流水表 (用于财务审计)
CREATE TABLE `sys_transaction_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '变动金额 (正数收入, 负数支出)',
  `balance_after` decimal(10,2) NOT NULL COMMENT '变动后余额 (快照)',
  `flow_type` tinyint NOT NULL COMMENT '类型: 1-充值, 2-支付订单, 3-课时费解冻收入, 4-提现, 5-退款',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID (可为空)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注 (如: 订单1001课时费结算)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB COMMENT='资金流水记录表';

-- 1.4 提现申请表 (教员提现)
CREATE TABLE `sys_withdrawal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '提现金额',
  `channel` tinyint DEFAULT '1' COMMENT '渠道: 1-微信, 2-支付宝, 3-银行卡',
  `account_no` varchar(64) NOT NULL COMMENT '收款账号',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-审核中, 1-已打款, 2-驳回',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB COMMENT='提现申请表';


-- ========================================
-- 2. 教员中心模块 (Tutor Center)
-- ========================================

-- 2.1 教员详细档案与认证表
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
  `introduction` text DEFAULT NULL COMMENT '自我介绍',
  `experience` varchar(255) DEFAULT NULL COMMENT '教学经验',
  `achievements` text DEFAULT NULL COMMENT '主要成就',
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

-- 2.2 教员可授课时间表
CREATE TABLE `tutor_schedule_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tutor_id` bigint NOT NULL COMMENT '教员档案ID',
  `day_of_week` tinyint NOT NULL COMMENT '星期几：1-7',
  `start_time` varchar(10) DEFAULT NULL COMMENT '开始时间(HH:mm格式)',
  `end_time` varchar(10) DEFAULT NULL COMMENT '结束时间(HH:mm格式)',
  `available` tinyint DEFAULT '1' COMMENT '是否可用：0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tutor_day` (`tutor_id`, `day_of_week`)
) ENGINE=InnoDB COMMENT='教员排课配置表';


-- ========================================
-- 3. 家长需求与匹配模块 (Demand & Matching)
-- ========================================

-- 3.1 学生档案表
CREATE TABLE `parent_student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL COMMENT '家长用户ID',
  `student_name` varchar(32) DEFAULT NULL COMMENT '学生姓名',
  `gender` tinyint DEFAULT '1' COMMENT '学生性别：0女 1男',
  `grade` varchar(32) DEFAULT NULL COMMENT '年级 (如: 小学三年级)',
  `school_name` varchar(64) DEFAULT NULL COMMENT '学校名称',
  `weak_subjects` varchar(255) DEFAULT NULL COMMENT '薄弱科目(JSON或逗号分隔)',
  `study_desc` text DEFAULT NULL COMMENT '学习情况描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB COMMENT='学生档案表';

-- 3.2 找家教需求单表
CREATE TABLE `demand_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `publisher_id` bigint NOT NULL COMMENT '发布者ID（家长用户ID）',
  `student_id` bigint DEFAULT NULL COMMENT '关联学生ID',
  `title` varchar(128) DEFAULT NULL COMMENT '需求标题',
  `subject` varchar(32) NOT NULL COMMENT '需求科目',
  `grade` varchar(32) NOT NULL COMMENT '需求年级',
  `skill_level` varchar(32) DEFAULT NULL COMMENT '基础水平：零基础、有基础、考级/比赛冲刺',
  `venue_type` tinyint DEFAULT NULL COMMENT '场地类型：1教员上门 2学员上门 3公共场馆',
  `expect_price` decimal(10,2) DEFAULT NULL COMMENT '期望价格(元/小时)',
  `schedule_require` text DEFAULT NULL COMMENT '课时要求(JSON数组)',
  `teach_mode` tinyint DEFAULT '3' COMMENT '授课方式：1上门 2网课 3均可',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `detail` text DEFAULT NULL COMMENT '需求详情',
  `status` tinyint DEFAULT '1' COMMENT '状态：0下架 1上架 2已匹配',
  `matched_tutor_id` bigint DEFAULT NULL COMMENT '匹配的教员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publisher` (`publisher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_subject_grade` (`subject`, `grade`),
  KEY `idx_location` (`longitude`, `latitude`)
) ENGINE=InnoDB COMMENT='需求发布表';

-- 3.3 用户浏览/搜索记录表 (用于推荐算法)
CREATE TABLE `user_action_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '家长ID',
  `target_id` bigint NOT NULL COMMENT '被查看的教员ID 或 搜索的关键词ID',
  `action_type` tinyint DEFAULT '1' COMMENT '1-查看教员详情, 2-搜索科目, 3-收藏教员',
  `duration` int DEFAULT '0' COMMENT '停留时长(秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_action` (`user_id`, `action_type`)
) ENGINE=InnoDB COMMENT='用户行为轨迹表';

-- 3.4 协同过滤相似度缓存表
CREATE TABLE IF NOT EXISTS `user_similarity_cache` (
    `user_a_id` BIGINT NOT NULL,
    `user_b_id` BIGINT NOT NULL,
    `similarity` DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    `common_items` INT NOT NULL DEFAULT 0,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_a_id`, `user_b_id`),
    INDEX `idx_user_a` (`user_a_id`),
    INDEX `idx_user_b` (`user_b_id`),
    INDEX `idx_similarity` (`similarity` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户相似度缓存表';


-- ========================================
-- 4. 交易与订单模块 (Transaction)
-- ========================================

-- 4.1 课程订单表
CREATE TABLE `course_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单号/主键ID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '订单编号',
  `parent_id` bigint NOT NULL COMMENT '家长用户ID',
  `student_id` bigint DEFAULT NULL COMMENT '学生ID',
  `tutor_id` bigint NOT NULL COMMENT '教员用户ID',
  `tutor_profile_id` bigint DEFAULT NULL COMMENT '教员档案ID',
  `demand_id` bigint DEFAULT NULL COMMENT '来源需求ID',
  `subject` varchar(32) DEFAULT NULL COMMENT '课程科目',
  `grade` varchar(32) DEFAULT NULL COMMENT '课程年级',
  `teach_mode` tinyint DEFAULT '1' COMMENT '授课方式：1上门 2网课',
  `unit_price` decimal(10,2) NOT NULL COMMENT '课时单价(元/小时)',
  `total_hours` int DEFAULT '1' COMMENT '总课时数',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额(托管金额)',
  `service_fee` decimal(10,2) DEFAULT '0.00' COMMENT '平台服务费',
  `tutor_amount` decimal(10,2) DEFAULT '0.00' COMMENT '教员实收金额',
  `used_hours` int DEFAULT '0' COMMENT '已上课时',
  `payment_mode` varchar(20) DEFAULT 'per_lesson' COMMENT '支付模式: full-一次性支付, per_lesson-按课时支付',
  `paid_hours` int DEFAULT '0' COMMENT '已支付课时数',
  `confirmed_hours` int DEFAULT '0' COMMENT '已确认课时数',
  `status` tinyint DEFAULT '0' COMMENT '状态: -1-待确认, 0-待支付, 1-已支付待上课, 2-进行中, 3-已完成, 4-已取消, 5-退款中, 6-已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1钱包 2微信 3支付宝',
  `pay_trade_no` varchar(64) DEFAULT NULL COMMENT '第三方支付流水号',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_tutor` (`tutor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='课程订单表';

-- 4.2 保险单表
CREATE TABLE `insurance_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `policy_no` varchar(64) DEFAULT NULL COMMENT '保险单号',
  `provider` varchar(64) DEFAULT 'PingAn' COMMENT '保险公司',
  `status` tinyint DEFAULT '1' COMMENT '1-生效中, 2-已过期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB COMMENT='保险单记录表';


-- ========================================
-- 5. 教学过程管控模块 (Process Control)
-- ========================================

-- 5.1 上课打卡记录表
CREATE TABLE `teaching_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `lesson_index` int NOT NULL COMMENT '第几节课',
  `start_time` datetime DEFAULT NULL COMMENT '实际上课时间',
  `end_time` datetime DEFAULT NULL COMMENT '实际下课时间',
  `scheduled_start_time` datetime DEFAULT NULL COMMENT '预定上课时间',
  `scheduled_end_time` datetime DEFAULT NULL COMMENT '预定下课时间',
  `clock_in_lat` decimal(10,6) DEFAULT NULL COMMENT '打卡纬度',
  `clock_in_lng` decimal(10,6) DEFAULT NULL COMMENT '打卡经度',
  `clock_in_img` varchar(255) DEFAULT NULL COMMENT '现场拍照(水印)',
  `content_summary` text DEFAULT NULL COMMENT '教学内容摘要',
  `homework_assigned` text DEFAULT NULL COMMENT '布置作业',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待上课, 1-上课中, 2-待确认, 3-已确认, 4-申诉中, 5-已解决, 6-已过期',
  `pay_status` tinyint DEFAULT '0' COMMENT '支付结算状态: 0-未结算, 1-已结算',
  `pay_time` datetime DEFAULT NULL COMMENT '结算时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  UNIQUE KEY `uk_order_lesson` (`order_id`, `lesson_index`)
) ENGINE=InnoDB COMMENT='课时打卡记录表';

-- 5.2 阶段学习报告表
CREATE TABLE `student_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `report_type` tinyint DEFAULT '1' COMMENT '1-月度报告, 2-阶段总结',
  `score_chart_data` json DEFAULT NULL COMMENT '成绩变化数据(ECharts JSON)',
  `tutor_comment` text DEFAULT NULL COMMENT '老师评语',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_student` (`student_id`)
) ENGINE=InnoDB COMMENT='学生阶段报告表';

-- 5.3 错题本表
CREATE TABLE `mistake_notebook` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `subject` varchar(32) DEFAULT NULL COMMENT '科目',
  `question_img` varchar(255) NOT NULL COMMENT '题目图片',
  `tags` varchar(255) DEFAULT NULL COMMENT '知识点标签',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_subject` (`student_id`, `subject`)
) ENGINE=InnoDB COMMENT='在线错题本';


-- ========================================
-- 6. 系统、社区与交互模块 (System & Interaction)
-- ========================================

-- 6.1 聊天消息表
CREATE TABLE `sys_chat_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text DEFAULT NULL COMMENT '消息内容',
  `msg_type` tinyint DEFAULT '1' COMMENT '1-文本, 2-图片, 3-简历卡片, 4-订单邀约',
  `is_read` tinyint DEFAULT '0' COMMENT '0-未读, 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat` (`sender_id`, `receiver_id`),
  KEY `idx_receiver` (`receiver_id`)
) ENGINE=InnoDB COMMENT='IM聊天记录表';

-- 6.2 评价表
CREATE TABLE `sys_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `from_user_id` bigint NOT NULL COMMENT '评论人',
  `to_user_id` bigint NOT NULL COMMENT '被评人',
  `score` tinyint DEFAULT '5' COMMENT '星级 1-5',
  `content` varchar(512) DEFAULT NULL COMMENT '评价内容',
  `tags` varchar(255) DEFAULT NULL COMMENT '评价标签: 准时, 讲课好',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_to_user` (`to_user_id`)
) ENGINE=InnoDB COMMENT='订单评价表';

-- 6.3 数据字典表 (系统配置)
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_code` varchar(64) NOT NULL COMMENT '类型编码: subject, grade, tag',
  `label` varchar(64) NOT NULL COMMENT '展示名: 数学, 高三',
  `value` varchar(64) NOT NULL COMMENT '存储值: math, grade_3',
  `sort` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type_code`)
) ENGINE=InnoDB COMMENT='数据字典表';

-- 6.4 社区帖子表
CREATE TABLE `community_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `topic_type` tinyint DEFAULT '1' COMMENT '1-经验分享, 2-难题求助',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `content` text DEFAULT NULL COMMENT '内容',
  `images` json DEFAULT NULL COMMENT '图片列表',
  `tags` varchar(256) DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `like_count` int DEFAULT '0' COMMENT '点赞量',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-正常, 0-已隐藏',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='社区帖子表';

-- 6.5 社区帖子点赞记录表（防重复点赞）
CREATE TABLE IF NOT EXISTS `community_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB COMMENT='帖子点赞记录表';

-- 6.6 社区评论表（支持多层回复）
CREATE TABLE `community_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `root_id` bigint NOT NULL DEFAULT 0 COMMENT '一级评论ID(0=一级评论)',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '直接回复的评论ID',
  `reply_to_id` bigint NOT NULL DEFAULT 0 COMMENT '被回复的评论ID',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复的用户ID',
  `user_id` bigint NOT NULL COMMENT '回复用户ID',
  `content` varchar(512) DEFAULT NULL COMMENT '回复内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int NOT NULL DEFAULT 0 COMMENT '回复数(仅一级评论维护)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-正常 3-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`),
  KEY `idx_root_id` (`root_id`, `status`, `create_time`),
  KEY `idx_post_root` (`post_id`, `root_id`, `status`, `create_time`)
) ENGINE=InnoDB COMMENT='社区评论表';

-- 6.7 评论点赞记录表
CREATE TABLE IF NOT EXISTS `community_comment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB COMMENT='评论点赞记录表';

-- 6.8 社区互动通知表
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


-- ========================================
-- 7. RAG / AI 模块 (Retrieval-Augmented Generation)
-- ========================================

-- 7.1 知识库文档表
CREATE TABLE IF NOT EXISTS `knowledge_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `doc_type` VARCHAR(50) NOT NULL,
    `source` VARCHAR(100),
    `tags` VARCHAR(500),
    `target_role` VARCHAR(20) DEFAULT 'ALL',
    `applicable_subjects` VARCHAR(500),
    `applicable_grades` VARCHAR(500),
    `status` TINYINT DEFAULT 1,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7.2 Prompt模板表
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `scene` VARCHAR(50) NOT NULL,
    `template` TEXT NOT NULL,
    `variables` TEXT,
    `is_active` TINYINT DEFAULT 1,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7.3 AI用户画像表
CREATE TABLE IF NOT EXISTS `user_profile_ai` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(20) NOT NULL,
    `teaching_style` VARCHAR(50),
    `expert_subjects` TEXT,
    `teaching_experience` VARCHAR(50),
    `student_grade` VARCHAR(50),
    `learning_style` VARCHAR(50),
    `weak_subjects` TEXT,
    `learning_needs` TEXT,
    `preferences` TEXT,
    `personalization_settings` TEXT,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7.4 电子协议签署记录表
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

-- 7.5 系统通知表
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

-- 7.6 课时反馈评价表
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


-- ========================================
-- 8. 基础数据初始化
-- ========================================

-- 8.1 数据字典 (sys_dict)
INSERT INTO `sys_dict` (type_code, label, value, sort) VALUES
('subject', '钢琴/乐器陪练', 'piano_instrument', 1),
('subject', '美术/书法', 'art_calligraphy', 2),
('subject', '声乐/视唱练耳', 'vocal_solfege', 3),
('subject', '中考体育专项', 'pe_exam', 4),
('subject', '羽毛球/网球陪练', 'badminton_tennis', 5),
('subject', '篮球/足球指导', 'basketball_football', 6),
('subject', '少儿编程(Scratch/Python)', 'kids_coding', 7),
('subject', '机器人/3D打印', 'robot_3dprint', 8),
('subject', '科学实验/航模', 'science_aeromodel', 9),
('grade', '小学一年级', 'primary_1', 1),
('grade', '小学二年级', 'primary_2', 2),
('grade', '小学三年级', 'primary_3', 3),
('grade', '小学四年级', 'primary_4', 4),
('grade', '小学五年级', 'primary_5', 5),
('grade', '小学六年级', 'primary_6', 6),
('grade', '初一', 'junior_1', 7),
('grade', '初二', 'junior_2', 8),
('grade', '初三', 'junior_3', 9),
('grade', '高一', 'senior_1', 10),
('grade', '高二', 'senior_2', 11),
('grade', '高三', 'senior_3', 12);

-- 8.2 系统用户 (sys_user) — 密码均为 test123456
-- 管理员 id:1
INSERT INTO `sys_user` (id, username, password, nickname, role, status, gender) VALUES
(1, 'admin', '47ec2dd791e31e2ef2076caf64ed9b3d', '系统管理员', 0, 1, 1);

-- 教员 id: 101-120 (20名)
INSERT INTO `sys_user` (id, username, password, nickname, role, status, gender, avatar) VALUES
(101, '13800138101', '47ec2dd791e31e2ef2076caf64ed9b3d', '张学霸', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'),
(102, '13800138102', '47ec2dd791e31e2ef2076caf64ed9b3d', '李音音', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka'),
(103, '13800138103', '47ec2dd791e31e2ef2076caf64ed9b3d', '王科创', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob'),
(104, '13800138104', '47ec2dd791e31e2ef2076caf64ed9b3d', '赵画画', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Cathy'),
(105, '13800138105', '47ec2dd791e31e2ef2076caf64ed9b3d', '孙全科', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=David'),
(106, '13800138106', '47ec2dd791e31e2ef2076caf64ed9b3d', '周编程', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Eva'),
(107, '13800138107', '47ec2dd791e31e2ef2076caf64ed9b3d', '吴书法', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Frank'),
(108, '13800138108', '47ec2dd791e31e2ef2076caf64ed9b3d', '郑声乐', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Grace'),
(109, '13800138109', '47ec2dd791e31e2ef2076caf64ed9b3d', '冯体育', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Harry'),
(110, '13800138110', '47ec2dd791e31e2ef2076caf64ed9b3d', '陈实验', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ivy'),
(111, '13800138111', '47ec2dd791e31e2ef2076caf64ed9b3d', '林清华', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack'),
(112, '13800138112', '47ec2dd791e31e2ef2076caf64ed9b3d', '黄北大', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Kate'),
(113, '13800138113', '47ec2dd791e31e2ef2076caf64ed9b3d', '刘复旦', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Leo'),
(114, '13800138114', '47ec2dd791e31e2ef2076caf64ed9b3d', '张交大', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mia'),
(115, '13800138115', '47ec2dd791e31e2ef2076caf64ed9b3d', '徐浙大', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Nick'),
(116, '13800138116', '47ec2dd791e31e2ef2076caf64ed9b3d', '朱南大', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Olivia'),
(117, '13800138117', '47ec2dd791e31e2ef2076caf64ed9b3d', '何中科', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Paul'),
(118, '13800138118', '47ec2dd791e31e2ef2076caf64ed9b3d', '罗同济', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Queen'),
(119, '13800138119', '47ec2dd791e31e2ef2076caf64ed9b3d', '高人大', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Rick'),
(120, '13800138120', '47ec2dd791e31e2ef2076caf64ed9b3d', '马北航', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Sara');

-- 家长 id: 201-220 (20名)
INSERT INTO `sys_user` (id, username, password, nickname, role, status, gender, avatar) VALUES
(201, '13900139201', '47ec2dd791e31e2ef2076caf64ed9b3d', '子涵妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom1'),
(202, '13900139202', '47ec2dd791e31e2ef2076caf64ed9b3d', '浩宇爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad1'),
(203, '13900139203', '47ec2dd791e31e2ef2076caf64ed9b3d', '欣怡妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom2'),
(204, '13900139204', '47ec2dd791e31e2ef2076caf64ed9b3d', '俊杰爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad2'),
(205, '13900139205', '47ec2dd791e31e2ef2076caf64ed9b3d', '梓涵妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom3'),
(206, '13900139206', '47ec2dd791e31e2ef2076caf64ed9b3d', '宇轩爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad3'),
(207, '13900139207', '47ec2dd791e31e2ef2076caf64ed9b3d', '雨桐妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom4'),
(208, '13900139208', '47ec2dd791e31e2ef2076caf64ed9b3d', '子轩爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad4'),
(209, '13900139209', '47ec2dd791e31e2ef2076caf64ed9b3d', '晨曦妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom5'),
(210, '13900139210', '47ec2dd791e31e2ef2076caf64ed9b3d', '浩然爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad5'),
(211, '13900139211', '47ec2dd791e31e2ef2076caf64ed9b3d', '诗涵妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom6'),
(212, '13900139212', '47ec2dd791e31e2ef2076caf64ed9b3d', '天宇爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad6'),
(213, '13900139213', '47ec2dd791e31e2ef2076caf64ed9b3d', '思琪妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom7'),
(214, '13900139214', '47ec2dd791e31e2ef2076caf64ed9b3d', '博文爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad7'),
(215, '13900139215', '47ec2dd791e31e2ef2076caf64ed9b3d', '紫萱妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom8'),
(216, '13900139216', '47ec2dd791e31e2ef2076caf64ed9b3d', '瑞泽爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad8'),
(217, '13900139217', '47ec2dd791e31e2ef2076caf64ed9b3d', '佳怡妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom9'),
(218, '13900139218', '47ec2dd791e31e2ef2076caf64ed9b3d', '子墨爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad9'),
(219, '13900139219', '47ec2dd791e31e2ef2076caf64ed9b3d', '雪儿妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom10'),
(220, '13900139220', '47ec2dd791e31e2ef2076caf64ed9b3d', '星宇爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad10');

-- 8.3 用户钱包 (sys_wallet)
INSERT INTO `sys_wallet` (user_id, balance, frozen_amount)
SELECT id, 0.00, 0.00 FROM sys_user;

-- 给部分用户充值
UPDATE `sys_wallet` SET balance = 5000.00 WHERE user_id BETWEEN 201 AND 210;

-- 8.4 教员档案 (tutor_profile)
INSERT INTO `tutor_profile` (user_id, real_name, university_name, major, education, teach_subjects, teach_grades, expect_price, longitude, latitude, address, cert_status, rating, introduction, teach_style, can_visit, can_online) VALUES
(101, '张伟', '北京大学', '计算机科学', 2, '["少儿编程(Scratch/Python)","机器人/3D打印"]', '["初一","初二","初三","高一"]', 150.00, 116.310003, 39.991957, '北京市海淀区颐和园路5号', 2, 4.9, '编程竞赛金牌得主，擅长逻辑思维培养', '严谨细致，深入浅出', 1, 1),
(102, '李娜', '中央音乐学院', '钢琴表演', 3, '["钢琴/乐器陪练"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级","初一","初二"]', 120.00, 116.315055, 39.957583, '北京市海淀区西三环北路2号', 2, 4.8, '钢琴十级，有两年少儿钢琴教学经验', '活泼生动，寓教于乐', 1, 1),
(103, '王强', '清华大学', '机械工程', 5, '["科学实验/航模","少儿编程(Scratch/Python)"]', '["高一","高二","高三"]', 200.00, 116.326759, 40.003304, '北京市海淀区双清路30号', 2, 5.0, '科创竞赛指导老师，擅长项目式教学', '重点突出，动手能力培养', 0, 1),
(104, '赵敏', '中央美术学院', '国画', 2, '["美术/书法","声乐/视唱练耳"]', '["初三","高一"]', 100.00, 116.372551, 39.965874, '北京市海淀区新街口外大街19号', 2, 4.7, '美院硕士，擅长国画书法启蒙和考级辅导', '亲切随和，循循善诱', 1, 0),
(105, '孙浩', '北京体育大学', '体育教育', 3, '["篮球/足球指导","羽毛球/网球陪练"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级"]', 90.00, 116.316833, 39.971556, '北京市海淀区中关村大街59号', 1, 4.5, '喜欢孩子，有耐心，善于沟通', '轻松愉快', 1, 1),
(106, '周婷', '北京理工大学', '软件工程', 3, '["少儿编程(Scratch/Python)"]', '["初一","初二","高一","高二"]', 130.00, 116.321111, 39.960000, '北京市海淀区中关村南大街5号', 2, 4.8, '信息学奥赛辅导经验丰富，Scratch/Python教学', '注重基础，稳扎稳打', 1, 1),
(107, '吴刚', '中央美术学院', '书法', 4, '["美术/书法","钢琴/乐器陪练"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级","初一"]', 110.00, 116.300000, 39.980000, '北京市海淀区', 2, 4.6, '书法功底深厚，硬笔软笔均可教学', '耐心引导，兴趣激发', 1, 1),
(108, '郑洁', '中国音乐学院', '声乐', 3, '["声乐/视唱练耳","美术/书法"]', '["初三","高三"]', 140.00, 116.350000, 39.970000, '北京市昌平区', 2, 4.9, '声乐专业硕士，视唱练耳考级辅导专家', '条理清晰', 0, 1),
(109, '冯涛', '北京体育大学', '运动训练', 4, '["中考体育专项"]', '["初一","初二","高一"]', 120.00, 116.340000, 39.985000, '北京市海淀区学院路', 2, 4.7, '中考体育满分指导，专项训练经验丰富', '因材施教，科学训练', 1, 0),
(110, '陈静', '北京航空航天大学', '航空宇航', 3, '["科学实验/航模","机器人/3D打印"]', '["高一","高二"]', 110.00, 116.360000, 40.010000, '北京市海淀区清华东路', 2, 4.8, '航模竞赛获奖者，科学实验教学能力强', '细致入微', 1, 1);

-- 更多教员数据，模拟不同状态 (待审核, 拒绝等)
INSERT INTO `tutor_profile` (user_id, real_name, university_name, major, education, teach_subjects, teach_grades, expect_price, longitude, latitude, address, cert_status, rating) VALUES
(111, '林风', '清华大学', '计算机', 3, '["少儿编程(Scratch/Python)","机器人/3D打印"]', '["初一","初二","初三"]', 180.00, 116.335000, 39.992000, '北京市海淀区五道口', 0, 0.0),
(112, '黄芸', '中央音乐学院', '音乐教育', 2, '["钢琴/乐器陪练","声乐/视唱练耳"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级"]', 100.00, 116.348000, 39.978000, '北京市西城区西直门', 1, 0.0),
(113, '刘星', '同济大学', '机械工程', 4, '["科学实验/航模"]', '["高一","高二","高三"]', 200.00, 116.355000, 39.968000, '北京市朝阳区望京', 3, 0.0),
(114, '张月', '中央音乐学院', '钢琴', 3, '["钢琴/乐器陪练"]', '["初一","初二","初三"]', 120.00, 116.380000, 39.940000, '北京市朝阳区国贸', 2, 5.0),
(115, '徐阳', '浙江大学', '计算机', 4, '["少儿编程(Scratch/Python)"]', '["高一","高二","高三"]', 160.00, 116.290000, 39.960000, '北京市海淀区苏州街', 2, 4.8);


-- 8.5 教员排课 (tutor_schedule_config)
INSERT INTO `tutor_schedule_config` (tutor_id, day_of_week, start_time, end_time, available) VALUES
(1, 1, '18:00', '21:00', 1), (1, 3, '18:00', '21:00', 1), (1, 6, '09:00', '12:00', 1),
(2, 2, '19:00', '21:00', 1), (2, 4, '19:00', '21:00', 1), (2, 7, '14:00', '17:00', 1),
(3, 6, '08:00', '20:00', 1), (3, 7, '08:00', '20:00', 1);

-- 8.6 家长学生 (parent_student)
INSERT INTO `parent_student` (parent_id, student_name, gender, grade, school_name, weak_subjects, study_desc) VALUES
(201, '子涵', 1, '初二', '北大附中', '少儿编程(Scratch/Python),机器人/3D打印', '对编程很感兴趣，想系统学习'),
(202, '浩宇', 1, '高一', '清华附中', '钢琴/乐器陪练', '学钢琴两年，准备考级'),
(203, '欣怡', 0, '小学三年级', '中关村一小', '美术/书法', '喜欢画画，想学国画和书法'),
(204, '俊杰', 1, '初三', '人大附中', '中考体育专项', '临近中考，体育项目需要专项训练'),
(205, '梓涵', 0, '高二', '101中学', '科学实验/航模', '对航模和科学实验很感兴趣'),
(206, '宇轩', 1, '小学五年级', '实验二小', '篮球/足球指导', '想提高篮球技术'),
(207, '雨桐', 0, '初一', '八一学校', '声乐/视唱练耳,美术/书法', '想发展艺术特长'),
(208, '子轩', 1, '高三', '十一学校', '少儿编程(Scratch/Python)', '准备信息学竞赛'),
(209, '晨曦', 0, '小学四年级', '史家小学', '钢琴/乐器陪练', '刚开始学钢琴，需要陪练'),
(210, '浩然', 1, '初二', '四中', '羽毛球/网球陪练', '想培养运动爱好');

-- 8.7 需求发布 (demand_post)
INSERT INTO `demand_post` (publisher_id, student_id, title, subject, grade, expect_price, teach_mode, longitude, latitude, address, detail, status) VALUES
(201, 1, '初二编程入门辅导', '少儿编程(Scratch/Python)', '初二', 150.00, 1, 116.310000, 39.990000, '北京市海淀区中关村', '孩子对编程感兴趣，希望系统学习Python', 1),
(202, 2, '高一钢琴考级陪练', '钢琴/乐器陪练', '高一', 120.00, 2, 116.320000, 39.980000, '北京市海淀区清华园', '准备钢琴八级考试，需要陪练指导', 1),
(203, 3, '小学美术书法启蒙', '美术/书法', '小学三年级', 100.00, 3, 116.330000, 39.970000, '北京市海淀区知春路', '寻找有国画书法教学经验的老师', 1),
(204, 4, '中考体育专项冲刺', '中考体育专项', '初三', 180.00, 1, 116.340000, 39.960000, '北京市海淀区万柳', '针对中考体育各项目专项训练', 1),
(205, 5, '高二科学实验航模', '科学实验/航模', '高二', 200.00, 2, 116.350000, 39.950000, '北京市海淀区五棵松', '想参加航模比赛，需要指导', 1),
(206, 6, '小学篮球启蒙', '篮球/足球指导', '小学五年级', 80.00, 2, 116.360000, 39.940000, '北京市西城区', '培养篮球兴趣和基本功', 2),
(207, 7, '初一声乐入门', '声乐/视唱练耳', '初一', 90.00, 1, 116.370000, 39.930000, '北京市东城区', '零基础学习声乐和视唱练耳', 2),
(208, 8, '高三编程竞赛冲刺', '少儿编程(Scratch/Python)', '高三', 250.00, 1, 116.380000, 39.920000, '北京市朝阳区', '目标信息学竞赛省一', 2);

UPDATE `demand_post` SET matched_tutor_id = 101 WHERE id = 8;
UPDATE `demand_post` SET matched_tutor_id = 105 WHERE id = 6;
UPDATE `demand_post` SET matched_tutor_id = 108 WHERE id = 7;

-- 8.8 课程订单 (course_order)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260101001', 208, 8, 101, 1, 8, '少儿编程(Scratch/Python)', '高三', 1, 250.00, 10, 2500.00, 250.00, 2250.00, 10, 3, DATE_SUB(NOW(), INTERVAL 10 DAY), 1),
('ORD20260101002', 206, 6, 105, 5, 6, '篮球/足球指导', '小学五年级', 2, 80.00, 20, 1600.00, 160.00, 1440.00, 5, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 2),
('ORD20260101003', 207, 7, 108, 8, 7, '声乐/视唱练耳', '初一', 1, 90.00, 10, 900.00, 90.00, 810.00, 0, 0, NULL, NULL);

-- 8.9 评价 (sys_comment)
INSERT INTO `sys_comment` (order_id, from_user_id, to_user_id, score, content, tags) VALUES
(1, 208, 101, 5, '张老师非常有水平，孩子编程能力提高很快！', '知识渊博,教学严谨');

-- 8.10 钱包流水
UPDATE `sys_wallet` SET balance = balance + 2250.00 WHERE user_id = 101;
INSERT INTO `sys_transaction_flow` (user_id, amount, balance_after, flow_type, order_id, remark) VALUES
(101, 2250.00, 2250.00, 3, 1, '订单ORD20260101001课时费结算'),
(208, -2500.00, 2500.00, 2, 1, '支付订单ORD20260101001');

-- 8.11 教学记录 (teaching_record)
INSERT INTO `teaching_record` (order_id, lesson_index, start_time, end_time, content_summary, status) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 2 HOUR), 'Python基础语法', 1),
(1, 2, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 9 DAY), INTERVAL 2 HOUR), 'Python函数与模块', 1),
(1, 3, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 8 DAY), INTERVAL 2 HOUR), 'Python列表和字典', 1),
(1, 4, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 7 DAY), INTERVAL 2 HOUR), 'Python面向对象', 1),
(1, 5, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 2 HOUR), 'Python文件操作', 1),
(2, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 1 HOUR), '篮球运球基本功', 1),
(2, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 1 HOUR), '篮球投篮技巧', 1),
(2, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 1 HOUR), '篮球传球配合', 1);

-- 8.12 更多课程订单
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260115001', 209, 9, 102, 2, NULL, '钢琴/乐器陪练', '小学四年级', 1, 120.00, 8, 960.00, 96.00, 864.00, 8, 3, DATE_SUB(NOW(), INTERVAL 20 DAY), 1),
('ORD20260120001', 203, 3, 104, 4, 3, '美术/书法', '小学三年级', 1, 100.00, 12, 1200.00, 120.00, 1080.00, 12, 3, DATE_SUB(NOW(), INTERVAL 15 DAY), 2),
('ORD20260201001', 204, 4, 109, 9, 4, '中考体育专项', '初三', 1, 120.00, 20, 2400.00, 240.00, 2160.00, 8, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), 1),
('ORD20260205001', 205, 5, 103, 3, 5, '科学实验/航模', '高二', 2, 200.00, 6, 1200.00, 120.00, 1080.00, 6, 3, DATE_SUB(NOW(), INTERVAL 12 DAY), 1),
('ORD20260210001', 201, 1, 106, 6, 1, '少儿编程(Scratch/Python)', '初二', 1, 130.00, 15, 1950.00, 195.00, 1755.00, 4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 2),
('ORD20260212001', 203, 3, 107, 7, NULL, '美术/书法', '小学三年级', 1, 110.00, 10, 1100.00, 110.00, 990.00, 10, 3, DATE_SUB(NOW(), INTERVAL 25 DAY), 1),
('ORD20260215001', 205, 5, 110, 10, NULL, '机器人/3D打印', '高二', 2, 110.00, 8, 880.00, 88.00, 792.00, 8, 3, DATE_SUB(NOW(), INTERVAL 18 DAY), 1);

-- 8.13 更多评价
INSERT INTO `sys_comment` (order_id, from_user_id, to_user_id, score, content, tags) VALUES
(4, 209, 102, 5, '李老师非常专业，孩子钢琴进步很快！', '耐心,专业,有经验'),
(5, 203, 104, 5, '赵老师教国画特别好，孩子很喜欢！', '亲切,专业,寓教于乐'),
(7, 205, 103, 5, '王老师科学实验课太棒了，孩子爱上了科创！', '知识渊博,动手能力强,课程有趣'),
(9, 203, 107, 4, '吴老师书法功底深厚，孩子进步明显', '功底扎实,教学认真'),
(10, 205, 110, 5, '陈老师3D打印课程很有创意', '课程新颖,有耐心');

-- 补充教员订单计数
UPDATE `tutor_profile` SET order_count = 2 WHERE user_id = 101;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 102;
UPDATE `tutor_profile` SET order_count = 2 WHERE user_id = 103;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 104;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 105;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 106;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 107;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 108;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 109;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 110;

-- 补充更多教学记录
INSERT INTO `teaching_record` (order_id, lesson_index, start_time, end_time, content_summary, status) VALUES
(4, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 1 HOUR), '钢琴基础指法练习', 1),
(4, 2, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 19 DAY), INTERVAL 1 HOUR), '简易乐曲演奏', 1),
(5, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 15 DAY), INTERVAL 2 HOUR), '国画基础: 笔墨纸砚', 1),
(5, 2, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 14 DAY), INTERVAL 2 HOUR), '山水画入门', 1),
(6, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 7 DAY), INTERVAL 1 HOUR), '中考体育: 跑步热身与技巧', 1),
(6, 2, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 1 HOUR), '中考体育: 引体向上训练', 1),
(7, 1, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 12 DAY), INTERVAL 2 HOUR), '航模原理与结构', 1),
(7, 2, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 11 DAY), INTERVAL 2 HOUR), '航模组装实操', 1),
(8, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 2 HOUR), 'Scratch图形化编程入门', 1),
(8, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 2 HOUR), 'Scratch动画与游戏制作', 1);

-- 8.14 更多需求
INSERT INTO `demand_post` (publisher_id, student_id, title, subject, grade, expect_price, teach_mode, longitude, latitude, address, detail, status) VALUES
(209, 9, '小学钢琴启蒙', '钢琴/乐器陪练', '小学四年级', 100.00, 1, 116.325000, 39.975000, '北京市东城区东直门', '孩子刚开始学钢琴，需要有耐心的老师', 1),
(210, 10, '初二羽毛球训练', '羽毛球/网球陪练', '初二', 80.00, 1, 116.345000, 39.980000, '北京市海淀区中关村', '培养运动特长，每周2-3次', 1),
(211, NULL, '声乐考级辅导', '声乐/视唱练耳', '初三', 130.00, 3, 116.362000, 39.955000, '北京市西城区金融街', '准备声乐考级，需要有经验的老师', 1),
(212, NULL, '中考体育训练', '中考体育专项', '初三', 100.00, 1, 116.318000, 39.998000, '北京市海淀区北大', '体育分不理想，急需提升', 1),
(213, NULL, '机器人编程入门', '机器人/3D打印', '初一', 150.00, 2, 116.370000, 39.945000, '北京市朝阳区三里屯', '对机器人充满兴趣', 1),
(214, NULL, '篮球基本功训练', '篮球/足球指导', '小学六年级', 70.00, 1, 116.310000, 39.965000, '北京市海淀区万泉河路', '培养孩子运动习惯', 1),
(215, NULL, '书法硬笔入门', '美术/书法', '小学二年级', 80.00, 1, 116.395000, 39.920000, '北京市朝阳区望京', '写字不好看，想练硬笔', 1);

-- 8.15 补充排课数据
INSERT INTO `tutor_schedule_config` (tutor_id, day_of_week, start_time, end_time, available) VALUES
(4, 1, '14:00', '17:00', 1), (4, 3, '14:00', '17:00', 1), (4, 6, '09:00', '17:00', 1),
(5, 2, '16:00', '18:00', 1), (5, 4, '16:00', '18:00', 1), (5, 6, '08:00', '12:00', 1), (5, 7, '08:00', '12:00', 1),
(6, 1, '19:00', '21:00', 1), (6, 3, '19:00', '21:00', 1), (6, 5, '19:00', '21:00', 1),
(7, 2, '15:00', '18:00', 1), (7, 4, '15:00', '18:00', 1), (7, 7, '09:00', '12:00', 1),
(8, 6, '10:00', '12:00', 1), (8, 6, '14:00', '17:00', 1), (8, 7, '10:00', '17:00', 1),
(9, 1, '06:00', '08:00', 1), (9, 3, '06:00', '08:00', 1), (9, 5, '06:00', '08:00', 1), (9, 6, '06:00', '10:00', 1),
(10, 2, '18:00', '21:00', 1), (10, 4, '18:00', '21:00', 1), (10, 7, '14:00', '18:00', 1);

COMMIT;
