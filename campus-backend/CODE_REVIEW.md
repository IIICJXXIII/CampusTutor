# 🔍 CampusTutor 后端代码审查报告

> **审查日期**: 2026-01-09  
> **项目版本**: 1.0.0  
> **技术栈**: Spring Boot 3.2.1 + MyBatis-Plus 3.5.5 + Redis + JWT

---

## 📋 审查概述

本次审查覆盖了 `campus-backend` 项目的全部核心代码，包括配置文件、通用模块、业务模块、安全认证等。整体代码质量良好，架构清晰，但存在一些需要改进的问题。

### 📊 审查评分

| 维度 | 评分 | 说明 |
|------|------|------|
| **代码结构** | ⭐⭐⭐⭐☆ | 模块划分清晰，遵循 DDD 分层思想 |
| **代码规范** | ⭐⭐⭐⭐☆ | 整体规范，少量不一致 |
| **安全性** | ⭐⭐⭐☆☆ | 存在敏感信息泄露风险 |
| **性能** | ⭐⭐⭐⭐☆ | 基本合理，部分可优化 |
| **可维护性** | ⭐⭐⭐⭐☆ | 代码可读性好 |

---

## 🚨 高优先级问题 (必须修复)

### 1. 敏感信息硬编码 ⚠️

**文件**: [application.properties](src/main/resources/application.properties)

**问题描述**: 
- 数据库密码、API Key、JWT Secret 等敏感信息直接硬编码在配置文件中
- 这些敏感信息会被提交到版本控制系统

**当前代码**:
```properties
spring.datasource.password=Sync743df
jwt.secret=CampusTutorSecretKey2026VeryLongSecretKeyForHS256Algorithm
baidu.ocr.api-key=gI099ZXdFhpgtTCFRh09oEhb
llm.api-key=sk-072b2ac3c89d4e69b7614fdffddfac95
amap.key=395b2de0b9cf263e585280d5d821821a4
```

**建议修复**:
```properties
# 使用环境变量
spring.datasource.password=${DB_PASSWORD:}
jwt.secret=${JWT_SECRET:}
baidu.ocr.api-key=${BAIDU_OCR_API_KEY:}
llm.api-key=${LLM_API_KEY:}
amap.key=${AMAP_KEY:}
```

---

### 2. 验证码安全漏洞 ⚠️

