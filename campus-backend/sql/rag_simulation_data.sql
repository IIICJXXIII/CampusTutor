-- RAG系统模拟数据补充脚本
-- 为AI功能RAG升级项目添加丰富的模拟数据
-- 执行顺序：在基础数据之后执行

-- =======================================================
-- 1. RAG知识库文档扩展数据
-- 增加更多素质教育相关文档，覆盖更多场景
-- =======================================================

INSERT INTO `knowledge_document` (`title`, `content`, `doc_type`, `source`, `tags`, `target_role`, `applicable_subjects`, `applicable_grades`, `status`) VALUES
('STEAM教育核心理念', 'STEAM教育五大核心理念：1. 跨学科整合；2. 项目式学习；3. 问题解决导向；4. 创新思维培养；5. 团队协作能力。适合科创类素质教育课程设计。', 'TEACHING_EXPERIENCE', '教育专家分享', 'STEAM,科创,教育理念', 'TEACHER', '编程,机器人,科学实验', '小学,初中', 1),

('艺术素养培养路径', '艺术素养培养三阶段：1. 启蒙期（3-6岁）：兴趣培养；2. 基础期（7-12岁）：技能训练；3. 提升期（13+岁）：创意表达。每个阶段的教学重点和方法不同。', 'TEACHING_EXPERIENCE', '艺术教育专家', '艺术,素养,培养路径', 'TEACHER', '素描,水彩,创意美术', '全科', 1),

('体育训练安全规范', '体育训练安全注意事项：1. 热身充分（10-15分钟）；2. 器材检查；3. 环境安全评估；4. 运动强度控制；5. 紧急处理预案；6. 家长沟通机制。', 'RULE', '体育安全指南', '体育,安全,训练', 'TEACHER', '游泳,篮球,中考体育', '初中,高中', 1),

('家长选择素质教育指南', '家长选择素质教育课程建议：1. 了解孩子兴趣；2. 考察教员资质；3. 试课体验；4. 课程体系评估；5. 学习效果跟踪；6. 安全环境确认。', 'FAQ', '家长帮助中心', '家长指南,选择,素质教育', 'PARENT', NULL, NULL, 1),

('编程思维培养方法', '编程思维培养四步法：1. 分解问题；2. 模式识别；3. 抽象思维；4. 算法设计。适合少儿编程启蒙教学，培养计算思维。', 'TEACHING_EXPERIENCE', '编程教育专家', '编程,思维,教学方法', 'TEACHER', 'Python,Scratch,少儿编程', '小学', 1),

('音乐考级准备指南', '音乐考级准备要点：1. 曲目选择；2. 技术训练；3. 乐理知识；4. 视唱练耳；5. 心理准备；6. 考前模拟。帮助学员系统准备考级。', 'FAQ', '音乐教育指南', '音乐,考级,准备', 'TEACHER', '钢琴,小提琴,乐理', '全科', 1),

('创意美术教学案例', '创意美术教学案例：1. 主题创作（如"我的梦想家园"）；2. 材料探索（水彩、油画棒、黏土）；3. 技法指导；4. 作品展示与评价。激发学生创造力。', 'LESSON_PLAN', '优秀教案分享', '美术,创意,教学案例', 'TEACHER', '创意美术,手工', '小学', 1),

('中考体育评分标准', '中考体育项目评分标准：1. 1000米/800米跑；2. 立定跳远；3. 实心球；4. 引体向上/仰卧起坐。各项目满分标准和训练方法。', 'RULE', '体育考试标准', '中考,体育,评分标准', 'ALL', '中考体育', '初中', 1);

-- =======================================================
-- 2. AI交互历史模拟数据
-- 模拟用户与AI助手的真实交互，用于训练和测试
-- =======================================================

