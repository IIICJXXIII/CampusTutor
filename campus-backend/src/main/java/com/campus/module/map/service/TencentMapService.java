package com.campus.module.map.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.campus.module.map.config.TencentMapConfig;
import com.campus.module.map.dto.DirectionResult;
import com.campus.module.map.dto.DistanceResult;
import com.campus.module.map.dto.GeocoderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
// 只有配置 map.provider=tencent 时才生效
@ConditionalOnProperty(name = "map.provider", havingValue = "tencent")
public class TencentMapService implements MapService {

    private final TencentMapConfig config;

    @Override
    public GeocoderResult reverseGeocode(double latitude, double longitude) {
        if (!isConfigured()) return createErrorResult("腾讯地图API未配置");
        try {
            String url = config.getBaseUrl() + config.getGeocoderUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("location", latitude + "," + longitude);
            params.put("key", config.getKey());
            params.put("get_poi", "1");
            String response = HttpUtil.get(url, params);
            return JSONUtil.toBean(response, GeocoderResult.class);
        } catch (Exception e) {
            log.error("逆地址解析失败", e);
            return createErrorResult("逆地址解析失败: " + e.getMessage());
        }
    }

    @Override
    public GeocoderResult geocode(String address) {
        if (!isConfigured()) return createErrorResult("腾讯地图API未配置");
        try {
            String url = config.getBaseUrl() + config.getAddressUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("address", address);
            params.put("key", config.getKey());
            String response = HttpUtil.get(url, params);
            return JSONUtil.toBean(response, GeocoderResult.class);
        } catch (Exception e) {
            log.error("地址解析失败", e);
            return createErrorResult("地址解析失败: " + e.getMessage());
        }
    }

    @Override
    public DirectionResult walkingDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getWalkingUrl(), fromLat, fromLng, toLat, toLng);
    }

    @Override
    public DirectionResult drivingDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getDrivingUrl(), fromLat, fromLng, toLat, toLng);
    }

    @Override
    public DirectionResult transitDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getTransitUrl(), fromLat, fromLng, toLat, toLng);
    }

    private DirectionResult direction(String apiPath, double fromLat, double fromLng, double toLat, double toLng) {
        if (!isConfigured()) return createErrorDirectionResult("腾讯地图API未配置");
        try {
            String url = config.getBaseUrl() + apiPath;
            Map<String, Object> params = new HashMap<>();
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toLat + "," + toLng);
            params.put("key", config.getKey());
            String response = HttpUtil.get(url, params);
            return JSONUtil.toBean(response, DirectionResult.class);
        } catch (Exception e) {
            log.error("路径规划失败", e);
            return createErrorDirectionResult("路径规划失败: " + e.getMessage());
        }
    }

    @Override
    public DistanceResult calculateDistance(double fromLat, double fromLng, double toLat, double toLng, String mode) {
        if (!isConfigured()) return createErrorDistanceResult("腾讯地图API未配置");
        try {
            String url = config.getBaseUrl() + config.getDistanceUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toLat + "," + toLng);
            params.put("mode", mode != null ? mode : "walking");
            params.put("key", config.getKey());
            String response = HttpUtil.get(url, params);
            return JSONUtil.toBean(response, DistanceResult.class);
        } catch (Exception e) {
            log.error("距离计算失败", e);
            return createErrorDistanceResult("距离计算失败: " + e.getMessage());
        }
    }

    private boolean isConfigured() { return StringUtils.hasText(config.getKey()); }

    private GeocoderResult createErrorResult(String message) {
        GeocoderResult result = new GeocoderResult();
        result.setStatus(-1);
        result.setMessage(message);
        return result;
    }

    private DirectionResult createErrorDirectionResult(String message) {
        DirectionResult result = new DirectionResult();
        result.setStatus(-1);
        result.setMessage(message);
        return result;
    }

    private DistanceResult createErrorDistanceResult(String message) {
        DistanceResult result = new DistanceResult();
        result.setStatus(-1);
        result.setMessage(message);
        return result;
    }
}