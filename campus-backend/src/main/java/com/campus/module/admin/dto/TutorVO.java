package com.campus.module.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师列表 VO
 */
@Data
public class TutorVO {

    private Long id;

    private Long userId;

    /** 真实姓名 */
    private String realName;

    /** 学校名称 */
    private String universityName;

    /** 专业 */
    private String major;

    /** 学历: 1-专科, 2-本科, 3-硕士, 4-博士 */
    private Integer education;

    /** 入学年份 */
    private Integer enrollYear;

    /** 认证状态: 0-待提交, 1-待审核, 2-已通过, 3-已拒绝 */
    private Integer certStatus;

    /** 评分 */
    private BigDecimal rating;

    /** 订单数 */
    private Integer orderCount;

    /** 授课科目 */
    private List<String> teachSubjects;

    /** 授课年级 */
    private List<String> teachGrades;

    /** 期望时薪 */
    private BigDecimal expectPrice;

    /** 可上门 */
    private Boolean canVisit;

    /** 可线上 */
    private Boolean canOnline;

    /** 个人简介 */
    private String introduction;

    /** 身份证号(脱敏) */
    private String idCard;

    /** 身份证正面照 */
    private String idCardFrontUrl;

    /** 身份证反面照 */
    private String idCardBackUrl;

    /** 学生证照片 */
    private String studentCardUrl;

    private LocalDateTime createTime;
}
