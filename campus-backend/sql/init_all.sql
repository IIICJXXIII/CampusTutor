/*
 * CampusTutor 校园智教平台 - 项目启动必需SQL（合并版）
 * Version: 2.1.0
 * Date: 2026-04-30
 * Description:
 *   合并了以下5个必需SQL文件的DDL及种子数据，已与后端Java实体类字段完全匹配：
 *     1. schema.sql                    — 核心19张表
 *     2. migration_20260429.sql        — sys_user地址字段、tutor_application表、course_order补充字段
 *     3. add_booking_request_table.sql — booking_request表
 *     4. final_rag_setup.sql           — RAG知识库DDL（3张表）+ 知识文档 + Prompt模板
 *     5. rag_simulation_data.sql       — RAG辅助DDL（3张表）+ 知识文档扩展
 *
 *   包含内容：
 *     - 27张表（21张业务表 + 6张RAG/AI表）
 *     - 16篇知识库文档（素质教育平台规则、教学指南等）
 *     - 5套Prompt模板（需求咨询、教员推荐、教案生成、评语润色、通用问答）
 *
 *   以下SQL文件属于测试数据/调试脚本，运行项目不需要：
 *     - data.sql, hunan_seed_data.sql, yelp_mock_data.sql（测试种子数据）
 *     - test_balance.sql, test_cf_data.sql（测试工具）
 *     - fix_checkin_block.sql（调试脚本）
 *     - drop_openid_from_sys_user.sql（schema已不含openid字段）
 *     - V2_transform_subjects.sql（字段已在schema中，且引用已废弃的sys_subject表）
 */

-- ----------------------------
-- 0. 数据库初始化
-- ----------------------------
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
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `address` varchar(255) DEFAULT NULL COMMENT '结构化地址',
  `role` tinyint NOT NULL COMMENT '角色: 0-管理员, 1-教员, 2-家长',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-正常, 0-禁用',
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

-- 3.3 用户浏览/搜索记录表 (用于Python推荐算法数据源)
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