**文件**: [AuthServiceImpl.java](src/main/java/com/campus/module/auth/service/impl/AuthServiceImpl.java#L167-L171)

**问题描述**: 
- 存在万能验证码 `123456`，生产环境有严重安全风险
- 应通过环境变量控制是否启用测试模式

**当前代码**:
```java
@Override
public boolean verifyCode(String phone, String code) {
    // 开发环境: 允许使用万能验证码 123456
    if ("123456".equals(code)) {
        return true;
    }
    // ...
}
```

**建议修复**:
```java
@Value("${app.dev-mode:false}")
private boolean devMode;

@Override
public boolean verifyCode(String phone, String code) {
    // 仅开发模式允许万能验证码
    if (devMode && "123456".equals(code)) {
        log.warn("开发模式: 使用万能验证码登录 - phone={}", phone);
        return true;
    }
    // ...
}
```

---

### 3. 依赖注入不一致 ⚠️

**文件**: [AuthController.java](src/main/java/com/campus/module/auth/controller/AuthController.java#L24-L26)

**问题描述**: 
- 同时使用 `@RequiredArgsConstructor` 和 `@Autowired`，风格不一致
- 导入语句中有多余空格 (`com.campus.common.result .Result`)

**当前代码**:
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired  // <--- 1. 关键：加这个
    private AuthService authService;
```

**建议修复**:
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
```

---

### 4. 密码加密强度不足 ⚠️

**文件**: [AuthServiceImpl.java](src/main/java/com/campus/module/auth/service/impl/AuthServiceImpl.java#L77)

**问题描述**: 
- 使用 MD5 进行密码加密，已被证明不安全
- 缺少盐值 (salt)，容易被彩虹表攻击

**当前代码**:
```java
String encryptPassword = cn.hutool.crypto.SecureUtil.md5(request.getPassword());
```

**建议修复**:
```java
// 使用 BCrypt 或 Argon2
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

// 加密
String encryptPassword = passwordEncoder.encode(request.getPassword());

// 验证
if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new BusinessException(ResultCode.PASSWORD_ERROR);
}
```

---

## ⚡ 中优先级问题 (建议修复)

### 5. 用户信息未脱敏返回

**文件**: [SysUserController.java](src/main/java/com/campus/module/user/controller/SysUserController.java#L26-L31)

**问题描述**: 
- 直接修改实体对象进行脱敏，可能影响缓存或其他引用
- 应使用 DTO 返回脱敏后的数据

**当前代码**:
```java
@GetMapping("/current")
public Result<SysUser> getCurrentUser() {
    Long userId = UserContext.getUserId();
    SysUser user = sysUserService.getById(userId);
    if (user != null) {
        user.setPassword(null);  // 直接修改实体
        user.setOpenid(null);
    }
    return Result.success(user);
}
```

**建议修复**:
```java
@GetMapping("/current")
public Result<UserVO> getCurrentUser() {
    Long userId = UserContext.getUserId();
    SysUser user = sysUserService.getById(userId);
    return Result.success(UserVO.fromEntity(user)); // 使用 VO 返回
}
```

---

### 6. 缺少权限校验

**文件**: [SysUserController.java](src/main/java/com/campus/module/user/controller/SysUserController.java#L57-L63)

**问题描述**: 
- `updateStatus` 接口缺少管理员权限校验
- 任何登录用户都可以更改其他用户的状态

**当前代码**:
```java
@PutMapping("/{id}/status")
public Result<Boolean> updateStatus(
        @PathVariable Long id,
        @RequestParam Integer status) {
    boolean result = sysUserService.updateStatus(id, status);
    return Result.success(result);
}
```

**建议修复**:
```java
@PutMapping("/{id}/status")
@PreAuthorize("hasRole('ADMIN')") // 或自定义注解 @RequireAdmin
public Result<Boolean> updateStatus(
        @PathVariable Long id,
        @RequestParam Integer status) {
    // 添加权限校验逻辑
    if (UserContext.getRole() != 0) {
        throw new BusinessException(ResultCode.FORBIDDEN);
    }
    boolean result = sysUserService.updateStatus(id, status);
    return Result.success(result);
}
```

---

### 7. 钱包操作缺少并发控制

**文件**: [SysWalletServiceImpl.java](src/main/java/com/campus/module/wallet/service/impl/SysWalletServiceImpl.java#L37-L47)

**问题描述**: 
- 余额操作使用 `getById` + `updateById`，存在并发问题
- 高并发场景下可能导致余额计算错误

**当前代码**:
```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean freeze(Long userId, BigDecimal amount) {
    SysWallet wallet = getByUserId(userId);
    if (wallet.getBalance().compareTo(amount) < 0) {
        return false;
    }
    wallet.setBalance(wallet.getBalance().subtract(amount));
    wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
    return updateById(wallet);
}
```

**建议修复**:
```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean freeze(Long userId, BigDecimal amount) {
    // 使用乐观锁或悲观锁
    int affected = walletMapper.freezeAmount(userId, amount);
    if (affected == 0) {
        throw new BusinessException("余额不足或操作失败");
    }
    return true;
}

// Mapper 层使用原子更新 SQL:
// UPDATE sys_wallet 
// SET balance = balance - #{amount}, 
//     frozen_amount = frozen_amount + #{amount},
//     version = version + 1
// WHERE user_id = #{userId} 
//   AND balance >= #{amount}
//   AND version = #{version}
```

---

### 8. Redis 配置类未启用

**文件**: [RedisConfig.java](src/main/java/com/campus/config/RedisConfig.java#L16)

**问题描述**: 
- `@Configuration` 注解被注释掉，配置类未生效
- 使用默认的 JDK 序列化，可读性差且效率低

**当前代码**:
```java
//@Configuration  // 被注释
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisConfig {
```

**建议修复**:
```java
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {
```

---

### 9. CORS 配置过于宽松

**文件**: [CorsConfig.java](src/main/java/com/campus/config/CorsConfig.java#L16-L20)

**问题描述**: 
- 允许所有来源 (`*`)，生产环境存在安全风险
- 应限制为特定的前端域名

**当前代码**:
```java
config.addAllowedOriginPattern("*");
config.setAllowCredentials(true);
config.addAllowedMethod("*");
config.addAllowedHeader("*");
```

**建议修复**:
```java
@Value("${app.cors.allowed-origins:http://localhost:3000}")
private String[] allowedOrigins;

@Bean
public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    for (String origin : allowedOrigins) {
        config.addAllowedOrigin(origin);
    }
    config.setAllowCredentials(true);
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.addExposedHeader("Authorization");
    // ...
}
```

---

### 10. 文件上传未限制类型验证不完整

**文件**: [LocalFileServiceImpl.java](src/main/java/com/campus/module/file/service/impl/LocalFileServiceImpl.java#L85-L96)

**问题描述**: 
- `validateFile` 方法只校验了大小和空文件
- 缺少实际的 MIME 类型校验代码 (被截断)
- 应该校验文件魔数而非仅依赖 Content-Type

**建议修复**:
```java
private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
        throw new BusinessException("请选择要上传的文件");
    }
    if (file.getSize() > MAX_SIZE) {
        throw new BusinessException("文件大小不能超过 10MB");
    }
    // 校验 MIME 类型
    String contentType = file.getContentType();
    if (!ALLOWED_TYPES.contains(contentType)) {
        throw new BusinessException("不支持的文件类型: " + contentType);
    }
    // 校验文件扩展名
    String ext = FileUtil.extName(file.getOriginalFilename());
    if (!Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "pdf").contains(ext.toLowerCase())) {
        throw new BusinessException("不支持的文件扩展名: " + ext);
    }
}
```

---

## 💡 低优先级建议 (可选优化)

### 11. 日志级别配置

**文件**: [application.properties](src/main/resources/application.properties#L48)

**问题**: 生产环境不应使用 `debug` 级别日志

```properties
# 当前
logging.level.com.campus=debug

# 建议 (通过 profile 区分)
# application-dev.properties
logging.level.com.campus=debug

# application-prod.properties
logging.level.com.campus=info
```

---

### 12. MyBatis SQL 日志

**文件**: [application.properties](src/main/resources/application.properties#L23)

**问题**: 生产环境打印 SQL 影响性能

```properties
# 当前
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# 建议: 开发环境使用，生产环境禁用
# mybatis-plus.configuration.log-impl=
```

---

### 13. 缺少请求日志切面

**建议**: 添加统一的请求/响应日志记录

```java
@Aspect
@Component
@Slf4j
public class RequestLogAspect {
    
    @Around("execution(* com.campus.module..controller.*.*(..))")
    public Object logRequest(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        
        try {
            Object result = pjp.proceed();
            log.info("Request: {} - {}ms", method, System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("Request Error: {} - {}", method, e.getMessage());
            throw e;
        }
    }
}
```

---

### 14. DTO 验证规则不完整

**文件**: [LoginRequest.java](src/main/java/com/campus/module/auth/dto/LoginRequest.java)

**问题**: `password` 和 `code` 字段缺少条件校验

**建议**: 使用分组校验或自定义校验器
```java
@AssertTrue(message = "密码和验证码不能同时为空")
private boolean isPasswordOrCodePresent() {
    return (password != null && !password.isEmpty()) 
        || (code != null && !code.isEmpty());
}
```

---

### 15. 异常信息国际化

**文件**: 多处硬编码中文错误信息

**建议**: 使用 `MessageSource` 实现国际化

```java
// 当前
throw new BusinessException("验证码错误或已过期");

// 建议
throw new BusinessException(messageSource.getMessage("error.code.invalid", null, locale));
```

---

## ✅ 代码亮点

1. **清晰的模块划分**: 按业务领域划分 module，每个模块包含完整的 controller/service/mapper/entity 结构

2. **统一的响应格式**: `Result<T>` 类封装良好，支持泛型和多种静态工厂方法

3. **完善的异常处理**: `GlobalExceptionHandler` 覆盖了常见的异常类型

4. **良好的 API 文档**: 使用 Knife4j + Swagger 注解，接口文档清晰

5. **合理的事务管理**: 关键业务方法使用 `@Transactional` 注解

6. **Redis 降级策略**: 验证码存储支持 Redis 不可用时降级到内存

---

## 📝 修复清单

| 优先级 | 问题 | 状态 |
|--------|------|------|
| 🔴 高 | 敏感信息硬编码 | ⬜ 待修复 |
| 🔴 高 | 万能验证码漏洞 | ⬜ 待修复 |
| 🔴 高 | 依赖注入不一致 | ⬜ 待修复 |
| 🔴 高 | MD5 密码加密不安全 | ⬜ 待修复 |
| 🟡 中 | 用户信息未脱敏 | ⬜ 待修复 |
| 🟡 中 | 缺少权限校验 | ⬜ 待修复 |
| 🟡 中 | 钱包并发控制 | ⬜ 待修复 |
| 🟡 中 | Redis 配置未启用 | ⬜ 待修复 |
| 🟡 中 | CORS 配置过于宽松 | ⬜ 待修复 |
| 🟡 中 | 文件类型校验不完整 | ⬜ 待修复 |
| 🟢 低 | 日志级别配置 | ⬜ 可选 |
| 🟢 低 | SQL 日志生产环境 | ⬜ 可选 |
| 🟢 低 | 请求日志切面 | ⬜ 可选 |
| 🟢 低 | DTO 验证规则 | ⬜ 可选 |
| 🟢 低 | 异常信息国际化 | ⬜ 可选 |

---

## 🛠️ 下一步行动

1. **立即修复**: 高优先级的安全问题
2. **计划修复**: 中优先级问题在下个迭代完成
3. **长期优化**: 低优先级建议可作为技术债务跟踪

---

*审查人: GitHub Copilot*  
*审查日期: 2026-01-09*
