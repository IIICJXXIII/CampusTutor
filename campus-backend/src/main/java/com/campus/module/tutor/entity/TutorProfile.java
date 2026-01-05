package com.campus.module.tutor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教员档案表
 */
@Data
@TableName("tutor_profile")
public class TutorProfile {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 身份证正面照URL
     */
    private String idCardFrontUrl;

    /**
     * 身份证背面照URL
     */
    private String idCardBackUrl;

    /**
     * 学校名称
     */
    private String universityName;

    /**
     * 专业
     */
    private String major;

    /**
     * 学历：1本科在读 2本科毕业 3硕士在读 4硕士毕业 5博士
     */
    private Integer education;

    /**
     * 入学年份
     */
    private Integer enrollYear;

    /**
     * 学生证照片URL
     */
    private String studentCardUrl;

    /**
     * 资质证书URLs(JSON数组)
     */
    private String certificateUrls;

    /**
     * 可授科目(JSON数组)
     */
    private String teachSubjects;

    /**
     * 可授年级(JSON数组)
     */
    private String teachGrades;

    /**
     * 教学风格
     */
    private String teachStyle;

    /**
     * 自我介绍
     */
    private String introduction;

    /**
     * 期望时薪(元)
     */
    private BigDecimal expectPrice;

    /**
     * 可上门：0否 1是
     */
    private Integer canVisit;

    /**
     * 可网课：0否 1是
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

    /**
     * 认证状态：0待提交 1待审核 2已通过 3已拒绝
     */
    private Integer certStatus;

    /**
     * 审核拒绝原因
     */
    private String rejectReason;

    /**
     * 综合评分
     */
    private BigDecimal rating;

    /**
     * 完成订单数
     */
    private Integer orderCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
