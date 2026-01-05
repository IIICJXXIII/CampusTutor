# CampusTutor API 接口文档指南

本文档旨在为前端开发人员（及 AI 辅助开发 Agent）提供后端接口的访问指南和集成说明。

## 1. 接口概览

- **后端服务地址**: `http://localhost:8080`
- **在线接口文档 (Knife4j)**: [http://localhost:8080/doc.html](http://localhost:8080/doc.html)
- **OpenAPI 规范 (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **提示**: 前端开发 Agent 可以直接读取 OpenAPI JSON 来自动生成 TypeScript 类型定义和 API 请求代码。

## 2. 认证机制

本系统使用 JWT (JSON Web Token) 进行身份验证。

- **登录接口**: `/api/auth/login`
- **Token 获取**: 登录成功后，响应数据中包含 `token` 字段。
- **请求头设置**:
  所有受保护的接口需要在 HTTP 请求头中携带 Token：
  ```http
  Authorization: Bearer <your_token_here>
  ```

## 3. 统一响应格式

所有 API 接口均返回统一的 JSON 格式：

```typescript
interface ApiResponse<T> {
  code: number;      // 状态码，200 表示成功
  msg: string;       // 提示信息
  data: T;           // 业务数据
  timestamp: number; // 响应时间戳
}
```

### 常用状态码
- `200`: 成功
- `401`: 未授权（未登录或 Token 过期）
- `403`: 禁止访问（权限不足）
- `500`: 服务器内部错误

## 4. 核心模块路由

| 模块名称 | 基础路径 | 描述 |
| :--- | :--- | :--- |
| **认证模块** | `/api/auth` | 登录、注册、验证码 |
| **用户模块** | `/api/user` | 用户信息管理 |
| **家教模块** | `/api/tutor` | 家教资料、日程设置 |
| **家长模块** | `/api/parent` | 学生管理 |
| **需求模块** | `/api/demand` | 需求发布、LBS 附近需求 |
| **匹配模块** | `/api/match` | 家教搜索、筛选 |
| **订单模块** | `/api/order` | 课程订单、支付 |
| **钱包模块** | `/api/wallet` | 余额、充值、提现 |
| **文件模块** | `/api/file` | 图片/文件上传 |

## 5. 快速集成指南 (For Frontend Agent)

如果你是辅助前端开发的 AI Agent，请遵循以下步骤：

1.  **获取类型定义**: 访问 `http://localhost:8080/v3/api-docs` 获取完整的 Schema 定义。
2.  **生成请求库**: 推荐使用 `axios` 或 `fetch` 封装统一请求工具，处理拦截器（自动添加 Token）。
3.  **处理文件上传**: 使用 `/api/file/upload` 接口，Content-Type 为 `multipart/form-data`。
4.  **处理分页**: 列表接口通常接受 `current` (页码) 和 `size` (每页条数) 参数。

## 6. 示例代码 (Axios 封装)

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
});

// 请求拦截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
api.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      // 处理业务错误
      console.error(res.msg);
      return Promise.reject(new Error(res.msg));
    }
    return res.data;
  },
  error => {
    // 处理 HTTP 错误
    return Promise.reject(error);
  }
);

export default api;
```
