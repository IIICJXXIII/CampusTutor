package com.campus.module.booking.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 创建预约请求DTO
 */
@Data
public class CreateBookingRequest {

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
     * 备注
     */
    private String remark;
}