-- 3.4 教师接单申请表
CREATE TABLE `tutor_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `demand_id` bigint NOT NULL COMMENT '需求ID',
  `tutor_id` bigint NOT NULL COMMENT '教师用户ID',
  `tutor_profile_id` bigint DEFAULT NULL COMMENT '教师档案ID',
  `total_hours` int DEFAULT '10' COMMENT '计划课时数',
  `remark` varchar(512) DEFAULT NULL COMMENT '申请备注',
  `status` tinyint DEFAULT '0' COMMENT '0-待审核 1-已接受 2-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_tutor_id` (`tutor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师接单申请表';


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
  `payment_mode` varchar(32) DEFAULT NULL COMMENT '支付模式: full(全额) / per_lesson(按课时)',
  `paid_hours` int DEFAULT '0' COMMENT '已支付课时数',
  `course_flow_mode` tinyint DEFAULT '1' COMMENT '上课流程模式: 1-先支付后上课 2-先上课后支付',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待支付, 1-已支付待上课, 2-进行中, 3-已完成, 4-已取消, 5-退款中, 6-已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1钱包 2微信 3支付宝',
  `pay_trade_no` varchar(64) DEFAULT NULL COMMENT '第三方支付流水号',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
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
  `clock_in_lat` decimal(10,6) DEFAULT NULL COMMENT '打卡纬度',
  `clock_in_lng` decimal(10,6) DEFAULT NULL COMMENT '打卡经度',
  `clock_in_img` varchar(255) DEFAULT NULL COMMENT '现场拍照(水印)',
  `content_summary` text DEFAULT NULL COMMENT '教学内容摘要',
  `homework_assigned` text DEFAULT NULL COMMENT '布置作业',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待确认, 1-家长已确认, 2-异常/申诉',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
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

-- 6.4 社区帖子表 (增加用户粘性)
CREATE TABLE `community_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `topic_type` tinyint DEFAULT '1' COMMENT '1-经验分享, 2-难题求助',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `content` text DEFAULT NULL COMMENT '内容',
  `images` json DEFAULT NULL COMMENT '图片列表',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `like_count` int DEFAULT '0' COMMENT '点赞量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB COMMENT='社区帖子表';

-- 6.5 社区评论表
CREATE TABLE `community_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '回复用户ID',
  `content` varchar(512) DEFAULT NULL COMMENT '回复内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB COMMENT='社区评论表';


-- ========================================
-- 7. 预约模块 (Booking)
-- ========================================

-- 7.1 预约请求表
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


-- ========================================
-- 8. RAG知识库与AI助手模块 (RAG & AI Assistant)
-- ========================================

-- 8.1 知识库文档表
CREATE TABLE `knowledge_document` (
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

-- 8.2 Prompt模板表
CREATE TABLE `prompt_template` (
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

-- 8.3 AI用户画像表
CREATE TABLE `user_profile_ai` (
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

-- 8.4 AI交互历史记录表
CREATE TABLE `ai_interaction_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT,
    `scene` VARCHAR(50) NOT NULL,
    `query` TEXT NOT NULL,
    `response` TEXT NOT NULL,
    `rag_enabled` TINYINT DEFAULT 0,
    `rag_score` DECIMAL(3,2),
    `tokens_used` INT,
    `response_time_ms` INT,
    `user_rating` TINYINT,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_scene` (`scene`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8.5 知识检索记录表
CREATE TABLE `knowledge_retrieval_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `query` TEXT NOT NULL,
    `document_ids` TEXT COMMENT '检索到的文档ID列表',
    `relevance_scores` TEXT COMMENT '相关性分数列表',
    `retrieval_time_ms` INT,
    `user_feedback` TINYINT COMMENT '用户反馈：1-相关，0-不相关',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8.6 AI功能使用统计表
CREATE TABLE `ai_function_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `function_name` VARCHAR(50) NOT NULL,
    `scene` VARCHAR(50) NOT NULL,
    `usage_count` INT DEFAULT 0,
    `success_count` INT DEFAULT 0,
    `avg_response_time_ms` INT,
    `avg_user_rating` DECIMAL(3,2),
    `last_used_time` DATETIME,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_function_scene` (`function_name`, `scene`),
    INDEX `idx_usage_count` (`usage_count`),
    INDEX `idx_last_used_time` (`last_used_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ========================================
-- 9. RAG知识库种子数据（AI助手必需）
-- ========================================

-- 9.1 知识库文档（16篇）
INSERT INTO `knowledge_document` (`title`, `content`, `doc_type`, `source`, `tags`, `target_role`, `applicable_subjects`, `applicable_grades`, `status`) VALUES
('素质教育平台服务协议', '校园智教素质教育平台服务协议：1. 平台专注于艺术、体育、科创素质教育；2. 课时费托管机制；3. 教员需完成专业认证；4. 家长可申请试课；5. 争议处理流程。', 'RULE', '平台官方', '规则,协议,素质教育', 'ALL', NULL, NULL, 1),
('如何发布素质教育需求？', '发布素质教育需求步骤：1. 登录家长账号；2. 进入发布需求页面；3. 选择素质教育类别（艺术/体育/科创）；4. 填写学生兴趣和基础；5. 设置期望价格；6. 选择授课方式；7. 确认发布。', 'FAQ', '平台帮助中心', '需求发布,家长指南,素质教育', 'PARENT', NULL, NULL, 1),
('钢琴启蒙教案模板', '钢琴启蒙教案结构：1. 教学目标（识谱、手型、节奏）；2. 教学重点难点；3. 教学过程（热身、基础练习、曲目学习、音乐欣赏）；4. 家庭练习建议；5. 教学反思。', 'LESSON_PLAN', '优秀教员分享', '钢琴,音乐,艺术,教案', 'TEACHER', '钢琴,音乐', '启蒙,初级', 1),
('素质教育学生评语模板', '素质教育评语要素：1. 肯定学生兴趣和创造力；2. 具体指出艺术/体育/科创方面的进步；3. 提出个性化发展建议；4. 表达鼓励和期待；5. 语言温暖专业。', 'COMMENT', '教学经验分享', '评语,反馈,素质教育', 'TEACHER', NULL, NULL, 1),
('素质教育教员认证流程', '素质教育教员认证步骤：1. 提交专业背景信息；2. 上传相关证书/作品集；3. 填写教学理念和经验；4. 等待专业审核（1-3个工作日）；5. 审核通过后即可接单。', 'FAQ', '平台帮助中心', '认证,教员指南,素质教育', 'TEACHER', NULL, NULL, 1),
('家长素质教育安全保障', '家长素质教育安全保障措施：1. 教员专业认证；2. 课时费托管；3. 不满意可申请退款；4. 客服全程跟进；5. 紧急联系渠道；6. 教学环境安全评估。', 'RULE', '平台官方', '安全,保障,家长,素质教育', 'PARENT', NULL, NULL, 1),
('少儿编程教学要点', '少儿编程教学核心：1. 培养计算思维；2. 项目式学习；3. 游戏化教学；4. 鼓励创造力；5. 安全上网教育；6. 团队协作能力培养。', 'TEACHING_EXPERIENCE', '教学专家分享', '编程,STEAM,科创,教学', 'TEACHER', '编程,STEAM', '小学,初中', 1),
('中考体育训练指南', '中考体育训练要点：1. 科学训练计划；2. 体能测试评估；3. 技术动作规范；4. 营养与恢复；5. 心理素质培养；6. 安全防护措施。', 'TEACHING_EXPERIENCE', '体育专家分享', '体育,中考,训练,健康', 'TEACHER', '体育,中考体育', '初中', 1),
('STEAM教育核心理念', 'STEAM教育五大核心理念：1. 跨学科整合；2. 项目式学习；3. 问题解决导向；4. 创新思维培养；5. 团队协作能力。适合科创类素质教育课程设计。', 'TEACHING_EXPERIENCE', '教育专家分享', 'STEAM,科创,教育理念', 'TEACHER', '编程,机器人,科学实验', '小学,初中', 1),
('艺术素养培养路径', '艺术素养培养三阶段：1. 启蒙期（3-6岁）：兴趣培养；2. 基础期（7-12岁）：技能训练；3. 提升期（13+岁）：创意表达。每个阶段的教学重点和方法不同。', 'TEACHING_EXPERIENCE', '艺术教育专家', '艺术,素养,培养路径', 'TEACHER', '素描,水彩,创意美术', '全科', 1),
('体育训练安全规范', '体育训练安全注意事项：1. 热身充分（10-15分钟）；2. 器材检查；3. 环境安全评估；4. 运动强度控制；5. 紧急处理预案；6. 家长沟通机制。', 'RULE', '体育安全指南', '体育,安全,训练', 'TEACHER', '游泳,篮球,中考体育', '初中,高中', 1),
('家长选择素质教育指南', '家长选择素质教育课程建议：1. 了解孩子兴趣；2. 考察教员资质；3. 试课体验；4. 课程体系评估；5. 学习效果跟踪；6. 安全环境确认。', 'FAQ', '家长帮助中心', '家长指南,选择,素质教育', 'PARENT', NULL, NULL, 1),
('编程思维培养方法', '编程思维培养四步法：1. 分解问题；2. 模式识别；3. 抽象思维；4. 算法设计。适合少儿编程启蒙教学，培养计算思维。', 'TEACHING_EXPERIENCE', '编程教育专家', '编程,思维,教学方法', 'TEACHER', 'Python,Scratch,少儿编程', '小学', 1),
('音乐考级准备指南', '音乐考级准备要点：1. 曲目选择；2. 技术训练；3. 乐理知识；4. 视唱练耳；5. 心理准备；6. 考前模拟。帮助学员系统准备考级。', 'FAQ', '音乐教育指南', '音乐,考级,准备', 'TEACHER', '钢琴,小提琴,乐理', '全科', 1),
('创意美术教学案例', '创意美术教学案例：1. 主题创作（如"我的梦想家园"）；2. 材料探索（水彩、油画棒、黏土）；3. 技法指导；4. 作品展示与评价。激发学生创造力。', 'LESSON_PLAN', '优秀教案分享', '美术,创意,教学案例', 'TEACHER', '创意美术,手工', '小学', 1),
('中考体育评分标准', '中考体育项目评分标准：1. 1000米/800米跑；2. 立定跳远；3. 实心球；4. 引体向上/仰卧起坐。各项目满分标准和训练方法。', 'RULE', '体育考试标准', '中考,体育,评分标准', 'ALL', '中考体育', '初中', 1);

-- 9.2 Prompt模板（5个场景）
INSERT INTO `prompt_template` (`name`, `scene`, `template`, `variables`, `is_active`) VALUES
('素质教育需求咨询助手', 'DEMAND_CONSULT', '你是"校园智教"素质教育平台的AI助手。你的任务是帮助家长发布素质教育需求。

平台功能介绍：
1. 家长可以发布素质教育需求，选择艺术、体育、科创等类别
2. 系统会智能匹配合适的专业教员
3. 家长可以查看教员的专业背景、教学评价等信息
4. 确认后可以预约试课、签约正式课程

你需要：
1. 引导家长描述孩子的兴趣和基础（艺术/体育/科创方向）
2. 询问对教员的期望（专业背景、教学风格、价格等）
3. 确认授课方式（上门/网课）和时间安排
4. 收集完信息后，告知家长可以提交需求了

当前用户信息：
角色：{{userRole}}
学生兴趣：{{studentInterest}}
已有基础：{{existingFoundation}}

回复要简洁友好，不要太长。用中文回复。', '{"userRole": "string", "studentInterest": "string", "existingFoundation": "string"}', 1),

('素质教育教员推荐助手', 'TUTOR_RECOMMEND', '你是"校园智教"素质教育平台的AI助手。你的任务是帮助家长了解和选择合适的素质教育教员。

你需要：
1. 解答关于教员专业资质、认证流程的问题
2. 说明平台的教员筛选标准（专业背景、教学经验等）
3. 帮助家长理解如何查看教员评价和作品集
4. 解释试课、签约、退费等流程

平台规则：
{{platformRules}}

回复要专业、简洁。用中文回复。', '{"platformRules": "string"}', 1),

('素质教育教案生成助手', 'LESSON_PLAN', '你是"校园智教"素质教育平台的AI教学赋能官，专业的教案生成助手。
你的任务是为素质教育教员生成详细的课程教案。

教案要求：
1. 结构清晰：包含热身、主要内容、实践、创意、总结等环节
2. 时间合理：根据给定的课时时长分配时间
3. 针对性强：根据学生兴趣和基础设计内容
4. 实用性高：提供具体的教学方法和实践内容
5. 创意性强：鼓励创造力和个性化表达
6. 语言专业：使用专业的教学术语，但保持易懂

教学信息：
科目：{{subject}}
学生基础：{{studentLevel}}
课时时长：{{lessonDuration}}
学生情况：{{studentInfo}}

参考教案模板：
{{lessonPlanTemplates}}

输出格式：
- 教案标题
- 适用学生：[学生情况]
- 课时时长：[时长]
- 教学目标：[具体目标]
- 教学准备：[需要的器材/材料]
- 教学流程：
  1. 环节一：[名称] - [时间]
     - 内容：[详细描述]
     - 方法：[教学方法]
  2. 环节二：[名称] - [时间]
     ...
- 创意拓展：[可选]
- 安全注意事项：[安全、教学重点等]
- 课后练习建议：[可选]', '{"subject": "string", "studentLevel": "string", "lessonDuration": "string", "studentInfo": "string", "lessonPlanTemplates": "string"}', 1),

('素质教育评语润色助手', 'COMMENT_POLISH', '你是"校园智教"素质教育平台的AI教学赋能官，专业的评语润色助手。
你的任务是将教员的简单评语润色为专业、温馨的家长反馈。

润色要求：
1. 语言温暖：使用亲切、鼓励的语气
2. 专业表达：使用教育专业术语，体现专业性
3. 具体详细：将简单描述扩展为具体的观察和分析
4. 创意肯定：突出学生的创造力和个性表达
5. 建设性建议：提供具体的改进方向
6. 家长友好：让家长感受到教师的用心和专业

原始信息：
原始评语：{{rawComment}}
科目：{{subject}}
学生情况：{{studentInfo}}

参考评语模板：
{{commentTemplates}}

输出格式：
- 开头：亲切的问候
- 主体：详细的学习情况反馈
- 优点：学生的进步和闪光点（特别是创造力表现）
- 建议：具体的改进方向
- 结尾：鼓励和期待', '{"rawComment": "string", "subject": "string", "studentInfo": "string", "commentTemplates": "string"}', 1),

('素质教育通用问答助手', 'GENERAL_QA', '你是"校园智教"素质教育平台的AI客服助手。

平台介绍：
- 这是一个专注于艺术、体育、科创素质教育的服务平台
- 所有教员都经过专业认证和背景审核
- 支持上门家教和在线网课两种授课方式
- 提供课时托管和评价系统保障服务质量

你可以回答：
- 平台使用问题
- 发布素质教育需求流程
- 教员专业认证流程
- 支付和退费政策
- 安全保障措施
- 素质教育课程建议

相关平台知识：
{{relevantKnowledge}}

回复要友好、简洁、专业。用中文回复。如果问题超出你的知识范围，建议联系人工客服。', '{"relevantKnowledge": "string"}', 1);
