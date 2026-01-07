package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能对话响应
 */
@Data
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
}
