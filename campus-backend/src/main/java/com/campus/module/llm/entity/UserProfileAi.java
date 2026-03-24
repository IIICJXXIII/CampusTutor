package com.campus.module.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI用户画像实体
 */
@Data
@TableName("user_profile_ai")
public class UserProfileAi {
    
    /**
     * 用户ID（关联sys_user.id）
     */
    @TableId(type = IdType.INPUT)
    private Long userId;
    
    /**
     * 用户角色
     * TEACHER - 教员
     * PARENT - 家长
     */
    private String role;
    
    /**
     * 偏好设置（JSON格式）
     * 例如：{"teachingStyle": "互动式", "communicationPreference": "文字"}
     */
    private String preferences;
    
    /**
     * 教学风格（教员专用）
     * INTERACTIVE - 互动式
     * STRUCTURED - 结构化
     * CREATIVE - 创意式
     * STRICT - 严格式
     */
    private String teachingStyle;
    
    /**
     * 擅长科目（教员专用，JSON数组）
     */
    private String expertSubjects;
    
    /**
     * 教学经验（教员专用）
     * BEGINNER - 新手（<1年）
     * INTERMEDIATE - 中级（1-3年）
     * EXPERIENCED - 有经验（3-5年）
     * EXPERT - 专家（>5年）
     */
    private String teachingExperience;
    
    /**
     * 学习需求（家长/学生专用，JSON格式）
     * 例如：{"weakSubjects": ["数学", "物理"], "targetScore": "提高20分"}
     */
    private String learningNeeds;
    
    /**
     * 学生年级（家长/学生专用）
     */
    private String studentGrade;
    
    /**
     * 学生薄弱科目（家长/学生专用，JSON数组）
     */
    private String weakSubjects;
    
    /**
     * 学习习惯（家长/学生专用）
     * VISUAL - 视觉型
     * AUDITORY - 听觉型
     * KINESTHETIC - 动觉型
     */
    private String learningStyle;
    
    /**
     * AI交互历史摘要（JSON格式）
     * 记录用户与AI的交互模式和偏好
     */
    private String interactionSummary;
    
    /**
     * 个性化设置（JSON格式）
     * 例如：{"responseLength": "detailed", "tone": "professional"}
     */
    private String personalizationSettings;
    
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