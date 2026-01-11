package com.campus.module.chat.service;

import com.campus.module.chat.dto.ChatMessageVO;
import com.campus.module.chat.dto.ChatSessionVO;
import com.campus.module.chat.entity.ChatMessage;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 发送消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param msgType    消息类型
     * @return 保存后的消息对象
     */
    ChatMessage sendMessage(Long senderId, Long receiverId, String content, Integer msgType);

    /**
     * 获取两个用户之间的聊天历史
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param page    页码
     * @param size    每页大小
     * @return 消息列表
     */
    List<ChatMessageVO> getChatHistory(Long userId1, Long userId2, Integer page, Integer size);

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSessionVO> getSessionList(Long userId);

    /**
     * 标记消息为已读
     *
     * @param senderId   发送者ID（对方）
     * @param receiverId 接收者ID（当前用户）
     */
    void markAsRead(Long senderId, Long receiverId);

    /**
     * 获取用户未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    Integer getUnreadCount(Long userId);

    /**
     * 将消息实体转换为VO
     *
     * @param message 消息实体
     * @return 消息VO
     */
    ChatMessageVO convertToVO(ChatMessage message);

    /**
     * 发送消息 (字符串类型)
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param type       消息类型字符串 (text/image/file)
     * @return 消息ID
     */
    Long sendMessage(Long senderId, Long receiverId, String content, String type);

    /**
     * 根据会话ID获取消息列表
     *
     * @param conversationId 会话ID
     * @param userId         当前用户ID
     * @param page           页码
     * @param size           每页大小
     * @return 消息列表
     */
    List<ChatMessageVO> getMessagesByConversationId(Long conversationId, Long userId, Integer page, Integer size);
}
