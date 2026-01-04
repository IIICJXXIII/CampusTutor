/*
 * CampusTutor 校园智教平台 - 完整数据库脚本
 * Version: 1.0.0
 * Date: 2026-01-04
 * Description: 包含用户、教员、需求、订单、教学、社区及系统配置全量表结构
 */

-- ----------------------------
-- 0. 数据库初始化
-- ----------------------------
DROP DATABASE IF EXISTS `campus_tutor_db`;
CREATE DATABASE `campus_tutor_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `campus_tutor_db`;

-- ----------------------------
-- 1. 用户与权限模块 (User & Auth)
-- ----------------------------

-- 1.1 系统用户表 (所有角色的登录凭证)
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '用户名/手机号',
  `password` varchar(128) NOT NULL COMMENT '加密密码',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` tinyint NOT NULL COMMENT '角色: 0-管理员, 1-教员, 2-家长',
  `openid` varchar(64) DEFAULT NULL COMMENT '微信OpenID (小程序用)',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-正常, 0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB COMMENT='系统用户表';

-- 1.2 钱包表 (资金托管与结算)
CREATE TABLE `sys_wallet` (
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '可用余额',
  `frozen_amount` decimal(10,2) DEFAULT '0.00' COMMENT '冻结金额(担保交易中)',
  `pay_password` varchar(128) DEFAULT NULL COMMENT '支付密码(加密)',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB COMMENT='用户钱包表';

-- 1.3 资金流水表 (新增: 用于财务审计)
CREATE TABLE `sys_transaction_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '变动金额 (正数收入, 负数支出)',
  `balance_after` decimal(10,2) NOT NULL COMMENT '变动后余额 (快照)',
  `flow_type` tinyint NOT NULL COMMENT '类型: 1-充值, 2-支付订单, 3-课时费解冻收入, 4-提现, 5-退款',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID (可为空)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注 (如: 订单1001课时费结算)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB COMMENT='资金流水记录表';

-- 1.4 提现申请表 (新增: 教员提现)
CREATE TABLE `sys_withdrawal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `channel` tinyint DEFAULT '1' COMMENT '1-微信, 2-支付宝, 3-银行卡',
  `account_no` varchar(64) NOT NULL COMMENT '收款账号',
  `status` tinyint DEFAULT '0' COMMENT '0-审核中, 1-已打款, 2-驳回',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='提现申请表';


-- ----------------------------
-- 2. 教员中心模块 (Tutor Center)
-- ----------------------------

-- 2.1 教员详细档案与认证表
CREATE TABLE `tutor_profile` (
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `real_name` varchar(32) DEFAULT NULL COMMENT '真实姓名(OCR识别)',
  `id_card_no` varchar(32) DEFAULT NULL COMMENT '身份证号(加密存储)',
  `university` varchar(64) DEFAULT NULL COMMENT '就读高校',
  `major` varchar(64) DEFAULT NULL COMMENT '专业',
  `student_id_img` varchar(255) DEFAULT NULL COMMENT '学生证/学信网截图',
  `degree_cert_img` varchar(255) DEFAULT NULL COMMENT '学历/学位证书',
  `teach_cert_img` varchar(255) DEFAULT NULL COMMENT '教师资格证',
  `video_url` varchar(255) DEFAULT NULL COMMENT '试讲视频URL',
  `bio` text COMMENT '自我介绍',
  `tags` json DEFAULT NULL COMMENT '标签: ["严厉", "幽默", "奥数"]',
  `verify_status` tinyint DEFAULT '0' COMMENT '认证状态: 0-未认证, 1-审核中, 2-已认证, 3-驳回',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `audit_time` datetime DEFAULT NULL COMMENT '最近审核时间',
  `credit_score` int DEFAULT '100' COMMENT '信用分 (初始100)',
  `star_rating` decimal(2,1) DEFAULT '5.0' COMMENT '综合评分(1-5星)',
  `lat` decimal(10,6) DEFAULT NULL COMMENT '常驻地纬度(LBS)',
  `lng` decimal(10,6) DEFAULT NULL COMMENT '常驻地经度(LBS)',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB COMMENT='教员档案认证表';

-- 2.2 教员可授课时间表
CREATE TABLE `tutor_schedule_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '教员ID',
  `day_of_week` tinyint NOT NULL COMMENT '周几: 1-7',
  `time_slots` json NOT NULL COMMENT '空闲时段JSON: ["08:00-10:00", "14:00-16:00"]',
  PRIMARY KEY (`id`),
  KEY `idx_user_day` (`user_id`, `day_of_week`)
) ENGINE=InnoDB COMMENT='教员排课配置表';


-- ----------------------------
-- 3. 家长需求与匹配模块 (Demand & Matching)
-- ----------------------------

-- 3.1 学生档案表
CREATE TABLE `parent_student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL COMMENT '家长用户ID',
  `name` varchar(32) DEFAULT NULL COMMENT '学生昵称',
  `gender` tinyint DEFAULT '1' COMMENT '1-男, 2-女',
  `grade` varchar(32) DEFAULT NULL COMMENT '年级 (如: 小学三年级)',
  `character_tags` varchar(255) DEFAULT NULL COMMENT '性格标签',
  `weak_subjects` varchar(255) DEFAULT NULL COMMENT '薄弱科目',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='学生档案表';

-- 3.2 找家教需求单表 (LBS核心表)
CREATE TABLE `demand_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `subject` varchar(32) NOT NULL COMMENT '科目',
  `grade_require` varchar(32) NOT NULL COMMENT '年级要求',
  `price_low` int NOT NULL COMMENT '最低预算',
  `price_high` int NOT NULL COMMENT '最高预算',
  `gender_require` tinyint DEFAULT '0' COMMENT '性别要求: 0-不限',
  `frequency` varchar(64) DEFAULT NULL COMMENT '频次: 每周2次',
  `address_detail` varchar(255) DEFAULT NULL COMMENT '详细地址(对教员脱敏)',
  `address_region` varchar(64) DEFAULT NULL COMMENT '区域(对外展示)',
  `lat` decimal(10,6) NOT NULL COMMENT '纬度',
  `lng` decimal(10,6) NOT NULL COMMENT '经度',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-发布中, 2-已关闭',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lat_lng` (`lat`, `lng`) COMMENT '地理位置索引'
) ENGINE=InnoDB COMMENT='家教需求发布表';

-- 3.3 用户浏览/搜索记录表 (新增: 用于Python推荐算法数据源)
CREATE TABLE `user_action_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '家长ID',
  `target_id` bigint NOT NULL COMMENT '被查看的教员ID 或 搜索的关键词ID',
  `action_type` tinyint DEFAULT '1' COMMENT '1-查看教员详情, 2-搜索科目, 3-收藏教员',
  `duration` int DEFAULT '0' COMMENT '停留时长(秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_action` (`user_id`, `action_type`)
) ENGINE=InnoDB COMMENT='用户行为轨迹表';


