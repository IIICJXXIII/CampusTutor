package com.campus.module.chat.config;

import com.campus.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 在 WebSocket 连接建立前验证 JWT Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 从 URL 参数获取 token
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 握手失败: 缺少 token 参数");
                return false;
            }

            // 验证 token
            if (!jwtUtils.validateToken(token)) {
                log.warn("WebSocket 握手失败: token 无效");
                return false;
            }

            // 提取用户信息
            Long userId = jwtUtils.getUserIdFromToken(token);
            Integer role = jwtUtils.getRoleFromToken(token);

            if (userId == null) {
                log.warn("WebSocket 握手失败: 无法从 token 中提取用户ID");
                return false;
            }

            // 将用户信息存入 WebSocket Session 属性
            attributes.put("userId", userId);
            attributes.put("role", role);

            log.info("WebSocket 握手成功: userId={}, role={}", userId, role);
            return true;
        }

        log.warn("WebSocket 握手失败: 请求类型不支持");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 握手后发生异常: {}", exception.getMessage());
        }
    }
}
