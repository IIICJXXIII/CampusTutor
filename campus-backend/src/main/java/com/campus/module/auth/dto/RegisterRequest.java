package com.campus.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色: 1-教员, 2-家长", example = "2")
    @NotNull(message = "请选择角色")
    private Integer role;
}
