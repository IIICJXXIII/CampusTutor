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

    @Schema(description = "对话场景（已废弃，不再影响逻辑，保留仅为向后兼容）", example = "general", deprecated = true)
    private String scene = "general";

    @Schema(description = "上一次对话的历史摘要（由后端生成，前端存储后回传）")
    private String summary;

    @Schema(description = "是否流式返回", example = "false")
    private Boolean stream = false;
}
