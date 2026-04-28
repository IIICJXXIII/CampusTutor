package com.campus.module.auth;

import com.campus.common.exception.BusinessException;
import com.campus.module.auth.dto.LoginRequest;
import com.campus.module.auth.dto.LoginResponse;
import com.campus.module.auth.dto.RegisterRequest;
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

    @Test
    @Order(6)
    @DisplayName("6. 模拟数据登录测试 - 验证 data.sql 中的测试账号")
    void testMockDataLogin() {
        // 测试 data.sql 中的模拟账号
        // 密码: test123456 (明文) -> MD5: 47ec2dd791e31e2ef2076caf64ed9b3d
        
        String plainPassword = "test123456";
        String expectedMd5 = "47ec2dd791e31e2ef2076caf64ed9b3d";
        
        // 验证 MD5 加密结果
        String actualMd5 = cn.hutool.crypto.SecureUtil.md5(plainPassword);
        System.out.println("明文密码: " + plainPassword);
        System.out.println("预期MD5: " + expectedMd5);
        System.out.println("实际MD5: " + actualMd5);
        assertEquals(expectedMd5, actualMd5, "MD5加密结果应与data.sql中的一致");
        
        // 尝试用模拟数据中的教员账号登录 (id=101)
        String tutorAccount = "13800138101";
        SysUser tutorUser = userService.getByUsername(tutorAccount);
        
        if (tutorUser != null) {
            System.out.println("\n教员账号 " + tutorAccount + " 存在于数据库中");
            System.out.println("数据库中的密码(MD5): " + tutorUser.getPassword());
            
            LoginRequest request = new LoginRequest();
            request.setAccount(tutorAccount);
            request.setPassword(plainPassword); // 使用明文密码
            request.setLoginType("password");
            
            try {
                LoginResponse response = authService.login(request);
                assertNotNull(response.getToken(), "登录应成功并返回Token");
                System.out.println("✅ 教员账号登录成功! Token: " + response.getToken().substring(0, 50) + "...");
            } catch (BusinessException e) {
                System.out.println("❌ 登录失败: " + e.getMessage());
                fail("模拟数据账号应能成功登录: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ 教员账号 " + tutorAccount + " 不存在，可能data.sql未初始化");
        }
        
        // 尝试用模拟数据中的家长账号登录 (id=201)
        String parentAccount = "13900139201";
        SysUser parentUser = userService.getByUsername(parentAccount);
        
        if (parentUser != null) {
            System.out.println("\n家长账号 " + parentAccount + " 存在于数据库中");
            System.out.println("数据库中的密码(MD5): " + parentUser.getPassword());
            
            LoginRequest request = new LoginRequest();
            request.setAccount(parentAccount);
            request.setPassword(plainPassword);
            request.setLoginType("password");
            
            try {
                LoginResponse response = authService.login(request);
                assertNotNull(response.getToken(), "登录应成功并返回Token");
                System.out.println("✅ 家长账号登录成功! Token: " + response.getToken().substring(0, 50) + "...");
            } catch (BusinessException e) {
                System.out.println("❌ 登录失败: " + e.getMessage());
                fail("模拟数据账号应能成功登录: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ 家长账号 " + parentAccount + " 不存在，可能data.sql未初始化");
        }
        
        System.out.println("\n✅ 模拟数据登录测试完成");
    }

    @Test
    @Order(7)
    @DisplayName("7. 密码加密验证 - Debug辅助")
    void testPasswordEncryptionDebug() {
        // 这个测试用于调试密码加密问题
        System.out.println("=== 密码加密调试 ===");
        
        // 测试常见的密码
        String[] testPasswords = {"test123456", "123456", "password", "admin123"};
        
        for (String pwd : testPasswords) {
            String md5 = cn.hutool.crypto.SecureUtil.md5(pwd);
            System.out.println("密码: " + pwd + " -> MD5: " + md5);
        }
        
        System.out.println("\n=== data.sql 中使用的密码 ===");
        System.out.println("明文: test123456");
        System.out.println("MD5: 47ec2dd791e31e2ef2076caf64ed9b3d");
        
        // 验证
        String computed = cn.hutool.crypto.SecureUtil.md5("test123456");
        boolean matches = "47ec2dd791e31e2ef2076caf64ed9b3d".equals(computed);
        System.out.println("验证结果: " + (matches ? "✅ 匹配" : "❌ 不匹配"));
        
        assertTrue(matches, "MD5加密结果应匹配");
    }

    @Test
    @Order(8)
    @DisplayName("8. 注册后登录测试 - 验证密码加密存储")
    @Transactional
    void testRegisterThenLogin() {
        // 测试注册流程是否正确加密密码
        String testPhone = "13812345678";
        String testPassword = "mypassword123";
        
        // 1. 注册新用户 (使用万能验证码 123456)
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPhone(testPhone);
        registerRequest.setPassword(testPassword);
        registerRequest.setCode("123456"); // 万能验证码
        registerRequest.setNickname("测试用户");
        registerRequest.setRole(2); // 家长
        
        LoginResponse registerResponse = authService.register(registerRequest);
        assertNotNull(registerResponse, "注册响应不应为空");
        assertNotNull(registerResponse.getToken(), "注册后应返回Token");
        System.out.println("✅ 注册成功，用户ID: " + registerResponse.getUserId());
        
        // 2. 验证数据库中密码是加密存储的
        SysUser savedUser = userService.getByUsername(testPhone);
        assertNotNull(savedUser, "应能查询到新注册的用户");
        
        String expectedMd5 = cn.hutool.crypto.SecureUtil.md5(testPassword);
        System.out.println("输入密码: " + testPassword);
        System.out.println("预期MD5: " + expectedMd5);
        System.out.println("数据库存储: " + savedUser.getPassword());
        
        assertEquals(expectedMd5, savedUser.getPassword(), 
            "数据库中的密码应是MD5加密后的值，而不是明文");
        System.out.println("✅ 密码已正确加密存储");
        
        // 3. 使用注册的账号登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount(testPhone);
        loginRequest.setPassword(testPassword); // 使用明文密码登录
        loginRequest.setLoginType("password");
        
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            assertNotNull(loginResponse.getToken(), "登录应成功并返回Token");
            assertEquals(registerResponse.getUserId(), loginResponse.getUserId(), "用户ID应一致");
            System.out.println("✅ 注册后登录成功!");
        } catch (BusinessException e) {
            System.out.println("❌ 登录失败: " + e.getMessage());
            System.out.println("数据库密码: " + savedUser.getPassword());
            System.out.println("输入密码MD5: " + expectedMd5);
            fail("注册后应能使用相同密码登录: " + e.getMessage());
        }
        
        System.out.println("\n✅ 注册-登录流程测试完成");
    }
}
