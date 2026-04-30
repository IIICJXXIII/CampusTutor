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
    private BigDecimal lessonFee;

    private Long lessonId;
}
