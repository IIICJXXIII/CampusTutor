package com.campus.module.llm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM配置类
 * 支持多种LLM服务商: OpenAI, Azure OpenAI, 通义千问, 文心一言等
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
     * LLM服务提供商: openai, azure, qwen, wenxin
     */
    private String provider = "openai";

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
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * 模型名称
     */
    private String model = "gpt-3.5-turbo";

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
