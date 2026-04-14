package com.campus.module.auth.controller;

import com.campus.common.result.Result;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.dto.RegisterRequest;
import com.campus.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.campus.common.exception.BusinessException;
import com.campus.module.user.service.SysUserService;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "登录、注册、验证码相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserService sysUserService;

    @Operation(summary = "用户登录", description = "支持密码登录和验证码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "用户注册", description = "注册新用户，需要验证码")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    @Operation(summary = "发送验证码", description = "发送短信验证码 (Mock)")
    @PostMapping("/send-code")
    public Result<Void> sendCode(
            @Parameter(description = "手机号") @RequestParam String phone) {
        authService.sendCode(phone);
        return Result.success("验证码已发送");
    }

    @Operation(summary = "密码找回 - 发送验证码", description = "用于密码找回的验证码发送")
    @PostMapping("/reset/send-code")
    public Result<Void> sendResetCode(
            @Parameter(description = "手机号") @RequestParam String phone) {
        // 检查手机号是否已注册
        if (!sysUserService.existsByUsername(phone)) {
            throw new BusinessException(5001, "用户不存在");
        }
        authService.sendCode(phone);
        return Result.success("验证码已发送");
    }

    @Operation(summary = "密码找回 - 重置密码", description = "使用验证码重置密码")
    @PostMapping("/reset/password")
    public Result<Void> resetPassword(
            @Parameter(description = "手机号") @RequestParam String phone,
            @Parameter(description = "验证码") @RequestParam String code,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        // 验证验证码
        if (!authService.verifyCode(phone, code)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
        // 重置密码
        sysUserService.resetPassword(phone, newPassword);
        return Result.success("密码重置成功");
    }

}
