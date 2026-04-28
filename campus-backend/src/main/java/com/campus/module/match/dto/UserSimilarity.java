package com.campus.module.match.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户相似度结果DTO
 * 存储两个用户之间的相似度值
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimilarity {

    /**
     * 相似用户ID
     */
    private Long userId;

    /**
     * 相似度分数 (0.0 ~ 1.0)
     * 1.0 表示完全相似，0.0 表示无相似性
     */
    private Double similarity;

    /**
     * 共同交互的教员数量
     */
    private Integer commonItems;
}
