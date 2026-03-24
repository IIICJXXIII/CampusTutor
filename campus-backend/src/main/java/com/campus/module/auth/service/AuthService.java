package com.campus.module.auth.service;

import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.dto.RegisterRequest;
import com.campus.module.auth.dto.WxLoginRequest;
import com.campus.module.auth.dto.WxPhoneLoginRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应 (包含 Token)
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册成功后的登录响应
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 微信登录
     *
     * @param request 微信登录请求
     * @return 登录响应
     */
    LoginResponse wxLogin(WxLoginRequest request);

    /**
     * 微信手机号一键登录
     *
     * @param request 微信手机号登录请求
     * @return 登录响应
     */
    LoginResponse wxPhoneLogin(WxPhoneLoginRequest request);

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendCode(String phone);

    /**
     * 校验验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否校验通过
     */
    boolean verifyCode(String phone, String code);
}
