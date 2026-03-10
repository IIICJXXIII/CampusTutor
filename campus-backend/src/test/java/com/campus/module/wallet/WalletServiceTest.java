package com.campus.module.wallet;

import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.service.SysWalletService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 钱包服务测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("钱包服务测试")
class WalletServiceTest {

    @Autowired
    private SysWalletService walletService;

    @Autowired
    private SysUserService userService;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        SysUser user = new SysUser();
        user.setUsername("wallet_test_" + System.currentTimeMillis());
        user.setPassword("test123456");
        user.setNickname("钱包测试用户");
        user.setRole(2);
        user.setStatus(1);
        userService.register(user);
        testUserId = user.getId();
    }

    @Test
    @Order(1)
    @DisplayName("1. 创建钱包测试")
    @Transactional
    void testCreateWallet() {
        walletService.createWallet(testUserId);

        SysWallet wallet = walletService.getByUserId(testUserId);
        assertNotNull(wallet, "钱包应被创建");
        assertEquals(testUserId, wallet.getUserId(), "用户ID应匹配");

        System.out.println("✅ 钱包创建成功");
    }

    @Test
    @Order(2)
    @DisplayName("2. 充值测试")
    @Transactional
    void testRecharge() {
        walletService.createWallet(testUserId);

        BigDecimal amount = new BigDecimal("100.00");
        boolean result = walletService.recharge(testUserId, amount);

        assertTrue(result, "充值应成功");

        SysWallet wallet = walletService.getByUserId(testUserId);
        assertEquals(0, wallet.getBalance().compareTo(amount), "余额应为100");

        System.out.println("✅ 充值成功，当前余额: " + wallet.getBalance());
    }

    @Test
    @Order(3)
    @DisplayName("3. 冻结金额测试")
    @Transactional
    void testFreeze() {
        walletService.createWallet(testUserId);
        walletService.recharge(testUserId, new BigDecimal("200.00"));

        boolean result = walletService.freeze(testUserId, new BigDecimal("80.00"));

        assertTrue(result, "冻结应成功");

        SysWallet wallet = walletService.getByUserId(testUserId);
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("120.00")), "可用余额应为120");

        System.out.println("✅ 冻结成功");
    }

    @Test
    @Order(4)
    @DisplayName("4. 扣款测试")
    @Transactional
    void testDeduct() {
        walletService.createWallet(testUserId);
        walletService.recharge(testUserId, new BigDecimal("100.00"));

        boolean result = walletService.deduct(testUserId, new BigDecimal("30.00"));

        assertTrue(result, "扣款应成功");

        SysWallet wallet = walletService.getByUserId(testUserId);
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("70.00")), "余额应为70");

        System.out.println("✅ 扣款成功");
    }

    @Test
    @Order(5)
    @DisplayName("5. 余额不足冻结测试")
    @Transactional
    void testFreezeInsufficientBalance() {
        walletService.createWallet(testUserId);
        walletService.recharge(testUserId, new BigDecimal("50.00"));

        boolean result = walletService.freeze(testUserId, new BigDecimal("100.00"));

        assertFalse(result, "余额不足时冻结应失败");

        System.out.println("✅ 余额不足冻结正确拒绝");
    }
}
