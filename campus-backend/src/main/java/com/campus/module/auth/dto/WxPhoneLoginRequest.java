package com.campus.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信手机号一键登录请求DTO
 */
@Data
@Schema(description = "微信手机号一键登录请求")
public class WxPhoneLoginRequest {

    @NotBlank(message = "微信登录code不能为空")
    @Schema(description = "微信登录code（通过wx.login获取）", required = true, example = "081abc123def456")
    private String loginCode;

    @NotBlank(message = "微信手机号code不能为空")
    @Schema(description = "微信手机号code（通过getPhoneNumber获取）", required = true, example = "phone_code_123456")
    private String phoneCode;
}