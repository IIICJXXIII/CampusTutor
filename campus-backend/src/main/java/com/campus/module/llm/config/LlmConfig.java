package com.campus.module.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM配置类
 * 支持多种LLM服务商: DeepSeek, OpenAI, Azure OpenAI, 通义千问等
 */
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
