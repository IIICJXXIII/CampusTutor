package com.campus.config;

import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.demand.service.GeoService;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GEO数据初始化器
 * 应用启动时将已认证教员和上架需求的位置信息同步到Redis
 * 先清空旧数据再全量写入，避免历史残留数据污染搜索结果
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeoDataInitializer implements CommandLineRunner {

    private final TutorProfileMapper tutorProfileMapper;
    private final DemandPostMapper demandPostMapper;
    private final GeoService geoService;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private static final String TUTOR_GEO_KEY = "geo:tutor";
    private static final String DEMAND_GEO_KEY = "geo:demand";

    @Override
    public void run(String... args) {
        if (!isRedisAvailable()) {
            log.warn("Redis不可用，跳过GEO位置数据初始化（不影响主业务，将降级使用数据库距离计算）");
            return;
        }
        initTutorGeoData();
        initDemandGeoData();
    }

    /**
     * 检查 Redis 是否真正可用
     */
    private boolean isRedisAvailable() {
        if (stringRedisTemplate == null) return false;
        try {
            stringRedisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 初始化教员GEO位置数据
     */
    private void initTutorGeoData() {
        try {
            log.info("开始初始化教员GEO位置数据...");

            // 先清空旧数据，避免历史残留
            if (stringRedisTemplate != null) {
                stringRedisTemplate.delete(TUTOR_GEO_KEY);
                log.info("已清空教员GEO旧数据");
            }
            
            List<TutorProfile> tutors = tutorProfileMapper.selectList(null);
            
            int count = 0;
            for (TutorProfile tutor : tutors) {
                if (tutor.getCertStatus() != null && tutor.getCertStatus() == 2 
                        && tutor.getLongitude() != null && tutor.getLatitude() != null) {
                    geoService.addTutorLocation(
                            tutor.getId(), 
                            tutor.getLongitude().doubleValue(), 
                            tutor.getLatitude().doubleValue()
                    );
                    count++;
                }
            }
            
            log.info("教员GEO位置数据初始化完成，共同步{}条记录", count);
        } catch (Exception e) {
            log.warn("教员GEO位置数据初始化失败（Redis可能不可用）: {}", e.getMessage());
        }
    }

    /**
     * 初始化需求GEO位置数据
     */
    private void initDemandGeoData() {
        try {
            log.info("开始初始化需求GEO位置数据...");

            // 先清空旧数据，避免历史残留
            if (stringRedisTemplate != null) {
                stringRedisTemplate.delete(DEMAND_GEO_KEY);
                log.info("已清空需求GEO旧数据");
            }
            
            // 查询所有上架且有位置信息的需求 (status=1表示上架)
            LambdaQueryWrapper<DemandPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DemandPost::getStatus, 1)
                   .isNotNull(DemandPost::getLongitude)
                   .isNotNull(DemandPost::getLatitude);
            List<DemandPost> demands = demandPostMapper.selectList(wrapper);
            
            int count = 0;
            for (DemandPost demand : demands) {
                geoService.addDemandLocation(
                        demand.getId(), 
                        demand.getLongitude().doubleValue(), 
                        demand.getLatitude().doubleValue()
                );
                count++;
            }
            
            log.info("需求GEO位置数据初始化完成，共同步{}条记录", count);
        } catch (Exception e) {
            log.warn("需求GEO位置数据初始化失败（Redis可能不可用）: {}", e.getMessage());
        }
    }
}

