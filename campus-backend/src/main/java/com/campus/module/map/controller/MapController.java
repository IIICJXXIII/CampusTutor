package com.campus.module.map.controller;

import com.campus.common.result.Result;
import com.campus.module.map.dto.DirectionRequest;
import com.campus.module.map.dto.DirectionResult;
import com.campus.module.map.dto.DistanceResult;
import com.campus.module.map.dto.GeocoderResult;
import com.campus.module.map.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "地图服务", description = "通用地图服务接口(逆地址解析、路径规划、距离计算)")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    // 核心修改：注入接口
    private final MapService mapService;

    @Operation(summary = "逆地址解析", description = "根据经纬度获取详细地址信息")
    @GetMapping("/geocoder/reverse")
    public Result<GeocoderResult> reverseGeocode(
            @Parameter(description = "纬度", required = true) @RequestParam Double latitude,
            @Parameter(description = "经度", required = true) @RequestParam Double longitude) {
        GeocoderResult result = mapService.reverseGeocode(latitude, longitude);
        return result.getStatus() == 0 ? Result.success(result) : Result.fail(result.getMessage());
    }

    @Operation(summary = "地址解析", description = "根据地址字符串获取经纬度")
    @GetMapping("/geocoder")
    public Result<GeocoderResult> geocode(
            @Parameter(description = "地址", required = true) @RequestParam String address) {
        GeocoderResult result = mapService.geocode(address);
        return result.getStatus() == 0 ? Result.success(result) : Result.fail(result.getMessage());
    }

    @Operation(summary = "路径规划", description = "获取两点间的路径规划信息")
    @PostMapping("/direction")
    public Result<DirectionResult> direction(@Valid @RequestBody DirectionRequest request) {
        DirectionResult result;
        String mode = request.getMode() != null ? request.getMode().toLowerCase() : "walking";

        switch (mode) {
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

        return result.getStatus() == 0 ? Result.success(result) : Result.fail(result.getMessage());
    }

    @Operation(summary = "距离计算", description = "计算两点间的距离和预估时间")
    @GetMapping("/distance")
    public Result<DistanceResult> distance(
            @RequestParam Double fromLatitude, @RequestParam Double fromLongitude,
            @RequestParam Double toLatitude, @RequestParam Double toLongitude,
            @RequestParam(defaultValue = "walking") String mode) {
        DistanceResult result = mapService.calculateDistance(
                fromLatitude, fromLongitude, toLatitude, toLongitude, mode);
        return result.getStatus() == 0 ? Result.success(result) : Result.fail(result.getMessage());
    }
}