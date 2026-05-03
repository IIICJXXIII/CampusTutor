package com.campus.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_order")
public class CourseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long parentId;

    private Long studentId;

    private Long tutorId;

    private Long tutorProfileId;

    private Long demandId;

    private String subject;

    private String grade;

    private Integer teachMode;

    private BigDecimal unitPrice;

    private Integer totalHours;

    private BigDecimal totalAmount;

    private BigDecimal serviceFee;

    private BigDecimal tutorAmount;

    private Integer usedHours;

    private Integer status;

    private LocalDateTime payTime;

    private Integer payType;

    private String payTradeNo;

    private String cancelReason;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
