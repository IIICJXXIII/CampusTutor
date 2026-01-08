package com.campus.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话视图对象
 * 用于展示会话列表
 */
@Data
public class ChatSessionVO {

    /**
     * 对方用户ID
     */
    private Long targetUserId;

    /**
     * 对方昵称
     */
    private String targetNickname;

    /**
     * 对方头像
     */
    private String targetAvatar;

    /**
     * 对方角色: 0-管理员, 1-教员, 2-家长
     */
    private Integer targetRole;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;
}
