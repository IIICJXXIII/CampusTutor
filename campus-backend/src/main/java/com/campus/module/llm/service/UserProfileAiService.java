构建一个可靠的 RAG（检索增强生成）知识库，本质上是在搭建一个“企业级搜索引擎+智能生成器”的复合系统。根据行业内的最佳实践，我为你梳理了一个从0到1的标准实施流程，分为五个核心阶段：

第一阶段：需求定义与可行性分析
在写代码之前，先明确目标，否则容易陷入“为了用而用”的陷阱。

场景界定：确定核心用途。是用于内部员工的运维/HR问答（注重高准确率、权限隔离），还是面向客户的产品售前支持（注重语义理解、多轮对话），或是代码库分析（注重代码逻辑树）？不同的场景决定了后续的技术选型。

量化标准：定义什么是“好”。通常需要设定 RAGAS（RAG Assessment，RAG评估体系） 指标，包括命中率（检索到的片段是否包含答案）、忠实度（生成的答案是否基于检索到的片段，没有幻觉）和答案相关性。

数据摸排：检查数据源。数据是结构化表格（如Excel/MySQL）、半结构化（如PDF中的混合内容），还是纯文本（Markdown/Word）？如果是复杂PDF（含图表、双栏排版），需要预估预处理难度。

第二阶段：数据工程——决定RAG质量的上限
“垃圾进，垃圾出”。这一阶段占据RAG项目约60%-80%的工作量。

数据清洗与预处理：

去除页眉页脚、特殊字符、乱码。

文档解析：针对PDF，需根据其性质选择工具。PyMuPDF适合文字版PDF，Unstructured.io或Docling适合处理复杂布局（保留标题层级），OCR（光学字符识别） 则用于扫描件。

表格处理：表格是难点。建议将表格转换为Markdown格式或进行“Text-to-SQL”（文本转结构化查询）预处理，因为直接切分表格容易破坏行列对应关系。

分块：

策略：不要简单按固定长度切分（如每500字一刀切）。推荐采用递归字符切分，并利用文档结构（如Markdown的标题、HTML的标签）进行语义切分，确保同一段落或同一章节的内容不被割裂。

大小：通常建议 512-1024 tokens。如果使用OpenAI的嵌入模型，文本长度会影响检索精度。对于需要处理大量代码或长文本的场景，可考虑上下文检索，即检索时返回块的前后文，弥补分块造成的语义断层。

向量化与嵌入：

选择嵌入模型时，建议在公有云场景下直接使用商业模型（如OpenAI的 text-embedding-3-large、智谱的 embedding-2），在私有化部署场景下可使用 BGE（BAAI General Embedding，智源通用向量模型） 或 E5-mistral。

微调：如果项目涉及大量专业术语（如医疗、法律、特定代码库），对嵌入模型进行领域微调会显著提升检索精度。

第三阶段：索引构建与存储
这是“建库”的核心步骤，决定了检索速度与能力。

向量数据库选型：

原型验证：使用 Chroma 或 FAISS，简单轻量。

生产环境：推荐 Milvus（功能最全，开源）、Qdrant（性能优异，云原生）或 Elasticsearch（如果你已经有ES生态，且需要混合检索）。

索引优化：

混合检索：这是提升精度的关键。不能只用向量（语义）检索，必须结合 BM25（Okapi BM25，一种基于词频的检索算法）（关键词）检索。例如，用户问“版本号是多少”时，BM25对“版本”一词的匹配通常比向量更准。

元数据过滤：在存储时，将“时间”、“部门”、“文档类型”、“权限级别”作为标量字段存入数据库。检索时先通过元数据过滤（如 where time > 2024），再在剩余数据中进行向量搜索，能极大提升效率。

父子文档：对于长文档，可以考虑“父文档索引”策略。检索时命中“子块”（精准），返回给大模型时提供“父块”（完整上下文）。

第四阶段：检索与生成——优化用户体验
这一阶段关注“如何让大模型看懂检索到的内容”。

查询重写：

用户输入往往口语化且信息不全。在检索前，先让大模型对用户问题进行改写。例如，将“那个上次出bug的项目叫啥”改写为“请检索在2024年10月出现生产事故的项目名称”。

