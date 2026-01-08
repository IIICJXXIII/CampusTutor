package com.campus.common.utils;

import java.util.*;

/**
 * 年级工具类
 * 用于处理年级字段的标准化和映射
 */
public class GradeUtils {

    // 标准年级列表
    public static final List<String> PRIMARY_GRADES = Arrays.asList(
            "小学一年级", "小学二年级", "小学三年级", "小学四年级", "小学五年级", "小学六年级"
    );
    
    public static final List<String> JUNIOR_GRADES = Arrays.asList("初一", "初二", "初三");
    
    public static final List<String> SENIOR_GRADES = Arrays.asList("高一", "高二", "高三");

    // 年级别名映射（用于兼容旧数据）
    private static final Map<String, List<String>> GRADE_ALIASES = new HashMap<>();
    
    static {
        // 全科映射到具体年级
        GRADE_ALIASES.put("小学全科", PRIMARY_GRADES);
        GRADE_ALIASES.put("初中全科", JUNIOR_GRADES);
        GRADE_ALIASES.put("高中全科", SENIOR_GRADES);
        
        // 初中一年级 -> 初一 等别名
        GRADE_ALIASES.put("初中一年级", Collections.singletonList("初一"));
        GRADE_ALIASES.put("初中二年级", Collections.singletonList("初二"));
        GRADE_ALIASES.put("初中三年级", Collections.singletonList("初三"));
        GRADE_ALIASES.put("高中一年级", Collections.singletonList("高一"));
        GRADE_ALIASES.put("高中二年级", Collections.singletonList("高二"));
        GRADE_ALIASES.put("高中三年级", Collections.singletonList("高三"));
    }

    /**
     * 获取年级的所有可能搜索关键词
     * 例如：搜索"小学一年级"时，也应该匹配"小学全科"
     * 
     * @param grade 目标年级
     * @return 需要搜索的关键词列表
     */
    public static List<String> getSearchKeywords(String grade) {
        List<String> keywords = new ArrayList<>();
        keywords.add(grade);
        
        // 添加全科关键词
        if (PRIMARY_GRADES.contains(grade)) {
            keywords.add("小学全科");
        } else if (JUNIOR_GRADES.contains(grade)) {
            keywords.add("初中全科");
        } else if (SENIOR_GRADES.contains(grade)) {
            keywords.add("高中全科");
        }
        
        return keywords;
    }

    /**
     * 标准化年级名称
     * 将别名转换为标准名称
     * 
     * @param grade 原始年级名称
     * @return 标准化后的年级名称
     */
    public static String normalize(String grade) {
        if (grade == null) return null;
        
        // 处理常见别名
        switch (grade) {
            case "初中一年级": return "初一";
            case "初中二年级": return "初二";
            case "初中三年级": return "初三";
            case "高中一年级": return "高一";
            case "高中二年级": return "高二";
            case "高中三年级": return "高三";
            default: return grade;
        }
    }

    /**
     * 判断是否为有效的标准年级
     */
    public static boolean isValidGrade(String grade) {
        return PRIMARY_GRADES.contains(grade) 
                || JUNIOR_GRADES.contains(grade) 
                || SENIOR_GRADES.contains(grade);
    }
}
