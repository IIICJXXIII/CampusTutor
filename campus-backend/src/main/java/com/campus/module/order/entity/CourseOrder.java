package com.campus.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程订单表
 */
@Data
@TableName("course_order")
public class CourseOrder {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 家长用户ID
     */
    private Long parentId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 教员用户ID
     */
    private Long tutorId;

    /**
     * 教员档案ID
     */
    private Long tutorProfileId;

    /**
     * 需求帖ID
     */
    private Long demandId;

    /**
     * 课程科目
     */
    private String subject;

    /**
     * 课程年级
     */
    private String grade;

    /**
     * 授课方式：1上门 2网课
     */
    private Integer teachMode;

    /**
     * 课时单价(元/小时)
     */
    private BigDecimal unitPrice;

    /**
     * 总课时数
     */
    private Integer totalHours;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 平台服务费
     */
    private BigDecimal serviceFee;

    /**
     * 教员实收金额
     */
    private BigDecimal tutorAmount;

    /**
     * 已上课时
     */
    private Integer usedHours;

    private Integer status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 支付方式：1钱包 2微信 3支付宝
     */
    private Integer payType;

    /**
     * 第三方支付流水号
     */
    private String payTradeNo;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
