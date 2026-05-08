package com.campus.module.teaching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课时记录响应 DTO（含关联订单/用户数据）
 */
@Data
@Builder
@Schema(description = "课时记录响应")
public class TeachingRecordDTO {

    // ==================== TeachingRecord 原始字段 ====================

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "第几节课")
    private Integer lessonIndex;

    @Schema(description = "实际上课时间")
    private LocalDateTime startTime;

    @Schema(description = "实际下课时间")
    private LocalDateTime endTime;

    @Schema(description = "预定上课时间")
    private LocalDateTime scheduledStartTime;

    @Schema(description = "预定下课时间")
    private LocalDateTime scheduledEndTime;

    @Schema(description = "打卡纬度")
    private BigDecimal clockInLat;

    @Schema(description = "打卡经度")
    private BigDecimal clockInLng;

    @Schema(description = "现场拍照URL")
    private String clockInImg;

    @Schema(description = "教学内容摘要")
    private String contentSummary;

    @Schema(description = "布置作业")
    private String homeworkAssigned;

    @Schema(description = "状态：0-待上课, 1-上课中, 2-待确认, 3-已确认, 4-申诉中, 5-已解决, 6-已过期")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "支付结算状态：0-未结算, 1-已结算")
    private Integer payStatus;

    @Schema(description = "结算时间")
    private LocalDateTime payTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ==================== 关联 CourseOrder 字段 ====================

    @Schema(description = "课程科目")
    private String subject;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "授课方式：1上门 2网课")
    private Integer teachingMode;

    @Schema(description = "上课地址")
    private String address;

    @Schema(description = "课时单价")
    private BigDecimal unitPrice;

    /** 每节课费用 = totalAmount / totalHours */
    @Schema(description = "本课时费用")
    private BigDecimal fee;

    /** 上课日期，等于 startTime 的日期部分 */
    @Schema(description = "上课日期")
    private LocalDateTime lessonDate;

    // ==================== 关联用户信息 ====================

    @Schema(description = "教员用户ID")
    private Long tutorUserId;

    @Schema(description = "教员姓名")
    private String tutorName;

    @Schema(description = "教员头像")
    private String tutorAvatar;

    @Schema(description = "教员学校")
    private String tutorUniversity;

    @Schema(description = "家长用户ID")
    private Long parentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学生头像")
    private String studentAvatar;

    // ==================== 前端兼容字段（别名） ====================

    /** 前端列表用：上课打卡时间 = startTime */
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    /** 前端列表用：下课打卡时间 = endTime */
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;

    /** 签到位置文本，由 clockInLat/clockInLng 拼接 */
    @Schema(description = "签到位置")
    private String checkInLocation;

    /** 签退位置文本 */
    @Schema(description = "签退位置")
    private String checkOutLocation;

    /** 签退内容 = contentSummary */
    @Schema(description = "签退内容")
    private String checkOutContent;

    /** 课时时长文字 */
    @Schema(description = "课时时长")
    private String duration;
}
