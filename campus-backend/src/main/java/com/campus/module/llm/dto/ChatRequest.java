package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 智能对话请求
 */
@Data
@Schema(description = "智能对话请求")
public class ChatRequest {

    @Schema(description = "对话消息列表", required = true)
    @NotEmpty(message = "消息列表不能为空")
    private List<ChatMessage> messages;

    @Schema(description = "对话场景: demand-需求咨询, tutor-教员推荐, general-通用问答", example = "demand")
    private String scene = "general";

    @Schema(description = "是否流式返回", example = "false")
    private Boolean stream = false;
}
