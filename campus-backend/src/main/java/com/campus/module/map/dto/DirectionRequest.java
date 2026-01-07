package com.campus.module.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 路径规划请求
 */
@Data
@Schema(description = "路径规划请求")
public class DirectionRequest {

    @Schema(description = "起点纬度", required = true, example = "39.984154")
    @NotNull(message = "起点纬度不能为空")
    private Double fromLatitude;

    @Schema(description = "起点经度", required = true, example = "116.307490")
    @NotNull(message = "起点经度不能为空")
    private Double fromLongitude;

    @Schema(description = "终点纬度", required = true, example = "39.998766")
    @NotNull(message = "终点纬度不能为空")
    private Double toLatitude;

    @Schema(description = "终点经度", required = true, example = "116.474977")
    @NotNull(message = "终点经度不能为空")
    private Double toLongitude;

    @Schema(description = "出行方式: walking-步行, driving-驾车, transit-公交", example = "walking")
    private String mode = "walking";
}
