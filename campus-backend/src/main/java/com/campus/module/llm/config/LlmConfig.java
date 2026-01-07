package com.campus.module.llm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM配置类
 * 支持多种LLM服务商: DeepSeek, OpenAI, Azure OpenAI, 通义千问等
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    /**
     * 是否启用LLM功能
     */
    private boolean enabled = false;

    /**
     * LLM服务提供商: deepseek, openai, azure, qwen
     */
    private String provider = "deepseek";

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API密钥(部分服务商需要)
     */
    private String secretKey;

    /**
     * API基础URL
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 模型名称
     */
    private String model = "deepseek-chat";

    /**
     * 最大Token数
     */
    private Integer maxTokens = 1000;

    /**
     * 温度参数(0-1，越低越确定性)
     */
    private Double temperature = 0.3;

    /**
     * 请求超时时间(秒)
     */
    private Integer timeout = 30;
}
