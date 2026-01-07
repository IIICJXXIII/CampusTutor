package com.campus.module.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现请求DTO
 */
@Data
@Schema(description = "提现请求")
public class WithdrawRequest {

    @Schema(description = "提现金额", required = true, example = "100.00")
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "1.00", message = "提现金额不能小于1元")
    private BigDecimal amount;

    @Schema(description = "渠道: 1-微信, 2-支付宝, 3-银行卡", required = true, example = "1")
    @NotNull(message = "提现渠道不能为空")
    private Integer channel;

    @Schema(description = "收款账号", required = true, example = "example@alipay.com")
    @NotBlank(message = "收款账号不能为空")
    private String accountNo;

    @Schema(description = "支付密码", example = "123456")
    private String payPassword;
}