-- 创建AI交互历史表（如果不存在）
CREATE TABLE IF NOT EXISTS `ai_interaction_history` (
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

-- 插入AI交互历史模拟数据
INSERT INTO `ai_interaction_history` (`user_id`, `scene`, `query`, `response`, `rag_enabled`, `rag_score`, `tokens_used`, `response_time_ms`, `user_rating`, `created_time`) VALUES
(1001, 'DEMAND_CONSULT', '我想给孩子找编程老师，孩子8岁，零基础', '您好！很高兴为您服务。8岁孩子学习编程是个很好的开始。请问孩子对哪种编程形式更感兴趣？比如图形化编程（Scratch）还是Python基础？另外，您希望是上门教学还是在线课程？', 1, 0.85, 120, 450, 5, '2026-03-20 10:30:00'),

(1002, 'TUTOR_RECOMMEND', '怎么查看教员的教学评价？', '您可以通过以下方式查看教员评价：1. 在教员详情页查看综合评分和详细评价；2. 查看已完成订单的家长反馈；3. 查看教员的回复率和响应速度。平台所有评价都来自真实订单，确保客观公正。', 1, 0.92, 95, 320, 4, '2026-03-20 14:15:00'),

(2001, 'LESSON_PLAN', '需要为10岁零基础学生设计Python入门课，90分钟', '好的，为您生成Python入门教案：\n教案标题：Python编程初体验\n适用学生：10岁零基础\n课时时长：90分钟\n教学目标：1. 了解编程概念；2. 掌握Python基础语法；3. 完成第一个小程序\n教学准备：电脑、Python环境、示例代码\n教学流程：1. 导入（15分钟）- 编程是什么；2. 基础（30分钟）- 变量和输出；3. 实践（30分钟）- 编写问候程序；4. 总结（15分钟）- 回顾与拓展\n安全注意事项：正确坐姿，定时休息', 1, 0.78, 180, 680, 5, '2026-03-21 09:20:00'),

(2002, 'COMMENT_POLISH', '学生进步很快，色彩感好', '亲爱的家长，您好！\n很高兴与您分享孩子的学习情况。这段时间，孩子在美术学习上展现了显著的进步，特别是在色彩感知和运用方面表现突出。\n优点：1. 对色彩有敏锐的感知力，能准确表达色彩情感；2. 学习态度积极，每次课程都认真完成；3. 创意表达能力强，作品充满个性。\n建议：1. 可以尝试更多绘画技法，如水彩渐变；2. 加强构图训练，让作品更有层次感。\n期待孩子继续发挥艺术天赋，创作更多精彩作品！', 1, 0.88, 210, 520, 5, '2026-03-21 16:45:00'),

(1003, 'GENERAL_QA', '平台的安全保障措施有哪些？', '平台提供多重安全保障：1. 教员专业认证和背景审核；2. 课时费托管机制，满意后再结算；3. 不满意可申请退款；4. 客服全程跟进服务；5. 紧急联系渠道24小时畅通；6. 教学环境安全评估。确保您的孩子在学习过程中安全无忧。', 1, 0.95, 110, 380, 5, '2026-03-22 11:10:00'),

(2003, 'LESSON_PLAN', '钢琴考级三级备考课程，60分钟', '钢琴三级备考教案：\n教案标题：钢琴三级考级强化训练\n适用学生：备考三级学员\n课时时长：60分钟\n教学目标：1. 熟练掌握考级曲目；2. 提升演奏技巧；3. 加强乐理知识\n教学准备：钢琴、考级教材、节拍器\n教学流程：1. 热身（10分钟）- 音阶练习；2. 曲目训练（30分钟）- 重点难点突破；3. 乐理（15分钟）- 视唱练耳；4. 模拟（5分钟）- 完整演奏\n课后练习：每天练习45分钟，重点攻克技术难点', 1, 0.82, 160, 510, 4, '2026-03-22 15:30:00'),

(2004, 'COMMENT_POLISH', '体能进步明显，协调性还需加强', '尊敬的家长，您好！\n感谢您对体育训练的支持。孩子在体能训练方面取得了明显进步，值得肯定。\n优点：1. 体能素质显著提升，耐力增强；2. 训练态度认真，积极参与；3. 技术动作掌握较快。\n建议：1. 加强协调性训练，如平衡练习；2. 增加柔韧性训练，预防运动损伤；3. 保持规律训练，巩固成果。\n相信通过持续努力，孩子的体育综合能力会有更大提升！', 1, 0.75, 190, 490, 4, '2026-03-23 10:20:00');

-- =======================================================
-- 3. 用户画像扩展数据
-- 为更多用户创建详细的AI用户画像
-- =======================================================

INSERT INTO `user_profile_ai` (`user_id`, `role`, `teaching_style`, `expert_subjects`, `teaching_experience`, `student_grade`, `learning_style`, `weak_subjects`, `learning_needs`, `preferences`, `personalization_settings`) VALUES
(1, 'ADMIN', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"communicationPreference": "专业", "responseStyle": "正式"}', '{"responseLength": "concise", "tone": "formal"}'),

(1003, 'PARENT', NULL, NULL, NULL, '初中一年级', 'VISUAL', '["数学逻辑", "英语语法"]', '{"targetGoal": "全面提升", "focusAreas": "理科思维,语言表达"}', '{"communicationPreference": "详细", "responseStyle": "专业"}', '{"responseLength": "detailed", "tone": "professional"}'),

(2005, 'TEACHER', 'TECHNICAL', '["信息学奥赛", "算法设计"]', 'BEGINNER', NULL, NULL, NULL, NULL, '{"communicationPreference": "技术化", "responseStyle": "精确", "teachingFocus": "算法思维"}', '{"responseLength": "technical", "tone": "precise", "focusArea": "algorithms"}');

-- =======================================================
-- 4. Prompt模板使用统计模拟数据
-- 模拟Prompt模板的使用情况，用于优化分析
-- =======================================================

-- 更新Prompt模板使用统计
UPDATE `prompt_template` SET `usage_count` = 15, `average_rating` = 4.6 WHERE `scene` = 'DEMAND_CONSULT';
UPDATE `prompt_template` SET `usage_count` = 8, `average_rating` = 4.8 WHERE `scene` = 'TUTOR_RECOMMEND';
UPDATE `prompt_template` SET `usage_count` = 12, `average_rating` = 4.7 WHERE `scene` = 'LESSON_PLAN';
UPDATE `prompt_template` SET `usage_count` = 10, `average_rating` = 4.9 WHERE `scene` = 'COMMENT_POLISH';
UPDATE `prompt_template` SET `usage_count` = 20, `average_rating` = 4.5 WHERE `scene` = 'GENERAL_QA';

-- =======================================================
-- 5. RAG检索效果模拟数据
-- 模拟知识库检索的相关性评分，用于优化检索算法
-- =======================================================

-- 创建知识检索记录表（如果不存在）
CREATE TABLE IF NOT EXISTS `knowledge_retrieval_log` (
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

-- 插入检索效果模拟数据
INSERT INTO `knowledge_retrieval_log` (`query`, `document_ids`, `relevance_scores`, `retrieval_time_ms`, `user_feedback`, `created_time`) VALUES
('编程教学安全注意事项', '[5, 8]', '[0.85, 0.72]', 120, 1, '2026-03-20 11:30:00'),
('美术考级准备', '[6, 7]', '[0.68, 0.91]', 95, 1, '2026-03-21 14:20:00'),
('体育训练受伤处理', '[3, 8]', '[0.92, 0.45]', 110, 1, '2026-03-22 09:45:00'),
('音乐教学收费标准', '[6]', '[0.55]', 85, 0, '2026-03-22 16:30:00'),
('STEAM教育理念', '[1, 5]', '[0.95, 0.78]', 130, 1, '2026-03-23 10:15:00');

-- =======================================================
-- 6. AI助手功能使用统计
-- 模拟各场景的使用频率和效果
-- =======================================================

-- 创建AI功能使用统计表（如果不存在）
CREATE TABLE IF NOT EXISTS `ai_function_stats` (
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

-- 插入功能使用统计模拟数据
INSERT INTO `ai_function_stats` (`function_name`, `scene`, `usage_count`, `success_count`, `avg_response_time_ms`, `avg_user_rating`, `last_used_time`) VALUES
('需求咨询助手', 'DEMAND_CONSULT', 45, 42, 420, 4.6, '2026-03-23 14:30:00'),
('教员推荐助手', 'TUTOR_RECOMMEND', 28, 26, 350, 4.8, '2026-03-23 11:20:00'),
('教案生成助手', 'LESSON_PLAN', 36, 34, 580, 4.7, '2026-03-23 09:45:00'),
('评语润色助手', 'COMMENT_POLISH', 32, 30, 510, 4.9, '2026-03-23 16:15:00'),
('通用问答助手', 'GENERAL_QA', 68, 65, 380, 4.5, '2026-03-23 17:30:00');

-- =======================================================
-- 7. 完成统计
-- =======================================================

SELECT 'RAG模拟数据插入完成' AS result;
SELECT '知识库文档总数：' AS label, COUNT(*) AS count FROM `knowledge_document` UNION
SELECT 'Prompt模板总数：', COUNT(*) FROM `prompt_template` UNION
SELECT '用户画像总数：', COUNT(*) FROM `user_profile_ai` UNION
SELECT 'AI交互记录：', COUNT(*) FROM `ai_interaction_history` UNION
SELECT '知识检索记录：', COUNT(*) FROM `knowledge_retrieval_log` UNION
SELECT '功能使用统计：', COUNT(*) FROM `ai_function_stats`;

-- 显示RAG系统数据概览
SELECT 
    'RAG系统数据概览' AS section,
    CONCAT('知识库：', (SELECT COUNT(*) FROM `knowledge_document`), ' 个文档') AS content
UNION
SELECT 
    'Prompt工程',
    CONCAT('模板：', (SELECT COUNT(*) FROM `prompt_template`), ' 个场景模板')
UNION
SELECT 
    '用户画像',
    CONCAT('画像：', (SELECT COUNT(*) FROM `user_profile_ai`), ' 个用户画像')
UNION
SELECT 
    'AI交互历史',
    CONCAT('记录：', (SELECT COUNT(*) FROM `ai_interaction_history`), ' 条交互记录')
UNION
SELECT 
    '检索效果',
    CONCAT('检索：', (SELECT COUNT(*) FROM `knowledge_retrieval_log`), ' 次检索记录')
UNION
SELECT 
    '功能统计',
    CONCAT('统计：', (SELECT COUNT(*) FROM `ai_function_stats`), ' 项功能统计');

-- 显示素质教育数据分布
SELECT '素质教育数据分布' AS category, COUNT(*) AS count FROM `knowledge_document` WHERE `tags` LIKE '%素质教育%' OR `tags` LIKE '%STEAM%' OR `tags` LIKE '%艺术%' OR `tags` LIKE '%体育%' OR `tags` LIKE '%科创%'
UNION
SELECT '艺术类文档', COUNT(*) FROM `knowledge_document` WHERE `tags` LIKE '%艺术%' OR `tags` LIKE '%美术%' OR `tags` LIKE '%音乐%'
UNION
SELECT '体育类文档', COUNT(*) FROM `knowledge_document` WHERE `tags` LIKE '%体育%' OR `tags` LIKE '%训练%' OR `tags` LIKE '%健康%'
UNION
SELECT '科创类文档', COUNT(*) FROM `knowledge_document` WHERE `tags` LIKE '%STEAM%' OR `tags` LIKE '%编程%' OR `tags` LIKE '%科创%';

-- 显示用户画像分布
SELECT '用户画像角色分布' AS role_type, COUNT(*) AS count FROM `user_profile_ai` GROUP BY `role`
UNION
SELECT '教学风格分布', COUNT(*) FROM `user_profile_ai` WHERE `teaching_style` IS NOT NULL GROUP BY `teaching_style`
UNION
SELECT '学习风格分布', COUNT(*) FROM `user_profile_ai` WHERE `learning_style` IS NOT NULL GROUP BY `learning_style`;

-- 完成提示
SELECT '✅ RAG模拟数据补充完成' AS completion_message;
SELECT '📊 数据已就绪，可以开始测试AI功能RAG升级效果' AS next_step;
