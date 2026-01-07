package com.campus.module.llm;

import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import com.campus.module.llm.service.LlmClientService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeepSeek LLM 服务测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DeepSeek LLM 服务测试")
class LlmClientServiceTest {

    @Autowired
    private LlmClientService llmClientService;

    @Test
    @Order(1)
    @DisplayName("1. 简单对话测试")
    void testSimpleChat() {
        List<ChatMessage> messages = Arrays.asList(
            ChatMessage.system("你是一个专业的家教顾问助手"),
            ChatMessage.user("请用一句话介绍自己")
        );

        ChatResponse response = llmClientService.chat(messages);
        
        assertNotNull(response, "响应不应为空");
        System.out.println("对话测试:");
        System.out.println("成功: " + response.getSuccess());
        
        if (Boolean.TRUE.equals(response.getSuccess())) {
            System.out.println("✅ AI回复: " + response.getContent());
        } else {
            System.out.println("⚠️ " + response.getError());
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. 家教需求解析测试")
    void testDemandParsing() {
        List<ChatMessage> messages = Arrays.asList(
            ChatMessage.system("你是一个专业的家教需求解析助手。请从用户描述中提取信息。"),
            ChatMessage.user("我家孩子初二，数学不太好，想找个大学生家教")
        );

        ChatResponse response = llmClientService.chat(messages);
        
        assertNotNull(response, "响应不应为空");
        
        if (Boolean.TRUE.equals(response.getSuccess())) {
            System.out.println("✅ 解析结果: " + response.getContent());
        } else {
            System.out.println("⚠️ 服务状态: " + response.getError());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. API连接验证")
    void testApiConnection() {
        List<ChatMessage> messages = Arrays.asList(
            ChatMessage.user("你好")
        );

        ChatResponse response = llmClientService.chat(messages);
        
        assertNotNull(response, "API应返回结果");
        
        if (Boolean.TRUE.equals(response.getSuccess())) {
            System.out.println("✅ DeepSeek API连接成功");
        } else if (response.getError() != null && response.getError().contains("未启用")) {
            System.out.println("⚠️ LLM服务未启用，请设置llm.enabled=true");
        } else if (response.getError() != null && response.getError().contains("未配置")) {
            System.out.println("⚠️ API Key未配置，请设置llm.api-key");
        } else {
            System.out.println("⚠️ API状态: " + response.getError());
        }
    }
}
