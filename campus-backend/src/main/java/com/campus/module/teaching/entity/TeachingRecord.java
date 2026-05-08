package com.campus.module.teaching.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课时打卡记录表
 */
@Data
@TableName("teaching_record")
public class TeachingRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 第几节课
     */
    private Integer lessonIndex;

    /**
     * 实际上课时间
     */
    private LocalDateTime startTime;

    /**
     * 实际下课时间
     */
    private LocalDateTime endTime;

    /**
     * 打卡纬度
     */
    private BigDecimal clockInLat;

    /**
     * 打卡经度
     */
    private BigDecimal clockInLng;

    /**
     * 现场拍照(水印)
     */
    private String clockInImg;

    /**
     * 教学内容摘要
     */
    private String contentSummary;

    /**
     * 布置作业
     */
    private String homeworkAssigned;

    /**
     * 预定上课时间
     */
    private LocalDateTime scheduledStartTime;

    /**
     * 预定下课时间
     */
    private LocalDateTime scheduledEndTime;

    /**
     * 状态：0-待上课, 1-上课中, 2-待确认, 3-已确认, 4-申诉中, 5-已解决, 6-已过期
     */
    private Integer status;

    /**
     * 支付结算状态：0-未结算, 1-已结算
     */
    private Integer payStatus;

    /**
     * 结算时间
     */
    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
