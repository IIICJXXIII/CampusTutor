package com.campus.module.llm.service;

import com.campus.module.llm.config.LlmConfig;
import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天上下文管理器
 * 实现滑动窗口 + 摘要压缩机制，控制发送给LLM的上下文大小
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatContextManager {

    private final LlmConfig config;
    private final LlmClientService llmClient;

    /**
     * 摘要生成的系统提示词
     */
    private static final String SUMMARY_SYSTEM_PROMPT = """
            你是一个对话摘要助手。你的任务是将一段对话历史压缩成简洁的摘要。
            
            规则：
            1. 保留关键信息：用户的需求、偏好、已确认的事项
            2. 保留重要的上下文：提到的科目、年级、地点、价格等具体信息
            3. 去除寒暄和重复内容
            4. 摘要使用第三人称描述
            5. 摘要要简洁，不超过指定字数
            
            如果提供了"之前的摘要"，请将其与新的对话内容合并，生成一份完整的更新摘要。
            """;

    /**
     * 管理上下文：如果消息量超过窗口大小，将旧消息压缩为摘要
     *
     * @param messages        前端传来的全部历史消息
     * @param previousSummary 上一次的摘要（可能为空）
     * @return 管理后的上下文结果
     */
    public ManagedContext manageContext(List<ChatMessage> messages, String previousSummary) {
        int windowSize = config.getContextWindowSize();
        boolean summaryEnabled = config.isEnableSummary();

        // 如果消息数量没有超出窗口，直接返回，不需要压缩
        if (messages.size() <= windowSize || !summaryEnabled) {
            return new ManagedContext(messages, previousSummary);
        }

        log.info("消息数量({})超出上下文窗口({}), 开始生成摘要", messages.size(), windowSize);

        // 拆分：旧消息需要被压缩，新消息保留原文
        List<ChatMessage> oldMessages = messages.subList(0, messages.size() - windowSize);
        List<ChatMessage> recentMessages = messages.subList(messages.size() - windowSize, messages.size());

        // 生成摘要
        String newSummary = generateSummary(oldMessages, previousSummary);

        return new ManagedContext(new ArrayList<>(recentMessages), newSummary);
    }

    /**
     * 将旧消息和之前的摘要合并生成新摘要
     */
    private String generateSummary(List<ChatMessage> oldMessages, String previousSummary) {
        try {
            // 构建要摘要的内容
            StringBuilder contentToSummarize = new StringBuilder();

            if (previousSummary != null && !previousSummary.trim().isEmpty()) {
                contentToSummarize.append("【之前的摘要】\n").append(previousSummary).append("\n\n");
            }

            contentToSummarize.append("【需要压缩的新对话】\n");
            for (ChatMessage msg : oldMessages) {
                // 只处理 user 和 assistant 角色的消息内容
                if (("user".equals(msg.getRole()) || "assistant".equals(msg.getRole()))
                        && msg.getContent() != null) {
                    String roleName = "user".equals(msg.getRole()) ? "用户" : "助手";
                    contentToSummarize.append(roleName).append(": ").append(msg.getContent()).append("\n");
                }
            }

            int maxLength = config.getSummaryMaxLength();
            String userPrompt = "请将以上对话内容压缩为不超过" + maxLength + "字的摘要：\n\n" + contentToSummarize;

            List<ChatMessage> summaryMessages = new ArrayList<>();
            summaryMessages.add(ChatMessage.system(SUMMARY_SYSTEM_PROMPT));
            summaryMessages.add(ChatMessage.user(userPrompt));

            // 调用LLM生成摘要（不使用工具）
            ChatResponse response = llmClient.chat(summaryMessages, null);

            if (response.getSuccess() && response.getContent() != null) {
                String summary = response.getContent().trim();
                // 截断防止摘要过长
                if (summary.length() > maxLength) {
                    summary = summary.substring(0, maxLength) + "...";
                }
                log.info("摘要生成成功, 长度: {}", summary.length());
                return summary;
            } else {
                log.warn("摘要生成失败: {}, 回退使用之前的摘要", response.getError());
                return previousSummary;
            }
        } catch (Exception e) {
            log.error("摘要生成异常", e);
            return previousSummary;
        }
    }

    /**
     * 管理后的上下文结果
     */
    public static class ManagedContext {
        private final List<ChatMessage> messages;
        private final String summary;

        public ManagedContext(List<ChatMessage> messages, String summary) {
            this.messages = messages;
            this.summary = summary;
        }

        public List<ChatMessage> getMessages() {
            return messages;
        }

        public String getSummary() {
            return summary;
        }

        public boolean hasSummary() {
            return summary != null && !summary.trim().isEmpty();
        }
    }
}
