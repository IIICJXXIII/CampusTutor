package com.campus.module.match.service;

import cn.hutool.json.JSONUtil;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.match.dto.MatchScoreResult;
import com.campus.module.match.dto.MatchViewType;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.match.config.MatchWeightConfig;
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

    private final MatchWeightConfig config;

    public MatchScoreCalculator(MatchWeightConfig config) {
        this.config = config;
    }

    // ============ 家长视角权重获取方法（找老师）============
    private double getWeightSubject() {
        return config.getParentSubject();
    }

    private double getWeightGrade() {
        return config.getParentGrade();
    }

    private double getWeightDistance() {
        return config.getParentDistance();
    }

    private double getWeightPrice() {
        return config.getParentPrice();
    }

    private double getWeightRating() {
        return config.getParentRating();
    }

    private double getWeightExperience() {
        return config.getParentExperience();
    }

    private double getWeightEducation() {
        return config.getParentEducation();
    }

    private double getWeightSpecialty() {
        return config.getParentSpecialty();
    }

    private double getWeightSkillLevel() {
        return config.getParentSkillLevel();
    }

    private double getWeightTeachMode() {
        return config.getParentTeachMode();
    }

    // ============ 素质教育：科目类别判断 ============

    private static final String[] SPORTS_KEYWORDS = {"体育", "跳绳", "篮球", "足球", "羽毛球", "网球", "游泳", "跑步"};
    private static final String[] ART_KEYWORDS = {"艺术", "钢琴", "乐器", "美术", "书法", "声乐", "舞蹈", "绘画"};
    private static final String[] STEAM_KEYWORDS = {"科创", "STEAM", "编程", "Scratch", "Python", "机器人", "3D打印", "航模", "科学"};

    /**
     * 判断科目所属大类：sports / art / steam / other
     */
    private String detectSubjectCategory(String subject) {
        if (subject == null) return "other";
        String s = subject.toLowerCase();
        for (String kw : SPORTS_KEYWORDS) { if (s.contains(kw.toLowerCase())) return "sports"; }
        for (String kw : ART_KEYWORDS) { if (s.contains(kw.toLowerCase())) return "art"; }
        for (String kw : STEAM_KEYWORDS) { if (s.contains(kw.toLowerCase())) return "steam"; }
        return "other";
    }

    // ============ 教师视角权重获取方法（找需求）============
    private double getTWeightSubject() {
        return config.getTeacherSubject();
    }

    private double getTWeightGrade() {
        return config.getTeacherGrade();
    }

    private double getTWeightDistance() {
        return config.getTeacherDistance();
    }

    private double getTWeightPrice() {
        return config.getTeacherPrice();
    }

    private double getTWeightTeachMode() {
        return config.getTeacherTeachMode();
    }

    private double getTWeightFreshness() {
        return config.getTeacherFreshness();
    }

    private double getTWeightDetail() {
        return config.getTeacherDetail();
    }

    // ============ 通用参数获取方法 ============
    private double getMaxDistanceKm() {
        return config.getMaxDistanceKm();
    }

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

        // 素质教育：根据科目类别动态调整权重
        String category = detectSubjectCategory(demand.getSubject());
        double distanceMultiplier = "sports".equals(category) ? 1.5 : 1.0;
        double skillMultiplier = ("sports".equals(category) ? 1.2 : ("art".equals(category) || "steam".equals(category)) ? 1.5 : 1.0);
        double ratingMultiplier = ("art".equals(category) || "steam".equals(category)) ? 1.2 : 1.0;

        // 1. 科目匹配分数
        double subjectScore = calculateSubjectScore(tutor.getTeachSubjects(), demand.getSubject());
        result.setSubjectScore(subjectScore);
        if (subjectScore >= getWeightSubject() * 0.8) {
            matchTags.add("科目匹配");
        }

        // 2. 年龄段匹配分数（原年级，权重已降低）
        double gradeScore = calculateGradeScore(tutor.getTeachGrades(), demand.getGrade());
        result.setGradeScore(gradeScore);
        if (gradeScore >= getWeightGrade() * 0.8) {
            matchTags.add("年龄段匹配");
        }

        // 3. 距离评分（体育类加权，多级距离标签）
        double distanceScore = calculateDistanceScore(distance) * distanceMultiplier;
        distanceScore = Math.min(distanceScore, getWeightDistance() * 1.5); // 上限
        result.setDistanceScore(distanceScore);
        if (distance != null) {
            if (distance <= 1.0) {
                matchTags.add("超近");
            } else if (distance <= 3.0) {
                matchTags.add("距离近");
            } else if (distance <= 5.0) {
                matchTags.add("同城");
            }
        }

        // 4. 价格匹配分数
        double priceScore = calculatePriceScore(tutor.getExpectPrice(), demand.getExpectPrice());
        result.setPriceScore(priceScore);
        if (priceScore >= getWeightPrice() * 0.8) {
            matchTags.add("价格合适");
        }
        if (demand.getExpectPrice() == null && tutor.getExpectPrice() != null
                && tutor.getExpectPrice().doubleValue() <= 100) {
            matchTags.add("性价比高");
        }

        // 5. 教员评分权重（艺术/科创类加权，多级标签）
        double ratingScore = calculateRatingScore(tutor.getRating()) * ratingMultiplier;
        ratingScore = Math.min(ratingScore, getWeightRating() * 1.5);
        result.setRatingScore(ratingScore);
        if (tutor.getRating() != null) {
            double r = tutor.getRating().doubleValue();
            if (r >= 4.8) {
                matchTags.add("口碑之星");
            } else if (r >= 4.5) {
                matchTags.add("高评分");
            } else if (r >= 4.0) {
                matchTags.add("好评");
            }
        }

        // 6. 教学经验评分（多级标签）
        double experienceScore = calculateExperienceScore(tutor);
        result.setExperienceScore(experienceScore);
        int orderCount = tutor.getOrderCount() != null ? tutor.getOrderCount() : 0;
        if (orderCount >= 50) {
            matchTags.add("资深名师");
        } else if (orderCount >= 20) {
            matchTags.add("经验丰富");
        } else if (orderCount >= 10) {
            matchTags.add("教学有方");
        }

        // 7. 学历背景评分（具体学历标签）
        double educationScore = calculateEducationScore(tutor.getEducation());
        result.setEducationScore(educationScore);
        if (tutor.getEducation() != null) {
            int edu = tutor.getEducation();
            if (edu >= 5) {
                matchTags.add("博士");
            } else if (edu >= 4) {
                matchTags.add("硕士");
            } else if (edu >= 3 && educationScore >= getWeightEducation() * 0.8) {
                matchTags.add("学历优秀");
            }
        }

        // 8. 教学特长评分（艺术/科创类加权）
        double specialtyScore = calculateSpecialtyScore(tutor.getTeachStyle(), null) * skillMultiplier;
        specialtyScore = Math.min(specialtyScore, getWeightSpecialty() * 1.5);
        result.setSpecialtyScore(specialtyScore);
        if (specialtyScore >= getWeightSpecialty() * 0.8) {
            matchTags.add("特长匹配");
        }

        // 9. 授课方式匹配评分
        double teachModeScore = calculateTeachModeScore(tutor.getCanVisit(), tutor.getCanOnline(),
                demand.getTeachMode());
        result.setTeachModeScore(teachModeScore);
        if (teachModeScore >= getWeightTeachMode() * 0.8) {
            matchTags.add("授课方式匹配");
        }

        // 10. 基础水平匹配评分（素质教育新增维度）
        double skillLevelScore = calculateSkillLevelScore(tutor.getTeachStyle(), demand.getSkillLevel());
        if (skillLevelScore >= getWeightSkillLevel() * 0.8) {
            matchTags.add("水平匹配");
        }

        // 11. 计算综合匹配分数
        double totalScore = subjectScore + gradeScore + distanceScore + priceScore + ratingScore +
                experienceScore + educationScore + specialtyScore + teachModeScore + skillLevelScore;
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
        if (subjectScore >= getWeightSubject() * 0.8) {
            matchTags.add("科目匹配");
        }

        // 2. 年级匹配分数
        double gradeScore = calculateGradeScore(tutor.getTeachGrades(), grade);
        result.setGradeScore(gradeScore);
        if (gradeScore >= getWeightGrade() * 0.8) {
            matchTags.add("年级匹配");
        }

        // 3. 距离评分（多级距离标签）
        double distanceScore = calculateDistanceScore(distance);
        result.setDistanceScore(distanceScore);
        if (distance != null) {
            if (distance <= 1.0) {
                matchTags.add("超近");
            } else if (distance <= 3.0) {
                matchTags.add("距离近");
            } else if (distance <= 5.0) {
                matchTags.add("同城");
            }
        }

        // 4. 价格匹配分数
        double priceScore = calculatePriceScore(tutor.getExpectPrice(), budgetPrice);
        result.setPriceScore(priceScore);
        if (priceScore >= getWeightPrice() * 0.8) {
            matchTags.add("价格合适");
        }
        if (budgetPrice == null && tutor.getExpectPrice() != null
                && tutor.getExpectPrice().doubleValue() <= 100) {
            matchTags.add("性价比高");
        }

        // 5. 教员评分权重（多级标签，不受筛选条件影响）
        double ratingScore = calculateRatingScore(tutor.getRating());
        result.setRatingScore(ratingScore);
        if (tutor.getRating() != null) {
            double r = tutor.getRating().doubleValue();
            if (r >= 4.8) {
                matchTags.add("口碑之星");
            } else if (r >= 4.5) {
                matchTags.add("高评分");
            } else if (r >= 4.0) {
                matchTags.add("好评");
            }
        }

        // 6. 教学经验评分（多级标签）
        double experienceScore = calculateExperienceScore(tutor);
        result.setExperienceScore(experienceScore);
        int orderCount = tutor.getOrderCount() != null ? tutor.getOrderCount() : 0;
        if (orderCount >= 50) {
            matchTags.add("资深名师");
        } else if (orderCount >= 20) {
            matchTags.add("经验丰富");
        } else if (orderCount >= 10) {
            matchTags.add("教学有方");
        }

        // 7. 学历背景评分（具体学历标签）
        double educationScore = calculateEducationScore(tutor.getEducation());
        result.setEducationScore(educationScore);
        if (tutor.getEducation() != null) {
            int edu = tutor.getEducation();
            if (edu >= 5) {
                matchTags.add("博士");
            } else if (edu >= 4) {
                matchTags.add("硕士");
            } else if (edu >= 3 && educationScore >= getWeightEducation() * 0.8) {
                matchTags.add("学历优秀");
            }
        }

        // 8. 教学特长评分
        double specialtyScore = calculateSpecialtyScore(tutor.getTeachStyle(), null);
        result.setSpecialtyScore(specialtyScore);
        if (specialtyScore >= getWeightSpecialty() * 0.8) {
            matchTags.add("教学特长");
        }

        // 9. 授课方式匹配评分（无具体需求时，给默认分数）
        double teachModeScore = calculateTeachModeScore(tutor.getCanVisit(), tutor.getCanOnline(), null);
        result.setTeachModeScore(teachModeScore);
        if (teachModeScore >= getWeightTeachMode() * 0.8) {
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
     * 计算基础水平匹配分数（素质教育新增）
     * 基于学生的基础水平和教员的教学风格进行匹配
     *
     * @param teachStyle 教员教学风格
     * @param skillLevel 需求基础水平：零基础、有基础、考级/比赛冲刺
     */
    private double calculateSkillLevelScore(String teachStyle, String skillLevel) {
        if (!StringUtils.hasText(skillLevel)) {
            return getWeightSkillLevel() * 0.5; // 未填写给一半分
        }
        if (!StringUtils.hasText(teachStyle)) {
            return getWeightSkillLevel() * 0.3; // 教员未填教学风格
        }

        String style = teachStyle.toLowerCase();
        String level = skillLevel.toLowerCase();

        // 零基础 → 匹配"启蒙、入门、基础、耐心、兴趣"
        if (level.contains("零基础")) {
            if (style.contains("启蒙") || style.contains("入门") || style.contains("基础")
                    || style.contains("耐心") || style.contains("兴趣")) {
                return getWeightSkillLevel();
            }
            return getWeightSkillLevel() * 0.5;
        }
        // 有基础 → 匹配"提升、进阶、系统、技巧"
        if (level.contains("有基础")) {
            if (style.contains("提升") || style.contains("进阶") || style.contains("系统")
                    || style.contains("技巧")) {
                return getWeightSkillLevel();
            }
            return getWeightSkillLevel() * 0.5;
        }
        // 考级/比赛冲刺 → 匹配"考级、竞赛、比赛、冲刺、专业"
        if (level.contains("考级") || level.contains("冲刺") || level.contains("比赛")) {
            if (style.contains("考级") || style.contains("竞赛") || style.contains("比赛")
                    || style.contains("冲刺") || style.contains("专业")) {
                return getWeightSkillLevel();
            }
            return getWeightSkillLevel() * 0.3;
        }

        return getWeightSkillLevel() * 0.5; // 默认
    }

    /**
     * 计算科目匹配分数
     */
    private double calculateSubjectScore(String teachSubjectsJson, String targetSubject) {
        if (!StringUtils.hasText(targetSubject) || !StringUtils.hasText(teachSubjectsJson)) {
            return getWeightSubject() * 0.5; // 无科目要求给一半分
        }
        try {
            List<String> subjects = JSONUtil.toList(teachSubjectsJson, String.class);
            for (String subject : subjects) {
                if (subject.contains(targetSubject) || targetSubject.contains(subject)) {
                    return getWeightSubject(); // 完全匹配
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
            return getWeightGrade() * 0.5; // 无年级要求给一半分
        }
        try {
            List<String> grades = JSONUtil.toList(teachGradesJson, String.class);
            for (String grade : grades) {
                if (grade.contains(targetGrade) || targetGrade.contains(grade)) {
                    return getWeightGrade(); // 完全匹配
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
            return getWeightDistance() * 0.5; // 无距离信息给一半分
        }
        if (distance <= 0) {
            return getWeightDistance(); // 距离为0，满分
        }
        if (distance >= getMaxDistanceKm()) {
            return 0; // 超过最大距离，0分
        }
        // 线性递减：距离每增加1km，减少分数
        return getWeightDistance() * (1 - distance / getMaxDistanceKm());
    }

    /**
     * 计算价格匹配分数
     * 教员期望价格 <= 家长预算时满分，超出预算越多分数越低
     */
    private double calculatePriceScore(BigDecimal tutorPrice, BigDecimal budgetPrice) {
        if (tutorPrice == null || budgetPrice == null) {
            return getWeightPrice() * 0.5; // 无价格信息给一半分
        }
        double tutor = tutorPrice.doubleValue();
        double budget = budgetPrice.doubleValue();

        if (tutor <= budget) {
            return getWeightPrice(); // 在预算内，满分
        }

        // 超出预算的比例
        double overRatio = (tutor - budget) / budget;
        if (overRatio >= config.getPriceOverRatioMax()) {
            return 0; // 超出阈值，0分
        }

        // 线性递减
        return getWeightPrice() * (1 - overRatio / config.getPriceOverRatioMax());
    }

    /**
     * 计算教员评分权重
     * 5分制，直接映射到权重分数
     */
    private double calculateRatingScore(BigDecimal rating) {
        if (rating == null) {
            return getWeightRating() * 0.6; // 新教员给6折分
        }
        double r = rating.doubleValue();
        return getWeightRating() * (r / config.getMaxRating());
    }

    /**
     * 计算教学经验评分
     * 基于订单数量
     */
    private double calculateExperienceScore(TutorProfile tutor) {
        int orderCount = tutor.getOrderCount() != null ? tutor.getOrderCount() : 0;

        // 订单数量评分
        if (orderCount >= 50) {
            return getWeightExperience(); // 丰富经验
        } else if (orderCount >= 20) {
            return getWeightExperience() * 0.8; // 较多经验
        } else if (orderCount >= 10) {
            return getWeightExperience() * 0.6; // 一般经验
        } else if (orderCount >= 5) {
            return getWeightExperience() * 0.4; // 较少经验
        } else {
            return getWeightExperience() * 0.2; // 新手
        }
    }

    /**
     * 计算学历背景评分
     * 学历越高分数越高
     */
    private double calculateEducationScore(Integer education) {
        if (education == null) {
            return getWeightEducation() * 0.5; // 无学历信息
        }

        // 学历等级评分
        switch (education) {
            case 5: // 博士
                return getWeightEducation();
            case 4: // 硕士
                return getWeightEducation() * 0.8;
            case 3: // 本科
                return getWeightEducation() * 0.6;
            case 2: // 大专
                return getWeightEducation() * 0.4;
            default: // 其他
                return getWeightEducation() * 0.2;
        }
    }

    /**
     * 计算教学特长评分
     * 基于教学风格与学习目标的匹配度
     */
    private double calculateSpecialtyScore(String teachStyle, String learningGoal) {
        if (!StringUtils.hasText(teachStyle)) {
            return getWeightSpecialty() * 0.5; // 无教学风格信息
        }

        if (!StringUtils.hasText(learningGoal)) {
            return getWeightSpecialty() * 0.7; // 无学习目标
        }

        // 关键词匹配
        String style = teachStyle.toLowerCase();
        String goal = learningGoal.toLowerCase();

        int matchCount = 0;

        // 常见学习目标与教学风格的匹配
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
            return getWeightSpecialty(); // 高度匹配
        } else if (matchCount >= 1) {
            return getWeightSpecialty() * 0.6; // 部分匹配
        } else {
            return getWeightSpecialty() * 0.3; // 不匹配
        }
    }

    /**
     * 计算授课方式匹配分数
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
                    return getWeightTeachMode(); // 满分
                } else {
                    return 0; // 不支持上门
                }
            case 2: // 需求要求网课
                if (online == 1) {
                    return getWeightTeachMode(); // 满分
                } else {
                    return 0; // 不支持网课
                }
            case 3: // 需求均可
            default:
                // 教师支持任意一种即可
                if (visit == 1 || online == 1) {
                    return getWeightTeachMode(); // 满分
                } else {
                    return getWeightTeachMode() * 0.5; // 信息不完整
                }
        }
    }

    /**
     * 基于需求信息计算教员匹配分数（教师端使用）
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

        // 3. 距离评分（多级距离标签）
        double distanceScore = calculateDistanceScoreWithWeight(distance, weightDistance);
        result.setDistanceScore(distanceScore);
        if (distance != null) {
            if (distance <= 1.0) {
                matchTags.add("超近");
            } else if (distance <= 3.0) {
                matchTags.add("距离近");
            } else if (distance <= 5.0) {
                matchTags.add("同城");
            }
        }

        // 4. 价格匹配分数
        double priceScore = calculatePriceScoreWithWeight(tutor.getExpectPrice(), budgetPrice, weightPrice);
        result.setPriceScore(priceScore);
        if (priceScore >= weightPrice * 0.8) {
            matchTags.add("价格合适");
        }
        // 无预算筛选时，价格较低的教员打上性价比标签
        if (budgetPrice == null && tutor.getExpectPrice() != null
                && tutor.getExpectPrice().doubleValue() <= 100) {
            matchTags.add("性价比高");
        }

        // 5. 教员评分权重（多级标签，不受筛选条件影响）
        double ratingScore = calculateRatingScoreWithWeight(tutor.getRating(), weightRating);
        result.setRatingScore(ratingScore);
        if (tutor.getRating() != null) {
            double r = tutor.getRating().doubleValue();
            if (r >= 4.8) {
                matchTags.add("口碑之星");
            } else if (r >= 4.5) {
                matchTags.add("高评分");
            } else if (r >= 4.0) {
                matchTags.add("好评");
            }
        }

        // 6. 教学经验评分（多级标签）
        double experienceScore = calculateExperienceScoreWithWeight(tutor, weightExperience);
        result.setExperienceScore(experienceScore);
        int orderCount = tutor.getOrderCount() != null ? tutor.getOrderCount() : 0;
        if (orderCount >= 50) {
            matchTags.add("资深名师");
        } else if (orderCount >= 20) {
            matchTags.add("经验丰富");
        } else if (orderCount >= 10) {
            matchTags.add("教学有方");
        }

        // 7. 学历背景评分（具体学历标签）
        double educationScore = calculateEducationScoreWithWeight(tutor.getEducation(), weightEducation);
        result.setEducationScore(educationScore);
        if (tutor.getEducation() != null) {
            int edu = tutor.getEducation();
            if (edu >= 5) {
                matchTags.add("博士");
            } else if (edu >= 4) {
                matchTags.add("硕士");
            } else if (edu >= 3 && educationScore >= weightEducation * 0.8) {
                matchTags.add("学历优秀");
            }
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
        if (distance >= getMaxDistanceKm()) {
            return 0;
        }
        return weight * (1 - distance / getMaxDistanceKm());
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
        if (overRatio >= config.getPriceOverRatioMax()) {
            return 0;
        }
        return weight * (1 - overRatio / config.getPriceOverRatioMax());
    }

    private double calculateRatingScoreWithWeight(BigDecimal rating, double weight) {
        if (rating == null) {
            return weight * 0.6;
        }
        double r = rating.doubleValue();
        return weight * (r / config.getMaxRating());
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
                getTWeightSubject());
        result.setSubjectScore(subjectScore);
        if (subjectScore >= getTWeightSubject() * 0.8) {
            matchTags.add("科目匹配");
        }

        // 2. 年级匹配分数
        double gradeScore = calculateGradeScoreWithWeight(tutor.getTeachGrades(), demand.getGrade(), getTWeightGrade());
        result.setGradeScore(gradeScore);
        if (gradeScore >= getTWeightGrade() * 0.8) {
            matchTags.add("年级匹配");
        }

        // 3. 距离评分
        double distanceScore = calculateDistanceScoreWithWeight(distance, getTWeightDistance());
        result.setDistanceScore(distanceScore);
        if (distance != null && distance <= 3.0) {
            matchTags.add("距离近");
        }

        // 4. 价格评分（教师视角：预算越高越好！）
        double priceScore = calculatePriceScoreForTeacher(tutor.getExpectPrice(), demand.getExpectPrice());
        result.setPriceScore(priceScore);
        if (priceScore >= getTWeightPrice() * 0.8) {
            matchTags.add("高薪需求💰");
        }

        // 5. 授课方式匹配
        double teachModeScore = calculateTeachModeScoreWithWeight(
                tutor.getCanVisit(), tutor.getCanOnline(), demand.getTeachMode(), getTWeightTeachMode());
        result.setTeachModeScore(teachModeScore);
        if (teachModeScore >= getTWeightTeachMode() * 0.8) {
            matchTags.add("授课方式匹配");
        }

        // 6. 需求新鲜度（新发布的更好）
        double freshnessScore = calculateFreshnessScore(demand.getCreateTime());
        if (freshnessScore >= getTWeightFreshness() * 0.8) {
            matchTags.add("新发布🔥");
        }

        // 7. 需求详细度（描述清晰更靠谱）
        double detailScore = calculateDetailScore(demand.getDetail());
        if (detailScore >= getTWeightDetail() * 0.8) {
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
            return getTWeightPrice() * 0.5; // 无价格信息
        }
        double tutor = tutorPrice.doubleValue();
        double budget = demandBudget.doubleValue();

        if (tutor <= 0) {
            return getTWeightPrice() * 0.5;
        }

        // 预算/期望 比值越高越好
        double ratio = budget / tutor;
        if (ratio >= 1.5) {
            return getTWeightPrice(); // 预算高于期望50%以上，满分
        } else if (ratio >= 1.0) {
            return getTWeightPrice() * (0.8 + 0.2 * (ratio - 1.0) / 0.5); // 线性增长
        } else if (ratio >= 0.8) {
            return getTWeightPrice() * 0.6; // 略低于期望
        } else {
            return getTWeightPrice() * 0.3; // 远低于期望
        }
    }

    /**
     * 需求新鲜度评分（最近发布的得分更高）
     */
    private double calculateFreshnessScore(java.time.LocalDateTime createTime) {
        if (createTime == null) {
            return getTWeightFreshness() * 0.5;
        }
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(createTime, java.time.LocalDateTime.now());
        if (daysDiff <= config.getFreshnessFullScoreDays()) {
            return getTWeightFreshness(); // 满分天数内，满分
        } else if (daysDiff <= 3) {
            return getTWeightFreshness() * 0.8; // 3天内
        } else if (daysDiff <= config.getFreshnessMaxDays()) {
            return getTWeightFreshness() * 0.6; // 最大天数内
        } else if (daysDiff <= 14) {
            return getTWeightFreshness() * 0.4; // 两周内
        } else {
            return getTWeightFreshness() * 0.2; // 超过两周
        }
    }

    /**
     * 需求详细度评分（描述越详细越靠谱）
     */
    private double calculateDetailScore(String detail) {
        if (!StringUtils.hasText(detail)) {
            return getTWeightDetail() * 0.2; // 无描述
        }
        int length = detail.length();
        if (length >= config.getDetailFullScoreChars()) {
            return getTWeightDetail(); // 详细描述，满分
        } else if (length >= 50) {
            return getTWeightDetail() * 0.8;
        } else if (length >= 20) {
            return getTWeightDetail() * 0.6;
        } else {
            return getTWeightDetail() * 0.4;
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

    // ============ 混合评分（协同过滤 + 内容匹配）============

    /**
     * 计算混合匹配分数（内容匹配 + 协同过滤）
     * 
     * 最终分数 = (1 - cfWeight) * contentScore + cfWeight * cfScore * 100
     *
     * @param tutor    教员档案
     * @param demand   需求帖子
     * @param distance 距离(公里)
     * @param cfScore  协同过滤预测分数(0.0~1.0)，可为null表示无CF分数
     * @param cfWeight 协同过滤权重(0.0~1.0)，如0.3表示CF占30%
     * @return 混合评分结果
     */
    public MatchScoreResult calculateHybridScore(
            TutorProfile tutor,
            DemandPost demand,
            Double distance,
            Double cfScore,
            double cfWeight) {

        // 1. 首先计算内容匹配分数
        MatchScoreResult contentResult = calculateScore(tutor, demand, distance);
        double contentScore = contentResult.getMatchScore();

        // 2. 如果没有CF分数，直接返回内容匹配结果
        if (cfScore == null || cfWeight <= 0) {
            return contentResult;
        }

        // 3. 计算混合分数
        double cfScoreNormalized = cfScore * 100; // 转换到0-100范围
        double hybridScore = (1 - cfWeight) * contentScore + cfWeight * cfScoreNormalized;

        // 4. 更新结果
        contentResult.setMatchScore(Math.min(100.0, hybridScore));
        contentResult.setCfScore(cfScore);

        // 5. 添加CF相关标签
        if (cfScore >= 0.7) {
            contentResult.getMatchTags().add("相似用户推荐⭐");
        } else if (cfScore >= 0.5) {
            contentResult.getMatchTags().add("智能推荐");
        }

        log.debug("Hybrid score for tutor {}: content={}, cf={}, hybrid={}",
                tutor.getId(), contentScore, cfScoreNormalized, hybridScore);

        return contentResult;
    }

    /**
     * 计算混合匹配分数（基于搜索条件，无需求对象）
     *
     * @param tutor       教员档案
     * @param subject     科目
     * @param grade       年级
     * @param distance    距离
     * @param budgetPrice 预算价格
     * @param cfScore     协同过滤预测分数(0.0~1.0)
     * @param cfWeight    协同过滤权重(0.0~1.0)
     * @return 混合评分结果
     */
    public MatchScoreResult calculateHybridScoreByCondition(
            TutorProfile tutor,
            String subject,
            String grade,
            Double distance,
            BigDecimal budgetPrice,
            Double cfScore,
            double cfWeight) {

        // 1. 首先计算内容匹配分数
        MatchScoreResult contentResult = calculateScoreByCondition(tutor, subject, grade, distance, budgetPrice);
        double contentScore = contentResult.getMatchScore();

        // 2. 如果没有CF分数，直接返回内容匹配结果
        if (cfScore == null || cfWeight <= 0) {
            return contentResult;
        }

        // 3. 计算混合分数
        double cfScoreNormalized = cfScore * 100;
        double hybridScore = (1 - cfWeight) * contentScore + cfWeight * cfScoreNormalized;

        // 4. 更新结果
        contentResult.setMatchScore(Math.min(100.0, hybridScore));
        contentResult.setCfScore(cfScore);

        // 5. 添加CF相关标签
        if (cfScore >= 0.7) {
            contentResult.getMatchTags().add("相似用户推荐⭐");
        } else if (cfScore >= 0.5) {
            contentResult.getMatchTags().add("智能推荐");
        }

        return contentResult;
    }
}
