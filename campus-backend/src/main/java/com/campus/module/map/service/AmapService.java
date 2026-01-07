package com.campus.module.map.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.module.map.config.AmapConfig;
import com.campus.module.map.dto.DirectionResult;
import com.campus.module.map.dto.DistanceResult;
import com.campus.module.map.dto.GeocoderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地图服务
 * 提供逆地址解析、路径规划、距离计算等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmapService {

    private final AmapConfig config;

    /**
     * 逆地址解析 - 根据经纬度获取地址信息
     * 高德使用 经度,纬度 格式（与腾讯相反）
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址解析结果
     */
    public GeocoderResult reverseGeocode(double latitude, double longitude) {
        if (!isConfigured()) {
            log.warn("高德地图API未配置");
            return createErrorResult("高德地图API未配置");
        }

        try {
            String url = config.getBaseUrl() + config.getRegeoUrl();
            Map<String, Object> params = new HashMap<>();
            // 高德格式: 经度,纬度
            params.put("location", longitude + "," + latitude);
            params.put("key", config.getKey());
            params.put("extensions", "all");
            params.put("output", "json");

            String response = HttpUtil.get(url, params);
            log.debug("高德逆地址解析响应: {}", response);

            return parseRegeoResponse(response);
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
            log.warn("高德地图API未配置");
            return createErrorResult("高德地图API未配置");
        }

        try {
            String url = config.getBaseUrl() + config.getGeocodeUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("address", address);
            params.put("key", config.getKey());
            params.put("output", "json");

            String response = HttpUtil.get(url, params);
            log.debug("高德地址解析响应: {}", response);

            return parseGeocodeResponse(response);
        } catch (Exception e) {
            log.error("地址解析失败: {}", e.getMessage(), e);
            return createErrorResult("地址解析失败: " + e.getMessage());
        }
    }

    /**
     * 路径规划 - 步行
     */
    public DirectionResult walkingDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getWalkingUrl(), fromLat, fromLng, toLat, toLng, "walking");
    }

    /**
     * 路径规划 - 驾车
     */
    public DirectionResult drivingDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return direction(config.getDrivingUrl(), fromLat, fromLng, toLat, toLng, "driving");
    }

    /**
     * 路径规划 - 公交
     */
    public DirectionResult transitDirection(double fromLat, double fromLng, double toLat, double toLng) {
        return transitDirection(fromLat, fromLng, toLat, toLng, "北京");
    }

    /**
     * 路径规划 - 公交（指定城市）
     */
    public DirectionResult transitDirection(double fromLat, double fromLng, double toLat, double toLng, String city) {
        if (!isConfigured()) {
            log.warn("高德地图API未配置");
            DirectionResult result = new DirectionResult();
            result.setStatus(-1);
            result.setMessage("高德地图API未配置");
            return result;
        }

        try {
            String url = config.getBaseUrl() + config.getTransitUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("origin", fromLng + "," + fromLat);
            params.put("destination", toLng + "," + toLat);
            params.put("city", city);
            params.put("key", config.getKey());
            params.put("output", "json");

            String response = HttpUtil.get(url, params);
            log.debug("高德公交路径规划响应: {}", response);

            return parseDirectionResponse(response, "transit");
        } catch (Exception e) {
            log.error("公交路径规划失败: {}", e.getMessage(), e);
            DirectionResult result = new DirectionResult();
            result.setStatus(-1);
            result.setMessage("公交路径规划失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 通用路径规划
     */
    private DirectionResult direction(String apiPath, double fromLat, double fromLng, double toLat, double toLng, String mode) {
        if (!isConfigured()) {
            log.warn("高德地图API未配置");
            DirectionResult result = new DirectionResult();
            result.setStatus(-1);
            result.setMessage("高德地图API未配置");
            return result;
        }

        try {
            String url = config.getBaseUrl() + apiPath;
            Map<String, Object> params = new HashMap<>();
            // 高德格式: 经度,纬度
            params.put("origin", fromLng + "," + fromLat);
            params.put("destination", toLng + "," + toLat);
            params.put("key", config.getKey());
            params.put("output", "json");

            String response = HttpUtil.get(url, params);
            log.debug("高德{}路径规划响应: {}", mode, response);

            return parseDirectionResponse(response, mode);
        } catch (Exception e) {
            log.error("路径规划失败: {}", e.getMessage(), e);
            DirectionResult result = new DirectionResult();
            result.setStatus(-1);
            result.setMessage("路径规划失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 计算两点间距离
     */
    public DistanceResult calculateDistance(double fromLat, double fromLng, double toLat, double toLng, String mode) {
        if (!isConfigured()) {
            log.warn("高德地图API未配置");
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("高德地图API未配置");
            return result;
        }

        try {
            String url = config.getBaseUrl() + config.getDistanceUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("origins", fromLng + "," + fromLat);
            params.put("destination", toLng + "," + toLat);
            // 高德距离类型: 0-直线距离, 1-驾车导航距离, 3-步行规划距离
            int type = "driving".equalsIgnoreCase(mode) ? 1 : ("walking".equalsIgnoreCase(mode) ? 3 : 0);
            params.put("type", type);
            params.put("key", config.getKey());
            params.put("output", "json");

            String response = HttpUtil.get(url, params);
            log.debug("高德距离计算响应: {}", response);

            return parseDistanceResponse(response);
        } catch (Exception e) {
            log.error("距离计算失败: {}", e.getMessage(), e);
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("距离计算失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 批量计算距离
     */
    public DistanceResult batchCalculateDistance(double fromLat, double fromLng, String toPoints, String mode) {
        if (!isConfigured()) {
            log.warn("高德地图API未配置");
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("高德地图API未配置");
            return result;
        }

        try {
            String url = config.getBaseUrl() + config.getDistanceUrl();
            Map<String, Object> params = new HashMap<>();
            params.put("origins", fromLng + "," + fromLat);
            // 转换坐标格式: "lat1,lng1;lat2,lng2" -> "lng1,lat1|lng2,lat2"
            String convertedPoints = convertToAmapFormat(toPoints);
            params.put("destination", convertedPoints);
            int type = "driving".equalsIgnoreCase(mode) ? 1 : ("walking".equalsIgnoreCase(mode) ? 3 : 0);
            params.put("type", type);
            params.put("key", config.getKey());
            params.put("output", "json");

            String response = HttpUtil.get(url, params);
            log.debug("高德批量距离计算响应: {}", response);

            return parseDistanceResponse(response);
        } catch (Exception e) {
            log.error("批量距离计算失败: {}", e.getMessage(), e);
            DistanceResult result = new DistanceResult();
            result.setStatus(-1);
            result.setMessage("批量距离计算失败: " + e.getMessage());
            return result;
        }
    }

    // ==================== 响应解析方法 ====================

    /**
     * 解析逆地理编码响应
     */
    private GeocoderResult parseRegeoResponse(String response) {
        JSONObject json = JSONUtil.parseObj(response);
        GeocoderResult result = new GeocoderResult();

        String status = json.getStr("status");
        if ("1".equals(status)) {
            result.setStatus(0);
            result.setMessage("成功");

            JSONObject regeocode = json.getJSONObject("regeocode");
            if (regeocode != null) {
                GeocoderResult.ResultData data = new GeocoderResult.ResultData();
                data.setAddress(regeocode.getStr("formatted_address"));

                JSONObject addressComponent = regeocode.getJSONObject("addressComponent");
                if (addressComponent != null) {
                    GeocoderResult.AddressComponent component = new GeocoderResult.AddressComponent();
                    component.setNation("中国");
                    component.setProvince(addressComponent.getStr("province"));
                    component.setCity(getStringOrFirst(addressComponent, "city"));
                    component.setDistrict(addressComponent.getStr("district"));
                    component.setStreet(addressComponent.getStr("streetNumber", new JSONObject()).toString());
                    data.setAddressComponent(component);
                }

                result.setResult(data);
            }
        } else {
            result.setStatus(-1);
            result.setMessage(json.getStr("info", "请求失败"));
        }

        return result;
    }

    /**
     * 解析地理编码响应
     */
    private GeocoderResult parseGeocodeResponse(String response) {
        JSONObject json = JSONUtil.parseObj(response);
        GeocoderResult result = new GeocoderResult();

        String status = json.getStr("status");
        if ("1".equals(status)) {
            JSONArray geocodes = json.getJSONArray("geocodes");
            if (geocodes != null && !geocodes.isEmpty()) {
                result.setStatus(0);
                result.setMessage("成功");

                JSONObject geocode = geocodes.getJSONObject(0);
                GeocoderResult.ResultData data = new GeocoderResult.ResultData();
                data.setAddress(geocode.getStr("formatted_address"));

                // 解析经纬度
                String location = geocode.getStr("location");
                if (StringUtils.hasText(location)) {
                    String[] parts = location.split(",");
                    if (parts.length == 2) {
                        GeocoderResult.Location loc = new GeocoderResult.Location();
                        loc.setLng(Double.parseDouble(parts[0]));
                        loc.setLat(Double.parseDouble(parts[1]));
                        data.setLocation(loc);
                    }
                }

                GeocoderResult.AddressComponent component = new GeocoderResult.AddressComponent();
                component.setProvince(geocode.getStr("province"));
                component.setCity(geocode.getStr("city"));
                component.setDistrict(geocode.getStr("district"));
                data.setAddressComponent(component);

                result.setResult(data);
            } else {
                result.setStatus(-1);
                result.setMessage("未找到地址");
            }
        } else {
            result.setStatus(-1);
            result.setMessage(json.getStr("info", "请求失败"));
        }

        return result;
    }

    /**
     * 解析路径规划响应
     */
    private DirectionResult parseDirectionResponse(String response, String mode) {
        JSONObject json = JSONUtil.parseObj(response);
        DirectionResult result = new DirectionResult();

        String status = json.getStr("status");
        if ("1".equals(status)) {
            result.setStatus(0);
            result.setMessage("成功");

            JSONObject route = json.getJSONObject("route");
            if (route != null) {
                DirectionResult.Route routeData = new DirectionResult.Route();

                JSONArray paths = route.getJSONArray("paths");
                if (paths != null && !paths.isEmpty()) {
                    JSONObject path = paths.getJSONObject(0);
                    routeData.setDistance(path.getInt("distance", 0));
                    routeData.setDuration(path.getInt("duration", 0));

                    // 解析路径步骤
                    JSONArray steps = path.getJSONArray("steps");
                    if (steps != null) {
                        List<DirectionResult.Step> stepList = new ArrayList<>();
                        for (int i = 0; i < steps.size(); i++) {
                            JSONObject stepJson = steps.getJSONObject(i);
                            DirectionResult.Step step = new DirectionResult.Step();
                            step.setInstruction(stepJson.getStr("instruction"));
                            step.setDistance(stepJson.getInt("distance", 0));
                            step.setDuration(stepJson.getInt("duration", 0));
                            step.setPolyline(stepJson.getStr("polyline"));
                            stepList.add(step);
                        }
                        routeData.setSteps(stepList);
                    }
                }

                result.setResult(routeData);
            }
        } else {
            result.setStatus(-1);
            result.setMessage(json.getStr("info", "请求失败"));
        }

        return result;
    }

    /**
     * 解析距离计算响应
     */
    private DistanceResult parseDistanceResponse(String response) {
        JSONObject json = JSONUtil.parseObj(response);
        DistanceResult result = new DistanceResult();

        String status = json.getStr("status");
        if ("1".equals(status)) {
            result.setStatus(0);
            result.setMessage("成功");

            JSONArray results = json.getJSONArray("results");
            if (results != null && !results.isEmpty()) {
                List<DistanceResult.Element> elements = new ArrayList<>();
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    DistanceResult.Element element = new DistanceResult.Element();
                    element.setDistance(item.getInt("distance", 0));
                    element.setDuration(item.getInt("duration", 0));
                    elements.add(element);
                }
                result.setElements(elements);
            }
        } else {
            result.setStatus(-1);
            result.setMessage(json.getStr("info", "请求失败"));
        }

        return result;
    }

    // ==================== 辅助方法 ====================

    private boolean isConfigured() {
        return StringUtils.hasText(config.getKey());
    }

    private GeocoderResult createErrorResult(String message) {
        GeocoderResult result = new GeocoderResult();
        result.setStatus(-1);
        result.setMessage(message);
        return result;
    }

    /**
     * 处理高德API中可能是数组或字符串的字段
     */
    private String getStringOrFirst(JSONObject json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            return arr.isEmpty() ? null : arr.getStr(0);
        }
        return value.toString();
    }

    /**
     * 将坐标格式从 "lat1,lng1;lat2,lng2" 转换为高德格式 "lng1,lat1|lng2,lat2"
     */
    private String convertToAmapFormat(String points) {
        if (!StringUtils.hasText(points)) {
            return "";
        }
        String[] pairs = points.split(";");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pairs.length; i++) {
            String[] coords = pairs[i].split(",");
            if (coords.length == 2) {
                if (i > 0) sb.append("|");
                sb.append(coords[1]).append(",").append(coords[0]); // 交换经纬度顺序
            }
        }
        return sb.toString();
    }
}
