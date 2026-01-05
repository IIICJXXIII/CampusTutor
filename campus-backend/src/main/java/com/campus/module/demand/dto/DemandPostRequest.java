package com.campus.module.demand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 发布需求请求
 */
@Data
public class DemandPostRequest {

    /**
     * 需求ID(编辑时必填)
     */
    private Long id;

    /**
     * 关联学生ID
     */
    private Long studentId;

    @NotBlank(message = "需求标题不能为空")
    private String title;

    @NotBlank(message = "科目不能为空")
    private String subject;

    @NotBlank(message = "年级不能为空")
    private String grade;

    /**
     * 期望价格
     */
    private BigDecimal expectPrice;

    /**
     * 课时要求
     */
    private List<String> scheduleRequire;

    @NotNull(message = "授课方式不能为空")
    private Integer teachMode;

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
     * 需求详情
     */
    private String detail;
}
