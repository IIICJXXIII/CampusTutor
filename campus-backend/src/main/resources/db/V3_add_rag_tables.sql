-- V3: 添加RAG增强AI功能相关表
-- 创建时间: 2026-03-24
-- 作者: AI助手

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

-- 插入初始数据
-- 1. 插入初始知识库文档
INSERT INTO `knowledge_document` (`title`, `content`, `doc_type`, `source`, `tags`, `target_role`, `applicable_subjects`, `applicable_grades`, `status`) VALUES
('平台服务协议', '校园智教平台服务协议：1. 平台收取10%服务费；2. 课时费托管机制；3. 教员需完成实名认证；4. 家长可申请退款；5. 争议处理流程。', 'RULE', '平台官方', '规则,协议,费用', 'ALL', NULL, NULL, 1),
('如何发布家教需求？', '发布家教需求步骤：1. 登录家长账号；2. 进入发布需求页面；3. 填写学生信息；4. 选择科目和年级；5. 设置期望价格；6. 选择授课方式；7. 确认发布。', 'FAQ', '平台帮助中心', '需求发布,家长指南', 'PARENT', NULL, NULL, 1),
('初中数学教案模板', '初中数学教案结构：1. 教学目标；2. 教学重点难点；3. 教学过程（导入、讲解、练习、总结）；4. 作业布置；5. 教学反思。', 'LESSON_PLAN', '优秀教员分享', '数学,初中,教案', 'TEACHER', '数学', '初中', 1),
('学生评语模板', '优秀评语要素：1. 肯定学生进步；2. 具体指出优点；3. 提出改进建议；4. 表达鼓励和期待；5. 语言温暖专业。', 'COMMENT', '教学经验分享', '评语,反馈,家长沟通', 'TEACHER', NULL, NULL, 1),
('教员认证流程', '教员认证步骤：1. 提交基本信息；2. 上传学生证/毕业证；3. 填写教学经历；4. 等待审核（1-3个工作日）；5. 审核通过后即可接单。', 'FAQ', '平台帮助中心', '认证,教员指南', 'TEACHER', NULL, NULL, 1),
('家长安全保障', '家长安全保障措施：1. 教员实名认证；2. 课时费托管；3. 不满意可申请退款；4. 客服全程跟进；5. 紧急联系渠道。', 'RULE', '平台官方', '安全,保障,家长', 'PARENT', NULL, NULL, 1);

