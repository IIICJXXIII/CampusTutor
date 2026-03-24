package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * RAG检索请求DTO
 */
@Data
@Schema(description = "RAG检索请求")
public class RagSearchRequest {
    
    @Schema(description = "查询文本", required = true)
    @NotBlank(message = "查询文本不能为空")
    private String query;
    
    @Schema(description = "检索场景")
    private String scene;
    
    @Schema(description = "用户角色：TEACHER/PARENT")
    private String userRole;
    
    @Schema(description = "科目（用于过滤）")
    private String subject;
    
    @Schema(description = "年级（用于过滤）")
    private String grade;
    
    @Schema(description = "返回结果数量，默认5")
    private Integer topK = 5;
    
    @Schema(description = "最小相关性阈值，默认0.3")
    private Double minScore = 0.3;
    
    @Schema(description = "是否启用关键词检索")
    private Boolean enableKeywordSearch = true;
    
    @Schema(description = "是否启用语义检索")
    private Boolean enableSemanticSearch = true;
}