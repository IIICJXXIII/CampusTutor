package com.campus.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebSocket 消息响应对象
 * 用于推送给客户端的消息格式
 */
@Data
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderNickname;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: 1-文本, 2-图片, 3-简历卡片, 4-订单邀约
     */
    private Integer msgType;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
