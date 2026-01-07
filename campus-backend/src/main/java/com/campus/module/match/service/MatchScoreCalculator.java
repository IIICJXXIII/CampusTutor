package com.campus.module.match.service;

import cn.hutool.json.JSONUtil;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.match.dto.MatchScoreResult;
import com.campus.module.tutor.entity.TutorProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 匹配评分计算器
 * 多维度加权评分算法
 */
@Slf4j
@Component
public class MatchScoreCalculator {

    // 各维度权重配置
    private static final double WEIGHT_SUBJECT = 30.0;    // 科目匹配 30%
    private static final double WEIGHT_GRADE = 20.0;      // 年级匹配 20%
    private static final double WEIGHT_DISTANCE = 25.0;   // 距离评分 25%
    private static final double WEIGHT_PRICE = 15.0;      // 价格匹配 15%
    private static final double WEIGHT_RATING = 10.0;     // 教员评分 10%

    // 距离评分参数
    private static final double MAX_DISTANCE_KM = 10.0;   // 最大考虑距离(公里)

    /**
     * 计算教员与需求的匹配分数
     *
     * @param tutor    教员档案
     * @param demand   需求帖子
     * @param distance 距离(公里)，可为null
     * @return 匹配评分结果
     */
    public MatchScoreResult calculateScore(TutorProfile tutor, DemandPost demand, Double distance) {
        MatchScoreResult result = new MatchScoreResult();
        List<String> matchTags = new ArrayList<>();

        // 1. 科目匹配分数
        double subjectScore = calculateSubjectScore(tutor.getTeachSubjects(), demand.getSubject());
        result.setSubjectScore(subjectScore);
        if (subjectScore >= WEIGHT_SUBJECT * 0.8) {
            matchTags.add("科目匹配");
        }

        // 2. 年级匹配分数
        double gradeScore = calculateGradeScore(tutor.getTeachGrades(), demand.getGrade());
        result.setGradeScore(gradeScore);
        if (gradeScore >= WEIGHT_GRADE * 0.8) {
            matchTags.add("年级匹配");
        }

        // 3. 距离评分
        double distanceScore = calculateDistanceScore(distance);
        result.setDistanceScore(distanceScore);
        if (distance != null && distance <= 3.0) {
            matchTags.add("距离近");
        }

        // 4. 价格匹配分数
        double priceScore = calculatePriceScore(tutor.getExpectPrice(), demand.getExpectPrice());
        result.setPriceScore(priceScore);
        if (priceScore >= WEIGHT_PRICE * 0.8) {
            matchTags.add("价格合适");
        }

        // 5. 教员评分权重
        double ratingScore = calculateRatingScore(tutor.getRating());
        result.setRatingScore(ratingScore);
        if (tutor.getRating() != null && tutor.getRating().doubleValue() >= 4.5) {
            matchTags.add("高评分");
        }

        // 6. 计算综合匹配分数
        double totalScore = subjectScore + gradeScore + distanceScore + priceScore + ratingScore;
        result.setMatchScore(Math.min(100.0, totalScore));
        result.setMatchTags(matchTags);

        return result;
    }

    /**
     * 基于搜索条件计算教员匹配分数（无需求对象时使用）
     *
     * @param tutor        教员档案
     * @param subject      科目
     * @param grade        年级
     * @param distance     距离
     * @param budgetPrice  预算价格
     * @return 匹配评分结果
     */
    public MatchScoreResult calculateScoreByCondition(
            TutorProfile tutor,
            String subject,
            String grade,
            Double distance,
            BigDecimal budgetPrice) {

        MatchScoreResult result = new MatchScoreResult();
        List<String> matchTags = new ArrayList<>();

        // 1. 科目匹配分数
        double subjectScore = calculateSubjectScore(tutor.getTeachSubjects(), subject);
        result.setSubjectScore(subjectScore);
        if (subjectScore >= WEIGHT_SUBJECT * 0.8) {
            matchTags.add("科目匹配");
        }

        // 2. 年级匹配分数
        double gradeScore = calculateGradeScore(tutor.getTeachGrades(), grade);
        result.setGradeScore(gradeScore);
        if (gradeScore >= WEIGHT_GRADE * 0.8) {
            matchTags.add("年级匹配");
        }

        // 3. 距离评分
        double distanceScore = calculateDistanceScore(distance);
        result.setDistanceScore(distanceScore);
        if (distance != null && distance <= 3.0) {
            matchTags.add("距离近");
        }

        // 4. 价格匹配分数
        double priceScore = calculatePriceScore(tutor.getExpectPrice(), budgetPrice);
        result.setPriceScore(priceScore);
        if (priceScore >= WEIGHT_PRICE * 0.8) {
            matchTags.add("价格合适");
        }

        // 5. 教员评分权重
        double ratingScore = calculateRatingScore(tutor.getRating());
        result.setRatingScore(ratingScore);
        if (tutor.getRating() != null && tutor.getRating().doubleValue() >= 4.5) {
            matchTags.add("高评分");
        }

        // 6. 计算综合匹配分数
        double totalScore = subjectScore + gradeScore + distanceScore + priceScore + ratingScore;
        result.setMatchScore(Math.min(100.0, totalScore));
        result.setMatchTags(matchTags);

        return result;
    }

