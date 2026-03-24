-- 完整的RAG系统设置和素质教育数据更新脚本
-- 执行顺序：
-- 1. 先创建RAG相关表（如果不存在）
-- 2. 然后清空现有业务数据并插入素质教育测试数据
-- 3. 最后插入RAG知识库数据

-- ============================================
-- 第一部分：创建RAG相关表
-- ============================================

-- 1. 创建知识库文档表（如果不存在）
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

-- 2. 创建Prompt模板表（如果不存在）
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

-- 3. 创建AI用户画像表（如果不存在）
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

-- ============================================
-- 第二部分：清空现有业务数据并插入素质教育测试数据
-- ============================================

-- 禁用外键检查，防止清空有关联的表时报错
SET FOREIGN_KEY_CHECKS = 0;

-- 清空核心业务表
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `sys_wallet`;
TRUNCATE TABLE `tutor_profile`;
TRUNCATE TABLE `tutor_schedule_config`;
TRUNCATE TABLE `parent_student`;
TRUNCATE TABLE `demand_post`;
TRUNCATE TABLE `user_action_log`;
TRUNCATE TABLE `course_order`;
TRUNCATE TABLE `sys_comment`;

-- 开启外键检查
SET FOREIGN_KEY_CHECKS = 1;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 彻底清空三张核心表，回归白纸状态
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `tutor_profile`;
TRUNCATE TABLE `user_action_log`;

