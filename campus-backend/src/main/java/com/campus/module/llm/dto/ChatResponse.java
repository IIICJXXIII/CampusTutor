package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 智能对话响应
 */
@Schema(description = "智能对话响应")
public class ChatResponse {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "消耗的Token数")
    private Integer tokensUsed;

    @Schema(description = "错误信息")
    private String error;

    @Schema(description = "工具调用")
    private cn.hutool.json.JSONArray toolCalls;

    public static ChatResponse success(String content, Integer tokens) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(true);
        response.setContent(content);
        response.setTokensUsed(tokens);
        return response;
    }

    public static ChatResponse fail(String error) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public cn.hutool.json.JSONArray getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(cn.hutool.json.JSONArray toolCalls) {
        this.toolCalls = toolCalls;
    }
}