    /**
     * 计算科目匹配分数
     */
    private double calculateSubjectScore(String teachSubjectsJson, String targetSubject) {
        if (!StringUtils.hasText(targetSubject) || !StringUtils.hasText(teachSubjectsJson)) {
            return WEIGHT_SUBJECT * 0.5; // 无科目要求给一半分
        }
        try {
            List<String> subjects = JSONUtil.toList(teachSubjectsJson, String.class);
            for (String subject : subjects) {
                if (subject.contains(targetSubject) || targetSubject.contains(subject)) {
                    return WEIGHT_SUBJECT; // 完全匹配
                }
            }
            return 0; // 不匹配
        } catch (Exception e) {
            log.warn("解析科目JSON失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 计算年级匹配分数
     */
    private double calculateGradeScore(String teachGradesJson, String targetGrade) {
        if (!StringUtils.hasText(targetGrade) || !StringUtils.hasText(teachGradesJson)) {
            return WEIGHT_GRADE * 0.5; // 无年级要求给一半分
        }
        try {
            List<String> grades = JSONUtil.toList(teachGradesJson, String.class);
            for (String grade : grades) {
                if (grade.contains(targetGrade) || targetGrade.contains(grade)) {
                    return WEIGHT_GRADE; // 完全匹配
                }
            }
            return 0; // 不匹配
        } catch (Exception e) {
            log.warn("解析年级JSON失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 计算距离评分
     * 距离越近分数越高，超过最大距离得0分
     */
    private double calculateDistanceScore(Double distance) {
        if (distance == null) {
            return WEIGHT_DISTANCE * 0.5; // 无距离信息给一半分
        }
        if (distance <= 0) {
            return WEIGHT_DISTANCE; // 距离为0，满分
        }
        if (distance >= MAX_DISTANCE_KM) {
            return 0; // 超过最大距离，0分
        }
        // 线性递减：距离每增加1km，减少 WEIGHT_DISTANCE / MAX_DISTANCE_KM 分
        return WEIGHT_DISTANCE * (1 - distance / MAX_DISTANCE_KM);
    }

    /**
     * 计算价格匹配分数
     * 教员期望价格 <= 家长预算时满分，超出预算越多分数越低
     */
    private double calculatePriceScore(BigDecimal tutorPrice, BigDecimal budgetPrice) {
        if (tutorPrice == null || budgetPrice == null) {
            return WEIGHT_PRICE * 0.5; // 无价格信息给一半分
        }
        double tutor = tutorPrice.doubleValue();
        double budget = budgetPrice.doubleValue();

        if (tutor <= budget) {
            return WEIGHT_PRICE; // 在预算内，满分
        }

        // 超出预算的比例
        double overRatio = (tutor - budget) / budget;
        if (overRatio >= 0.5) {
            return 0; // 超出50%以上，0分
        }

        // 线性递减
        return WEIGHT_PRICE * (1 - overRatio * 2);
    }

    /**
     * 计算教员评分权重
     * 5分制，直接映射到权重分数
     */
    private double calculateRatingScore(BigDecimal rating) {
        if (rating == null) {
            return WEIGHT_RATING * 0.6; // 新教员给6折分
        }
        double r = rating.doubleValue();
        return WEIGHT_RATING * (r / 5.0);
    }
}
