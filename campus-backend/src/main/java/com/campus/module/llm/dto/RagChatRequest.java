package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * RAG增强聊天请求DTO
 */
@Data
@Schema(description = "RAG增强聊天请求")
public class RagChatRequest {
    
    @Schema(description = "对话消息列表", required = true)
    @NotNull(message = "消息列表不能为空")
    @Valid
    private List<ChatMessage> messages;
    
    @Schema(description = "场景：demand/tutor/general/lesson_plan/comment_polish", required = true)
    @NotBlank(message = "场景不能为空")
    private String scene;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户角色：TEACHER/PARENT")
    private String userRole;
    
    @Schema(description = "是否启用RAG检索，默认true")
    private Boolean enableRag = true;
    
    @Schema(description = "RAG检索配置")
    private RagConfig ragConfig;
    
    @Schema(description = "个性化配置")
    private PersonalizationConfig personalization;
    
    @Data
    @Schema(description = "RAG配置")
    public static class RagConfig {
        
        @Schema(description = "检索topK数量，默认3")
        private Integer topK = 3;
        
        @Schema(description = "最小相关性阈值，默认0.4")
        private Double minScore = 0.4;
        
        @Schema(description = "是否在回答中引用来源")
        private Boolean citeSources = true;
        
        @Schema(description = "检索模式：HYBRID/KEYWORD/SEMANTIC")
        private String searchMode = "HYBRID";
        
        @Schema(description = "知识库文档类型过滤")
        private List<String> docTypes;
    }
    
    @Data
    @Schema(description = "个性化配置")
    public static class PersonalizationConfig {
        
        @Schema(description = "回答长度：CONCISE/NORMAL/DETAILED")
        private String responseLength = "NORMAL";
        
        @Schema(description = "语气：FRIENDLY/PROFESSIONAL/ENCOURAGING")
        private String tone = "FRIENDLY";
        
        @Schema(description = "是否使用用户画像")
        private Boolean useProfile = true;
        
        @Schema(description = "是否记忆对话历史")
        private Boolean rememberHistory = true;
    }
}