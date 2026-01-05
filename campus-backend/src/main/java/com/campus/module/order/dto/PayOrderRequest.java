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
}
