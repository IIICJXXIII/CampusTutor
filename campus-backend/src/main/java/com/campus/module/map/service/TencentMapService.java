package com.campus.module.map.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.campus.module.map.config.TencentMapConfig;
import com.campus.module.map.dto.DirectionResult;
import com.campus.module.map.dto.DistanceResult;
import com.campus.module.map.dto.GeocoderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯地图服务
 * 提供逆地址解析、路径规划、距离计算等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TencentMapService {

    private final TencentMapConfig config;

    /**
     * 逆地址解析 - 根据经纬度获取地址信息
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址解析结果
     */
    public GeocoderResult reverseGeocode(double latitude, double longitude) {
        if (!isConfigured()) {
            log.warn("腾讯地图API未配置");
            return createErrorResult("腾讯地图API未配置");
        }

        try {
            String url = config.getBaseUrl() + config.getGeocoderUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("location", latitude + "," + longitude);
            params.put("key", config.getKey());
            params.put("get_poi", "1");

            String response = HttpUtil.get(url, params);
            log.debug("逆地址解析响应: {}", response);

            return JSONUtil.toBean(response, GeocoderResult.class);
        } catch (Exception e) {
            log.error("逆地址解析失败: {}", e.getMessage(), e);
            return createErrorResult("逆地址解析失败: " + e.getMessage());
        }
    }

    /**
     * 地址解析 - 根据地址获取经纬度
     *
     * @param address 地址字符串
     * @return 地址解析结果(包含经纬度)
     */
    public GeocoderResult geocode(String address) {
        if (!isConfigured()) {
            log.warn("腾讯地图API未配置");
            return createErrorResult("腾讯地图API未配置");
        }

        try {
            String url = config.getBaseUrl() + config.getAddressUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("address", address);
            params.put("key", config.getKey());

            String response = HttpUtil.get(url, params);
            log.debug("地址解析响应: {}", response);

            return JSONUtil.toBean(response, GeocoderResult.class);
        } catch (Exception e) {
            log.error("地址解析失败: {}", e.getMessage(), e);
            return createErrorResult("地址解析失败: " + e.getMessage());
        }
    }

    /**
     * 路径规划 - 步行
     *
     * @param fromLat 起点纬度
     * @param fromLng 起点经度
     * @param toLat   终点纬度
     * @param toLng   终点经度
     * @return 路径规划结果
     */
    public DirectionResult walkingDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getWalkingUrl(), fromLat, fromLng, toLat, toLng);
    }

    /**
     * 路径规划 - 驾车
     *
     * @param fromLat 起点纬度
     * @param fromLng 起点经度
     * @param toLat   终点纬度
     * @param toLng   终点经度
     * @return 路径规划结果
     */
    public DirectionResult drivingDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getDrivingUrl(), fromLat, fromLng, toLat, toLng);
    }

    /**
     * 路径规划 - 公交
     *
     * @param fromLat 起点纬度
     * @param fromLng 起点经度
     * @param toLat   终点纬度
     * @param toLng   终点经度
     * @return 路径规划结果
     */
    public DirectionResult transitDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getTransitUrl(), fromLat, fromLng, toLat, toLng);
    }

    /**
     * 通用路径规划
     */
    private DirectionResult direction(String apiPath, double fromLat, double fromLng, double toLat, double toLng) {
        if (!isConfigured()) {
            log.warn("腾讯地图API未配置");
            DirectionResult result = new DirectionResult();
            result.setStatus(-1);
            result.setMessage("腾讯地图API未配置");
            return result;
        }

        try {
            String url = config.getBaseUrl() + apiPath;
            Map<String, Object> params = new HashMap<>();
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toLat + "," + toLng);
            params.put("key", config.getKey());

            String response = HttpUtil.get(url, params);
            log.debug("路径规划响应: {}", response);

            return JSONUtil.toBean(response, DirectionResult.class);
        } catch (Exception e) {
            log.error("路径规划失败: {}", e.getMessage(), e);
            DirectionResult result = new DirectionResult();
            result.setStatus(-1);
            result.setMessage("路径规划失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 计算两点间距离和时间
     *
     * @param fromLat 起点纬度
     * @param fromLng 起点经度
     * @param toLat   终点纬度
     * @param toLng   终点经度
     * @param mode    出行方式: walking-步行, driving-驾车
     * @return 距离计算结果
     */
    public DistanceResult calculateDistance(double fromLat, double fromLng, double toLat, double toLng, String mode) {
        if (!isConfigured()) {
            log.warn("腾讯地图API未配置");
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("腾讯地图API未配置");
            return result;
        }

        try {
            String url = config.getBaseUrl() + config.getDistanceUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toLat + "," + toLng);
            params.put("mode", mode != null ? mode : "walking");
            params.put("key", config.getKey());

            String response = HttpUtil.get(url, params);
            log.debug("距离计算响应: {}", response);

            return JSONUtil.toBean(response, DistanceResult.class);
        } catch (Exception e) {
            log.error("距离计算失败: {}", e.getMessage(), e);
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("距离计算失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 批量计算距离(一对多)
     *
     * @param fromLat   起点纬度
     * @param fromLng   起点经度
     * @param toPoints  终点列表，格式: "lat1,lng1;lat2,lng2;..."
     * @param mode      出行方式
     * @return 距离计算结果
     */
    public DistanceResult batchCalculateDistance(double fromLat, double fromLng, String toPoints, String mode) {
        if (!isConfigured()) {
            log.warn("腾讯地图API未配置");
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("腾讯地图API未配置");
            return result;
        }

        try {
            String url = config.getBaseUrl() + config.getDistanceUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toPoints);
            params.put("mode", mode != null ? mode : "walking");
            params.put("key", config.getKey());

            String response = HttpUtil.get(url, params);
            log.debug("批量距离计算响应: {}", response);

            return JSONUtil.toBean(response, DistanceResult.class);
        } catch (Exception e) {
            log.error("批量距离计算失败: {}", e.getMessage(), e);
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("批量距离计算失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 检查是否已配置API Key
     */
    private boolean isConfigured() {
        return StringUtils.hasText(config.getKey());
    }

    /**
     * 创建错误结果
     */
    private GeocoderResult createErrorResult(String message) {
        GeocoderResult result = new GeocoderResult();
        result.setStatus(-1);
        result.setMessage(message);
        return result;
    }
}