package com.campus.module.map.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯地图配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "tencent.map")
public class TencentMapConfig {

    /**
     * 腾讯地图API Key
     */
    private String key;

    /**
     * 腾讯地图API密钥(用于签名)
     */
    private String secretKey;

    /**
     * API基础URL
     */
    private String baseUrl = "https://apis.map.qq.com";

    /**
     * 逆地址解析接口
     */
    private String geocoderUrl = "/ws/geocoder/v1/";

    /**
     * 地址解析接口
     */
    private String addressUrl = "/ws/geocoder/v1/";

    /**
     * 路径规划-驾车
     */
    private String drivingUrl = "/ws/direction/v1/driving/";

    /**
     * 路径规划-步行
     */
    private String walkingUrl = "/ws/direction/v1/walking/";

    /**
     * 路径规划-公交
     */
    private String transitUrl = "/ws/direction/v1/transit/";

    /**
     * 距离计算接口
     */
    private String distanceUrl = "/ws/distance/v1/matrix/";
}
