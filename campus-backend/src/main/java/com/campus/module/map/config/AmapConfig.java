package com.campus.module.map.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {

    /**
     * 高德地图Web服务API Key
     */
    private String key;

    /**
     * 高德地图API密钥(用于签名，可选)
     */
    private String secretKey;

    /**
     * API基础URL
     */
    private String baseUrl = "https://restapi.amap.com";

    /**
     * 地理编码/逆地理编码接口
     */
    private String geocodeUrl = "/v3/geocode/geo";

    /**
     * 逆地理编码接口
     */
    private String regeoUrl = "/v3/geocode/regeo";

    /**
     * 路径规划-步行
     */
    private String walkingUrl = "/v3/direction/walking";

    /**
     * 路径规划-驾车
     */
    private String drivingUrl = "/v3/direction/driving";

    /**
     * 路径规划-公交
     */
    private String transitUrl = "/v3/direction/transit/integrated";

    /**
     * 路径规划-骑行
     */
    private String bicyclingUrl = "/v4/direction/bicycling";

    /**
     * 距离计算接口
     */
    private String distanceUrl = "/v3/distance";
}