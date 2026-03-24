-- V3: 添加RAG增强AI功能相关表（素质教育版本）
-- 创建时间: 2026-03-24
-- 作者: AI助手
-- 更新说明: 适配素质教育转型，删除传统学科数据，添加素质教育数据

-- 1. 知识库文档表
CREATE TABLE IF NOT EXISTS `knowledge_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
    `content` TEXT NOT NULL COMMENT '文档内容',
    `doc_type` VARCHAR(50) NOT NULL COMMENT '文档类型: RULE/FAQ/LESSON_PLAN/COMMENT/OTHER',
    `source` VARCHAR(100) COMMENT '来源',
    `tags` VARCHAR(500) COMMENT '标签，逗号分隔',
    `target_role` VARCHAR(20) DEFAULT 'ALL' COMMENT '适用角色: TEACHER/PARENT/ALL',
    `applicable_subjects` VARCHAR(500) COMMENT '适用科目，逗号分隔',
    `applicable_grades` VARCHAR(500) COMMENT '适用年级，逗号分隔',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_doc_type` (`doc_type`),
    INDEX `idx_target_role` (`target_role`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- 2. Prompt模板表
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `scene` VARCHAR(50) NOT NULL COMMENT '场景标识',
    `template` TEXT NOT NULL COMMENT '模板内容',
    `variables` TEXT COMMENT '变量定义(JSON格式)',
    `examples` TEXT COMMENT '示例对话(JSON格式)',
    `constraints` TEXT COMMENT '约束条件',
    `output_format` TEXT COMMENT '输出格式',
    `version` INT DEFAULT 1 COMMENT '版本号',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `average_rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scene_version` (`scene`, `version`),
    INDEX `idx_scene` (`scene`),
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt模板表';

-- 3. AI用户画像表
CREATE TABLE IF NOT EXISTS `user_profile_ai` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '用户角色: TEACHER/PARENT',
    `teaching_style` VARCHAR(50) COMMENT '教学风格(教员)',
    `expert_subjects` TEXT COMMENT '擅长科目(教员, JSON格式)',
    `teaching_experience` VARCHAR(50) COMMENT '教学经验: BEGINNER/INTERMEDIATE/ADVANCED',
    `student_grade` VARCHAR(50) COMMENT '学生年级(家长)',
    `learning_style` VARCHAR(50) COMMENT '学习习惯(家长)',
    `weak_subjects` TEXT COMMENT '薄弱科目(家长, JSON格式)',
    `learning_needs` TEXT COMMENT '学习需求(家长, JSON格式)',
    `preferences` TEXT COMMENT '偏好设置(JSON格式)',
    `personalization_settings` TEXT COMMENT '个性化设置(JSON格式)',
    `interaction_summary` TEXT COMMENT '交互历史摘要',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_role` (`role`),
    INDEX `idx_teaching_style` (`teaching_style`),
    INDEX `idx_student_grade` (`student_grade`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI用户画像表';

-- 4. AI交互历史表
CREATE TABLE IF NOT EXISTS `ai_interaction_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT COMMENT '用户ID',
    `scene` VARCHAR(50) NOT NULL COMMENT '场景',
    `query` TEXT NOT NULL COMMENT '用户查询',
    `response` TEXT NOT NULL COMMENT 'AI响应',
    `rag_enabled` TINYINT DEFAULT 0 COMMENT '是否启用RAG',
    `rag_score` DECIMAL(3,2) COMMENT 'RAG相关性分数',
    `tokens_used` INT COMMENT '使用的token数量',
    `response_time_ms` INT COMMENT '响应时间(毫秒)',
    `user_rating` TINYINT COMMENT '用户评分(1-5)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_scene` (`scene`),
    INDEX `idx_created_time` (`created_time`),
    INDEX `idx_rag_enabled` (`rag_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI交互历史表';

-- 5. 知识库向量表（预留，用于未来向量检索）
CREATE TABLE IF NOT EXISTS `knowledge_vector` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `vector_data` BLOB COMMENT '向量数据',
    `vector_dimension` INT COMMENT '向量维度',
    `embedding_model` VARCHAR(50) COMMENT '嵌入模型',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_document_id` (`document_id`),
    FOREIGN KEY (`document_id`) REFERENCES `knowledge_document`(`id`) ON DELETE CASCADE,
    INDEX `idx_embedding_model` (`embedding_model`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库向量表';

-- 插入素质教育初始数据
-- 1. 插入素质教育知识库文档
INSERT INTO `knowledge_document` (`title`, `content`, `doc_type`, `source`, `tags`, `target_role`, `applicable_subjects`, `applicable_grades`, `status`) VALUES
('平台素质教育服务协议', '校园智教平台素质教育服务协议：1. 平台专注于艺术、体育、科创素质教育；2. 收取10%服务费用于质量保障；3. 教员需完成专业能力认证；4. 家长可申请不满意退款；5. 提供专业场地推荐服务。', 'RULE', '平台官方', '素质教育,规则,协议,费用', 'ALL', NULL, NULL, 1),
('如何发布素质教育需求？', '发布素质教育需求步骤：1. 登录家长账号；2. 进入发布需求页面；3. 选择素质教育类别（艺术/体育/科创）；4. 选择具体科目和技能水平；5. 设置期望价格和授课方式；6. 填写学生兴趣和基础；7. 确认发布。', 'FAQ', '平台帮助中心', '需求发布,家长指南,素质教育', 'PARENT', NULL, NULL, 1),
('钢琴陪练教案模板', '钢琴陪练教案结构：1. 教学目标（指法、节奏、表现力）；2. 教学重点难点；3. 教学过程（热身练习、曲目练习、技巧训练、音乐欣赏）；4. 家庭练习建议；5. 教学反思与调整。', 'LESSON_PLAN', '优秀钢琴教员分享', '钢琴,乐器,艺术,教案', 'TEACHER', '钢琴/乐器陪练', '小学,初中,高中', 1),
('少儿编程教案模板', '少儿编程教案结构：1. 教学目标（逻辑思维、创造力、问题解决）；2. 教学重点难点；3. 教学过程（项目引入、代码讲解、实践操作、作品展示）；4. 拓展练习；5. 教学评估。', 'LESSON_PLAN', '优秀编程教员分享', '编程,科创,STEAM,教案', 'TEACHER', '少儿编程(Scratch/Python)', '小学,初中', 1),
('体育训练教案模板', '体育训练教案结构：1. 教学目标（体能、技能、团队合作）；2. 教学重点难点；3. 教学过程（热身活动、技能训练、对抗练习、放松拉伸）；4. 安全注意事项；5. 训练计划建议。', 'LESSON_PLAN', '优秀体育教员分享', '体育,健康,训练,教案', 'TEACHER', '中考体育专项,羽毛球/网球陪练,篮球/足球指导', '小学,初中,高中', 1),
('素质教育评语模板', '素质教育评语要素：1. 肯定学生在艺术/体育/科创方面的进步；2. 具体指出技能提升和创造力表现；3. 提出个性化改进建议；4. 鼓励持续探索和兴趣培养；5. 语言温暖专业，体现素质教育理念。', 'COMMENT', '教学经验分享', '评语,反馈,家长沟通,素质教育', 'TEACHER', NULL, NULL, 1),
('教员专业认证流程', '教员专业认证步骤：1. 提交基本信息和个人专长；2. 上传相关证书/作品集；3. 填写教学经验和教学理念；4. 参加平台专业能力评估；5. 等待审核（1-3个工作日）；6. 审核通过后即可接单。', 'FAQ', '平台帮助中心', '认证,教员指南,专业能力', 'TEACHER', NULL, NULL, 1),
('素质教育安全保障', '素质教育安全保障措施：1. 教员专业能力认证；2. 课时费托管保障；3. 专业场地安全评估；4. 不满意可申请退款；5. 客服全程跟进；6. 紧急情况处理预案。', 'RULE', '平台官方', '安全,保障,家长,素质教育', 'PARENT', NULL, NULL, 1);

-- 2. 插入素质教育Prompt模板
INSERT INTO `prompt_template` (`name`, `scene`, `template`, `variables`, `examples`, `is_active`) VALUES
('素质教育需求咨询助手', 'DEMAND_CONSULT', '你是"校园智教"素质教育平台的AI助手。你的任务是帮助家长发布素质教育需求。

平台功能介绍：
1. 平台专注于艺术、体育、科创三大类素质教育
2. 家长可以发布需求，描述孩子的兴趣、基础、学习目标
3. 系统会智能匹配合适的专业教员
4. 提供专业场地推荐和教学资源支持

你需要：
1. 引导家长选择素质教育类别（艺术/体育/科创）
2. 询问具体科目需求和技能水平
3. 了解孩子的兴趣特点和基础情况
4. 确认授课方式（上门/场馆/网课）和时间安排
5. 收集完信息后，告知家长可以提交需求了

当前用户信息：
角色：{{userRole}}
学生年级：{{studentGrade}}
兴趣科目：{{interestSubjects}}
技能水平：{{skillLevel}}

回复要简洁友好，体现素质教育理念。用中文回复。', '{"userRole": "string", "studentGrade": "string", "interestSubjects": "string", "skillLevel": "string"}', '[{"user": "我想给孩子找钢琴老师", "assistant": "您好！很高兴为您服务。请问孩子目前钢琴学习处于什么水平？是零基础、有基础还是考级冲刺？"}, {"user": "孩子零基础，小学三年级", "assistant": "好的，零基础小学三年级。请问您希望老师侧重哪些方面？是兴趣培养、基础训练还是考级准备？"}]', 1),

('素质教育教员推荐助手', 'TUTOR_RECOMMEND', '你是"校园智教"素质教育平台的AI助手。你的任务是帮助家长了解和选择合适的素质教育教员。

你需要：
1. 解答关于教员专业资质、认证流程的问题
2. 说明平台的教员筛选标准和专业评估
3. 帮助家长理解如何查看教员作品集和评价
4. 解释试课、签约、退费等流程
5. 提供专业场地和安全保障信息

平台规则：
{{platformRules}}

回复要专业、简洁，体现素质教育专业性。用中文回复。', '{"platformRules": "string"}', '[{"user": "钢琴老师需要什么资质？", "assistant": "我们的钢琴教员都需要通过专业能力认证，包括：1. 音乐相关专业学历或考级证书；2. 教学经验证明；3. 作品集或演奏视频；4. 教学理念陈述。平台会进行综合评估后认证。"}]', 1),

('素质教育教案生成助手', 'LESSON_PLAN', '你是"校园智教"平台的AI教学赋能官，专业的素质教育教案生成助手。
你的任务是为素质教育教员生成详细、创新的课程教案。

教案要求：
1. 结构清晰：包含热身、技能训练、创意实践、总结等环节
2. 趣味性强：融入游戏、项目、实践等元素
3. 个性化设计：根据学生水平和兴趣特点定制内容
4. 安全性高：充分考虑教学安全和场地要求
5. 评估科学：设计合理的教学评估方式

教学信息：
科目：{{subject}}
学生水平：{{studentLevel}}
课时时长：{{lessonDuration}}
学生情况：{{studentInfo}}
教学场地：{{teachingVenue}}

参考教案模板：
{{lessonPlanTemplates}}

输出格式：
- 教案标题
- 适用学生：[学生情况]
- 教学目标：[具体目标，分技能、情感、认知维度]
- 教学准备：[需要的器材/材料/场地]
- 安全注意事项：[重点安全提示]
- 教学流程：
  1. 环节一：[名称] - [时间]
     - 内容：[详细描述]
     - 方法：[教学方法]
     - 评估：[如何评估学习效果]
  2. 环节二：[名称] - [时间]
     ...
- 课后延伸：[可选的家庭练习或拓展活动]
- 教学反思提示：[教员可记录的重点]', '{"subject": "string", "studentLevel": "string", "lessonDuration": "string", "studentInfo": "string", "teachingVenue": "string", "lessonPlanTemplates": "string"}', '[{"subject": "钢琴陪练", "studentLevel": "有基础", "output": "生成包含指法训练、曲目练习、音乐欣赏的完整教案"}]', 1),

('素质教育评语润色助手', 'COMMENT_POLISH', '你是"校园智教"平台的AI教学赋能官，专业的素质教育评语润色助手。
你的任务是将教员的简单评语润色为专业、温馨的素质教育反馈。

润色要求：
1. 语言温暖：使用亲切、鼓励的语气，体现对学生的关爱
2. 专业表达：使用素质教育专业术语，体现专业性
3. 具体详细：将简单描述扩展为具体的技能观察和进步分析
4. 正面引导：突出学生在创造力、合作精神、毅力等方面的进步
5. 建设性建议：提供个性化的改进方向和发展建议
6. 家长友好：让家长感受到教师的用心和专业，理解素质教育价值

原始信息：
原始评语：{{rawComment}}
科目：{{subject}}
学生情况：{{studentInfo}}
教学重点：{{teachingFocus}}

参考评语模板：
{{commentTemplates}}

输出格式：
- 开头：亲切的问候和总体评价
- 主体：详细的学习情况反馈（分技能、态度、创造力等方面）
- 亮点：学生的进步和闪光点（具体事例）
- 建议：个性化的改进方向和发展建议
- 结尾：鼓励和期待，体现素质教育理念', '{"rawComment": "string", "subject": "string", "studentInfo": "string", "teachingFocus": "string", "commentTemplates": "string"}', '[{"rawComment": "学生上课认真，练习积极", "output": "润色为包含具体进步描述和发展建议的专业评语"}]', 1),

('素质教育通用问答助手', 'GENERAL_QA', '你是"校园智教"素质教育平台的AI客服助手。

平台介绍：
- 这是一个专注于艺术、体育、科创素质教育的专业平台
- 所有教员都经过专业能力认证和教学评估
- 支持上门教学、专业场馆、在线网课多种授课方式
- 提供教学资源支持、场地推荐、安全保障全程服务

你可以回答：
- 平台使用和注册问题
- 素质教育需求发布流程
- 教员专业认证和筛选标准
- 支付、退费和保障政策
- 教学场地和安全问题
- 素质教育理念和方法咨询

相关平台知识：
{{relevantKnowledge}}

回复要友好、简洁、专业，体现素质教育特色。用中文回复。如果问题超出你的知识范围，建议联系人工客服。', '{"relevantKnowledge": "string"}', '[{"user": "平台有哪些素质教育科目？", "assistant": "我们提供三大类素质教育：1. 艺术素养（钢琴/乐器、美术/书法、声乐）；2. 