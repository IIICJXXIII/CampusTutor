package com.campus.module.recommend.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 相似教员推荐 DTO
 */
@Data
public class SimilarTutorDTO {

    /**
     * 教员ID
     */
    private Long tutorId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名（脱敏）
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 大学名称
     */
    private String universityName;

    /**
     * 专业
     */
    private String major;

    /**
     * 期望价格
     */
    private BigDecimal expectPrice;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 教授科目（JSON）
     */
    private String teachSubjects;

    /**
     * 相似度分数 (0-1)
     */
    private Double similarityScore;

    /**
     * 共同交互用户数
     */
    private Integer coInteractionCount;
}
