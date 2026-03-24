package com.campus.module.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体
 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 文档类型
     * RULE - 平台规则
     * LESSON_PLAN - 教案模板
     * COMMENT - 评语模板
     * FAQ - 常见问题
     * TEACHING_EXPERIENCE - 教学经验
     * KNOWLEDGE_POINT - 知识点
     */
    private String docType;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 标签（JSON数组）
     */
    private String tags;
    
    /**
     * 向量ID（在向量数据库中的ID）
     */
    private String embeddingId;
    
    /**
     * 相关性评分（用于排序）
     */
    private Double relevanceScore;
    
    /**
     * 适用角色
     * TEACHER - 教员
     * PARENT - 家长
     * ALL - 所有角色
     */
    private String targetRole;
    
    /**
     * 适用科目（JSON数组）
     */
    private String applicableSubjects;
    
    /**
     * 适用年级（JSON数组）
     */
    private String applicableGrades;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}