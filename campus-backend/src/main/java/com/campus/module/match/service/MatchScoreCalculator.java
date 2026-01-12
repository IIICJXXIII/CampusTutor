package com.campus.module.match.service;

import cn.hutool.json.JSONUtil;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.match.dto.MatchScoreResult;
import com.campus.module.match.dto.MatchViewType;
import com.campus.module.tutor.entity.TutorProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 匹配评分计算器
 * 多维度加权评分算法 - 支持家长视角和教师视角
 */
@Component
public class MatchScoreCalculator {

    private static final Logger log = LoggerFactory.getLogger(MatchScoreCalculator.class);

    // ============ 家长视角权重配置（找老师）============
    // 总计100%，关注教师质量和性价比
    private static final double WEIGHT_SUBJECT = 23.0; // 科目匹配 23%
    private static final double WEIGHT_GRADE = 14.0; // 年级匹配 14%
    private static final double WEIGHT_DISTANCE = 18.0; // 距离评分 18%
    private static final double WEIGHT_PRICE = 10.0; // 价格匹配 10%（教师报价越低越好）
    private static final double WEIGHT_RATING = 10.0; // 教员评分 10%
    private static final double WEIGHT_EXPERIENCE = 10.0; // 教学经验 10%
    private static final double WEIGHT_EDUCATION = 5.0; // 学历背景 5%
    private static final double WEIGHT_SPECIALTY = 5.0; // 教学特长 5%
    private static final double WEIGHT_TEACH_MODE = 5.0; // 授课方式匹配 5%

    // ============ 教师视角权重配置（找需求）============
    // 总计100%，关注收益和匹配度
    private static final double T_WEIGHT_SUBJECT = 25.0; // 科目匹配 25%
    private static final double T_WEIGHT_GRADE = 15.0; // 年级匹配 15%
    private static final double T_WEIGHT_DISTANCE = 15.0; // 距离评分 15%
    private static final double T_WEIGHT_PRICE = 20.0; // 价格匹配 20%（需求预算越高越好）⭐
    private static final double T_WEIGHT_TEACH_MODE = 10.0; // 授课方式匹配 10%
    private static final double T_WEIGHT_FRESHNESS = 10.0; // 需求新鲜度 10%（新发布更好）
    private static final double T_WEIGHT_DETAIL = 5.0; // 需求详细度 5%（描述清晰更靠谱）

    // 距离评分参数
    private static final double MAX_DISTANCE_KM = 10.0; // 最大考虑距离(公里)

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

        // 6. 教学经验评分
        double experienceScore = calculateExperienceScore(tutor);
        result.setExperienceScore(experienceScore);
        if (experienceScore >= WEIGHT_EXPERIENCE * 0.8) {
            matchTags.add("经验丰富");
        }

        // 7. 学历背景评分
        double educationScore = calculateEducationScore(tutor.getEducation());
        result.setEducationScore(educationScore);
        if (educationScore >= WEIGHT_EDUCATION * 0.8) {
            matchTags.add("学历优秀");
        }

        // 8. 教学特长评分
        double specialtyScore = calculateSpecialtyScore(tutor.getTeachStyle(), null);
        result.setSpecialtyScore(specialtyScore);
        if (specialtyScore >= WEIGHT_SPECIALTY * 0.8) {
            matchTags.add("特长匹配");
        }

        // 9. 授课方式匹配评分（新增）
        double teachModeScore = calculateTeachModeScore(tutor.getCanVisit(), tutor.getCanOnline(),
                demand.getTeachMode());
        result.setTeachModeScore(teachModeScore);
        if (teachModeScore >= WEIGHT_TEACH_MODE * 0.8) {
            matchTags.add("授课方式匹配");
        }

        // 10. 计算综合匹配分数
        double totalScore = subjectScore + gradeScore + distanceScore + priceScore + ratingScore +
                experienceScore + educationScore + specialtyScore + teachModeScore;
        result.setMatchScore(Math.min(100.0, totalScore));
        result.setMatchTags(matchTags);

