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
     * 搜索附近的教员(带距离)
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     * @param radiusKm 半径(公里)
     * @return Map<教员ID, 距离(公里)>
     */
    public java.util.Map<Long, Double> searchNearbyTutorsWithDistance(double longitude, double latitude, double radiusKm) {
        return searchNearbyWithDistance(TUTOR_GEO_KEY, longitude, latitude, radiusKm);
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
     * 搜索附近的需求(带距离)
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     * @param radiusKm 半径(公里)
     * @return Map<需求ID, 距离(公里)>
     */
    public java.util.Map<Long, Double> searchNearbyDemandsWithDistance(double longitude, double latitude, double radiusKm) {
        return searchNearbyWithDistance(DEMAND_GEO_KEY, longitude, latitude, radiusKm);
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
     * 通用附近搜索(带距离信息)
     * @return Map<ID, 距离(公里)>，按距离升序排列
     */
    private java.util.Map<Long, Double> searchNearbyWithDistance(String key, double longitude, double latitude, double radiusKm) {
        java.util.Map<Long, Double> result = new java.util.LinkedHashMap<>();
        
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，附近搜索返回空Map");
            return result;
        }
        
        try {
            Circle circle = new Circle(new Point(longitude, latitude), 
                    new Distance(radiusKm, Metrics.KILOMETERS));
            
            // 添加距离参数
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()
                    .sortAscending()
                    .limit(100);
            
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = 
                    redisTemplate.opsForGeo().radius(key, circle, args);
            
            if (geoResults != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoResults) {
                    String member = geoResult.getContent().getName();
                    Distance distance = geoResult.getDistance();
                    double distanceKm = distance != null ? distance.getValue() : 0.0;
                    result.put(Long.parseLong(member), distanceKm);
                }
            }
        } catch (Exception e) {
            log.warn("附近搜索(带距离)失败: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * 使用Haversine公式计算两点间距离(当Redis不可用时备用)
     * @param lon1 点1经度
     * @param lat1 点1纬度
     * @param lon2 点2经度
     * @param lat2 点2纬度
     * @return 距离(公里)
     */
    public double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        final double R = 6371.0; // 地球半径(公里)
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
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
