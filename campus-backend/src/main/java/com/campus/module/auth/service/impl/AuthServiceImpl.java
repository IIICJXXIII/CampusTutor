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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCrypt;

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
    
    public AuthServiceImpl(SysUserService sysUserService, SysWalletService sysWalletService,
            JwtUtils jwtUtils) {
        this.sysUserService = sysUserService;
        this.sysWalletService = sysWalletService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据账号/手机号查询用户
        SysUser user = sysUserService.getByUsername(request.getAccount());
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(5002, "账号已被禁用");
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
            // 密码登录（账号或手机号）
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
                throw new BusinessException(5004, "密码错误");
            }

            if (shouldUpgrade) {
                user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
                sysUserService.updateById(user);
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
                .needBind(false)
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
            throw new BusinessException(5003, "用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getPhone());
        // 密码加密存储 (BCrypt)
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
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
                .needBind(false)
                .build();
    }

    @Override
    public boolean sendCode(String phone) {
        // 防刷检查：1分钟内不能重复发送
        String rateLimitKey = "sms:rate:" + phone;
        if (stringRedisTemplate != null) {
            try {
                if (stringRedisTemplate.hasKey(rateLimitKey)) {
                    throw new BusinessException(400, "验证码发送过于频繁，请1分钟后再试");
                }
            } catch (Exception e) {
                // Redis不可用时忽略防刷检查
            }
        }

        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        String key = CODE_PREFIX + phone;
        
        // 优先使用 Redis，不可用时使用内存缓存
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                // 设置防刷限制
                stringRedisTemplate.opsForValue().set(rateLimitKey, "1", 1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis不可用，使用内存缓存存储验证码");
                memoryCodeCache.put(key, code);
            }
        } else {
            memoryCodeCache.put(key, code);
        }

        // 实际项目中接入短信服务商 API 发送短信
        // 这里只做 Mock，打印日志
        log.info("【Mock短信】手机号: {}, 验证码: {}, 有效期: {}分钟", phone, code, CODE_EXPIRE_MINUTES);

        // TODO: 集成真实的短信服务
        // 示例：阿里云短信服务调用
        /*
        try {
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam("{\"code\":\"" + code + "\"}");
            SendSmsResponse response = client.getAcsResponse(request);
            return "OK".equals(response.getCode());
        } catch (Exception e) {
            log.error("发送短信失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "发送短信失败，请稍后重试");
        }
        */

        return true;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
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
