package com.campus.module.recommend.dto;

import java.math.BigDecimal;

/**
 * 相似教员推荐 DTO
 */
public class SimilarTutorDTO {

    /**
     * 教员ID
     */
    private Long tutorId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名（脱敏）
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 大学名称
     */
    private String universityName;

    /**
     * 专业
     */
    private String major;

    /**
     * 期望价格
     */
    private BigDecimal expectPrice;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 教授科目（JSON）
     */
    private String teachSubjects;

    /**
     * 相似度分数 (0-1)
     */
    private Double similarityScore;

    /**
     * 共同交互用户数
     */
    private Integer coInteractionCount;

    // 显式的getter和setter方法
    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        this.tutorId = tutorId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public BigDecimal getExpectPrice() {
        return expectPrice;
    }

    public void setExpectPrice(BigDecimal expectPrice) {
        this.expectPrice = expectPrice;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getTeachSubjects() {
        return teachSubjects;
    }

    public void setTeachSubjects(String teachSubjects) {
        this.teachSubjects = teachSubjects;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Integer getCoInteractionCount() {
        return coInteractionCount;
    }

    public void setCoInteractionCount(Integer coInteractionCount) {
        this.coInteractionCount = coInteractionCount;
    }
}
