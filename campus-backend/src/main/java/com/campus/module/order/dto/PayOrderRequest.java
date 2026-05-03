package com.campus.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayOrderRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    private String payPassword;

    private BigDecimal lessonFee;

    private Integer lessonCount = 1;

    private String paymentMode = "per_lesson";

    private Long lessonId;
}
