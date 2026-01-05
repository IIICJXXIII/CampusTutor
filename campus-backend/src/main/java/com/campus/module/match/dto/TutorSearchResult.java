package com.campus.module.match.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教员搜索结果
 */
@Data
public class TutorSearchResult {

    /**
     * 教员档案ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名(脱敏)
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 学校名称
     */
    private String universityName;

    /**
     * 专业
     */
    private String major;

    /**
     * 学历
     */
    private Integer education;

    /**
     * 可授科目
     */
    private List<String> teachSubjects;

    /**
     * 可授年级
     */
    private List<String> teachGrades;

    /**
     * 教学风格
     */
    private String teachStyle;

    /**
     * 自我介绍
     */
    private String introduction;

    /**
     * 期望时薪
     */
    private BigDecimal expectPrice;

    /**
     * 可上门
     */
    private Integer canVisit;

    /**
     * 可网课
     */
    private Integer canOnline;

    /**
     * 综合评分
     */
    private BigDecimal rating;

    /**
     * 完成订单数
     */
    private Integer orderCount;

    /**
     * 距离(公里)
     */
    private Double distance;
}
