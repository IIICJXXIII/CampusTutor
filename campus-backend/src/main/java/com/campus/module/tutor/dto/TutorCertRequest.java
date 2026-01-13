package com.campus.module.tutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教员认证提交请求
 */
@Data
public class TutorCertRequest {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "身份证正面照不能为空")
    private String idCardFrontUrl;

    @NotBlank(message = "身份证背面照不能为空")
    private String idCardBackUrl;

    @NotBlank(message = "学校名称不能为空")
    private String universityName;

    @NotBlank(message = "专业不能为空")
    private String major;

    @NotNull(message = "学历不能为空")
    private Integer education;

    @NotNull(message = "入学年份不能为空")
    private Integer enrollYear;

    @NotBlank(message = "学生证照片不能为空")
    private String studentCardUrl;

    /**
     * 资质证书URLs
     */
    private List<String> certificateUrls;

    /**
     * 可授年级（如：小学一年级、初一、高三等）
     */
    private List<String> teachGrades;

    /**
     * 可授科目（如：语文、数学、英语等）
     */
    private List<String> teachSubjects;

    /**
     * 期望时薪(元)
     */
    private BigDecimal expectPrice;
}
