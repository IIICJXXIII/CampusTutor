package com.campus.module.chat.dto;

import lombok.Data;

/**
 * WebSocket 消息传输对象
 * 用于 WebSocket 消息的发送和接收
 */
@Data
public class ChatMessageDTO {

    /**
     * 消息类型: send-发送消息, read-标记已读
     */
    private String type;

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
}
