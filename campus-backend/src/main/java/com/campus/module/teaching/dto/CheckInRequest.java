package com.campus.module.teaching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 教师打卡请求 DTO
 */
@Data
@Schema(description = "教师打卡请求")
public class CheckInRequest {

    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "打卡纬度", required = true)
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @Schema(description = "打卡经度", required = true)
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @Schema(description = "现场拍照URL", required = true)
    private String photoUrl;

    @Schema(description = "教学内容摘要")
    private String contentSummary;

    @Schema(description = "布置作业")
    private String homeworkAssigned;
}