-- 2. 插入测试基础账号
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar`, `gender`, `role`, `status`, `create_time`) VALUES
(1001, 'parent_a', 'e10adc3949ba59abbe56e057f20f883e', '海淀张先生', 'https://api.dicebear.com/7.x/avataaars/svg?seed=P1', 1, 2, 1, NOW()),
(1002, 'parent_b', 'e10adc3949ba59abbe56e057f20f883e', '朝阳李女士', 'https://api.dicebear.com/7.x/avataaars/svg?seed=P2', 2, 2, 1, NOW()),
(2001, 'tutor_steam', 'e10adc3949ba59abbe56e057f20f883e', '北航刘教员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=T1', 1, 1, 1, '2025-01-01 10:00:00'),
(2002, 'tutor_art', 'e10adc3949ba59abbe56e057f20f883e', '央美陈教员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=T2', 2, 1, 1, '2025-05-01 10:00:00'),
(2003, 'tutor_music', 'e10adc3949ba59abbe56e057f20f883e', '央音赵教员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=T3', 2, 1, 1, '2025-02-01 10:00:00'),
(2004, 'tutor_sport', 'e10adc3949ba59abbe56e057f20f883e', '北体王教员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=T4', 1, 1, 1, NOW());

-- 3. 为测试教员补齐画像与 LBS 坐标 (模拟真实北京坐标)
INSERT INTO `tutor_profile` (`id`, `user_id`, `real_name`, `university_name`, `teach_subjects`, `expect_price`, `can_visit`, `can_online`, `longitude`, `latitude`, `address`, `cert_status`, `rating`, `order_count`, `create_time`) VALUES
(2001, 2001, '刘蒸汽', '北京航空航天大学', '["STEAM", "少儿编程", "机器人"]', 150.00, 1, 1, 116.347, 39.982, '北京市海淀区学院路37号', 2, 4.9, 120, NOW()),
(2002, 2002, '陈艺术', '中央美术学院', '["素描", "水彩", "艺术鉴赏"]', 200.00, 1, 0, 116.476, 40.019, '北京市朝阳区花家地南街8号', 2, 4.8, 85, NOW()),
(2003, 2003, '赵音符', '中央音乐学院', '["钢琴", "小提琴", "乐理"]', 300.00, 1, 1, 116.363, 39.901, '北京市西城区鲍家街43号', 2, 5.0, 210, NOW()),
(2004, 2004, '王田径', '北京体育大学', '["中考体育", "篮球", "游泳"]', 120.00, 1, 0, 116.315, 40.030, '北京市海淀区信息路48号', 2, 4.7, 300, NOW());

-- 4. 锁定未来真实用户的起点为 1,000,000 (百万起步)
ALTER TABLE `sys_user` AUTO_INCREMENT = 1000000;
ALTER TABLE `tutor_profile` AUTO_INCREMENT = 1000000;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第三部分：插入RAG知识库数据
-- ============================================

-- 清空现有知识库数据（如果存在）
DELETE FROM `knowledge_document`;
DELETE FROM `prompt_template`;
DELETE FROM `user_profile_ai`;

-- 插入素质教育知识库文档
INSERT INTO `knowledge_document` (`title`, `content`, `doc_type`, `source`, `tags`, `target_role`, `applicable_subjects`, `applicable_grades`, `status`) VALUES
('素质教育平台服务协议', '校园智教素质教育平台服务协议：1. 平台专注于艺术、体育、科创素质教育；2. 课时费托管机制；3. 教员需完成专业认证；4. 家长可申请试课；5. 争议处理流程。', 'RULE', '平台官方', '规则,协议,素质教育', 'ALL', NULL, NULL, 1),
('如何发布素质教育需求？', '发布素质教育需求步骤：1. 登录家长账号；2. 进入发布需求页面；3. 选择素质教育类别（艺术/体育/科创）；4. 填写学生兴趣和基础；5. 设置期望价格；6. 选择授课方式；7. 确认发布。', 'FAQ', '平台帮助中心', '需求发布,家长指南,素质教育', 'PARENT', NULL, NULL, 1),
('钢琴启蒙教案模板', '钢琴启蒙教案结构：1. 教学目标（识谱、手型、节奏）；2. 教学重点难点；3. 教学过程（热身、基础练习、曲目学习、音乐欣赏）；4. 家庭练习建议；5. 教学反思。', 'LESSON_PLAN', '优秀教员分享', '钢琴,音乐,艺术,教案', 'TEACHER', '钢琴,音乐', '启蒙,初级', 1),
('素质教育学生评语模板', '素质教育评语要素：1. 肯定学生兴趣和创造力；2. 具体指出艺术/体育/科创方面的进步；3. 提出个性化发展建议；4. 表达鼓励和期待；5. 语言温暖专业。', 'COMMENT', '教学经验分享', '评语,反馈,素质教育', 'TEACHER', NULL, NULL, 1),
('素质教育教员认证流程', '素质教育教员认证步骤：1. 提交专业背景信息；2. 上传相关证书/作品集；3. 填写教学理念和经验；4. 等待专业审核（1-3个工作日）；5. 审核通过后即可接单。', 'FAQ', '平台帮助中心', '认证,教员指南,素质教育', 'TEACHER', NULL, NULL, 1),
('家长素质教育安全保障', '家长素质教育安全保障措施：1. 教员专业认证；2. 课时费托管；3. 不满意可申请退款；4. 客服全程跟进；5. 紧急联系渠道；6. 教学环境安全评估。', 'RULE', '平台官方', '安全,保障,家长,素质教育', 'PARENT', NULL, NULL, 1),
('少儿编程教学要点', '少儿编程教学核心：1. 培养计算思维；2. 项目式学习；3. 游戏化教学；4. 鼓励创造力；5. 安全上网教育；6. 团队协作能力培养。', 'TEACHING_EXPERIENCE', '教学专家分享', '编程,STEAM,科创,教学', 'TEACHER', '编程,STEAM', '小学,初中', 1),
('中考体育训练指南', '中考体育训练要点：1. 科学训练计划；2. 体能测试评估；3. 技术动作规范；4. 营养与恢复；5. 心理素质培养；6. 安全防护措施。', 'TEACHING_EXPERIENCE', '体育专家分享', '体育,中考,训练,健康', 'TEACHER', '体育,中考体育', '初中', 1);

-- 插入素质教育Prompt模板
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

-- 插入素质教育用户画像（示例数据）
INSERT INTO `user_profile_ai` (`user_id`, `role`, `teaching_style`, `expert_subjects`, `teaching_experience`, `student_grade`, `learning_style`, `weak_subjects`, `learning_needs`, `preferences`, `personalization_settings`) VALUES
(2001, 'TEACHER', 'PROJECT_BASED', '["STEAM", "少儿编程", "机器人"]', 'ADVANCED', NULL, NULL, NULL, NULL, '{"communicationPreference": "详细", "responseStyle": "专业", "teachingFocus": "创造力培养"}', '{"responseLength": "detailed", "tone": "professional", "focusArea": "creativity"}'),
(2002, 'TEACHER', 'CREATIVE', '["素描", "水彩", "艺术鉴赏"]', 'INTERMEDIATE', NULL, NULL, NULL, NULL, '{"communicationPreference": "艺术化", "responseStyle": "创意", "teachingFocus": "审美培养"}', '{"responseLength": "moderate", "tone": "creative", "focusArea": "aesthetics"}'),
(2003, 'TEACHER', 'MUSICAL', '["钢琴", "小提琴", "乐理"]', 'ADVANCED', NULL, NULL, NULL, NULL, '{"communicationPreference": "音乐化", "responseStyle": "优雅", "teachingFocus": "音乐素养"}', '{"responseLength": "moderate", "tone": "elegant", "focusArea": "music"}'),
(2004, 'TEACHER', 'ACTIVE', '["中考体育", "篮球", "游泳"]', 'INTERMEDIATE', NULL, NULL, NULL, NULL, '{"communicationPreference": "简洁", "responseStyle": "活力", "teachingFocus": "体能提升"}', '{"responseLength": "concise", "tone": "energetic", "focusArea": "fitness"}'),
(1001, 'PARENT', NULL, NULL, NULL, '小学五年级', 'HANDS_ON', '["艺术基础", "体育协调性"]', '{"targetGoal": "培养兴趣爱好", "focusAreas": "艺术启蒙,体育锻炼"}', '{"communicationPreference": "简洁", "responseStyle": "友好"}', '{"responseLength": "concise", "tone": "friendly"}'),
(1002, 'PARENT', NULL, NULL, NULL, '初中二年级', 'LOGICAL', '["编程基础", "科学思维"]', '{"targetGoal": "科创能力培养", "focusAreas": "编程思维,科学实验"}', '{"communicationPreference": "详细", "responseStyle": "专业"}', '{"responseLength": "detailed", "tone": "professional"}');

-- 完成更新
SELECT 'RAG系统设置和素质教育数据更新完成' AS result;

SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. 清空所有核心业务表，回归绝对纯净状态
-- =======================================================
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `sys_wallet`;
TRUNCATE TABLE `sys_transaction_flow`;
TRUNCATE TABLE `tutor_profile`;
TRUNCATE TABLE `parent_student`;
TRUNCATE TABLE `demand_post`;
TRUNCATE TABLE `course_order`;
TRUNCATE TABLE `teaching_record`;
TRUNCATE TABLE `user_action_log`;
TRUNCATE TABLE `community_post`;

-- =======================================================
-- 2. 基础用户数据 (Admin x1, 家长 x3, 教员 x5)
-- 密码统一为 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
-- =======================================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `role`, `status`, `create_time`) VALUES
                                                                                                       (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统超管', 0, 1, NOW()),
                                                                                                       (1001, 'parent_a', 'e10adc3949ba59abbe56e057f20f883e', '海淀张先生', 2, 1, NOW()),
                                                                                                       (1002, 'parent_b', 'e10adc3949ba59abbe56e057f20f883e', '朝阳李女士', 2, 1, NOW()),
                                                                                                       (1003, 'parent_c', 'e10adc3949ba59abbe56e057f20f883e', '顺义王先生', 2, 1, NOW()),
                                                                                                       (2001, 'tutor_code', 'e10adc3949ba59abbe56e057f20f883e', '北航刘教员', 1, 1, '2025-01-01 10:00:00'),
                                                                                                       (2002, 'tutor_art', 'e10adc3949ba59abbe56e057f20f883e', '央美陈教员', 1, 1, '2025-05-01 10:00:00'),
                                                                                                       (2003, 'tutor_music', 'e10adc3949ba59abbe56e057f20f883e', '央音赵教员', 1, 1, '2025-02-01 10:00:00'),
                                                                                                       (2004, 'tutor_sport', 'e10adc3949ba59abbe56e057f20f883e', '北体王教员', 1, 1, NOW()),
                                                                                                       (2005, 'tutor_new', 'e10adc3949ba59abbe56e057f20f883e', '未认证萌新', 1, 1, NOW());

-- =======================================================
-- 3. 钱包与资金流水 (测试支付、托管、退款状态机)
-- =======================================================
INSERT INTO `sys_wallet` (`user_id`, `balance`, `frozen_amount`) VALUES
                                                                     (1001, 1500.00, 360.00), -- 充值了点钱，且有360元冻结在进行中的订单里
                                                                     (1002, 50.00, 0.00),     -- 余额不足，用于测试拦截
                                                                     (2001, 800.00, 0.00),    -- 教员已提现或结算的余额
                                                                     (2002, 0.00, 400.00);    -- 教员有400元待结算(进行中)

-- =======================================================
-- 4. 素质教育教员画像 (精准 LBS 投放与科目对齐)
-- 包含：1个未认证(用于测试隐身)、不同价格段、不同距离
-- =======================================================
INSERT INTO `tutor_profile` (`id`, `user_id`, `real_name`, `university_name`, `teach_subjects`, `teach_grades`, `expect_price`, `can_visit`, `can_online`, `longitude`, `latitude`, `address`, `cert_status`, `rating`, `order_count`) VALUES
                                                                                                                                                                                                                                           (2001, 2001, '刘代码', '北京航空航天大学', '["Python", "C++", "少儿编程"]', '["小学", "初中"]', 180.00, 1, 1, 116.317, 39.980, '北京市海淀区中关村', 2, 4.9, 120),
                                                                                                                                                                                                                                           (2002, 2002, '陈速写', '中央美术学院', '["素描", "水彩", "创意美术"]', '["全科"]', 200.00, 1, 0, 116.460, 39.920, '北京市朝阳区大望路', 2, 4.8, 85),
                                                                                                                                                                                                                                           (2003, 2003, '赵钢琴', '中央音乐学院', '["钢琴", "乐理", "声乐"]', '["小学", "初中", "高中"]', 350.00, 1, 1, 116.363, 39.901, '北京市西城区复兴门', 2, 5.0, 210),
                                                                                                                                                                                                                                           (2004, 2004, '王游泳', '北京体育大学', '["游泳", "体能训练", "中考体育"]', '["初中", "高中"]', 150.00, 1, 0, 116.650, 40.130, '北京市顺义区马坡 (远距离)', 2, 4.7, 30),
                                                                                                                                                                                                                                           (2005, 2005, '李黑客', '清华大学', '["信息学奥赛"]', '["高中"]', 500.00, 0, 1, 116.320, 40.000, '北京市海淀区五道口', 0, 0.0, 0); -- cert_status=0 待提交

-- =======================================================
-- 5. 家长学生档案
-- =======================================================
INSERT INTO `parent_student` (`id`, `parent_id`, `student_name`, `gender`, `grade`, `weak_subjects`) VALUES
                                                                                                         (1, 1001, '小张', 1, '小学四年级', '["逻辑思维"]'),
                                                                                                         (2, 1002, '小李', 0, '初中二年级', '["色彩感"]');

-- =======================================================
-- 6. 家长需求大厅 (测试匹配与 LBS 抢单)
-- =======================================================
INSERT INTO `demand_post` (`id`, `publisher_id`, `student_id`, `title`, `subject`, `grade`, `expect_price`, `teach_mode`, `longitude`, `latitude`, `status`) VALUES
                                                                                                                                                                 (1, 1001, 1, '海淀寻 Python 启蒙老师', 'Python', '小学', 180.00, 3, 116.317, 39.980, 1), -- 状态 1：上架中，完美匹配刘教员(2001)
                                                                                                                                                                 (2, 1002, 2, '朝阳急寻美术特长陪练', '素描', '初中', 200.00, 1, 116.460, 39.920, 2); -- 状态 2：已匹配，用于关联进行中的订单

-- =======================================================
-- 7. 核心交易订单表 (测试业务状态机)
-- =======================================================
INSERT INTO `course_order` (`id`, `order_no`, `parent_id`, `tutor_id`, `demand_id`, `subject`, `teach_mode`, `unit_price`, `total_hours`, `total_amount`, `used_hours`, `status`, `pay_type`) VALUES
                                                                                                                                                                                                  (1, 'ORD20260324001', 1002, 2002, 2, '素描', 1, 200.00, 2, 400.00, 1, 2, 1), -- 状态 2：进行中 (已上1节课)
                                                                                                                                                                                                  (2, 'ORD20260324002', 1001, 2003, NULL, '钢琴', 2, 350.00, 1, 350.00, 1, 3, 2), -- 状态 3：已完成 (微信支付)
                                                                                                                                                                                                  (3, 'ORD20260324003', 1003, 2004, NULL, '游泳', 1, 150.00, 5, 750.00, 0, 0, NULL),-- 状态 0：待支付
                                                                                                                                                                                                  (4, 'ORD20260324004', 1001, 2001, NULL, 'Python', 2, 180.00, 2, 360.00, 0, 1, 1); -- 状态 1：已支付待上课 (资金被冻结)

-- =======================================================
-- 8. 教学过程打卡 (配合进行中的订单 ORD...001)
-- =======================================================
INSERT INTO `teaching_record` (`id`, `order_id`, `lesson_index`, `start_time`, `end_time`, `status`, `content_summary`) VALUES
    (1, 1, 1, '2026-03-20 14:00:00', '2026-03-20 15:00:00', 1, '基础素描排线训练，学生掌握较快');

-- =======================================================
-- 9. 行为日志 (喂给协同过滤 CF 和实时意图 Intent)
-- =======================================================
INSERT INTO `user_action_log` (`user_id`, `target_id`, `action_type`, `duration`) VALUES
                                                                                      (1001, 2001, 1, 120), -- 张先生查看刘代码 120秒
                                                                                      (1001, 2001, 3, 0),   -- 张先生收藏了刘代码
                                                                                      (1001, 2003, 1, 45),  -- 张先生查看赵钢琴 45秒
                                                                                      (1002, 2002, 1, 300); -- 李女士深度查看陈速写

-- =======================================================
-- 10. 社区交互测试数据
-- =======================================================
INSERT INTO `community_post` (`id`, `user_id`, `topic_type`, `title`, `content`, `view_count`, `like_count`) VALUES
    (1, 2001, 1, '如何引导小学生入门 Python？', '重点在于图形化展示和游戏化逻辑，少讲语法多做案例...', 256, 45);

-- 锁定未来真实自增起点为 1,000,000
ALTER TABLE `sys_user` AUTO_INCREMENT = 1000000;
ALTER TABLE `tutor_profile` AUTO_INCREMENT = 1000000;

SET FOREIGN_KEY_CHECKS = 1;