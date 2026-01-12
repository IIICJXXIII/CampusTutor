package com.campus.module.llm.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.module.llm.config.LlmConfig;
import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * LLM客户端服务
 * 封装与各种LLM服务商的API调用
 */
@Service
public class LlmClientService {

    private static final Logger log = LoggerFactory.getLogger(LlmClientService.class);
    
    private final LlmConfig config;
    
    public LlmClientService(LlmConfig config) {
        this.config = config;
    }

    /**
     * 发送聊天请求
     *
     * @param messages 消息列表
     * @return 聊天响应
     */
    public ChatResponse chat(List<ChatMessage> messages) {
        if (!config.isEnabled()) {
            return ChatResponse.fail("LLM服务未启用");
        }

        if (!StringUtils.hasText(config.getApiKey())) {
            return ChatResponse.fail("LLM API Key未配置");
        }

        try {
            switch (config.getProvider().toLowerCase()) {
                case "deepseek":
                    return callDeepSeek(messages);
                case "openai":
                case "azure":
                    return callOpenAI(messages);
                case "qwen":
                    return callQwen(messages);
                default:
                    return callDeepSeek(messages);
            }
        } catch (Exception e) {
            log.error("LLM调用失败: {}", e.getMessage(), e);
            return ChatResponse.fail("LLM调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用DeepSeek API
     * DeepSeek V3使用OpenAI兼容的API格式
     */
    private ChatResponse callDeepSeek(List<ChatMessage> messages) {
        String url = config.getBaseUrl() + "/chat/completions";

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", config.getModel());
        requestBody.set("max_tokens", config.getMaxTokens());
        requestBody.set("temperature", config.getTemperature());

        JSONArray messagesArray = new JSONArray();
        for (ChatMessage msg : messages) {
            JSONObject msgObj = new JSONObject();
            msgObj.set("role", msg.getRole());
            msgObj.set("content", msg.getContent());
            messagesArray.add(msgObj);
        }
        requestBody.set("messages", messagesArray);

        log.debug("DeepSeek请求: {}", requestBody);

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(config.getTimeout() * 1000)
                .execute();

        String responseBody = response.body();
        log.debug("DeepSeek响应: {}", responseBody);

        if (!response.isOk()) {
            return ChatResponse.fail("DeepSeek API调用失败: " + response.getStatus() + " - " + responseBody);
        }

        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getStr("content");

            JSONObject usage = jsonResponse.getJSONObject("usage");
            Integer tokensUsed = usage != null ? usage.getInt("total_tokens") : null;

            return ChatResponse.success(content, tokensUsed);
        }

        return ChatResponse.fail("DeepSeek API响应格式错误");
    }

    /**
     * 调用OpenAI兼容的API
     */
    private ChatResponse callOpenAI(List<ChatMessage> messages) {
        String url = config.getBaseUrl() + "/chat/completions";

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", config.getModel());
        requestBody.set("max_tokens", config.getMaxTokens());
        requestBody.set("temperature", config.getTemperature());

        JSONArray messagesArray = new JSONArray();
        for (ChatMessage msg : messages) {
            JSONObject msgObj = new JSONObject();
            msgObj.set("role", msg.getRole());
            msgObj.set("content", msg.getContent());
            messagesArray.add(msgObj);
        }
        requestBody.set("messages", messagesArray);

        log.debug("OpenAI请求: {}", requestBody);

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(config.getTimeout() * 1000)
                .execute();

        String responseBody = response.body();
        log.debug("OpenAI响应: {}", responseBody);

        if (!response.isOk()) {
            return ChatResponse.fail("API调用失败: " + response.getStatus() + " - " + responseBody);
        }

        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getStr("content");

            JSONObject usage = jsonResponse.getJSONObject("usage");
            Integer tokensUsed = usage != null ? usage.getInt("total_tokens") : null;

            return ChatResponse.success(content, tokensUsed);
        }

        return ChatResponse.fail("API响应格式错误");
    }

    /**
     * 调用通义千问API
     */
    private ChatResponse callQwen(List<ChatMessage> messages) {
        String url = config.getBaseUrl();
        if (!url.contains("dashscope")) {
            url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        }

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", config.getModel());

        JSONObject input = new JSONObject();
        JSONArray messagesArray = new JSONArray();
        for (ChatMessage msg : messages) {
            JSONObject msgObj = new JSONObject();
            msgObj.set("role", msg.getRole());
            msgObj.set("content", msg.getContent());
            messagesArray.add(msgObj);
        }
        input.set("messages", messagesArray);
        requestBody.set("input", input);

        JSONObject parameters = new JSONObject();
        parameters.set("max_tokens", config.getMaxTokens());
        parameters.set("temperature", config.getTemperature());
        requestBody.set("parameters", parameters);

        log.debug("Qwen请求: {}", requestBody);

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(config.getTimeout() * 1000)
                .execute();

        String responseBody = response.body();
        log.debug("Qwen响应: {}", responseBody);

        if (!response.isOk()) {
            return ChatResponse.fail("API调用失败: " + response.getStatus() + " - " + responseBody);
        }

        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
        JSONObject output = jsonResponse.getJSONObject("output");
        if (output != null) {
            String content = output.getStr("text");
            if (content == null) {
                JSONArray choices = output.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    content = message.getStr("content");
                }
            }

            JSONObject usage = jsonResponse.getJSONObject("usage");
            Integer tokensUsed = usage != null ? usage.getInt("total_tokens") : null;

            return ChatResponse.success(content, tokensUsed);
        }

        return ChatResponse.fail("API响应格式错误");
    }
}
