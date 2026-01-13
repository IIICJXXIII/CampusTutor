package com.campus.module.match.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协同过滤推荐结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CFRecommendation {

    /**
     * 教员ID
     */
    private Long tutorId;

    /**
     * 协同过滤预测分数 (0.0 ~ 1.0)
     */
    private Double cfScore;

    /**
     * 贡献该推荐的相似用户数量
     */
    private Integer contributorCount;

    /**
     * 推荐来源说明（可用于解释）
     */
    private String source;
}
