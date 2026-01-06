package com.campus.module.teaching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课时记录响应 DTO
 */
@Data
@Builder
@Schema(description = "课时记录响应")
public class TeachingRecordDTO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "第几节课")
    private Integer lessonIndex;

    @Schema(description = "上课时间")
    private LocalDateTime startTime;

    @Schema(description = "下课时间")
    private LocalDateTime endTime;

    @Schema(description = "打卡纬度")
    private BigDecimal clockInLat;

    @Schema(description = "打卡经度")
    private BigDecimal clockInLng;

    @Schema(description = "现场拍照")
    private String clockInImg;

    @Schema(description = "教学内容摘要")
    private String contentSummary;

    @Schema(description = "布置作业")
    private String homeworkAssigned;

    @Schema(description = "状态：0-待确认, 1-家长已确认, 2-异常/申诉")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
