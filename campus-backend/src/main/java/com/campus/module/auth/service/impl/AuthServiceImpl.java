package com.campus.module.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.campus.common.exception.BusinessException;
import com.campus.common.utils.JwtUtils;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.dto.RegisterRequest;
import com.campus.module.auth.dto.WxLoginRequest;
import com.campus.module.auth.dto.WxPhoneLoginRequest;
import com.campus.module.auth.service.AuthService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.service.SysWalletService;
import com.campus.service.WechatService;
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
    private final WechatService wechatService;
    
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
                          JwtUtils jwtUtils, WechatService wechatService) {
        this.sysUserService = sysUserService;
        this.sysWalletService = sysWalletService;
        this.jwtUtils = jwtUtils;
        this.wechatService = wechatService;
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
        String encryptPassword = cn.hutool.crypto.SecureUtil.md5(request.getPassword());
        if (!encryptPassword.equals(user.getPassword())) {
            throw new BusinessException(5004, "密码错误");
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
        // 密码加密存储 (MD5)
        user.setPassword(cn.hutool.crypto.SecureUtil.md5(request.getPassword()));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse wxLogin(WxLoginRequest request) {
        // 1. 调用微信API获取openid和session_key
        Map<String, String> wxResult = wechatService.code2Session(request.getCode());
        String openid = wxResult.get("openid");
        String sessionKey = wxResult.get("session_key");
        
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException("微信登录失败，无法获取用户标识");
        }
        
        // 2. 查询是否已注册
        SysUser user = sysUserService.getByOpenid(openid);
        
        if (user == null) {
            // 3. 新用户，需要注册
            if (StrUtil.isBlank(request.getEncryptedData()) || 
                StrUtil.isBlank(request.getIv())) {
                // 返回需要用户授权获取更多信息
                // 创建一个响应，表示需要绑定
                return LoginResponse.builder()
                        .token(null)
                        .userId(null)
                        .username(null)
                        .nickname(null)
                        .avatar(null)
                        .role(null)
                        .needBind(true)  // 添加needBind字段
                        .build();
            }
            
            // 解密用户信息
            Map<String, String> userInfo = wechatService.decryptUserInfo(
                request.getEncryptedData(), request.getIv(), sessionKey);
            
            // 创建新用户
            user = new SysUser();
            user.setOpenid(openid);
            user.setUsername("wx_" + openid.substring(0, 8));
            user.setNickname(userInfo.get("nickName"));
            user.setAvatarUrl(userInfo.get("avatarUrl")); // 注意：使用setAvatarUrl而不是setAvatar
            user.setGender(Integer.parseInt(userInfo.getOrDefault("gender", "0")));
            user.setRole(1); // 默认教师角色，后续可修改
            user.setStatus(1);
            sysUserService.save(user);
            
            // 初始化钱包
            SysWallet wallet = new SysWallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            sysWalletService.save(wallet);
        }
        
        // 4. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(5002, "账号已被禁用");
        }
        
        // 5. 生成JWT token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());
        
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl()) // 注意：使用getAvatarUrl而不是getAvatar
                .role(user.getRole())
                .needBind(false)  // 已绑定，不需要绑定
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse wxPhoneLogin(WxPhoneLoginRequest request) {
        log.info("开始微信手机号一键登录，loginCode: {}, phoneCode: {}", 
                request.getLoginCode().substring(0, Math.min(10, request.getLoginCode().length())) + "...",
                request.getPhoneCode().substring(0, Math.min(10, request.getPhoneCode().length())) + "...");

        // 1. 使用login_code获取openid
        Map<String, String> wxResult = wechatService.code2Session(request.getLoginCode());
        String openid = wxResult.get("openid");
        String sessionKey = wxResult.get("session_key");
        
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException("微信登录失败，无法获取用户标识");
        }
        
        // 2. 使用phone_code获取手机号
        String phoneNumber = wechatService.getPhoneNumber(request.getPhoneCode());
        if (StrUtil.isBlank(phoneNumber)) {
            throw new BusinessException("获取手机号失败");
        }
        
        // 3. 查询是否已注册（通过openid或手机号）
        SysUser user = sysUserService.getByOpenid(openid);
        if (user == null) {
            // 通过手机号查询
            user = sysUserService.getByUsername(phoneNumber);
        }
        
        if (user == null) {
            // 4. 新用户，自动注册
            log.info("新用户注册，openid: {}, phone: {}", openid, phoneNumber);
            
            user = new SysUser();
            user.setOpenid(openid);
            user.setUsername(phoneNumber); // 手机号作为用户名
            user.setNickname("微信用户_" + RandomUtil.randomNumbers(6));
            user.setRole(1); // 默认教师角色
            user.setStatus(1);
            sysUserService.save(user);
            
            // 初始化钱包
            SysWallet wallet = new SysWallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            sysWalletService.save(wallet);
            
            log.info("新用户注册成功，userId: {}", user.getId());
        } else {
            // 5. 老用户，更新openid（如果未绑定）
            // 注意：手机号已经存储在username字段中，不需要单独存储
            boolean updated = false;
            if (StrUtil.isBlank(user.getOpenid())) {
                user.setOpenid(openid);
                updated = true;
            }
            // 如果用户名不是手机号，可以更新用户名（可选）
            if (!phoneNumber.equals(user.getUsername())) {
                // 检查手机号是否已被其他用户使用
                SysUser existingUser = sysUserService.getByUsername(phoneNumber);
                if (existingUser == null || existingUser.getId().equals(user.getId())) {
                    user.setUsername(phoneNumber);
                    updated = true;
                }
            }
            if (updated) {
                sysUserService.updateById(user);
                log.info("用户信息更新成功，userId: {}", user.getId());
            }
        }
        
        // 6. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(5002, "账号已被禁用");
        }
        
        // 7. 生成JWT token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());
        
        // 8. 返回登录响应
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .role(user.getRole())
                .needBind(false)  // 一键登录不需要绑定
                .build();
    }
}
