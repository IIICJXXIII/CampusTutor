package com.campus.module.demand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 发布需求请求
 */
@Data
public class DemandPostRequest {

    /**
     * 需求ID(编辑时必填)
     */
    private Long id;

    /**
     * 关联学生ID
     */
    private Long studentId;

    @NotBlank(message = "需求标题不能为空")
    private String title;

    @NotBlank(message = "科目不能为空")
    private String subject;

    /**
     * 年龄段（原年级，素质教育转型后改为可选）
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
     * 期望价格
     */
    private BigDecimal expectPrice;

    /**
     * 课时要求
     */
    @Schema(description = "时间与各种杂项要求")
    private Object scheduleRequire;

    @NotNull(message = "授课方式不能为空")
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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
