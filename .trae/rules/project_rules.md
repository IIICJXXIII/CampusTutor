# CampusTutor 项目开发规则

## 功能冗余评估机制

### 1. 新增功能前的冗余检查清单
在新增任何功能模块、API端点或组件之前，必须完成以下检查：

- [ ] **后端端点查重**：搜索现有Controller中是否已存在功能相同或相似的端点
- [ ] **前端API查重**：搜索 `campus-web-shared/api/` 目录下是否已存在功能相同的API函数
- [ ] **组件查重**：搜索 `campus-web/src/views/` 目录下是否已存在功能相同的Vue组件
- [ ] **数据库表查重**：检查 `schema.sql` 和 `campus-backend/sql/` 目录下是否已存在功能重叠的表
- [ ] **跨角色查重**：如果功能同时涉及家长端和教师端，确认是否应提取为公共组件

### 2. 冗余判定标准

| 冗余等级 | 判定条件 | 处理方式 |
|---------|---------|---------|
| **完全冗余** | 两个模块功能完全相同，仅命名不同 | 移除其中一个，统一使用保留的模块 |
| **部分冗余** | 两个模块有部分功能重叠 | 提取公共逻辑为共享模块，各模块保留差异部分 |
| **概念冗余** | 两个模块解决同一业务问题但实现方式不同 | 统一业务流程，移除废弃的实现方式 |
| **潜在冗余** | 模块当前未被使用但未来可能需要 | 标记为 `@deprecated`，在下一个大版本中评估是否移除 |

### 3. API层规范

- **一个后端端点只对应一个前端API函数**：禁止为同一端点创建多个别名函数
- **跨角色API分离**：用户端API（`campus-web-shared/api/`）与管理端API（`campus-web-admin/src/api/`）独立维护，允许功能等价但路径不同
- **删除后端端点时同步删除前端API**：避免前端调用不存在的端点导致404
- **新增API函数必须有前端调用方**：禁止创建"预留"API函数，待实际需要时再创建

### 4. 组件规范

- **同名组件必须功能不同**：parent/order/OrderList.vue 和 teacher/order/OrderList.vue 虽同名但面向不同角色，属于合理设计
- **公共组件优先**：当家长端和教师端需要相同UI时，提取到 `common/` 目录
- **路由注册验证**：新增Vue组件后必须在路由中注册，否则视为死代码

### 5. 定期冗余扫描

每个迭代版本发布前，执行以下冗余扫描：

```bash
# 扫描前端未使用的API函数
grep -r "export function" campus-web-shared/api/ | while read line; do
  func=$(echo "$line" | sed 's/.*export function \([a-zA-Z]*\).*/\1/')
  count=$(grep -r "$func" campus-web/src/ --include="*.vue" --include="*.js" | wc -l)
  if [ $count -le 1 ]; then echo "UNUSED: $func"; fi
done

# 扫描后端未被前端调用的端点
grep -r "@Mapping" campus-backend/src/ | while read line; do
  path=$(echo "$line" | grep -oP '"[^"]*"' | head -1 | tr -d '"')
  if [ -n "$path" ]; then
    count=$(grep -r "$path" campus-web-shared/api/ campus-web-admin/src/api/ | wc -l)
    if [ $count -eq 0 ]; then echo "ORPHAN ENDPOINT: $path"; fi
  fi
done
```

### 6. 已知冗余决策记录

| 日期 | 冗余项 | 决策 | 原因 |
|------|-------|------|------|
| 2026-04-22 | BookingController vs OrderController | 移除Booking，保留Order | 预约功能已融入订单系统，通过status=-1区分 |
| 2026-04-22 | auth.js wxLogin | 移除 | 项目已从小程序迁移到Web端 |
| 2026-04-22 | llm.js streamChat | 移除 | 后端无此端点，属于死代码 |
| 2026-04-22 | recommend.js 整个文件 | 移除前端API | 前端未调用，后端RecommendController保留待后续集成 |
| 2026-04-22 | components.d.ts 跨项目重复 | 保留 | 自动生成文件，跨项目重复属正常现象 |
| 2026-04-22 | parent/teacher 同名组件 | 保留 | 面向不同角色，功能有差异，属合理设计 |
