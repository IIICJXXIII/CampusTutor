package com.campus.module.auth;

import com.campus.common.exception.BusinessException;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.service.AuthService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试
 * 测试登录、JWT等功能
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private SysUserService userService;

    private static final String TEST_PHONE = "13800" + System.currentTimeMillis() % 1000000;
    private static final String TEST_PASSWORD = "test123456";

    @Test
    @Order(1)
    @DisplayName("1. 密码登录测试")
    @Transactional
    void testPasswordLogin() {
        // 先创建用户
        SysUser user = new SysUser();
        user.setUsername(TEST_PHONE + "_login");
        user.setPassword(TEST_PASSWORD);
        user.setNickname("登录测试");
        user.setRole(1);
        user.setStatus(1);
        userService.register(user);

        // 登录
        LoginRequest request = new LoginRequest();
        request.setAccount(user.getUsername());
        request.setPassword(TEST_PASSWORD);
        request.setLoginType("password");

        LoginResponse response = authService.login(request);
        
        assertNotNull(response, "登录响应不应为空");
        assertNotNull(response.getToken(), "应返回Token");
        assertEquals(user.getId(), response.getUserId(), "用户ID应匹配");
        
        System.out.println("✅ 密码登录成功");
    }

    @Test
    @Order(2)
    @DisplayName("2. 密码错误测试")
    @Transactional
    void testWrongPassword() {
        // 先创建用户
        SysUser user = new SysUser();
        user.setUsername(TEST_PHONE + "_wrong");
        user.setPassword(TEST_PASSWORD);
        user.setNickname("密码测试");
        user.setRole(2);
        user.setStatus(1);
        userService.register(user);

        // 使用错误密码登录
        LoginRequest request = new LoginRequest();
        request.setAccount(user.getUsername());
        request.setPassword("wrong_password");
        request.setLoginType("password");

        assertThrows(BusinessException.class, () -> {
            authService.login(request);
        }, "错误密码应抛出异常");
        
        System.out.println("✅ 密码错误正确拒绝");
    }

    @Test
    @Order(3)
    @DisplayName("3. 用户不存在测试")
    void testUserNotExist() {
        LoginRequest request = new LoginRequest();
        request.setAccount("nonexistent_user_12345");
        request.setPassword(TEST_PASSWORD);
        request.setLoginType("password");

        assertThrows(BusinessException.class, () -> {
            authService.login(request);
        }, "不存在的用户应抛出异常");
        
        System.out.println("✅ 不存在用户正确拒绝");
    }

    @Test
    @Order(4)
    @DisplayName("4. 禁用用户登录测试")
    @Transactional
    void testDisabledUserLogin() {
        // 创建禁用用户
        SysUser user = new SysUser();
        user.setUsername(TEST_PHONE + "_disabled");
        user.setPassword(TEST_PASSWORD);
        user.setNickname("禁用测试");
        user.setRole(2);
        user.setStatus(0); // 禁用状态
        userService.register(user);

        LoginRequest request = new LoginRequest();
        request.setAccount(user.getUsername());
        request.setPassword(TEST_PASSWORD);
        request.setLoginType("password");

        assertThrows(BusinessException.class, () -> {
            authService.login(request);
        }, "禁用用户应无法登录");
        
        System.out.println("✅ 禁用用户正确拒绝登录");
    }

    @Test
    @Order(5)
    @DisplayName("5. JWT Token格式验证")
    @Transactional
    void testTokenFormat() {
        // 创建用户并登录获取Token
        SysUser user = new SysUser();
        user.setUsername(TEST_PHONE + "_jwt");
        user.setPassword(TEST_PASSWORD);
        user.setNickname("JWT测试");
        user.setRole(1);
        user.setStatus(1);
        userService.register(user);

        LoginRequest request = new LoginRequest();
        request.setAccount(user.getUsername());
        request.setPassword(TEST_PASSWORD);
        request.setLoginType("password");
        
        LoginResponse response = authService.login(request);
        
        String token = response.getToken();
        assertNotNull(token, "Token不应为空");
        
        // JWT格式: header.payload.signature
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT应由三部分组成");
        
        System.out.println("✅ JWT Token格式正确");
        System.out.println("Token长度: " + token.length());
    }
}