-- ----------------------------
-- 4. 交易与订单模块 (Transaction)
-- ----------------------------

-- 4.1 课程订单表
CREATE TABLE `course_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单号',
  `parent_id` bigint NOT NULL,
  `tutor_id` bigint NOT NULL,
  `demand_id` bigint NOT NULL COMMENT '来源需求ID',
  `course_type` tinyint DEFAULT '1' COMMENT '类型: 1-单次, 2-课时包',
  `total_lessons` int DEFAULT '1' COMMENT '购买总课时数',
  `remain_lessons` int DEFAULT '1' COMMENT '剩余课时数',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价(托管金额)',
  `status` tinyint DEFAULT '0' COMMENT '0-待支付, 1-已支付(托管中), 2-进行中, 3-已完成, 4-退款中, 5-已退款',
  `contract_url` varchar(255) DEFAULT NULL COMMENT 'PDF电子合同地址',
  `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '已退款金额',
  `refund_reason` varchar(255) DEFAULT NULL COMMENT '退款原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_tutor` (`tutor_id`)
) ENGINE=InnoDB COMMENT='课程订单表';

-- 4.2 保险单表
CREATE TABLE `insurance_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `policy_no` varchar(64) DEFAULT NULL COMMENT '保险单号',
  `provider` varchar(64) DEFAULT 'PingAn' COMMENT '保险公司',
  `status` tinyint DEFAULT '1' COMMENT '1-生效中, 2-已过期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='保险单记录表';


-- ----------------------------
-- 5. 教学过程管控模块 (Process Control)
-- ----------------------------

-- 5.1 上课打卡记录表
CREATE TABLE `teaching_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `lesson_index` int NOT NULL COMMENT '第几节课',
  `start_time` datetime DEFAULT NULL COMMENT '实际上课时间',
  `end_time` datetime DEFAULT NULL COMMENT '实际下课时间',
  `clock_in_lat` decimal(10,6) DEFAULT NULL COMMENT '打卡纬度',
  `clock_in_lng` decimal(10,6) DEFAULT NULL COMMENT '打卡经度',
  `clock_in_img` varchar(255) DEFAULT NULL COMMENT '现场拍照(水印)',
  `content_summary` text COMMENT '教学内容摘要',
  `homework_assigned` text COMMENT '布置作业',
  `status` tinyint DEFAULT '0' COMMENT '0-待确认, 1-家长已确认, 2-异常/申诉',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB COMMENT='课时打卡记录表';

