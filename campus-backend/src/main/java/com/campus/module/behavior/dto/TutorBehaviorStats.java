package com.campus.module.behavior.dto;

import lombok.Data;

/**
 * 教员行为统计 DTO
 * 用于匹配算法计算热度分
 */
@Data
public class TutorBehaviorStats {

    /**
     * 教员ID
     */
    private Long tutorId;

    /**
     * 24小时内被查看次数
     */
    private Integer viewCount24h = 0;

    /**
     * 24小时内聊天发起数
     */
    private Integer chatCount24h = 0;

    /**
     * 总收藏数
     */
    private Integer favoriteCount = 0;

    /**
     * 点击率 = 被点击次数 / 被曝光次数
     */
    private Double clickThroughRate = 0.0;

    /**
     * 聊天率 = 聊天发起数 / 被点击次数
     */
    private Double chatRate = 0.0;

    /**
     * 平均停留时长(秒)
     */
    private Double avgViewDuration = 0.0;

    /**
     * 综合热度分(0-100)
     */
    private Double hotnessScore = 0.0;

    // 显式的getter和setter方法
    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        this.tutorId = tutorId;
    }

    public Integer getViewCount24h() {
        return viewCount24h;
    }

    public void setViewCount24h(Integer viewCount24h) {
        this.viewCount24h = viewCount24h;
    }

    public Integer getChatCount24h() {
        return chatCount24h;
    }

    public void setChatCount24h(Integer chatCount24h) {
        this.chatCount24h = chatCount24h;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Double getChatRate() {
        return chatRate;
    }

    public void setChatRate(Double chatRate) {
        this.chatRate = chatRate;
    }

    public Double getAvgViewDuration() {
        return avgViewDuration;
    }

    public void setAvgViewDuration(Double avgViewDuration) {
        this.avgViewDuration = avgViewDuration;
    }

    public Double getHotnessScore() {
        return hotnessScore;
    }

    public void setHotnessScore(Double hotnessScore) {
        this.hotnessScore = hotnessScore;
    }
}
