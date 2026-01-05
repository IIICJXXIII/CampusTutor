package com.campus.module.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.common.utils.JwtUtils;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.dto.RegisterRequest;
import com.campus.module.auth.service.AuthService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.service.SysWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final SysWalletService sysWalletService;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    /** 验证码 Redis Key 前缀 */
    private static final String CODE_PREFIX = "sms:code:";
    /** 验证码有效期 (分钟) */
    private static final long CODE_EXPIRE_MINUTES = 5;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据手机号查询用户
        SysUser user = sysUserService.getByUsername(request.getPhone());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 根据登录方式校验
        if ("code".equals(request.getLoginType())) {
            // 验证码登录
            if (!verifyCode(request.getPhone(), request.getCode())) {
                throw new BusinessException("验证码错误或已过期");
            }
        } else {
            // 密码登录
            String encryptPassword = SecureUtil.md5(request.getPassword());
            if (!encryptPassword.equals(user.getPassword())) {
                throw new BusinessException(ResultCode.PASSWORD_ERROR);
            }
        }

        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        // 校验验证码
        if (!verifyCode(request.getPhone(), request.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 检查手机号是否已注册
        if (sysUserService.existsByUsername(request.getPhone())) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getPhone());
        user.setPassword(SecureUtil.md5(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : "用户" + RandomUtil.randomNumbers(6));
        user.setRole(request.getRole());
        user.setStatus(1);
        sysUserService.save(user);

        // 初始化钱包
        SysWallet wallet = new SysWallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenAmount(BigDecimal.ZERO);
        sysWalletService.save(wallet);

        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    @Override
    public boolean sendCode(String phone) {
        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 存入 Redis，设置过期时间
        String key = CODE_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // TODO: 实际项目中接入短信服务商 API 发送短信
        // 这里只做 Mock，打印日志
        log.info("【Mock短信】手机号: {}, 验证码: {}, 有效期: {}分钟", phone, code, CODE_EXPIRE_MINUTES);

        return true;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        // 开发环境: 允许使用万能验证码 123456
        if ("123456".equals(code)) {
            return true;
        }

        String key = CODE_PREFIX + phone;
        String cachedCode = stringRedisTemplate.opsForValue().get(key);

        if (cachedCode != null && cachedCode.equals(code)) {
            // 验证成功后删除验证码
            stringRedisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
