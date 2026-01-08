package com.campus.config;

import com.campus.module.demand.service.GeoService;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GEO数据初始化器
 * 应用启动时将已认证教员的位置信息同步到Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeoDataInitializer implements CommandLineRunner {

    private final TutorProfileMapper tutorProfileMapper;
    private final GeoService geoService;

    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化教员GEO位置数据...");
            
            // 查询所有已认证且有位置信息的教员
            List<TutorProfile> tutors = tutorProfileMapper.selectList(null);
            
            int count = 0;
            for (TutorProfile tutor : tutors) {
                // 只同步已认证且有位置信息的教员
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
}
