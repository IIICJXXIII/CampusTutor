package com.campus.module.match.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 教员流量池赛马机制配置
 * 基于 MVP 推荐架构方案 §4.2
 * 通过 application.properties 中 campus.traffic-pool.* 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "campus.traffic-pool")
public class TrafficPoolConfig {

    /** 是否启用流量池机制 */
    private boolean enabled = true;

    /** BASIC池加分（新教员曝光保护） */
    private double basicBoost = 5.0;

    /** WARM池加分（验证期教员） */
    private double warmBoost = 3.0;

    /** HOT池加分（热门教员额外加权） */
    private double hotBoost = 8.0;

    /** BASIC池观察期（天） */
    private int basicPeriodDays = 7;

    /** 详情页点击率（CTR）晋级基准线 */
    private double ctrThreshold = 0.05;

    /** 流量池级别缓存过期时间（秒），默认7天 */
    private int cacheExpireSeconds = 604800;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public double getBasicBoost() { return basicBoost; }
    public void setBasicBoost(double basicBoost) { this.basicBoost = basicBoost; }
    public double getWarmBoost() { return warmBoost; }
    public void setWarmBoost(double warmBoost) { this.warmBoost = warmBoost; }
    public double getHotBoost() { return hotBoost; }
    public void setHotBoost(double hotBoost) { this.hotBoost = hotBoost; }
    public int getBasicPeriodDays() { return basicPeriodDays; }
    public void setBasicPeriodDays(int basicPeriodDays) { this.basicPeriodDays = basicPeriodDays; }
    public int getCacheExpireSeconds() { return cacheExpireSeconds; }
    public void setCacheExpireSeconds(int cacheExpireSeconds) { this.cacheExpireSeconds = cacheExpireSeconds; }
}