HyDE（假设性文档嵌入）：对于开放性问答，可让大模型先“假设”一段答案，用这段假设答案去检索，能提高相关度。

重排序：

向量检索初步召回Top-20或Top-50的文档块后，需要一个重排序模型（如 Cohere Rerank、BGE-reranker）对这些块进行精细排序。

这一步是精度提升最大的环节。最终只将精排后的Top-3或Top-5送入大模型，既能解决“上下文窗口被无关信息占满”的问题，又能大幅提升准确率。

提示词工程与生成：

提示词模板必须包含：角色设定（如“你是客服”）、检索上下文（限定使用 <context></context> 标签包裹）、拒绝策略（“如果上下文中没有明确答案，请回答‘未找到相关信息’，不要编造”）。

引用溯源：要求大模型在输出答案时，附上来源（如 [参考文档：xxx.pdf 第3页]），这对于建立用户信任至关重要。

第五阶段：评估与迭代闭环
RAG不是“一次性交付”的项目，需要持续维护。

离线评估：构建一个包含“问题、标准答案、预期检索文档ID”的测试集。每次修改分块策略、嵌入模型或提示词后，运行自动化测试。如果命中率下降，需要回滚修改。

在线监控：生产环境需记录“用户反馈”（点赞/点踩）和“重试率”。如果用户频繁点踩，可以将该对话放入标注池，用于后续微调。

高级优化：当基础RAG满足不了需求时，可以考虑引入 Agentic RAG（智能体RAG），即让智能体自主决策：先查向量库，如果没找到，再调用API或联网搜索，甚至根据答案好坏进行多轮迭代检索。

总结：快速启动的推荐路径
如果你刚起步，可以遵循以下顺序：

原型阶段：用 LlamaIndex（擅长索引构建）或 LangChain（擅长编排） + Chroma + 开源嵌入模型，快速跑通“上传文档-问答”的Demo。

优化阶段：重点攻克 文档解析（处理你的实际PDF样本）和 混合检索+重排序 的配置。

生产阶段：切换至 Milvus 或云原生向量库，增加 缓存层（高频问题直接查缓存，不调大模型），并建立 用户反馈闭环。

