# 教员接单接口使用说明（Web端）

## 接口信息

| 项目 | 说明 |
|------|------|
| 接口地址 | `POST /api/order/accept` |
| 请求方式 | POST |
| Content-Type | application/json |
| 鉴权方式 | `Authorization: Bearer {token}` |

## 请求参数

```json
{
  "demandId": 123,
  "totalHours": 10,
  "remark": "可选备注"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| demandId | Long | 是 | 需求帖 ID |
| totalHours | Integer | 否 | 课时数，默认 10 |
| remark | String | 否 | 接单备注 |

## 响应

成功：

```json
{
  "code": 200,
  "msg": "success",
  "data": 456
}
```

`data` 为创建后的订单 ID。

## 常见错误

| code | message | 说明 |
|------|---------|------|
| 400 | 需求不存在 | demandId 无效 |
| 400 | 需求已下架或已被接单 | 需求不可接 |
| 400 | 教员未认证或档案不存在 | 当前账号不满足接单条件 |
| 401 | 未登录 | token 失效或缺失 |

## 业务流

1. 教员在需求列表/地图页点击立即接单。
2. 前端调用 `/api/order/accept`。
3. 后端校验需求状态与教员认证状态。
4. 创建待家长确认订单并返回订单 ID。
5. 家长后续调用 `/api/order/{id}/confirm` 进入支付流程。

## Web 端调用示例（Axios）

```javascript
import request from '@shared/api/request'

export function acceptDemand(data) {
  return request({
    url: '/api/order/accept',
    method: 'post',
    data
  })
}
```
