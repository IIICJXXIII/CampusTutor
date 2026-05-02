package com.campus.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "登录请求")
@Data
public class LoginRequest {

    @Schema(description = "账号/手机号", example = "13800138000")
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

}