        return result;
    }

    /**
     * 基于搜索条件计算教员匹配分数（无需求对象时使用）
     *
     * @param tutor       教员档案
     * @param subject     科目
     * @param grade       年级
     * @param distance    距离
     * @param budgetPrice 预算价格
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

        // 6. 教学经验评分
        double experienceScore = calculateExperienceScore(tutor);
        result.setExperienceScore(experienceScore);
        if (experienceScore >= WEIGHT_EXPERIENCE * 0.8) {
            matchTags.add("经验丰富");
        }

        // 7. 学历背景评分
        double educationScore = calculateEducationScore(tutor.getEducation());
        result.setEducationScore(educationScore);
        if (educationScore >= WEIGHT_EDUCATION * 0.8) {
            matchTags.add("学历优秀");
        }

        // 8. 教学特长评分
        double specialtyScore = calculateSpecialtyScore(tutor.getTeachStyle(), null);
        result.setSpecialtyScore(specialtyScore);
        if (specialtyScore >= WEIGHT_SPECIALTY * 0.8) {
            matchTags.add("教学特长");
        }

        // 9. 授课方式匹配评分（无具体需求时，给默认分数）
        double teachModeScore = calculateTeachModeScore(tutor.getCanVisit(), tutor.getCanOnline(), null);
        result.setTeachModeScore(teachModeScore);
        if (teachModeScore >= WEIGHT_TEACH_MODE * 0.8) {
            matchTags.add("授课方式灵活");
        }

        // 10. 计算综合匹配分数
        double totalScore = subjectScore + gradeScore + distanceScore + priceScore + ratingScore +
                experienceScore + educationScore + specialtyScore + teachModeScore;
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

    /**
     * 计算教学经验评分
     * 基于订单数量和从教年限
     */
    private double calculateExperienceScore(TutorProfile tutor) {
        int orderCount = tutor.getOrderCount() != null ? tutor.getOrderCount() : 0;

        // 订单数量评分
        if (orderCount >= 50) {
            return WEIGHT_EXPERIENCE; // 丰富经验
        } else if (orderCount >= 20) {
            return WEIGHT_EXPERIENCE * 0.8; // 较多经验
        } else if (orderCount >= 10) {
            return WEIGHT_EXPERIENCE * 0.6; // 一般经验
        } else if (orderCount >= 5) {
            return WEIGHT_EXPERIENCE * 0.4; // 较少经验
        } else {
            return WEIGHT_EXPERIENCE * 0.2; // 新手
        }
    }

    /**
     * 计算学历背景评分
     * 学历越高分数越高
     */
    private double calculateEducationScore(Integer education) {
        if (education == null) {
            return WEIGHT_EDUCATION * 0.5; // 无学历信息
        }

        // 学历等级评分
        switch (education) {
            case 5: // 博士
                return WEIGHT_EDUCATION;
            case 4: // 硕士
                return WEIGHT_EDUCATION * 0.8;
            case 3: // 本科
                return WEIGHT_EDUCATION * 0.6;
            case 2: // 大专
                return WEIGHT_EDUCATION * 0.4;
            default: // 其他
                return WEIGHT_EDUCATION * 0.2;
        }
    }

    /**
     * 计算教学特长评分
     * 基于教学风格与学习目标的匹配度
     */
    private double calculateSpecialtyScore(String teachStyle, String learningGoal) {
        if (!StringUtils.hasText(teachStyle)) {
            return WEIGHT_SPECIALTY * 0.5; // 无教学风格信息
        }

        if (!StringUtils.hasText(learningGoal)) {
            return WEIGHT_SPECIALTY * 0.7; // 无学习目标
        }

        // 关键词匹配
        String style = teachStyle.toLowerCase();
        String goal = learningGoal.toLowerCase();

        int matchCount = 0;

        // 常见学习目标与教学风格的匹配
        if (goal.contains("提高") && style.contains("提升")) {
            matchCount++;
        }
        if (goal.contains("基础") && style.contains("基础")) {
            matchCount++;
        }
        if (goal.contains("考试") && style.contains("应试")) {
            matchCount++;
        }
        if (goal.contains("兴趣") && style.contains("兴趣")) {
            matchCount++;
        }
        if (goal.contains("竞赛") && style.contains("竞赛")) {
            matchCount++;
        }

        if (matchCount >= 2) {
            return WEIGHT_SPECIALTY; // 高度匹配
        } else if (matchCount >= 1) {
            return WEIGHT_SPECIALTY * 0.6; // 部分匹配
        } else {
            return WEIGHT_SPECIALTY * 0.3; // 不匹配
        }
    }

    /**
     * 计算授课方式匹配分数（新增）
     * 验证教师支持的授课方式是否满足需求
     *
     * @param canVisit  教师是否支持上门: 0否 1是
     * @param canOnline 教师是否支持网课: 0否 1是
     * @param teachMode 需求授课方式: 1上门 2网课 3均可
     * @return 授课方式匹配分数
     */
    private double calculateTeachModeScore(Integer canVisit, Integer canOnline, Integer teachMode) {
        // 默认值处理
        int visit = canVisit != null ? canVisit : 0;
        int online = canOnline != null ? canOnline : 0;
        int mode = teachMode != null ? teachMode : 3; // 默认均可

        // 匹配逻辑
        switch (mode) {
            case 1: // 需求要求上门
                if (visit == 1) {
                    return WEIGHT_TEACH_MODE; // 满分
                } else {
                    return 0; // 不支持上门
                }
            case 2: // 需求要求网课
                if (online == 1) {
                    return WEIGHT_TEACH_MODE; // 满分
                } else {
                    return 0; // 不支持网课
                }
            case 3: // 需求均可
            default:
                // 教师支持任意一种即可
                if (visit == 1 || online == 1) {
                    return WEIGHT_TEACH_MODE; // 满分
                } else {
                    return WEIGHT_TEACH_MODE * 0.5; // 信息不完整
                }
        }
    }

    /**
     * 基于需求信息计算教员匹配分数（教师端使用）
     *
     * @param tutor       教员档案
     * @param subject     需求科目
     * @param grade       需求年级
     * @param distance    距离
     * @param budgetPrice 需求预算价格
     * @return 匹配评分结果
     */
    public MatchScoreResult calculateScoreByDemand(
            TutorProfile tutor,
            String subject,
            String grade,
            Double distance,
            BigDecimal budgetPrice) {
        return calculateScoreByCondition(tutor, subject, grade, distance, budgetPrice);
    }

    /**
     * 带行为信号和动态权重的匹配分数计算（升级版）
     *
     * @param tutor            教员档案
     * @param subject          科目
     * @param grade            年级
     * @param distance         距离
     * @param budgetPrice      预算价格
     * @param hotnessScore     教员热度分(0-100)，从BehaviorService获取
     * @param weightSubject    科目权重
     * @param weightGrade      年级权重
     * @param weightDistance   距离权重
     * @param weightPrice      价格权重
     * @param weightRating     评分权重
     * @param weightExperience 经验权重
     * @param weightEducation  学历权重
     * @param weightSpecialty  特长权重
     * @param weightHotness    热度权重
     * @return 匹配评分结果
     */
    public MatchScoreResult calculateScoreWithBehavior(
            TutorProfile tutor,
            String subject,
            String grade,
            Double distance,
            BigDecimal budgetPrice,
            Double hotnessScore,
            double weightSubject,
            double weightGrade,
            double weightDistance,
            double weightPrice,
            double weightRating,
            double weightExperience,
            double weightEducation,
            double weightSpecialty,
            double weightHotness) {

        MatchScoreResult result = new MatchScoreResult();
        List<String> matchTags = new ArrayList<>();

        // 1. 科目匹配分数（使用动态权重）
        double subjectScore = calculateSubjectScoreWithWeight(tutor.getTeachSubjects(), subject, weightSubject);
        result.setSubjectScore(subjectScore);
        if (subjectScore >= weightSubject * 0.8) {
            matchTags.add("科目匹配");
        }

        // 2. 年级匹配分数
        double gradeScore = calculateGradeScoreWithWeight(tutor.getTeachGrades(), grade, weightGrade);
        result.setGradeScore(gradeScore);
        if (gradeScore >= weightGrade * 0.8) {
            matchTags.add("年级匹配");
        }

        // 3. 距离评分
        double distanceScore = calculateDistanceScoreWithWeight(distance, weightDistance);
        result.setDistanceScore(distanceScore);
        if (distance != null && distance <= 3.0) {
            matchTags.add("距离近");
        }

        // 4. 价格匹配分数
        double priceScore = calculatePriceScoreWithWeight(tutor.getExpectPrice(), budgetPrice, weightPrice);
        result.setPriceScore(priceScore);
        if (priceScore >= weightPrice * 0.8) {
            matchTags.add("价格合适");
        }

        // 5. 教员评分权重
        double ratingScore = calculateRatingScoreWithWeight(tutor.getRating(), weightRating);
        result.setRatingScore(ratingScore);
        if (tutor.getRating() != null && tutor.getRating().doubleValue() >= 4.5) {
            matchTags.add("高评分");
        }

        // 6. 教学经验评分
        double experienceScore = calculateExperienceScoreWithWeight(tutor, weightExperience);
        result.setExperienceScore(experienceScore);
        if (experienceScore >= weightExperience * 0.8) {
            matchTags.add("经验丰富");
        }

        // 7. 学历背景评分
        double educationScore = calculateEducationScoreWithWeight(tutor.getEducation(), weightEducation);
        result.setEducationScore(educationScore);
        if (educationScore >= weightEducation * 0.8) {
            matchTags.add("学历优秀");
        }

        // 8. 教学特长评分
        double specialtyScore = calculateSpecialtyScoreWithWeight(tutor.getTeachStyle(), null, weightSpecialty);
        result.setSpecialtyScore(specialtyScore);
        if (specialtyScore >= weightSpecialty * 0.8) {
            matchTags.add("教学特长");
        }

        // 9. 热度评分（新增维度）
        double hotnessScoreWeighted = 0;
        if (hotnessScore != null && hotnessScore > 0) {
            // 热度分0-100，转换为权重分
            hotnessScoreWeighted = (hotnessScore / 100.0) * weightHotness;
            if (hotnessScore >= 60) {
                matchTags.add("热门教员");
            }
        }
        result.setHotnessScore(hotnessScoreWeighted);

        // 10. 计算综合匹配分数
        double totalScore = subjectScore + gradeScore + distanceScore + priceScore +
                ratingScore + experienceScore + educationScore +
                specialtyScore + hotnessScoreWeighted;
        result.setMatchScore(Math.min(100.0, totalScore));
        result.setMatchTags(matchTags);

        return result;
    }

    // ============ 带动态权重的各维度计算方法 ============

    private double calculateSubjectScoreWithWeight(String teachSubjectsJson, String targetSubject, double weight) {
        if (!StringUtils.hasText(targetSubject) || !StringUtils.hasText(teachSubjectsJson)) {
            return weight * 0.5;
        }
        try {
            List<String> subjects = JSONUtil.toList(teachSubjectsJson, String.class);
            for (String subject : subjects) {
                if (subject.contains(targetSubject) || targetSubject.contains(subject)) {
                    return weight;
                }
            }
            return 0;
        } catch (Exception e) {
            log.warn("解析科目JSON失败: {}", e.getMessage());
            return 0;
        }
    }

    private double calculateGradeScoreWithWeight(String teachGradesJson, String targetGrade, double weight) {
        if (!StringUtils.hasText(targetGrade) || !StringUtils.hasText(teachGradesJson)) {
            return weight * 0.5;
        }
        try {
            List<String> grades = JSONUtil.toList(teachGradesJson, String.class);
            for (String grade : grades) {
                if (grade.contains(targetGrade) || targetGrade.contains(grade)) {
                    return weight;
                }
            }
            return 0;
        } catch (Exception e) {
            log.warn("解析年级JSON失败: {}", e.getMessage());
            return 0;
        }
    }

    private double calculateDistanceScoreWithWeight(Double distance, double weight) {
        if (distance == null) {
            return weight * 0.5;
        }
        if (distance <= 0) {
            return weight;
        }
        if (distance >= MAX_DISTANCE_KM) {
            return 0;
        }
        return weight * (1 - distance / MAX_DISTANCE_KM);
    }

    private double calculatePriceScoreWithWeight(BigDecimal tutorPrice, BigDecimal budgetPrice, double weight) {
        if (tutorPrice == null || budgetPrice == null) {
            return weight * 0.5;
        }
        double tutor = tutorPrice.doubleValue();
        double budget = budgetPrice.doubleValue();
        if (tutor <= budget) {
            return weight;
        }
        double overRatio = (tutor - budget) / budget;
        if (overRatio >= 0.5) {
            return 0;
        }
        return weight * (1 - overRatio * 2);
    }

    private double calculateRatingScoreWithWeight(BigDecimal rating, double weight) {
        if (rating == null) {
            return weight * 0.6;
        }
        double r = rating.doubleValue();
        return weight * (r / 5.0);
    }

    private double calculateExperienceScoreWithWeight(TutorProfile tutor, double weight) {
        int orderCount = tutor.getOrderCount() != null ? tutor.getOrderCount() : 0;
        if (orderCount >= 50) {
            return weight;
        } else if (orderCount >= 20) {
            return weight * 0.8;
        } else if (orderCount >= 10) {
            return weight * 0.6;
        } else if (orderCount >= 5) {
            return weight * 0.4;
        } else {
            return weight * 0.2;
        }
    }

    private double calculateEducationScoreWithWeight(Integer education, double weight) {
        if (education == null) {
            return weight * 0.5;
        }
        switch (education) {
            case 5:
                return weight;
            case 4:
                return weight * 0.8;
            case 3:
                return weight * 0.6;
            case 2:
                return weight * 0.4;
            default:
                return weight * 0.2;
        }
    }

    private double calculateSpecialtyScoreWithWeight(String teachStyle, String learningGoal, double weight) {
        if (!StringUtils.hasText(teachStyle)) {
            return weight * 0.5;
        }
        if (!StringUtils.hasText(learningGoal)) {
            return weight * 0.7;
        }
        String style = teachStyle.toLowerCase();
        String goal = learningGoal.toLowerCase();
        int matchCount = 0;
        if (goal.contains("提高") && style.contains("提升"))
            matchCount++;
        if (goal.contains("基础") && style.contains("基础"))
            matchCount++;
        if (goal.contains("考试") && style.contains("应试"))
            matchCount++;
        if (goal.contains("兴趣") && style.contains("兴趣"))
            matchCount++;
        if (goal.contains("竞赛") && style.contains("竞赛"))
            matchCount++;

        if (matchCount >= 2) {
            return weight;
        } else if (matchCount >= 1) {
            return weight * 0.6;
        } else {
            return weight * 0.3;
        }
    }

    // ============ 教师视角专用方法 ============

    /**
     * 教师视角：计算需求对教师的吸引力分数
     * 与家长视角不同：价格越高越好，关注需求热度和详细度
     *
     * @param tutor    教员档案
     * @param demand   需求帖子
     * @param distance 距离(公里)
     * @return 匹配评分结果
     */
    public MatchScoreResult calculateScoreForTeacher(TutorProfile tutor, DemandPost demand, Double distance) {
        MatchScoreResult result = new MatchScoreResult();
        List<String> matchTags = new ArrayList<>();

        // 1. 科目匹配分数（使用教师视角权重）
        double subjectScore = calculateSubjectScoreWithWeight(tutor.getTeachSubjects(), demand.getSubject(),
                T_WEIGHT_SUBJECT);
        result.setSubjectScore(subjectScore);
        if (subjectScore >= T_WEIGHT_SUBJECT * 0.8) {
            matchTags.add("科目匹配");
        }

        // 2. 年级匹配分数
        double gradeScore = calculateGradeScoreWithWeight(tutor.getTeachGrades(), demand.getGrade(), T_WEIGHT_GRADE);
        result.setGradeScore(gradeScore);
        if (gradeScore >= T_WEIGHT_GRADE * 0.8) {
            matchTags.add("年级匹配");
        }

        // 3. 距离评分
        double distanceScore = calculateDistanceScoreWithWeight(distance, T_WEIGHT_DISTANCE);
        result.setDistanceScore(distanceScore);
        if (distance != null && distance <= 3.0) {
            matchTags.add("距离近");
        }

        // 4. 价格评分（教师视角：预算越高越好！）
        double priceScore = calculatePriceScoreForTeacher(tutor.getExpectPrice(), demand.getExpectPrice());
        result.setPriceScore(priceScore);
        if (priceScore >= T_WEIGHT_PRICE * 0.8) {
            matchTags.add("高薪需求💰");
        }

        // 5. 授课方式匹配
        double teachModeScore = calculateTeachModeScoreWithWeight(
                tutor.getCanVisit(), tutor.getCanOnline(), demand.getTeachMode(), T_WEIGHT_TEACH_MODE);
        result.setTeachModeScore(teachModeScore);
        if (teachModeScore >= T_WEIGHT_TEACH_MODE * 0.8) {
            matchTags.add("授课方式匹配");
        }

        // 6. 需求新鲜度（新发布的更好）
        double freshnessScore = calculateFreshnessScore(demand.getCreateTime());
        if (freshnessScore >= T_WEIGHT_FRESHNESS * 0.8) {
            matchTags.add("新发布🔥");
        }

        // 7. 需求详细度（描述清晰更靠谱）
        double detailScore = calculateDetailScore(demand.getDetail());
        if (detailScore >= T_WEIGHT_DETAIL * 0.8) {
            matchTags.add("需求清晰");
        }

        // 8. 计算综合匹配分数
        double totalScore = subjectScore + gradeScore + distanceScore + priceScore +
                teachModeScore + freshnessScore + detailScore;
        result.setMatchScore(Math.min(100.0, totalScore));
        result.setMatchTags(matchTags);

        return result;
    }

    /**
     * 教师视角：价格评分（预算越高越好）
     */
    private double calculatePriceScoreForTeacher(BigDecimal tutorPrice, BigDecimal demandBudget) {
        if (tutorPrice == null || demandBudget == null) {
            return T_WEIGHT_PRICE * 0.5; // 无价格信息
        }
        double tutor = tutorPrice.doubleValue();
        double budget = demandBudget.doubleValue();

        if (tutor <= 0) {
            return T_WEIGHT_PRICE * 0.5;
        }

        // 预算/期望 比值越高越好
        double ratio = budget / tutor;
        if (ratio >= 1.5) {
            return T_WEIGHT_PRICE; // 预算高于期望50%以上，满分
        } else if (ratio >= 1.0) {
            return T_WEIGHT_PRICE * (0.8 + 0.2 * (ratio - 1.0) / 0.5); // 线性增长
        } else if (ratio >= 0.8) {
            return T_WEIGHT_PRICE * 0.6; // 略低于期望
        } else {
            return T_WEIGHT_PRICE * 0.3; // 远低于期望
        }
    }

    /**
     * 需求新鲜度评分（最近发布的得分更高）
     */
    private double calculateFreshnessScore(java.time.LocalDateTime createTime) {
        if (createTime == null) {
            return T_WEIGHT_FRESHNESS * 0.5;
        }
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(createTime, java.time.LocalDateTime.now());
        if (daysDiff <= 1) {
            return T_WEIGHT_FRESHNESS; // 今天或昨天发布，满分
        } else if (daysDiff <= 3) {
            return T_WEIGHT_FRESHNESS * 0.8; // 3天内
        } else if (daysDiff <= 7) {
            return T_WEIGHT_FRESHNESS * 0.6; // 一周内
        } else if (daysDiff <= 14) {
            return T_WEIGHT_FRESHNESS * 0.4; // 两周内
        } else {
            return T_WEIGHT_FRESHNESS * 0.2; // 超过两周
        }
    }

    /**
     * 需求详细度评分（描述越详细越靠谱）
     */
    private double calculateDetailScore(String detail) {
        if (!StringUtils.hasText(detail)) {
            return T_WEIGHT_DETAIL * 0.2; // 无描述
        }
        int length = detail.length();
        if (length >= 100) {
            return T_WEIGHT_DETAIL; // 详细描述，满分
        } else if (length >= 50) {
            return T_WEIGHT_DETAIL * 0.8;
        } else if (length >= 20) {
            return T_WEIGHT_DETAIL * 0.6;
        } else {
            return T_WEIGHT_DETAIL * 0.4;
        }
    }

    /**
     * 带权重参数的授课方式匹配计算
     */
    private double calculateTeachModeScoreWithWeight(Integer canVisit, Integer canOnline, Integer teachMode,
            double weight) {
        int visit = canVisit != null ? canVisit : 0;
        int online = canOnline != null ? canOnline : 0;
        int mode = teachMode != null ? teachMode : 3;

        switch (mode) {
            case 1: // 需求要求上门
                return visit == 1 ? weight : 0;
            case 2: // 需求要求网课
                return online == 1 ? weight : 0;
            case 3: // 需求均可
            default:
                return (visit == 1 || online == 1) ? weight : weight * 0.5;
        }
    }
}
