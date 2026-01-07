package com.campus.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "账号/手机号", example = "13800138000 或 admin01")
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "密码 (与验证码二选一)")
    private String password;

    @Schema(description = "验证码 (与密码二选一)", example = "123456")
    private String code;

    @Schema(description = "登录方式: password-密码登录, code-验证码登录", example = "password")
    private String loginType = "password";
}
