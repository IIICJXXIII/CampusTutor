package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * RAG检索结果DTO
 */
@Data
@Schema(description = "RAG检索结果")
public class RagSearchResult {
    
    @Schema(description = "检索到的文档列表")
    private List<KnowledgeDocumentDTO> documents;
    
    @Schema(description = "检索到的相关文本片段")
    private List<String> relevantTexts;
    
    @Schema(description = "检索耗时（毫秒）")
    private Long searchTimeMs;
    
    @Schema(description = "检索模式：KEYWORD/SEMANTIC/HYBRID")
    private String searchMode;
    
    @Schema(description = "是否命中知识库")
    private Boolean hasRelevantContent;
    
    @Schema(description = "最高相关性分数")
    private Double maxScore;
    
    @Schema(description = "平均相关性分数")
    private Double avgScore;
    
    @Schema(description = "检索统计信息")
    private SearchStats stats;
    
    @Data
    @Schema(description = "知识库文档DTO")
    public static class KnowledgeDocumentDTO {
        
        @Schema(description = "文档ID")
        private Long id;
        
        @Schema(description = "文档标题")
        private String title;
        
        @Schema(description = "文档内容摘要")
        private String contentSummary;
        
        @Schema(description = "文档类型")
        private String docType;
        
        @Schema(description = "来源")
        private String source;
        
        @Schema(description = "标签")
        private List<String> tags;
        
        @Schema(description = "相关性分数")
        private Double relevanceScore;
        
        @Schema(description = "适用角色")
        private String targetRole;
        
        @Schema(description = "适用科目")
        private List<String> applicableSubjects;
        
        @Schema(description = "适用年级")
        private List<String> applicableGrades;
    }
    
    @Data
    @Schema(description = "检索统计信息")
    public static class SearchStats {
        
        @Schema(description = "总文档数")
        private Integer totalDocuments;
        
        @Schema(description = "检索到的文档数")
        private Integer retrievedDocuments;
        
        @Schema(description = "关键词检索命中数")
        private Integer keywordHits;
        
        @Schema(description = "语义检索命中数")
        private Integer semanticHits;
        
        @Schema(description = "过滤掉的文档数")
        private Integer filteredDocuments;
    }
}