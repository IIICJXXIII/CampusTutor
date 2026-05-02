package com.campus.module.demand.dto;

import com.campus.module.demand.entity.DemandPost;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 带有匹配度的需求信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DemandWithMatchScore extends DemandPost {

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
     * 匹配标签（如：科目匹配、距离近等）
     */
    private List<String> matchTags;

    /**
     * 匹配等级
     */
    private String matchLevel;

    /**
     * 与搜索位置的距离(公里)
     */
    private Double distance;
}
