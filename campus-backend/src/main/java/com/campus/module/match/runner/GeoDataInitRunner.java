package com.campus.module.match.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot 启动后自动执行：同步 LBS 数据到 Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({ "dev", "test" }) // ⚠️ 核心：只在开发和测试环境生效
public class GeoDataInitRunner implements CommandLineRunner {

    private final TutorProfileMapper tutorProfileMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String TUTOR_GEO_KEY = "tutor:geo:locations";

    @Override
    public void run(String... args) {
        log.info("🚀 [系统启动] 开始全量同步教员 LBS 坐标到 Redis...");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 查询所有带坐标的教员
            LambdaQueryWrapper<TutorProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNotNull(TutorProfile::getLongitude)
                    .isNotNull(TutorProfile::getLatitude);
            List<TutorProfile> tutors = tutorProfileMapper.selectList(queryWrapper);

            if (tutors.isEmpty()) {
                log.warn("⚠️ 数据库中没有带坐标的教员数据。");
                return;
            }

            // 2. 构建 Redis GEO 写入列表
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(tutors.size());
            for (TutorProfile tutor : tutors) {
                Point point = new Point(tutor.getLongitude().doubleValue(), tutor.getLatitude().doubleValue());
                locations.add(new RedisGeoCommands.GeoLocation<>(tutor.getId().toString(), point));
            }

            // 3. 先清空旧数据，再批量写入新数据
            stringRedisTemplate.delete(TUTOR_GEO_KEY);
            stringRedisTemplate.opsForGeo().add(TUTOR_GEO_KEY, locations);

            log.info("✅ [同步完成] 成功将 {} 条教员坐标刷入 Redis! 耗时: {}ms", locations.size(),
                    (System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            log.error("❌ Redis GEO 同步失败!", e);
        }
    }
}