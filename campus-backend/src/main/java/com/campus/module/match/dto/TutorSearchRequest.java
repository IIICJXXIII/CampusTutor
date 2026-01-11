package com.campus.module.match.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教员搜索请求
 */
@Data
public class TutorSearchRequest {

    /**
     * 当前用户ID（用于动态权重计算）
     */
    private Long userId;

    /**
     * 科目筛选
     */
    private String subject;

    /**
     * 年级筛选
     */
    private String grade;

    /**
     * 最低时薪
     */
    private BigDecimal minPrice;

    /**
     * 最高时薪
     */
    private BigDecimal maxPrice;

    /**
     * 授课方式：1上门 2网课
     */
    private Integer teachMode;

    /**
     * 学历筛选
     */
    private List<Integer> educations;

    /**
     * 性别筛选
     */
    private Integer gender;

    /**
     * 中心点经度(LBS搜索)
     */
    private Double longitude;

    /**
     * 中心点纬度(LBS搜索)
     */
    private Double latitude;

    /**
     * 搜索半径(公里)
     */
    private Double radius;

    /**
     * 排序方式：distance距离 rating评分 price价格
     */
    private String sortBy;

    /**
     * 排序顺序：asc升序 desc降序
     */
    private String sortOrder;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;
}
