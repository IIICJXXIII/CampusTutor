package com.campus.module.tutor.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教员档案更新请求
 */
@Data
public class TutorProfileUpdateRequest {

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 学校名称
     */
    private String universityName;

    /**
     * 专业
     */
    private String major;

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
     * 教学经验
     */
    private String experience;

    /**
     * 教学成果
     */
    private String achievements;

    /**
     * 期望时薪
     */
    private BigDecimal expectPrice;

    /**
     * 是否可上门：0否 1是
     */
    private Integer canVisit;

    /**
     * 是否可网课：0否 1是
     */
    private Integer canOnline;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 详细地址
     */
    private String address;
}