如果你方便的话，可以告诉我你的项目更侧重于哪类数据（比如大量扫描件、复杂代码库，还是结构化的表格数据），我可以针对性地帮你分析关键的技术难点。package com.campus.module.llm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.llm.entity.UserProfileAi;
import com.campus.module.llm.mapper.UserProfileAiMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI用户画像服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileAiService extends ServiceImpl<UserProfileAiMapper, UserProfileAi> {
    
    private final UserProfileAiMapper userProfileAiMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 创建或更新用户画像
     */
    public UserProfileAi saveOrUpdateProfile(UserProfileAi profile) {
        if (profile.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        UserProfileAi existing = getByUserId(profile.getUserId());
        if (existing == null) {
            // 新用户画像
            profile.setCreatedTime(LocalDateTime.now());
            profile.setUpdatedTime(LocalDateTime.now());
            save(profile);
            log.info("创建用户画像成功，用户ID: {}, 角色: {}", profile.getUserId(), profile.getRole());
        } else {
            // 更新现有画像
            profile.setCreatedTime(existing.getCreatedTime());
            profile.setUpdatedTime(LocalDateTime.now());
            updateById(profile);
            log.info("更新用户画像成功，用户ID: {}", profile.getUserId());
        }
        
        return profile;
    }
    
    /**
     * 根据用户ID获取用户画像
     */
    public UserProfileAi getByUserId(Long userId) {
        return userProfileAiMapper.findByUserId(userId);
    }
    
    /**
     * 根据角色获取用户画像列表
     */
    public List<UserProfileAi> getByRole(String role, Integer limit) {
        return userProfileAiMapper.findByRole(role, limit != null ? limit : 10);
    }
    
    /**
     * 根据教学风格获取教员画像
     */
    public List<UserProfileAi> getTeachersByTeachingStyle(String teachingStyle) {
        return userProfileAiMapper.findTeachersByTeachingStyle(teachingStyle);
    }
    
    /**
     * 根据科目获取教员画像
     */
    public List<UserProfileAi> getTeachersBySubject(String subject, Integer limit) {
        return userProfileAiMapper.findTeachersBySubject(subject, limit != null ? limit : 10);
    }
    
    /**
     * 根据学生年级获取家长画像
     */
    public List<UserProfileAi> getParentsByStudentGrade(String grade) {
        return userProfileAiMapper.findParentsByStudentGrade(grade);
    }
    
    /**
     * 更新交互历史摘要
     */
    public void updateInteractionSummary(Long userId, String summary) {
        userProfileAiMapper.updateInteractionSummary(userId, summary);
        log.debug("更新用户交互历史摘要，用户ID: {}", userId);
    }
    
    /**
     * 更新个性化设置
     */
    public void updatePersonalizationSettings(Long userId, String settings) {
        userProfileAiMapper.updatePersonalizationSettings(userId, settings);
        log.debug("更新用户个性化设置，用户ID: {}", userId);
    }
    
    /**
     * 获取用户的个性化特征
     */
    public Map<String, Object> getUserPersonalizationFeatures(Long userId) {
        UserProfileAi profile = getByUserId(userId);
        if (profile == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> features = new HashMap<>();
        
        // 基本特征
        features.put("role", profile.getRole());
        
        // 教员特征
        if ("TEACHER".equals(profile.getRole())) {
            features.put("teachingStyle", profile.getTeachingStyle());
            features.put("teachingExperience", profile.getTeachingExperience());
            
            if (StringUtils.hasText(profile.getExpertSubjects())) {
                try {
                    List<String> subjects = objectMapper.readValue(
                            profile.getExpertSubjects(), 
                            new TypeReference<List<String>>() {}
                    );
                    features.put("expertSubjects", subjects);
                } catch (Exception e) {
                    log.error("解析擅长科目失败: {}", e.getMessage());
                }
            }
        }
        
        // 家长/学生特征
        if ("PARENT".equals(profile.getRole())) {
            features.put("studentGrade", profile.getStudentGrade());
            features.put("learningStyle", profile.getLearningStyle());
            
            if (StringUtils.hasText(profile.getWeakSubjects())) {
                try {
                    List<String> weakSubjects = objectMapper.readValue(
                            profile.getWeakSubjects(), 
                            new TypeReference<List<String>>() {}
                    );
                    features.put("weakSubjects", weakSubjects);
                } catch (Exception e) {
                    log.error("解析薄弱科目失败: {}", e.getMessage());
                }
            }
            
            if (StringUtils.hasText(profile.getLearningNeeds())) {
                try {
                    Map<String, Object> learningNeeds = objectMapper.readValue(
                            profile.getLearningNeeds(), 
                            new TypeReference<Map<String, Object>>() {}
                    );
                    features.put("learningNeeds", learningNeeds);
                } catch (Exception e) {
                    log.error("解析学习需求失败: {}", e.getMessage());
                }
            }
        }
        
        // 偏好设置
        if (StringUtils.hasText(profile.getPreferences())) {
            try {
                Map<String, Object> preferences = objectMapper.readValue(
                        profile.getPreferences(), 
                        new TypeReference<Map<String, Object>>() {}
                );
                features.put("preferences", preferences);
            } catch (Exception e) {
                log.error("解析偏好设置失败: {}", e.getMessage());
            }
        }
        
        // 个性化设置
        if (StringUtils.hasText(profile.getPersonalizationSettings())) {
            try {
                Map<String, Object> personalization = objectMapper.readValue(
                        profile.getPersonalizationSettings(), 
                        new TypeReference<Map<String, Object>>() {}
                );
                features.put("personalization", personalization);
            } catch (Exception e) {
                log.error("解析个性化设置失败: {}", e.getMessage());
            }
        }
        
        return features;
    }
    
    /**
     * 根据用户画像生成个性化提示
     */
    public String generatePersonalizedPrompt(Long userId, String basePrompt) {
        Map<String, Object> features = getUserPersonalizationFeatures(userId);
        if (features.isEmpty()) {
            return basePrompt;
        }
        
        StringBuilder personalizedPrompt = new StringBuilder(basePrompt);
        
        // 添加用户特征信息
        personalizedPrompt.append("\n\n用户特征：\n");
        
        if (features.containsKey("role")) {
            personalizedPrompt.append("- 角色：").append(features.get("role")).append("\n");
        }
        
        // 教员特征
        if ("TEACHER".equals(features.get("role"))) {
            if (features.containsKey("teachingStyle")) {
                personalizedPrompt.append("- 教学风格：").append(features.get("teachingStyle")).append("\n");
            }
            if (features.containsKey("teachingExperience")) {
                personalizedPrompt.append("- 教学经验：").append(features.get("teachingExperience")).append("\n");
            }
            if (features.containsKey("expertSubjects")) {
                List<String> subjects = (List<String>) features.get("expertSubjects");
                personalizedPrompt.append("- 擅长科目：").append(String.join("、", subjects)).append("\n");
            }
        }
        
        // 家长/学生特征
        if ("PARENT".equals(features.get("role"))) {
            if (features.containsKey("studentGrade")) {
                personalizedPrompt.append("- 学生年级：").append(features.get("studentGrade")).append("\n");
            }
            if (features.containsKey("learningStyle")) {
                personalizedPrompt.append("- 学习习惯：").append(features.get("learningStyle")).append("\n");
            }
            if (features.containsKey("weakSubjects")) {
                List<String> weakSubjects = (List<String>) features.get("weakSubjects");
                personalizedPrompt.append("- 薄弱科目：").append(String.join("、", weakSubjects)).append("\n");
            }
        }
        
        // 添加个性化指导
        personalizedPrompt.append("\n个性化指导：\n");
        personalizedPrompt.append("请根据以上用户特征，提供更贴合用户需求的回答。");
        
        // 根据偏好调整回答风格
        if (features.containsKey("preferences")) {
            Map<String, Object> preferences = (Map<String, Object>) features.get("preferences");
            if (preferences.containsKey("communicationPreference")) {
                String preference = (String) preferences.get("communicationPreference");
                if ("简洁".equals(preference)) {
                    personalizedPrompt.append("回答要简洁明了，突出重点。");
                } else if ("详细".equals(preference)) {
                    personalizedPrompt.append("回答要详细全面，提供具体建议。");
                }
            }
        }
        
        log.debug("生成个性化提示，用户ID: {}, 提示长度: {}", userId, personalizedPrompt.length());
        return personalizedPrompt.toString();
    }
    
    /**
     * 分析对话历史，更新用户画像
     */
    public void analyzeConversationAndUpdateProfile(Long userId, List<Map<String, String>> conversationHistory) {
        UserProfileAi profile = getByUserId(userId);
        if (profile == null) {
            log.warn("用户画像不存在，无法分析对话历史，用户ID: {}", userId);
            return;
        }
        
        try {
            // 分析对话特征
            Map<String, Object> analysis = analyzeConversationFeatures(conversationHistory);
            
            // 更新交互历史摘要
            String summary = objectMapper.writeValueAsString(analysis);
            updateInteractionSummary(userId, summary);
            
            // 根据分析结果更新偏好设置
            updatePreferencesFromAnalysis(userId, analysis);
            
            log.info("分析对话历史并更新用户画像成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("分析对话历史失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 分析对话特征
     */
    private Map<String, Object> analyzeConversationFeatures(List<Map<String, String>> conversationHistory) {
        Map<String, Object> features = new HashMap<>();
        
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return features;
        }
        
        // 分析话题分布
        Map<String, Integer> topicCount = new HashMap<>();
        List<String> commonQuestions = new ArrayList<>();
        
        for (Map<String, String> message : conversationHistory) {
            String content = message.get("content");
            String role = message.get("role");
            
            if ("user".equals(role) && content != null) {
                // 简单的话题分析（实际应该使用更复杂的NLP）
                if (content.contains("价格") || content.contains("费用") || content.contains("收费")) {
                    topicCount.put("price", topicCount.getOrDefault("price", 0) + 1);
                }
                if (content.contains("认证") || content.contains("资质") || content.contains("资格")) {
                    topicCount.put("certification", topicCount.getOrDefault("certification", 0) + 1);
                }
                if (content.contains("教案") || content.contains("教学") || content.contains("课程")) {
                    topicCount.put("teaching", topicCount.getOrDefault("teaching", 0) + 1);
                }
                if (content.contains("评价") || content.contains("评分") || content.contains("反馈")) {
                    topicCount.put("review", topicCount.getOrDefault("review", 0) + 1);
                }
                
                // 记录常见问题
                if (content.length() < 50) { // 简单问题
                    commonQuestions.add(content);
                }
            }
        }
        
        features.put("topicDistribution", topicCount);
        features.put("commonQuestions", commonQuestions);
        features.put("totalConversations", conversationHistory.size() / 2); // 假设每轮对话有user和assistant两条消息
        features.put("lastAnalysisTime", LocalDateTime.now().toString());
        
        return features;
    }
    
    /**
     * 根据分析结果更新偏好设置
     */
    private void updatePreferencesFromAnalysis(Long userId, Map<String, Object> analysis) {
        try {
            UserProfileAi profile = getByUserId(userId);
            if (profile == null) {
                return;
            }
            
            Map<String, Object> preferences = new HashMap<>();
            if (StringUtils.hasText(profile.getPreferences())) {
                preferences = objectMapper.readValue(
                        profile.getPreferences(), 
                        new TypeReference<Map<String, Object>>() {}
                );
            }
            
            // 根据话题分布更新偏好
            Map<String, Integer> topicDistribution = (Map<String, Integer>) analysis.get("topicDistribution");
            if (topicDistribution != null) {
                String mostFrequentTopic = topicDistribution.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                
                if (mostFrequentTopic != null) {
                    preferences.put("frequentTopic", mostFrequentTopic);
                }
            }
            
            // 更新偏好设置
            String updatedPreferences = objectMapper.writeValueAsString(preferences);
            profile.setPreferences(updatedPreferences);
            updateById(profile);
            
        } catch (Exception e) {
            log.error("更新偏好设置失败: {}", e.getMessage());
        }
    }
    
    /**
     * 导入初始用户画像数据（测试用）
     */
    public void importInitialProfiles() {
        log.info("开始导入初始用户画像数据...");
        
        try {
            // 教员画像示例
            UserProfileAi teacherProfile = new UserProfileAi();
            teacherProfile.setUserId(1001L);
            teacherProfile.setRole("TEACHER");
            teacherProfile.setTeachingStyle("INTERACTIVE");
            teacherProfile.setExpertSubjects("[\"数学\", \"物理\"]");
            teacherProfile.setTeachingExperience("INTERMEDIATE");
            teacherProfile.setPreferences("{\"communicationPreference\": \"详细\", \"responseStyle\": \"专业\"}");
            teacherProfile.setPersonalizationSettings("{\"responseLength\": \"detailed\", \"tone\": \"professional\"}");
            saveOrUpdateProfile(teacherProfile);
            
            // 家长画像示例
            UserProfileAi parentProfile = new UserProfileAi();
            parentProfile.setUserId(2001L);
            parentProfile.setRole("PARENT");
            parentProfile.setStudentGrade("初中二年级");
            parentProfile.setWeakSubjects("[\"数学\", \"英语\"]");
            parentProfile.setLearningStyle("VISUAL");
            parentProfile.setLearningNeeds("{\"targetScore\": \"提高20分\", \"focusAreas\": \"基础巩固\"}");
            parentProfile.setPreferences("{\"communicationPreference\": \"简洁\", \"responseStyle\": \"友好\"}");
            parentProfile.setPersonalizationSettings("{\"responseLength\": \"concise\", \"tone\": \"friendly\"}");
            saveOrUpdateProfile(parentProfile);
            
            log.info("初始用户画像数据导入完成，共导入2个画像");
        } catch (Exception e) {
            log.error("导入初始用户画像数据失败: {}", e.getMessage(), e);
        }
    }
}