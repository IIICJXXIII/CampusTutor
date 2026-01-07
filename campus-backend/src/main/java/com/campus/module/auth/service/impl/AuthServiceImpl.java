package com.campus.module.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final SysWalletService sysWalletService;
    private final JwtUtils jwtUtils;
    
    // Redis 可选注入
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    // 内存缓存作为 Redis 不可用时的后备方案（仅用于开发环境）
    private final Map<String, String> memoryCodeCache = new ConcurrentHashMap<>();

    /** 验证码 Redis Key 前缀 */
    private static final String CODE_PREFIX = "sms:code:";
    /** 验证码有效期 (分钟) */
    private static final long CODE_EXPIRE_MINUTES = 5;
    
    public AuthServiceImpl(SysUserService sysUserService, SysWalletService sysWalletService, JwtUtils jwtUtils) {
        this.sysUserService = sysUserService;
        this.sysWalletService = sysWalletService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据账号/手机号查询用户
        SysUser user = sysUserService.getByUsername(request.getAccount());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 根据登录方式校验
        if ("code".equals(request.getLoginType())) {
            // 验证码登录，仅支持手机号
            String phone = request.getAccount();
            if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException("验证码登录仅支持手机号");
            }
            if (!verifyCode(phone, request.getCode())) {
                throw new BusinessException("验证码错误或已过期");
            }
        } else {
            // 密码登录（账号或手机号），开发环境暂时使用明文比对
            if (!request.getPassword().equals(user.getPassword())) {
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
        // 开发环境暂存明文密码（上线前务必改为加密存储）
        user.setPassword(request.getPassword());
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

        String key = CODE_PREFIX + phone;
        
        // 优先使用 Redis，不可用时使用内存缓存
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis不可用，使用内存缓存存储验证码");
                memoryCodeCache.put(key, code);
            }
        } else {
            memoryCodeCache.put(key, code);
        }

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
        String cachedCode = null;
        
        // 优先使用 Redis，不可用时使用内存缓存
        if (stringRedisTemplate != null) {
            try {
                cachedCode = stringRedisTemplate.opsForValue().get(key);
                if (cachedCode != null && cachedCode.equals(code)) {
                    stringRedisTemplate.delete(key);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Redis不可用，使用内存缓存验证验证码");
                cachedCode = memoryCodeCache.get(key);
            }
        } else {
            cachedCode = memoryCodeCache.get(key);
        }

        if (cachedCode != null && cachedCode.equals(code)) {
            memoryCodeCache.remove(key);
            return true;
        }
        return false;
    }
}
