package com.campus.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求DTO
 */
@Data
@Schema(description = "微信登录请求")
public class WxLoginRequest {

    @NotBlank(message = "微信code不能为空")
    @Schema(description = "微信登录code", required = true, example = "081abc123def456")
    private String code;

    @Schema(description = "加密的用户数据", example = "encryptedData...")
    private String encryptedData;

    @Schema(description = "加密算法的初始向量", example = "iv...")
    private String iv;
}