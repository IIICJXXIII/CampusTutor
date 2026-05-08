package com.campus.module.match.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教员搜索结果
 */
public class TutorSearchResult {

    /**
     * 教员档案ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名(脱敏)
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 学校名称
     */
    private String universityName;

    /**
     * 专业
     */
    private String major;

    /**
     * 学历
     */
    private Integer education;

    /**
     * 可授科目
     */
    private List<String> teachSubjects;

    /**
     * 可授年级
     */
    private List<String> teachGrades;

    /**
     * 教学风格
     */
    private String teachStyle;

    /**
     * 自我介绍
     */
    private String introduction;

    /**
     * 期望时薪
     */
    private BigDecimal expectPrice;

    /**
     * 可上门
     */
    private Integer canVisit;

    /**
     * 可网课
     */
    private Integer canOnline;

    /**
     * 综合评分
     */
    private BigDecimal rating;

    /**
     * 完成订单数
     */
    private Integer orderCount;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 距离(公里)
     */
    private Double distance;

    /**
     * 性别: 1-男, 2-女
     */
    private Integer gender;

    /**
     * 认证状态：0待提交 1待审核 2已通过 3已拒绝
     */
    private Integer certStatus;

    // 显式的getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getEducation() {
        return education;
    }

    public void setEducation(Integer education) {
        this.education = education;
    }

    public List<String> getTeachSubjects() {
        return teachSubjects;
    }

    public void setTeachSubjects(List<String> teachSubjects) {
        this.teachSubjects = teachSubjects;
    }

    public List<String> getTeachGrades() {
        return teachGrades;
    }

    public void setTeachGrades(List<String> teachGrades) {
        this.teachGrades = teachGrades;
    }

    public String getTeachStyle() {
        return teachStyle;
    }

    public void setTeachStyle(String teachStyle) {
        this.teachStyle = teachStyle;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public BigDecimal getExpectPrice() {
        return expectPrice;
    }

    public void setExpectPrice(BigDecimal expectPrice) {
        this.expectPrice = expectPrice;
    }

    public Integer getCanVisit() {
        return canVisit;
    }

    public void setCanVisit(Integer canVisit) {
        this.canVisit = canVisit;
    }

    public Integer getCanOnline() {
        return canOnline;
    }

    public void setCanOnline(Integer canOnline) {
        this.canOnline = canOnline;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(Integer certStatus) {
        this.certStatus = certStatus;
    }
}
