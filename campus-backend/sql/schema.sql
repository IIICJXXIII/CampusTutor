/*
 * CampusTutor 校园智教平台 - 完整数据库建表脚本（合并版）
 * Version: 2.0.0
 * Date: 2026-01-06
 * Description: 已与后端Java实体类字段完全匹配的DDL脚本
 * 
 * 修复记录：
 * - tutor_schedule_config: 从time_slots(JSON)改为独立字段start_time/end_time/available
 * - parent_student: name→student_name, 新增school_name/study_desc/时间戳
 * - demand_post: 使用新结构(publisher_id/longitude/latitude等)
 * - course_order: 新增order_no/student_id/tutor_profile_id/subject/grade等完整字段
 * - teaching_record: 新增create_time/update_time字段
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

-- 2.2 教员可授课时间表 (已与TutorScheduleConfig实体类匹配)
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

-- 3.1 学生档案表 (已与ParentStudent实体类匹配)
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

-- 3.2 找家教需求单表 (已与DemandPost实体类匹配)
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


-- ========================================
-- 4. 交易与订单模块 (Transaction)
-- ========================================

-- 4.1 课程订单表 (已与CourseOrder实体类匹配)
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
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待支付, 1-已支付待上课, 2-进行中, 3-已完成, 4-已取消, 5-退款中, 6-已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1钱包 2微信 3支付宝',
  `pay_trade_no` varchar(64) DEFAULT NULL COMMENT '第三方支付流水号',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
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

-- 5.1 上课打卡记录表 (已与TeachingRecord实体类匹配)
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


COMMIT;
