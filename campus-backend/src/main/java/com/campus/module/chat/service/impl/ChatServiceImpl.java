package com.campus.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.chat.dto.ChatMessageVO;
import com.campus.module.chat.dto.ChatSessionVO;
import com.campus.module.chat.entity.ChatMessage;
import com.campus.module.chat.mapper.ChatMessageMapper;
import com.campus.module.chat.service.ChatService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content, Integer msgType) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMsgType(msgType != null ? msgType : 1); // 默认文本消息
        message.setIsRead(0); // 默认未读
        message.setCreateTime(LocalDateTime.now());

        chatMessageMapper.insert(message);
        log.info("消息已保存: {} -> {}, 内容: {}", senderId, receiverId, content);

        return message;
    }

    @Override
    public List<ChatMessageVO> getChatHistory(Long userId1, Long userId2, Integer page, Integer size) {
        // 构建查询条件：查询两个用户之间的所有消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(inner -> inner.eq(ChatMessage::getSenderId, userId1).eq(ChatMessage::getReceiverId, userId2))
                .or(inner -> inner.eq(ChatMessage::getSenderId, userId2).eq(ChatMessage::getReceiverId, userId1)));
        wrapper.orderByDesc(ChatMessage::getCreateTime);

        // 分页查询
        Page<ChatMessage> pageResult = chatMessageMapper.selectPage(
                new Page<>(page, size),
                wrapper);

        // 转换为 VO
        return pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatSessionVO> getSessionList(Long userId) {
        List<ChatSessionVO> sessions = chatMessageMapper.findSessionsByUserId(userId);

        // 🚨 核心修复：遍历会话列表，获取最新一条消息的具体类型进行文案兜底
        for (ChatSessionVO session : sessions) {
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w
                    .and(inner -> inner.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId,
                            session.getTargetUserId()))
                    .or(inner -> inner.eq(ChatMessage::getSenderId, session.getTargetUserId())
                            .eq(ChatMessage::getReceiverId, userId)));
            wrapper.orderByDesc(ChatMessage::getCreateTime);

            // 仅查询最新的一条消息
            Page<ChatMessage> pageResult = chatMessageMapper.selectPage(new Page<>(1, 1), wrapper);
            if (pageResult.getRecords() != null && !pageResult.getRecords().isEmpty()) {
                ChatMessage lastMsg = pageResult.getRecords().get(0);

                // 根据实体类中定义的 msgType 进行翻译映射
                if (lastMsg.getMsgType() == 2) {
                    session.setLastMessage("[图片]");
                } else if (lastMsg.getMsgType() == 3) {
                    session.setLastMessage("[简历卡片]");
                } else if (lastMsg.getMsgType() == 4) {
                    session.setLastMessage("[订单邀约]");
                } else {
                    // 如果是普通文本（1），正常显示文本内容
                    session.setLastMessage(lastMsg.getContent());
                }
            }
        }

        return sessions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long senderId, Long receiverId) {
        int count = chatMessageMapper.markAsRead(senderId, receiverId);
        log.info("标记已读: {} -> {}, 共 {} 条消息", senderId, receiverId, count);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return chatMessageMapper.countUnreadByReceiverId(userId);
    }

    @Override
    public ChatMessageVO convertToVO(ChatMessage message) {
        if (message == null) {
            return null;
        }

        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setMsgType(message.getMsgType());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());

        // 获取发送者信息
        SysUser sender = sysUserMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderNickname(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatarUrl());
        }

        return vo;
    }

    @Override
    public Long sendMessageWithTypeString(Long senderId, Long receiverId, String content, String type) {
        // 将字符串类型转换为整数类型
        Integer msgType = 1; // 默认文本
        if ("image".equals(type)) {
            msgType = 2;
        } else if ("file".equals(type)) {
            msgType = 3;
        }

        ChatMessage message = sendMessage(senderId, receiverId, content, msgType);
        return message != null ? message.getId() : null;
    }

    @Override
    public List<ChatMessageVO> getMessagesByConversationId(Long conversationId, Long userId, Integer page,
            Integer size) {
        // 简单实现：conversationId 可以是对方用户ID
        // 实际生产中应该有会话表来管理
        return getChatHistory(userId, conversationId, page, size);
    }
}