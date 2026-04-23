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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "管理员认证", description = "管理员登录相关接口")
public class AdminAuthController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);

    private final SysUserMapper sysUserMapper;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "使用用户名密码登录管理后台")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("管理员登录请求: {}", request.getAccount());

        SysUser user = sysUserMapper.selectByUsername(request.getAccount());

        if (user == null) {
            return Result.fail("用户名或密码错误");
        }

        String storedPassword = user.getPassword();
        
        boolean verified;
        boolean shouldUpgrade = false;
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            verified = BCrypt.checkpw(request.getPassword(), storedPassword);
        } else {
            String md5Password = cn.hutool.crypto.SecureUtil.md5(request.getPassword());
            verified = md5Password.equals(storedPassword);
            shouldUpgrade = verified;
        }
        if (!verified) {
            log.warn("管理员密码验证失败, account={}", request.getAccount());
            return Result.fail("用户名或密码错误");
        }

        if (shouldUpgrade) {
            user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            sysUserMapper.updateById(user);
        }

        // 检查是否为管理员角色 (role = 0 或 username = 'admin')
        if (user.getRole() != 0 && !"admin".equals(user.getUsername())) {
            return Result.fail("无管理员权限");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            return Result.fail("账号已被禁用");
        }

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
