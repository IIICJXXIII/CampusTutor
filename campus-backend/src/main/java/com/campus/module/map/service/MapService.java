package com.campus.module.map.service;

import com.campus.module.map.dto.DirectionResult;
import com.campus.module.map.dto.DistanceResult;
import com.campus.module.map.dto.GeocoderResult;

/**
 * 地图服务通用接口
 * 定义所有地图提供商必须实现的方法
 */
public interface MapService {
    /**
     * 逆地址解析 - 根据经纬度获取地址
     */
    GeocoderResult reverseGeocode(double latitude, double longitude);

    /**
     * 地址解析 - 根据地址获取经纬度
     */
    GeocoderResult geocode(String address);

    /**
     * 路径规划 - 步行
     */
    DirectionResult walkingDirection(double fromLat, double fromLng, double toLat, double toLng);

    /**
     * 路径规划 - 驾车
     */
    DirectionResult drivingDirection(double fromLat, double fromLng, double toLat, double toLng);

    /**
     * 路径规划 - 公交
     */
    DirectionResult transitDirection(double fromLat, double fromLng, double toLat, double toLng);

    /**
     * 计算距离
     */
    DistanceResult calculateDistance(double fromLat, double fromLng, double toLat, double toLng, String mode);
}