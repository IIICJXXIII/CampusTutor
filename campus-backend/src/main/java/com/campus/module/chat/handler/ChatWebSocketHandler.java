package com.campus.module.chat.handler;

import com.campus.module.chat.dto.ChatMessageDTO;
import com.campus.module.chat.dto.ChatMessageVO;
import com.campus.module.chat.entity.ChatMessage;
import com.campus.module.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 消息处理器
 * 处理 WebSocket 连接和消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatSessionManager sessionManager;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    /**
     * 连接建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessionManager.addSession(userId, session);

            // 🚨 修复：在发送初始化消息前，确认 session 没有被极速并发的新连接关掉
            if (session.isOpen()) {
                sendMessage(session, Map.of(
                        "type", "connected",
                        "message", "连接成功",
                        "userId", userId));
            }
        } else {
            log.warn("无法获取用户ID，关闭连接");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * 收到客户端消息后调用
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 心跳处理等其他逻辑可以放这里
        log.debug("收到消息: {}", message.getPayload());
    }

    /**
     * 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
    }

    /**
     * 处理传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误: {}", exception.getMessage());
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 从 session 属性中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 发送消息到指定会话
     */
    private void sendMessage(WebSocketSession session, Object message) {
        try {
            // 🚨 修复：严格判断状态，防止向已被 ChatSessionManager 关闭的会话发消息
            if (session != null && session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IllegalStateException e) {
            // 🚨 修复：捕获并发挤占时的状态异常，转为静默警告而不是刷屏报错
            log.warn("会话已被新连接挤占关闭，丢弃待发送消息: {}", e.getMessage());
        } catch (IOException e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }
}