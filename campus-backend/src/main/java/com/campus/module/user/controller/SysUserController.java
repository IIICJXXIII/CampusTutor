package com.campus.module.user.controller;

import com.campus.common.result.Result;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户 Controller
 */
@Tag(name = "用户管理", description = "用户信息相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "根据ID获取用户信息")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @Operation(summary = "根据用户名获取用户信息")
    @GetMapping("/username/{username}")
    public Result<SysUser> getByUsername(@Parameter(description = "用户名") @PathVariable String username) {
        SysUser user = sysUserService.getByUsername(username);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysUser user) {
        user.setPassword(null);
        user.setOpenid(null);
        boolean result = sysUserService.updateById(user);
        return Result.success(result);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态: 1-正常, 0-禁用") @RequestParam Integer status) {
        boolean result = sysUserService.updateStatus(id, status);
        return Result.success(result);
    }
}
