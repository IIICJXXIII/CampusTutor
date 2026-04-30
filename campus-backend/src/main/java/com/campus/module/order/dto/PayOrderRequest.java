package com.campus.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单支付请求
 */
@Data
public class PayOrderRequest {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 支付方式：1钱包 2微信 3支付宝
     */
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    /**
     * 支付密码(钱包支付时需要)
     */
    private String payPassword;

    /**
     * 支付模式：full(全额) / per_lesson(按课时)
     */
    private String paymentMode;

    /**
     * 按课时支付的课时数(paymentMode=per_lesson时使用)
     */
    private Integer lessonCount;

    /**
     * 指定支付的课时记录ID(按课时支付时可选)
     */
    private Long lessonId;
}
