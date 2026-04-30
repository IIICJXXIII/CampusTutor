package com.campus.module.auth.controller;

import com.campus.common.result.Result;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.dto.RegisterRequest;
import com.campus.module.auth.service.AuthService;
import com.campus.module.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录、注册相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserService sysUserService;

    @Operation(summary = "用户登录", description = "使用账号和密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    @Operation(summary = "重置密码", description = "通过旧密码重置密码")
    @PostMapping("/reset/password")
    public Result<Void> resetPassword(@RequestBody java.util.Map<String, String> params) {
        String phone = params.get("phone");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.fail(400, "手机号不能为空");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.fail(400, "旧密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.fail(400, "新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.fail(400, "新密码至少6位");
        }
        sysUserService.updatePasswordByPhone(phone, oldPassword, newPassword);
        return Result.success("密码重置成功");
    }
}
