package com.campus.module.map.controller;

import com.campus.common.result.Result;
import com.campus.module.map.dto.DirectionRequest;
import com.campus.module.map.dto.DirectionResult;
import com.campus.module.map.dto.DistanceResult;
import com.campus.module.map.dto.GeocoderResult;
import com.campus.module.map.service.AmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 地图服务控制器
 * 使用高德地图API
 */
@Tag(name = "地图服务", description = "逆地址解析、路径规划、距离计算（高德地图）")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final AmapService mapService;

    /**
     * 逆地址解析 - 根据经纬度获取地址
     */
    @Operation(summary = "逆地址解析", description = "根据经纬度获取详细地址信息")
    @GetMapping("/geocoder/reverse")
    public Result<GeocoderResult> reverseGeocode(
            @Parameter(description = "纬度", required = true, example = "39.984154")
            @RequestParam Double latitude,
            @Parameter(description = "经度", required = true, example = "116.307490")
            @RequestParam Double longitude) {
        GeocoderResult result = mapService.reverseGeocode(latitude, longitude);
        if (result.getStatus() == 0) {
            return Result.success(result);
        }
        return Result.fail(result.getMessage());
    }

    /**
     * 地址解析 - 根据地址获取经纬度
     */
    @Operation(summary = "地址解析", description = "根据地址字符串获取经纬度")
    @GetMapping("/geocoder")
    public Result<GeocoderResult> geocode(
            @Parameter(description = "地址", required = true, example = "北京市海淀区中关村")
            @RequestParam String address) {
        GeocoderResult result = mapService.geocode(address);
        if (result.getStatus() == 0) {
            return Result.success(result);
        }
        return Result.fail(result.getMessage());
    }

    /**
     * 路径规划
     */
    @Operation(summary = "路径规划", description = "获取两点间的路径规划信息")
    @PostMapping("/direction")
    public Result<DirectionResult> direction(@Valid @RequestBody DirectionRequest request) {
        DirectionResult result;
        switch (request.getMode()) {
            case "driving":
                result = mapService.drivingDirection(
                        request.getFromLatitude(), request.getFromLongitude(),
                        request.getToLatitude(), request.getToLongitude());
                break;
            case "transit":
                result = mapService.transitDirection(
                        request.getFromLatitude(), request.getFromLongitude(),
                        request.getToLatitude(), request.getToLongitude());
                break;
            default:
                result = mapService.walkingDirection(
                        request.getFromLatitude(), request.getFromLongitude(),
                        request.getToLatitude(), request.getToLongitude());
        }

        if (result.getStatus() == 0) {
            return Result.success(result);
        }
        return Result.fail(result.getMessage());
    }

    /**
     * 距离计算
     */
    @Operation(summary = "距离计算", description = "计算两点间的距离和预估时间")
    @GetMapping("/distance")
    public Result<DistanceResult> distance(
            @Parameter(description = "起点纬度", required = true)
            @RequestParam Double fromLatitude,
            @Parameter(description = "起点经度", required = true)
            @RequestParam Double fromLongitude,
            @Parameter(description = "终点纬度", required = true)
            @RequestParam Double toLatitude,
            @Parameter(description = "终点经度", required = true)
            @RequestParam Double toLongitude,
            @Parameter(description = "出行方式: walking-步行, driving-驾车")
            @RequestParam(defaultValue = "walking") String mode) {
        DistanceResult result = mapService.calculateDistance(
                fromLatitude, fromLongitude, toLatitude, toLongitude, mode);
        if (result.getStatus() == 0) {
            return Result.success(result);
        }
        return Result.fail(result.getMessage());
    }
}
