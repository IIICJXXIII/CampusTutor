package com.campus.module.admin.controller;

import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtils;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "管理员认证", description = "管理员登录相关接口")
public class AdminAuthController {

    private final SysUserMapper sysUserMapper;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "使用用户名密码登录管理后台")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("管理员登录请求: {}", request.getAccount());

        // 查询用户
        SysUser user = sysUserMapper.selectByUsername(request.getAccount());

        if (user == null) {
            return Result.fail("用户名或密码错误");
        }

        // 验证密码 (MD5)
        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        // 检查是否为管理员角色 (role = 0 或 username = 'admin')
        if (user.getRole() != 0 && !"admin".equals(user.getUsername())) {
            return Result.fail("无管理员权限");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            return Result.fail("账号已被禁用");
        }

        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        // 构建响应 - 使用 Builder 模式
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .role(user.getRole())
                .build();

        log.info("管理员 {} 登录成功", user.getUsername());
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        // Token 无状态，前端清除即可
        return Result.success();
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前管理员信息")
    public Result<LoginResponse> getProfile() {
        // 从 Token 获取用户信息
        Long userId = com.campus.common.context.UserContext.getUserId();
        if (userId == null) {
            return Result.fail("未登录");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .role(user.getRole())
                .build();

        return Result.success(response);
    }
}
