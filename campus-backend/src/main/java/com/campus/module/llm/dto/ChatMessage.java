package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 聊天消息
 */
public class ChatMessage {

    @Schema(description = "角色: system, user, assistant, tool")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    private String toolCallId;

    private cn.hutool.json.JSONArray toolCalls;

    public ChatMessage() {}

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }

    public static ChatMessage assistantWithTool(cn.hutool.json.JSONArray toolCalls) {
        ChatMessage msg = new ChatMessage("assistant", null);
        msg.setToolCalls(toolCalls);
        return msg;
    }

    public static ChatMessage toolResult(String toolCallId, String content) {
        ChatMessage msg = new ChatMessage("tool", content);
        msg.setToolCallId(toolCallId);
        return msg;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public cn.hutool.json.JSONArray getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(cn.hutool.json.JSONArray toolCalls) {
        this.toolCalls = toolCalls;
    }
}
