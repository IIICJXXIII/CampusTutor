package com.campus.module.match.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 匹配算法权重配置
 * 通过 application.properties 配置匹配算法的各维度权重
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "campus.match")
public class MatchWeightConfig {

    // ============ 家长视角权重配置（找老师，总计 100%）============

    /** 科目匹配权重 */
    private double parentSubject = 23.0;

    /** 年级匹配权重 */
    private double parentGrade = 14.0;

    /** 距离评分权重 */
    private double parentDistance = 18.0;

    /** 价格匹配权重（教师报价越低越好） */
    private double parentPrice = 10.0;

    /** 教员评分权重 */
    private double parentRating = 10.0;

    /** 教学经验权重 */
    private double parentExperience = 10.0;

    /** 学历背景权重 */
    private double parentEducation = 5.0;

    /** 教学特长权重 */
    private double parentSpecialty = 5.0;

    /** 授课方式匹配权重 */
    private double parentTeachMode = 5.0;

    // ============ 教师视角权重配置（找需求，总计 100%）============

    /** 科目匹配权重 */
    private double teacherSubject = 25.0;

    /** 年级匹配权重 */
    private double teacherGrade = 15.0;

    /** 距离评分权重 */
    private double teacherDistance = 15.0;

    /** 价格匹配权重（需求预算越高越好） */
    private double teacherPrice = 20.0;

    /** 授课方式匹配权重 */
    private double teacherTeachMode = 10.0;

    /** 需求新鲜度权重（新发布更好） */
    private double teacherFreshness = 10.0;

    /** 需求详细度权重（描述清晰更靠谱） */
    private double teacherDetail = 5.0;

    // ============ 通用参数 ============

    /** 最大考虑距离(公里)，超过此距离得分为0 */
    private double maxDistanceKm = 10.0;

    /** 最高评分基准值 */
    private double maxRating = 5.0;

    /** 价格超出预算的最大容忍比例（超出此比例得分为0） */
    private double priceOverRatioMax = 0.5;

    /** 需求新鲜度：满分天数（发布天数小于此值得满分） */
    private int freshnessFullScoreDays = 1;

    /** 需求新鲜度：最大考虑天数（超过此天数得分较低） */
    private int freshnessMaxDays = 7;

    /** 需求详细度：满分字符数 */
    private int detailFullScoreChars = 100;
}
