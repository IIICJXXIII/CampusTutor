package com.campus.module.match.dto;

/**
 * 教员流量池级别枚举
 *
 * 基于 MVP 推荐架构方案 §4.2 的阶梯式流量爬升体系：
 * - BASIC：基础冷启流量池（新教员，7天观察期内给予优先曝光）
 * - WARM：温水放大流量池（CTR达标或获得首次私信，扩大曝光范围）
 * - HOT：热门核心流量池（多次成交+高好评，"明星教员"顶级权重）
 */
public enum TrafficPoolLevel {

    /**
     * 基础冷启流量池 (Level 1 - 探索期)
     * 触发: 新教员完成注册认证
     * 策略: 7天内给予200-500次优先曝光，精准定向LBS范围
     * 考核: CTR是否达到基准线(5%)
     */
    BASIC("基础池", "新晋教员"),

    /**
     * 温水放大流量池 (Level 2 - 验证期)
     * 触发: CTR达标或获得首次有效私信
     * 策略: 扩大曝光范围，类目聚合页更高基准排序
     * 考核: 私信转化率(CVR)与试听成功率
     */
    WARM("验证池", "潜力教员"),

    /**
     * 热门核心流量池 (Level 3 - 爆发期)
     * 触发: 多次实质交易+正向评价
     * 策略: "明星教员"标识，首页兜底推荐
     * 退出: 数据停滞/拒单过多/评分下降时降级
     */
    HOT("热门池", "明星教员⭐");

    private final String displayName;
    private final String tag;

    TrafficPoolLevel(String displayName, String tag) {
        this.displayName = displayName;
        this.tag = tag;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTag() {
        return tag;
    }
}
