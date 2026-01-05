package com.campus.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {

    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    /**
     * 教员档案ID
     */
    @NotNull(message = "教员ID不能为空")
    private Long tutorProfileId;

    /**
     * 需求帖ID(可选)
     */
    private Long demandId;

    /**
     * 课程科目
     */
    @NotNull(message = "科目不能为空")
    private String subject;

    /**
     * 课程年级
     */
    @NotNull(message = "年级不能为空")
    private String grade;

    /**
     * 授课方式：1上门 2网课
     */
    @NotNull(message = "授课方式不能为空")
    private Integer teachMode;

    /**
     * 课时单价(元/小时)
     */
    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    /**
     * 总课时数
     */
    @NotNull(message = "课时数不能为空")
    private Integer totalHours;

    /**
     * 备注
     */
    private String remark;
}
