package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * RAG增强聊天响应DTO
 */
@Data
@Schema(description = "RAG增强聊天响应")
public class RagChatResponse {
    
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "AI回答内容")
    private String content;
    
    @Schema(description = "错误信息")
    private String error;
    
    @Schema(description = "使用的token数量")
    private Integer tokensUsed;
    
    @Schema(description = "RAG检索信息")
    private RagInfo ragInfo;
    
    @Schema(description = "个性化信息")
    private PersonalizationInfo personalizationInfo;
    
    @Schema(description = "响应时间（毫秒）")
    private Long responseTimeMs;
    
    @Data
    @Schema(description = "RAG信息")
    public static class RagInfo {
        
        @Schema(description = "是否启用了RAG")
        private Boolean enabled;
        
        @Schema(description = "检索到的相关文档")
        private List<KnowledgeDocumentRef> relevantDocuments;
        
        @Schema(description = "使用的知识片段")
        private List<String> usedKnowledgeSnippets;
        
        @Schema(description = "检索耗时（毫秒）")
        private Long searchTimeMs;
        
        @Schema(description = "是否命中知识库")
        private Boolean hasRelevantContent;
        
        @Schema(description = "最高相关性分数")
        private Double maxRelevanceScore;
    }
    
    @Data
    @Schema(description = "知识库文档引用")
    public static class KnowledgeDocumentRef {
        
        @Schema(description = "文档ID")
        private Long id;
        
        @Schema(description = "文档标题")
        private String title;
        
        @Schema(description = "文档类型")
        private String docType;
        
        @Schema(description = "相关性分数")
        private Double relevanceScore;
        
        @Schema(description = "引用的内容片段")
        private String citedContent;
    }
    
    @Data
    @Schema(description = "个性化信息")
    public static class PersonalizationInfo {
        
        @Schema(description = "是否使用了用户画像")
        private Boolean usedProfile;
        
        @Schema(description = "使用的用户画像特征")
        private List<String> usedProfileFeatures;
        
        @Schema(description = "使用的Prompt模板")
        private String promptTemplate;
        
        @Schema(description = "回答风格")
        private String responseStyle;
    }
    
    /**
     * 创建成功响应
     */
    public static RagChatResponse success(String content, Integer tokensUsed, RagInfo ragInfo, PersonalizationInfo personalizationInfo) {
        RagChatResponse response = new RagChatResponse();
        response.setSuccess(true);
        response.setContent(content);
        response.setTokensUsed(tokensUsed);
        response.setRagInfo(ragInfo);
        response.setPersonalizationInfo(personalizationInfo);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static RagChatResponse fail(String error) {
        RagChatResponse response = new RagChatResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
}