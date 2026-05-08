#!/bin/bash
# ============================================================
# CampusTutor 服务器一键部署脚本
# 使用: bash scripts/deploy.sh
# ============================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT_DIR="$(cd "$PROJECT_DIR/.." && pwd)"

echo "=============================================="
echo "  CampusTutor 服务器部署"
echo "  目标服务器: 114.55.59.30"
echo "=============================================="

# -------- 1. 检查 Node.js（构建前端）--------
if ! command -v node &> /dev/null; then
  echo "❌ 未找到 Node.js，请先安装: https://nodejs.org/"
  exit 1
fi
echo "✅ Node.js $(node -v)"

# -------- 2. 构建家长/教师端 --------
echo ""
echo "=== [1/4] 构建家长/教师端 (campus-web) ==="
cd "$ROOT_DIR/campus-web"
if [ ! -d "node_modules" ]; then
  echo "安装依赖..."
  npm install
fi
npm run build
echo "✅ campus-web 构建完成 → dist/"

# -------- 3. 构建管理后台 --------
echo ""
echo "=== [2/4] 构建管理后台 (campus-web-admin) ==="
cd "$ROOT_DIR/campus-web-admin"
if [ ! -d "node_modules" ]; then
  echo "安装依赖..."
  npm install
fi
npm run build
echo "✅ campus-web-admin 构建完成 → dist/"

# -------- 4. 生成 SSL 证书 --------
echo ""
echo "=== [3/4] 生成 SSL 证书 ==="
cd "$PROJECT_DIR"
bash scripts/generate-cert.sh

# -------- 5. 启动 Docker 服务 --------
echo ""
echo "=== [4/4] 启动 Docker 服务 ==="
cd "$PROJECT_DIR"
docker compose up -d --build

echo ""
echo "=============================================="
echo "  部署完成!"
echo "  访问地址: https://114.55.59.30"
echo "  管理后台: https://114.55.59.30/admin"
echo ""
echo "  ⚠️ 自签名证书会导致浏览器安全警告，"
echo "     请点击「高级」→「继续访问」即可。"
echo "     Geolocation 定位功能需点击允许。"
echo ""
echo "  📌 后续待办:"
echo "     1. 高德开放平台添加 IP 白名单 (见下方说明)"
echo "     2. 购买域名 + 替换正式 SSL 证书"
echo "=============================================="
