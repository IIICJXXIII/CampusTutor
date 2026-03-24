package com.campus.module.llm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.llm.entity.PromptTemplate;
import com.campus.module.llm.mapper.PromptTemplateMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prompt模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateService extends ServiceImpl<PromptTemplateMapper, PromptTemplate> {
    
    private final PromptTemplateMapper promptTemplateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 创建或更新Prompt模板
     */
    public PromptTemplate saveOrUpdateTemplate(PromptTemplate template) {
        if (template.getId() == null) {
            // 新模板：设置版本号为1
            template.setVersion(1);
            template.setCreatedTime(LocalDateTime.now());
            template.setUsageCount(0);
            template.setAverageRating(0.0);
        } else {
            // 更新模板：版本号+1
            PromptTemplate existing = getById(template.getId());
            if (existing != null) {
                template.setVersion(existing.getVersion() + 1);
            }
        }
        
        template.setUpdatedTime(LocalDateTime.now());
        
        // 如果启用新模板，禁用其他版本的模板
        if (template.getIsActive() == 1) {
            promptTemplateMapper.disableOtherVersions(template.getScene(), template.getId());
        }
        
        saveOrUpdate(template);
        log.info("保存Prompt模板成功，ID: {}, 场景: {}, 版本: {}", 
                template.getId(), template.getScene(), template.getVersion());
        
        return template;
    }
    
    /**
     * 根据场景获取启用的模板
     */
    public PromptTemplate getActiveTemplateByScene(String scene) {
        PromptTemplate template = promptTemplateMapper.findActiveByScene(scene);
        if (template != null) {
            // 增加使用次数
            promptTemplateMapper.incrementUsageCount(template.getId());
        }
        return template;
    }
    
    /**
     * 根据场景获取所有模板版本
     */
    public List<PromptTemplate> getAllTemplatesByScene(String scene) {
        return promptTemplateMapper.findAllByScene(scene);
    }
    
    /**
     * 获取所有启用的模板
     */
    public List<PromptTemplate> getAllActiveTemplates() {
        return promptTemplateMapper.findAllActive();
    }
    
    /**
     * 渲染Prompt模板
     */
    public String renderTemplate(String scene, Map<String, Object> variables) {
        PromptTemplate template = getActiveTemplateByScene(scene);
        if (template == null) {
            log.warn("未找到场景对应的Prompt模板: {}", scene);
            return null;
        }
        
        String rendered = template.getTemplate();
        
        // 替换变量
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                rendered = rendered.replace(placeholder, value);
            }
        }
        
        // 添加示例（如果有）
        if (StringUtils.hasText(template.getExamples())) {
            try {
                List<Map<String, String>> examples = objectMapper.readValue(
                        template.getExamples(), 
                        new TypeReference<List<Map<String, String>>>() {}
                );
                
                StringBuilder examplesBuilder = new StringBuilder("\n\n示例对话：\n");
                for (Map<String, String> example : examples) {
                    examplesBuilder.append("用户：").append(example.get("user")).append("\n");
                    examplesBuilder.append("助手：").append(example.get("assistant")).append("\n\n");
                }
                rendered += examplesBuilder.toString();
            } catch (Exception e) {
                log.error("解析示例失败: {}", e.getMessage());
            }
        }
        
        // 添加约束条件（如果有）
        if (StringUtils.hasText(template.getConstraints())) {
            rendered += "\n\n约束条件：\n" + template.getConstraints();
        }
        
        // 添加输出格式（如果有）
        if (StringUtils.hasText(template.getOutputFormat())) {
            rendered += "\n\n输出格式：\n" + template.getOutputFormat();
        }
        
        log.debug("渲染Prompt模板完成，场景: {}, 长度: {}", scene, rendered.length());
        return rendered;
    }
    
    /**
     * 更新模板评分
     */
    public void updateTemplateRating(Long templateId, Double rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("评分必须在0-5之间");
        }
        
        PromptTemplate template = getById(templateId);
        if (template != null) {
            // 计算新的平均评分
            double currentRating = template.getAverageRating() != null ? template.getAverageRating() : 0;
            int usageCount = template.getUsageCount() != null ? template.getUsageCount() : 0;
            
            double newRating = (currentRating * usageCount + rating) / (usageCount + 1);
            
            promptTemplateMapper.updateAverageRating(templateId, newRating);
            log.info("更新模板评分成功，ID: {}, 新评分: {}", templateId, newRating);
        }
    }
    
    /**
     * 启用指定模板，禁用其他版本
     */
    public void activateTemplate(Long templateId) {
        PromptTemplate template = getById(templateId);
        if (template != null) {
            template.setIsActive(1);
            template.setUpdatedTime(LocalDateTime.now());
            updateById(template);
            
            // 禁用其他版本的模板
            promptTemplateMapper.disableOtherVersions(template.getScene(), templateId);
            
            log.info("启用Prompt模板成功，ID: {}, 场景: {}", templateId, template.getScene());
        }
    }
    
    /**
     * 导入初始Prompt模板
     */
    public void importInitialTemplates() {
        log.info("开始导入初始Prompt模板...");
        
        // 需求咨询场景模板
        PromptTemplate demandTemplate = new PromptTemplate();
        demandTemplate.setName("需求咨询助手");
        demandTemplate.setScene("DEMAND_CONSULT");
        demandTemplate.setTemplate("""
            你是"校园智教"家教平台的AI助手。你的任务是帮助家长发布家教需求。
            
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
            
            回复要简洁友好，不要太长。用中文回复。
            """);
        demandTemplate.setVariables("{\"userRole\": \"string\", \"studentGrade\": \"string\", \"weakSubjects\": \"string\"}");
        demandTemplate.setIsActive(1);
        saveOrUpdateTemplate(demandTemplate);
        
        // 教员推荐场景模板
        PromptTemplate tutorTemplate = new PromptTemplate();
        tutorTemplate.setName("教员推荐助手");
        tutorTemplate.setScene("TUTOR_RECOMMEND");
        tutorTemplate.setTemplate("""
            你是"校园智教"家教平台的AI助手。你的任务是帮助家长了解和选择合适的教员。
            
            你需要：
            1. 解答关于教员资质、认证流程的问题
            2. 说明平台的教员筛选标准
            3. 帮助家长理解如何查看教员评价
            4. 解释试课、签约、退费等流程
            
            平台规则：
            {{platformRules}}
            
            回复要专业、简洁。用中文回复。
            """);
        tutorTemplate.setVariables("{\"platformRules\": \"string\"}");
        tutorTemplate.setIsActive(1);
        saveOrUpdateTemplate(tutorTemplate);
        
        // 教案生成场景模板
        PromptTemplate lessonPlanTemplate = new PromptTemplate();
        lessonPlanTemplate.setName("教案生成助手");
        lessonPlanTemplate.setScene("LESSON_PLAN");
        lessonPlanTemplate.setTemplate("""
            你是"校园智教"平台的AI教学赋能官，专业的教案生成助手。
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
            - 课后作业：[可选]
            """);
        lessonPlanTemplate.setVariables("{\"subject\": \"string\", \"studentLevel\": \"string\", \"lessonDuration\": \"string\", \"studentInfo\": \"string\", \"lessonPlanTemplates\": \"string\"}");
        lessonPlanTemplate.setIsActive(1);
        saveOrUpdateTemplate(lessonPlanTemplate);
        
        // 评语润色场景模板
        PromptTemplate commentTemplate = new PromptTemplate();
        commentTemplate.setName("评语润色助手");
        commentTemplate.setScene("COMMENT_POLISH");
        commentTemplate.setTemplate("""
            你是"校园智教"平台的AI教学赋能官，专业的评语润色助手。
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
            - 结尾：鼓励和期待
            """);
        commentTemplate.setVariables("{\"rawComment\": \"string\", \"subject\": \"string\", \"studentInfo\": \"string\", \"commentTemplates\": \"string\"}");
        commentTemplate.setIsActive(1);
        saveOrUpdateTemplate(commentTemplate);
        
        // 通用问答场景模板
        PromptTemplate generalTemplate = new PromptTemplate();
        generalTemplate.setName("通用问答助手");
        generalTemplate.setScene("GENERAL_QA");
        generalTemplate.setTemplate("""
            你是"校园智教"家教平台的AI客服助手。
            
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
            
            回复要友好、简洁、专业。用中文回复。如果问题超出你的知识范围，建议联系人工客服。
            """);
        generalTemplate.setVariables("{\"relevantKnowledge\": \"string\"}");
        generalTemplate.setIsActive(1);
        saveOrUpdateTemplate(generalTemplate);
        
        log.info("初始Prompt模板导入完成，共导入5个模板");
    }
    
    /**
     * 获取场景到模板的映射
     */
    public Map<String, String> getSceneMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("demand", "DEMAND_CONSULT");
        mapping.put("tutor", "TUTOR_RECOMMEND");
        mapping.put("general", "GENERAL_QA");
        mapping.put("lesson_plan", "LESSON_PLAN");
        mapping.put("comment_polish", "COMMENT_POLISH");
        return mapping;
    }
}