-- 2. 插入初始Prompt模板
INSERT INTO `prompt_template` (`name`, `scene`, `template`, `variables`, `is_active`) VALUES
('需求咨询助手', 'DEMAND_CONSULT', '你是"校园智教"家教平台的AI助手。你的任务是帮助家长发布家教需求。

平台功能介绍：
1. 家长可以发布家教需求，描述孩子的年级、科目、学习问题等
2. 系统会智能匹配合适的大学生教员
3. 家长可以查看教员的学校、专业、教学评价等信息
4. 确认后可以预约试课、签约正式课程

你需要：
1. 引导家长描述孩子的学习需求（年级、科目、学习困难等）
2. 询问对教员的期望（性别、学历、价格等）
3. 确认授课方式（上门/网课）和时间安排
4. 收集完信息后，告知家长可以提交需求了

当前用户信息：
角色：{{userRole}}
学生年级：{{studentGrade}}
薄弱科目：{{weakSubjects}}

回复要简洁友好，不要太长。用中文回复。', '{"userRole": "string", "studentGrade": "string", "weakSubjects": "string"}', 1),

('教员推荐助手', 'TUTOR_RECOMMEND', '你是"校园智教"家教平台的AI助手。你的任务是帮助家长了解和选择合适的教员。

你需要：
1. 解答关于教员资质、认证流程的问题
2. 说明平台的教员筛选标准
3. 帮助家长理解如何查看教员评价
4. 解释试课、签约、退费等流程

平台规则：
{{platformRules}}

回复要专业、简洁。用中文回复。', '{"platformRules": "string"}', 1),

('教案生成助手', 'LESSON_PLAN', '你是"校园智教"平台的AI教学赋能官，专业的教案生成助手。
你的任务是为大学生教员生成详细的课程教案。

教案要求：
1. 结构清晰：包含热身、主要内容、练习、游戏、总结等环节
2. 时间合理：根据给定的课时时长分配时间
3. 针对性强：根据学生水平和科目特点设计内容
4. 实用性高：提供具体的教学方法和练习内容
5. 语言专业：使用专业的教学术语，但保持易懂

教学信息：
科目：{{subject}}
学生水平：{{studentLevel}}
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
- 注意事项：[安全、教学重点等]
- 课后作业：[可选]', '{"subject": "string", "studentLevel": "string", "lessonDuration": "string", "studentInfo": "string", "lessonPlanTemplates": "string"}', 1),

('评语润色助手', 'COMMENT_POLISH', '你是"校园智教"平台的AI教学赋能官，专业的评语润色助手。
你的任务是将教员的简单评语润色为专业、温馨的家长反馈。

润色要求：
1. 语言温暖：使用亲切、鼓励的语气
2. 专业表达：使用教育专业术语，体现专业性
3. 具体详细：将简单描述扩展为具体的观察和分析
4. 正面引导：突出学生的进步和优点
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
- 优点：学生的进步和闪光点
- 建议：具体的改进方向
- 结尾：鼓励和期待', '{"rawComment": "string", "subject": "string", "studentInfo": "string", "commentTemplates": "string"}', 1),

('通用问答助手', 'GENERAL_QA', '你是"校园智教"家教平台的AI客服助手。

平台介绍：
- 这是一个连接家长和大学生教员的家教服务平台
- 所有教员都经过实名认证和学历认证
- 支持上门家教和在线网课两种授课方式
- 提供课时托管和评价系统保障服务质量

你可以回答：
- 平台使用问题
- 发布需求流程
- 教员认证流程
- 支付和退费政策
- 安全保障措施

相关平台知识：
{{relevantKnowledge}}

回复要友好、简洁、专业。用中文回复。如果问题超出你的知识范围，建议联系人工客服。', '{"relevantKnowledge": "string"}', 1);

-- 3. 插入初始用户画像（示例数据）
INSERT INTO `user_profile_ai` (`user_id`, `role`, `teaching_style`, `expert_subjects`, `teaching_experience`, `student_grade`, `learning_style`, `weak_subjects`, `learning_needs`, `preferences`, `personalization_settings`) VALUES
(1001, 'TEACHER', 'INTERACTIVE', '["数学", "物理"]', 'INTERMEDIATE', NULL, NULL, NULL, NULL, '{"communicationPreference": "详细", "responseStyle": "专业"}', '{"responseLength": "detailed", "tone": "professional"}'),
(2001, 'PARENT', NULL, NULL, NULL, '初中二年级', 'VISUAL', '["数学", "英语"]', '{"targetScore": "提高20分", "focusAreas": "基础巩固"}', '{"communicationPreference": "简洁", "responseStyle": "友好"}', '{"responseLength": "concise", "tone": "friendly"}');

-- 创建索引优化查询性能
CREATE INDEX idx_knowledge_document_tags ON `knowledge_document`(`tags`(255));
CREATE INDEX idx_knowledge_document_applicable_subjects ON `knowledge_document`(`applicable_subjects`(255));
CREATE INDEX idx_knowledge_document_applicable_grades ON `knowledge_document`(`applicable_grades`(255));

CREATE INDEX idx_prompt_template_usage_count ON `prompt_template`(`usage_count`);
CREATE INDEX idx_prompt_template_average_rating ON `prompt_template`(`average_rating`);

CREATE INDEX idx_user_profile_ai_teaching_experience ON `user_profile_ai`(`teaching_experience`);
CREATE INDEX idx_user_profile_ai_learning_style ON `user_profile_ai`(`learning_style`);

CREATE INDEX idx_ai_interaction_history_user_rating ON `ai_interaction_history`(`user_rating`);
CREATE INDEX idx_ai_interaction_history_response_time_ms ON `ai_interaction_history`(`response_time_ms`);

-- 添加注释
ALTER TABLE `knowledge_document` COMMENT = '知识库文档表 - 存储平台规则、FAQ、教案模板等知识';
ALTER TABLE `prompt_template` COMMENT = 'Prompt模板表 - 存储不同场景的AI提示词模板';
ALTER TABLE `user_profile_ai` COMMENT = 'AI用户画像表 - 存储用户的个性化特征和偏好';
ALTER TABLE `ai_interaction_history` COMMENT = 'AI交互历史表 - 记录用户与AI的交互历史';
ALTER TABLE `knowledge_vector` COMMENT = '知识库向量表 - 存储文档的向量表示（预留）';

-- 完成迁移
SELECT 'V3迁移完成：RAG增强AI功能相关表已创建并初始化数据' AS migration_result;