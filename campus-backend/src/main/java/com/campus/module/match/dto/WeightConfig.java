package com.campus.module.match.dto;

import lombok.Data;

/**
 * 动态权重配置
 * 根据用户成熟度动态调整各维度权重
 */
@Data
public class WeightConfig {

    // 科目匹配权重
    private double subjectWeight = 22.0;

    // 年级匹配权重
    private double gradeWeight = 13.0;

    // 距离评分权重
    private double distanceWeight = 18.0;

    // 价格匹配权重
    private double priceWeight = 10.0;

    // 教员评分权重
    private double ratingWeight = 10.0;

    // 教学经验权重
    private double experienceWeight = 7.0;

    // 学历背景权重
    private double educationWeight = 5.0;

    // 教学特长权重
    private double specialtyWeight = 5.0;

    // 热度评分权重（新增）
    private double hotnessWeight = 5.0;

    // 响应速度权重（新增）
    private double responseWeight = 5.0;

    /**
     * 使用默认权重配置
     */
    public static WeightConfig defaultConfig() {
        return new WeightConfig();
    }

    /**
     * 新用户权重配置：更依赖评分和学历
     */
    public static WeightConfig newUserConfig() {
        WeightConfig config = new WeightConfig();
        config.setRatingWeight(15.0); // 原10% -> 15%
        config.setEducationWeight(8.0); // 原5% -> 8%
        config.setHotnessWeight(2.0); // 原5% -> 2%
        config.setExperienceWeight(5.0); // 原7% -> 5%
        return config;
    }

    /**
     * 活跃用户权重配置：更依赖热度和行为
     */
    public static WeightConfig activeUserConfig() {
        WeightConfig config = new WeightConfig();
        config.setHotnessWeight(10.0); // 原5% -> 10%
        config.setRatingWeight(7.0); // 原10% -> 7%
        config.setResponseWeight(8.0); // 原5% -> 8%
        return config;
    }
}
