package com.campus.module.match.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 匹配评分结果
 */
@Data
public class MatchScoreResult extends TutorSearchResult {

    /**
     * 综合匹配分数 (0-100)
     */
    private Double matchScore;

    /**
     * 科目匹配分数
     */
    private Double subjectScore;

    /**
     * 年级匹配分数
     */
    private Double gradeScore;

    /**
     * 距离匹配分数
     */
    private Double distanceScore;

    /**
     * 价格匹配分数
     */
    private Double priceScore;

    /**
     * 评分权重分数
     */
    private Double ratingScore;

    /**
     * 教学经验分数
     */
    private Double experienceScore;

    /**
     * 学历背景分数
     */
    private Double educationScore;

    /**
     * 教学特长分数
     */
    private Double specialtyScore;

    /**
     * 匹配标签（如：科目匹配、距离近、评分高等）
     */
    private List<String> matchTags;
}
