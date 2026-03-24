package com.campus.module.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Prompt模板实体
 */
@Data
@TableName("prompt_template")
public class PromptTemplate {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 场景类型
     * DEMAND_CONSULT - 需求咨询
     * TUTOR_RECOMMEND - 教员推荐
     * GENERAL_QA - 通用问答
     * LESSON_PLAN - 教案生成
     * COMMENT_POLISH - 评语润色
     * DEMAND_PARSE - 需求解析
     */
    private String scene;
    
    /**
     * 模板内容
     */
    private String template;
    
    /**
     * 变量定义（JSON格式）
     * 例如：{"userRole": "string", "subject": "string"}
     */
    private String variables;
    
    /**
     * 示例对话（JSON数组）
     */
    private String examples;
    
    /**
     * 约束条件
     */
    private String constraints;
    
    /**
     * 输出格式
     */
    private String outputFormat;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;
    
    /**
     * 使用次数
     */
    private Integer usageCount;
    
    /**
     * 平均评分（0-5）
     */
    private Double averageRating;
    
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