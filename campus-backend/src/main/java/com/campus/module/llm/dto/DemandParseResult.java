package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 需求解析结果
 */
@Data
@Schema(description = "需求解析结果")
public class DemandParseResult {

    /**
     * 解析是否成功
     */
    @Schema(description = "解析是否成功")
    private Boolean success;

    /**
     * 原始输入文本
     */
    @Schema(description = "原始输入文本")
    private String originalText;

    /**
     * 解析出的科目
     */
    @Schema(description = "科目", example = "数学")
    private String subject;

    /**
     * 解析出的年级
     */
    @Schema(description = "年级", example = "初二")
    private String grade;

    /**
     * 解析出的预算价格(元/小时)
     */
    @Schema(description = "预算价格", example = "150")
    private BigDecimal expectPrice;

    /**
     * 解析出的授课方式: 1上门 2网课 3均可
     */
    @Schema(description = "授课方式: 1上门 2网课 3均可", example = "1")
    private Integer teachMode;

    /**
     * 解析出的性别偏好: 1男 2女 null不限
     */
    @Schema(description = "教员性别偏好: 1男 2女", example = "2")
    private Integer preferGender;

    /**
     * 解析出的学历要求
     */
    @Schema(description = "学历要求列表")
    private List<Integer> educations;

    /**
     * 解析出的时间要求
     */
    @Schema(description = "时间要求", example = "周末上午")
    private String scheduleRequire;

    /**
     * 解析出的地址信息
     */
    @Schema(description = "地址信息", example = "北京市海淀区中关村")
    private String address;

    /**
     * 解析出的需求详情
     */
    @Schema(description = "需求详情")
    private String detail;

    /**
     * 解析置信度(0-1)
     */
    @Schema(description = "解析置信度", example = "0.85")
    private Double confidence;

    /**
     * 补充说明/建议
     */
    @Schema(description = "补充说明")
    private String suggestion;
}
