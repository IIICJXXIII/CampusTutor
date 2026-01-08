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

            // 发送连接成功消息
            sendMessage(session, Map.of(
                    "type", "connected",
                    "message", "连接成功",
                    "userId", userId));
        } else {
            log.warn("无法获取用户ID，关闭连接");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * 处理接收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = getUserIdFromSession(session);
        if (senderId == null) {
            log.warn("无法识别发送者，忽略消息");
            return;
        }

        try {
            // 解析消息
            ChatMessageDTO dto = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);

            if ("send".equals(dto.getType())) {
                // 发送消息
                handleSendMessage(senderId, dto);
            } else if ("read".equals(dto.getType())) {
                // 标记已读
                handleReadMessage(senderId, dto.getReceiverId());
            } else if ("ping".equals(dto.getType())) {
                // 心跳响应
                sendMessage(session, Map.of("type", "pong"));
            }
        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage(), e);
            sendMessage(session, Map.of(
                    "type", "error",
                    "message", "消息处理失败: " + e.getMessage()));
        }
    }

    /**
     * 处理发送消息
     */
    private void handleSendMessage(Long senderId, ChatMessageDTO dto) {
        // 保存消息到数据库
        ChatMessage savedMessage = chatService.sendMessage(
                senderId,
                dto.getReceiverId(),
                dto.getContent(),
                dto.getMsgType());

        // 转换为 VO
        ChatMessageVO messageVO = chatService.convertToVO(savedMessage);

        // 构建推送消息
        Map<String, Object> pushMessage = new HashMap<>();
        pushMessage.put("type", "message");
        pushMessage.put("data", messageVO);

        // 推送给接收者（如果在线）
        WebSocketSession receiverSession = sessionManager.getSession(dto.getReceiverId());
        if (receiverSession != null && receiverSession.isOpen()) {
            sendMessage(receiverSession, pushMessage);
            log.info("消息已推送给用户 {}", dto.getReceiverId());
        } else {
            log.info("用户 {} 不在线，消息已保存", dto.getReceiverId());
        }

        // 也推送给发送者（确认消息已发送）
        WebSocketSession senderSession = sessionManager.getSession(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            pushMessage.put("type", "sent");
            sendMessage(senderSession, pushMessage);
        }
    }

    /**
     * 处理标记已读
     */
    private void handleReadMessage(Long receiverId, Long senderId) {
        chatService.markAsRead(senderId, receiverId);

        // 通知对方消息已读
        WebSocketSession senderSession = sessionManager.getSession(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            sendMessage(senderSession, Map.of(
                    "type", "read",
                    "readBy", receiverId));
        }
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
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }
}
