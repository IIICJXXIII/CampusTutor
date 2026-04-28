package com.campus.module.chat.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器
 * 管理在线用户的 WebSocket 连接
 */
@Component
public class ChatSessionManager {

    private static final Logger log = LoggerFactory.getLogger(ChatSessionManager.class);

    /**
     * 存储用户ID -> WebSocketSession 的映射
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 添加用户会话
     *
     * @param userId  用户ID
     * @param session WebSocket 会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        // 如果用户已有连接，先关闭旧连接
        WebSocketSession oldSession = sessions.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
                log.info("关闭用户 {} 的旧连接", userId);
            } catch (Exception e) {
                log.error("关闭旧连接失败", e);
            }
        }

        sessions.put(userId, session);
        log.info("用户 {} 上线，当前在线人数: {}", userId, sessions.size());
    }

    /**
     * 移除用户会话
     *
     * @param userId 用户ID
     */
    public void removeSession(Long userId) {
        sessions.remove(userId);
        log.info("用户 {} 下线，当前在线人数: {}", userId, sessions.size());
    }

    /**
     * 获取用户会话
     *
     * @param userId 用户ID
     * @return WebSocket 会话，如果用户不在线则返回 null
     */
    public WebSocketSession getSession(Long userId) {
        return sessions.get(userId);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取当前在线人数
     *
     * @return 在线人数
     */
    public int getOnlineCount() {
        return sessions.size();
    }
}
