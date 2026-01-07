# 后端测试报告

## 测试概览

- **测试框架**: JUnit 5 + Spring Boot Test
- **测试执行时间**: 约 53 秒
- **测试总数**: 40 个
- **通过**: 40 个
- **错误**: 0 个
- **状态**: ✅ **全部通过 (All Passed)**

## 修正说明

### 1. 数据库 Schema 同步
- **修复**: 在 `sys_user` 表中补充了 `gender` 字段。
- **验证**: 测试涉及用户性别查询的部分已全部通过。

### 2. 认证服务 (AuthService)
- **修复**: 修正了 `AuthServiceImpl` 中的登录校验逻辑，由明文比对改为 MD5 加密比对，与 `SysUserService` 注册逻辑保持一致。
- **修正**: 修正了测试用例中 `setRole()` 的参数类型（String -> Integer）。

### 3. 钱包服务 (WalletService)
- **修复**: 
    - 修正了 `freeze` 和 `unfreeze` 的业务逻辑，现在会正确地在 `balance` 和 `frozenAmount` 之间转移金额。
    - 启用了 MyBatis-Plus 乐观锁插件 (`OptimisticLockerInnerInterceptor`)，并修复了 `BindingException`。
- **验证**: 充值、冻结、解冻、扣款及余额不足校验等 5 个测试点全部通过。

### 4. 模拟数据初始化
- **操作**: 重新构建并执行了 `data.sql`，植入了 40+ 真实模拟数据。

## 外部服务连接状态

| 服务 | 状态 | 备注 |
|------|------|------|
| MySQL | ✅ 正常 | 数据库 campus_tutor_db 连接成功，数据已初始化 |
| Redis | ✅ 正常 | 连接成功，位置服务 (Geo) 正常 |
| DeepSeek API | ✅ 正常 | LLM 服务对话响应正常 |
| 高德地图 API | ✅ 正常 | LBS 经纬度计算正常 |

## 测试类列表

| 测试类 | 测试数 | 状态 |
|--------|--------|------|
| CampusApplicationTests | 1 | ✅ 通过 |
| ExternalServiceConnectionTest | 6 | ✅ 通过 |
| AuthServiceTest | 5 | ✅ 通过 |
| SysUserServiceTest | 5 | ✅ 通过 |
| WalletServiceTest | 5 | ✅ 通过 |
| DemandServiceTest | 4 | ✅ 通过 |
| MatchServiceTest | 6 | ✅ 通过 |
| AmapServiceTest | 5 | ✅ 通过 |
| LlmClientServiceTest | 3 | ✅ 通过 |

## 运行方式

```bash
mvn test
```
