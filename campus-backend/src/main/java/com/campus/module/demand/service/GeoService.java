package com.campus.module.demand.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 地理位置服务(基于Redis GEO)
 * Redis 不可用时会返回空结果，不影响主业务
 */
@Slf4j
@Service
public class GeoService {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
     * 教员位置Key
     */
    private static final String TUTOR_GEO_KEY = "geo:tutor";

    /**
     * 需求位置Key
     */
    private static final String DEMAND_GEO_KEY = "geo:demand";
    
    /**
     * 检查 Redis 是否可用
     */
    private boolean isRedisAvailable() {
        return redisTemplate != null;
    }

    /**
     * 添加教员位置
     * @param tutorId 教员ID
     * @param longitude 经度
     * @param latitude 纬度
     */
    public void addTutorLocation(Long tutorId, double longitude, double latitude) {
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，跳过添加教员位置");
            return;
        }
        try {
            redisTemplate.opsForGeo().add(TUTOR_GEO_KEY, 
                    new Point(longitude, latitude), tutorId.toString());
        } catch (Exception e) {
            log.warn("添加教员位置失败: {}", e.getMessage());
        }
    }

    /**
     * 添加需求位置
     * @param demandId 需求ID
     * @param longitude 经度
     * @param latitude 纬度
     */
    public void addDemandLocation(Long demandId, double longitude, double latitude) {
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，跳过添加需求位置");
            return;
        }
        try {
            redisTemplate.opsForGeo().add(DEMAND_GEO_KEY, 
                    new Point(longitude, latitude), demandId.toString());
        } catch (Exception e) {
            log.warn("添加需求位置失败: {}", e.getMessage());
        }
    }

    /**
     * 移除教员位置
     * @param tutorId 教员ID
     */
    public void removeTutorLocation(Long tutorId) {
        if (!isRedisAvailable()) return;
        try {
            redisTemplate.opsForGeo().remove(TUTOR_GEO_KEY, tutorId.toString());
        } catch (Exception e) {
            log.warn("移除教员位置失败: {}", e.getMessage());
        }
    }

    /**
     * 移除需求位置
     * @param demandId 需求ID
     */
    public void removeDemandLocation(Long demandId) {
        if (!isRedisAvailable()) return;
        try {
            redisTemplate.opsForGeo().remove(DEMAND_GEO_KEY, demandId.toString());
        } catch (Exception e) {
            log.warn("移除需求位置失败: {}", e.getMessage());
        }
    }

    /**
     * 搜索附近的教员
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     * @param radiusKm 半径(公里)
     * @return 教员ID列表(按距离排序)
     */
    public List<Long> searchNearbyTutors(double longitude, double latitude, double radiusKm) {
        return searchNearby(TUTOR_GEO_KEY, longitude, latitude, radiusKm);
    }

    /**
     * 搜索附近的需求
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     * @param radiusKm 半径(公里)
     * @return 需求ID列表(按距离排序)
     */
    public List<Long> searchNearbyDemands(double longitude, double latitude, double radiusKm) {
        return searchNearby(DEMAND_GEO_KEY, longitude, latitude, radiusKm);
    }

    /**
     * 通用附近搜索
     */
    private List<Long> searchNearby(String key, double longitude, double latitude, double radiusKm) {
        List<Long> result = new ArrayList<>();
        
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，附近搜索返回空列表");
            return result;
        }
        
        try {
            Circle circle = new Circle(new Point(longitude, latitude), 
                    new Distance(radiusKm, Metrics.KILOMETERS));
            
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .sortAscending()
                    .limit(100);
            
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = 
                    redisTemplate.opsForGeo().radius(key, circle, args);
            
            if (geoResults != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoResults) {
                    String member = geoResult.getContent().getName();
                    result.add(Long.parseLong(member));
                }
            }
        } catch (Exception e) {
            log.warn("附近搜索失败: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * 计算两点间距离(公里)
     * @param key GEO key
     * @param member1 成员1
     * @param member2 成员2
     * @return 距离(公里)
     */
    public Double getDistance(String key, String member1, String member2) {
        if (!isRedisAvailable()) return null;
        try {
            Distance distance = redisTemplate.opsForGeo().distance(key, member1, member2, Metrics.KILOMETERS);
            return distance != null ? distance.getValue() : null;
        } catch (Exception e) {
            log.warn("计算距离失败: {}", e.getMessage());
            return null;
        }
    }
}
