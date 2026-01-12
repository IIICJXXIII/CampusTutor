package com.campus.module.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约请求表
 */
@Data
@TableName("booking_request")
public class BookingRequest {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 家长用户ID
     */
    private Long parentId;

    /**
     * 教师用户ID
     */
    private Long tutorId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 科目
     */
    private String subject;

    /**
     * 年级
     */
    private String grade;

    /**
     * 预约日期
     */
    private LocalDateTime bookingDate;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 状态：0-待教师确认, 1-教师已确认, 2-教师已拒绝, 3-家长已取消
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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