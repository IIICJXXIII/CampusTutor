package com.campus.module.llm.service;

import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能客服服务
 * 提供家教平台相关的问答服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantService {

    private final LlmClientService llmClient;

    /**
     * 需求咨询场景的系统提示词
     */
    private static final String DEMAND_SYSTEM_PROMPT = """
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
            
            回复要简洁友好，不要太长。用中文回复。
            """;

    /**
     * 教员推荐场景的系统提示词
     */
    private static final String TUTOR_SYSTEM_PROMPT = """
            你是"校园智教"家教平台的AI助手。你的任务是帮助家长了解和选择合适的教员。
            
            你需要：
            1. 解答关于教员资质、认证流程的问题
            2. 说明平台的教员筛选标准
            3. 帮助家长理解如何查看教员评价
            4. 解释试课、签约、退费等流程
            
            回复要专业、简洁。用中文回复。
            """;

    /**
     * 通用问答场景的系统提示词
     */
    private static final String GENERAL_SYSTEM_PROMPT = """
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
            
            回复要友好、简洁、专业。用中文回复。如果问题超出你的知识范围，建议联系人工客服。
            """;

    /**
     * 智能对话
     *
     * @param messages 历史消息
     * @param scene    场景: demand, tutor, general
     * @return 回复
     */
    public ChatResponse chat(List<ChatMessage> messages, String scene) {
        // 根据场景选择系统提示词
        String systemPrompt = switch (scene) {
            case "demand" -> DEMAND_SYSTEM_PROMPT;
            case "tutor" -> TUTOR_SYSTEM_PROMPT;
            default -> GENERAL_SYSTEM_PROMPT;
        };

        // 构建完整消息列表
        List<ChatMessage> fullMessages = new ArrayList<>();
        fullMessages.add(ChatMessage.system(systemPrompt));
        fullMessages.addAll(messages);

        return llmClient.chat(fullMessages);
    }

    /**
     * 快速问答（无历史上下文）
     *
     * @param question 问题
     * @return 回答
     */
    public String quickAnswer(String question) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.user(question));

        ChatResponse response = chat(messages, "general");
        if (response.getSuccess()) {
            return response.getContent();
        } else {
            return "抱歉，暂时无法回答您的问题。您可以联系人工客服获取帮助。";
        }
    }

    /**
     * 教案生成场景的系统提示词
     */
    private static final String LESSON_PLAN_SYSTEM_PROMPT = """
            你是"校园智教"平台的AI教学赋能官，专业的教案生成助手。
            你的任务是为大学生教员生成详细的课程教案。
            
            教案要求：
            1. 结构清晰：包含热身、主要内容、练习、游戏、总结等环节
            2. 时间合理：根据给定的课时时长分配时间
            3. 针对性强：根据学生水平和科目特点设计内容
            4. 实用性高：提供具体的教学方法和练习内容
            5. 语言专业：使用专业的教学术语，但保持易懂
            
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
            """;

    /**
     * 评语润色场景的系统提示词
     */
    private static final String COMMENT_POLISH_SYSTEM_PROMPT = """
            你是"校园智教"平台的AI教学赋能官，专业的评语润色助手。
            你的任务是将教员的简单评语润色为专业、温馨的家长反馈。
            
            润色要求：
            1. 语言温暖：使用亲切、鼓励的语气
            2. 专业表达：使用教育专业术语，体现专业性
            3. 具体详细：将简单描述扩展为具体的观察和分析
            4. 正面引导：突出学生的进步和优点
            5. 建设性建议：提供具体的改进方向
            6. 家长友好：让家长感受到教师的用心和专业
            
            输出格式：
            - 开头：亲切的问候
            - 主体：详细的学习情况反馈
            - 优点：学生的进步和闪光点
            - 建议：具体的改进方向
            - 结尾：鼓励和期待
            """;

    /**
     * 生成教案
     *
     * @param subject      教学科目
     * @param studentLevel 学生水平
     * @param lessonDuration 课时时长
     * @param studentInfo  学生情况
     * @return 教案内容
     */
    public String generateLessonPlan(String subject, String studentLevel, String lessonDuration, String studentInfo) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(LESSON_PLAN_SYSTEM_PROMPT));
        messages.add(ChatMessage.user("请为以下情况生成详细教案：\n" +
                "科目：" + subject + "\n" +
                "学生水平：" + studentLevel + "\n" +
                "课时时长：" + lessonDuration + "\n" +
                "学生情况：" + studentInfo));
        
        ChatResponse response = llmClient.chat(messages);
        if (!response.getSuccess()) {
            throw new RuntimeException("AI教案生成失败：" + response.getError());
        }
        return response.getContent();
    }

    /**
     * 润色评语
     *
     * @param rawComment 原始评语
     * @param subject    教学科目
     * @param studentInfo 学生情况
     * @return 润色后的评语
     */
    public String polishComment(String rawComment, String subject, String studentInfo) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(COMMENT_POLISH_SYSTEM_PROMPT));
        messages.add(ChatMessage.user("请润色以下评语：\n" +
                "原始评语：" + rawComment + "\n" +
                "科目：" + subject + "\n" +
                "学生情况：" + studentInfo));
        
        ChatResponse response = llmClient.chat(messages);
        if (!response.getSuccess()) {
            throw new RuntimeException("AI评语润色失败：" + response.getError());
        }
        return response.getContent();
    }
}
