package com.campus.module.demand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 需求帖子表
 */
@Data
@TableName("demand_post")
public class DemandPost {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 关联学生ID
     */
    private Long studentId;

    /**
     * 需求标题
     */
    private String title;

    /**
     * 需求科目
     */
    private String subject;

    /**
     * 年龄段（原年级字段，素质教育转型后改为可选）
     */
    private String grade;

    /**
     * 基础水平：零基础、有基础、考级/比赛冲刺
     */
    private String skillLevel;

    /**
     * 场地类型：1教员上门 2学员上门 3公共场馆
     */
    private Integer venueType;

    /**
     * 期望价格(元/小时)
     */
    private BigDecimal expectPrice;

    /**
     * 课时要求(JSON数组)
     */
    private String scheduleRequire;

    /**
     * 授课方式：1上门 2网课 3均可
     */
    private Integer teachMode;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 需求详情
     */
    private String detail;

    /**
     * 状态：0下架 1上架 2已匹配 3已完成
     */
    private Integer status;

    /**
     * 匹配的教员ID
     * 使用 ALWAYS 策略确保 updateById 时 null 值也能写入数据库
     * 注意: 使用 ALWAYS 策略确保 null 值能正确写入数据库，
     * 否则 MyBatis-Plus 默认 NOT_NULL 策略会跳过 null 字段，
     * 导致取消订单后需求无法正确重新上架。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long matchedTutorId;

    /**
     * 申请数量（非数据库字段，查询时动态计算）
     */
    @TableField(exist = false)
    private Integer applyCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
