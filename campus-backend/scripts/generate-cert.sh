#!/bin/bash
# 生成自签名 SSL 证书（无域名临时方案）
# 后续购买域名后，替换为 Let's Encrypt 或阿里云免费证书

SSL_DIR="$(cd "$(dirname "$0")/../nginx/ssl" && pwd)"

echo "=== 生成自签名 SSL 证书 ==="
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$SSL_DIR/server.key" \
  -out "$SSL_DIR/server.crt" \
  -subj "/C=CN/ST=Zhejiang/L=Hangzhou/O=CampusTutor/CN=114.55.59.30"

echo "证书已生成:"
echo "  私钥: $SSL_DIR/server.key"
echo "  证书: $SSL_DIR/server.crt"
echo ""
echo "注意: 自签名证书会导致浏览器显示安全警告，"
echo "     但 HTTPS 连接有效，Geolocation API 可以正常工作。"
echo "     建议购买域名后使用正式 SSL 证书替换。"
