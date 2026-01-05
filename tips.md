# 📘 CampusTutor 团队协作保姆级手册

## ⚠️ 核心原则 (铁律)

1. **永远不要** 直接在 `main` 分支上写代码。
2. **永远不要** 强制推送 (`git push -f`)。
3. **每天开工前**，必须先同步 `main` 分支的最新代码。

---

## 🛠 日常开发“四步走” (Standard Workflow)

请每天**开始写代码前**，按顺序执行以下命令：

### 第一步：同步主分支 (Sync Main)

先把远程仓库的最新代码拉到本地的 `main` 分支，确保你的底座是最新的。

```bash
# 1. 下载远程仓库的所有变动信息
git fetch --all

# 2. 切换到主分支
git checkout main

# 3. 拉取主分支最新代码
git pull origin main

```

*(此时，你本地的 `main` 分支已经是最新版本了 ✅)*

### 第二步：注入最新代码 (Merge into Feature) 🔥 **关键步骤**

把你刚刚拉下来的 `main` 代码，合并到你自己的功能分支里。
*这一步能让你在写代码前就解决掉潜在的冲突，而不是等到最后提交时才发现爆炸。*

```bash
# 1. 切换回你自己的功能分支 (如果没有就用 checkout -b 创建)
# 例如：git checkout feature/login
git checkout "你的分支名"

# 2. 【关键】把 main 的更新合并进来
git merge main

```

*(此时，你的分支既有你写的代码，也有队友写的最新代码，可以放心开发了 ✅)*

### 第三步：开发与存档 (Coding & Commit)

开始写代码... 完成一个小功能点后：

```bash
# 1. 把修改的文件添加到暂存区
git add .

# 2. 提交并写备注 (备注要清晰，说明改了什么)
git commit -m "feat: 完成了xx功能的开发"

```

### 第四步：推送与合并 (Push & PR)

把你的代码上传到 GitHub。

```bash
# 推送到远程服务器的对应分支
git push origin "你的分支名"

```

**最后一步：**
打开 GitHub 网页，点击 **"Compare & pull request"** 绿色按钮，申请把你的代码合并进 `main`。

---

## 🚑 常见情况急救

### Q1: 执行 `git merge main` 时提示 CONFLICT (冲突)？

* **现象：** 终端提示 `Automatic merge failed; fix conflicts and then commit the result.`
* **怎么办：**
1. 打开代码编辑器（IDEA/VS Code）。
2. 找到标红的文件，搜索 `<<<<<<<` 符号。
3. 手动修改代码，决定保留谁的（或者两个都保留）。
4. 改完后删除那些特殊符号。
5. 执行 `git add .` 和 `git commit -m "fix: 解决合并冲突"`。



### Q2: 我是第一次开发新功能，没有分支怎么办？

* **创建新分支命令：**
```bash
# 基于当前的 main 创建一个新分支
git checkout main
git pull origin main
git checkout -b feature/你的功能名

```



---

## 📋 极简命令清单 (Cheat Sheet)

如果你已经很熟练了，可以直接看这里：

```bash
# --- 1. 早上开工 ---
git checkout main
git pull origin main
git checkout feature/我的分支
git merge main

# --- 2. 写完代码 ---
git add .
git commit -m "feat: 更新说明"
git push origin feature/我的分支

```