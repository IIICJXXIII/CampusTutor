package com.campus.module.user;

import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("用户服务测试")
class SysUserServiceTest {

    @Autowired
    private SysUserService sysUserService;

    private static final String TEST_USERNAME = "test_user_" + System.currentTimeMillis();

    @Test
    @Order(1)
    @DisplayName("1. 数据库连接测试")
    void testDatabaseConnection() {
        long count = sysUserService.count();
        System.out.println("✅ 数据库连接成功");
        System.out.println("当前用户总数: " + count);
        assertTrue(count >= 0, "用户数量应为非负数");
    }

    @Test
    @Order(2)
    @DisplayName("2. 用户注册测试")
    @Transactional
    void testRegisterUser() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setPassword("test123456");
        user.setNickname("测试用户");
        user.setRole(2);
        user.setStatus(1);
        user.setGender(1);

        boolean result = sysUserService.register(user);
        
        assertTrue(result, "用户注册应成功");
        assertNotNull(user.getId(), "注册后应分配用户ID");
        System.out.println("✅ 用户注册成功，ID: " + user.getId());
    }

    @Test
    @Order(3)
    @DisplayName("3. 用户查询测试")
    @Transactional
    void testQueryUser() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME + "_query");
        user.setPassword("test123456");
        user.setNickname("查询测试用户");
        user.setRole(1);
        user.setStatus(1);
        sysUserService.register(user);

        SysUser found = sysUserService.getByUsername(user.getUsername());
        
        assertNotNull(found, "应能查询到用户");
        assertEquals(user.getUsername(), found.getUsername(), "用户名应匹配");
        System.out.println("✅ 用户查询成功: " + found.getNickname());
    }

    @Test
    @Order(4)
    @DisplayName("4. 用户名存在检查")
    @Transactional
    void testUsernameExists() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME + "_exists");
        user.setPassword("test123456");
        user.setNickname("存在性测试");
        user.setRole(2);
        user.setStatus(1);
        sysUserService.register(user);

        boolean exists = sysUserService.existsByUsername(user.getUsername());
        assertTrue(exists, "已注册的用户名应存在");

        boolean notExists = sysUserService.existsByUsername("nonexistent_user_12345");
        assertFalse(notExists, "不存在的用户名应返回false");
        
        System.out.println("✅ 用户名存在性检查正常");
    }

    @Test
    @Order(5)
    @DisplayName("5. Gender字段测试")
    @Transactional
    void testGenderField() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME + "_gender");
        user.setPassword("test123456");
        user.setNickname("性别测试");
        user.setRole(1);
        user.setStatus(1);
        user.setGender(2);

        sysUserService.register(user);
        
        SysUser found = sysUserService.getById(user.getId());
        assertNotNull(found.getGender(), "性别字段不应为空");
        assertEquals(2, found.getGender(), "性别应为2(女)");
        
        System.out.println("✅ Gender字段存储正常");
    }
}