-- 5.2 阶段学习报告表
CREATE TABLE `student_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `report_type` tinyint DEFAULT '1' COMMENT '1-月度报告, 2-阶段总结',
  `score_chart_data` json DEFAULT NULL COMMENT '成绩变化数据(ECharts JSON)',
  `tutor_comment` text COMMENT '老师评语',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='学生阶段报告表';

-- 5.3 错题本表
CREATE TABLE `mistake_notebook` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `subject` varchar(32) DEFAULT NULL,
  `question_img` varchar(255) NOT NULL COMMENT '题目图片',
  `tags` varchar(255) DEFAULT NULL COMMENT '知识点标签',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='在线错题本';


-- ----------------------------
-- 6. 系统、社区与交互模块 (System & Interaction)
-- ----------------------------

-- 6.1 聊天消息表
CREATE TABLE `sys_chat_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `content` text COMMENT '消息内容',
  `msg_type` tinyint DEFAULT '1' COMMENT '1-文本, 2-图片, 3-简历卡片, 4-订单邀约',
  `is_read` tinyint DEFAULT '0' COMMENT '0-未读, 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chat` (`sender_id`, `receiver_id`)
) ENGINE=InnoDB COMMENT='IM聊天记录表';

-- 6.2 评价表
CREATE TABLE `sys_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `from_user_id` bigint NOT NULL COMMENT '评论人',
  `to_user_id` bigint NOT NULL COMMENT '被评人',
  `score` tinyint DEFAULT '5' COMMENT '星级 1-5',
  `content` varchar(512) DEFAULT NULL,
  `tags` varchar(255) DEFAULT NULL COMMENT '评价标签: 准时, 讲课好',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='订单评价表';

-- 6.3 数据字典表 (新增: 系统配置)
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_code` varchar(64) NOT NULL COMMENT '类型编码: subject, grade, tag',
  `label` varchar(64) NOT NULL COMMENT '展示名: 数学, 高三',
  `value` varchar(64) NOT NULL COMMENT '存储值: math, grade_3',
  `sort` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type_code`)
) ENGINE=InnoDB COMMENT='数据字典表';

-- 6.4 社区帖子表 (新增: 增加用户粘性)
CREATE TABLE `community_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `topic_type` tinyint DEFAULT '1' COMMENT '1-经验分享, 2-难题求助',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `images` json DEFAULT NULL COMMENT '图片列表',
  `view_count` int DEFAULT '0',
  `like_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='社区帖子表';

-- 6.5 社区评论表 (新增)
CREATE TABLE `community_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` varchar(512),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='社区评论表';


COMMIT;