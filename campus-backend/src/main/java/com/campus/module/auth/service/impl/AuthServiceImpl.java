package com.campus.module.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.campus.common.exception.BusinessException;
import com.campus.common.utils.JwtUtils;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.dto.RegisterRequest;
import com.campus.module.auth.service.AuthService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.service.SysWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final SysWalletService sysWalletService;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(SysUserService sysUserService, SysWalletService sysWalletService,
            JwtUtils jwtUtils) {
        this.sysUserService = sysUserService;
        this.sysWalletService = sysWalletService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserService.getByUsername(request.getAccount());
        if (user == null) {
            throw new BusinessException(5004, "账号或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(5002, "账号已被禁用");
        }

        String storedPassword = user.getPassword();
        boolean verified = false;
        boolean shouldUpgrade = false;

        if (StrUtil.isNotBlank(storedPassword) && storedPassword.startsWith("$2")) {
            verified = BCrypt.checkpw(request.getPassword(), storedPassword);
        } else {
            String md5Password = cn.hutool.crypto.SecureUtil.md5(request.getPassword());
            verified = md5Password.equals(storedPassword);
            shouldUpgrade = verified;
        }

        if (!verified) {
            throw new BusinessException(5004, "账号或密码错误");
        }

        if (shouldUpgrade) {
            user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            sysUserService.updateById(user);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .role(user.getRole())
                .needBind(false)
                .phone(user.getUsername())
                .longitude(user.getLongitude())
                .latitude(user.getLatitude())
                .address(user.getAddress())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        if (sysUserService.existsByUsername(request.getPhone())) {
            throw new BusinessException(5003, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getPhone());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : "用户" + RandomUtil.randomNumbers(6));
        user.setRole(request.getRole());
        user.setStatus(1);
        sysUserService.save(user);

        SysWallet wallet = new SysWallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenAmount(BigDecimal.ZERO);
        sysWalletService.save(wallet);

        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        log.info("[用户注册] 手机号: {}, 角色: {}, 用户ID: {}", request.getPhone(), request.getRole(), user.getId());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .needBind(false)
                .phone(user.getUsername())
                .build();
    }
}
