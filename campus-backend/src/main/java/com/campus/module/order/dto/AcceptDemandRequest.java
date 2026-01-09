package com.campus.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教师接单请求
 */
@Data
public class AcceptDemandRequest {

    /**
     * 需求帖ID
     */
    @NotNull(message = "需求ID不能为空")
    private Long demandId;

    /**
     * 课时数(可选，默认10课时)
     */
    private Integer totalHours;

    /**
     * 备注
     */
    private String remark;
}
