package com.campus.module.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社区互动通知实体
 */
@Data
@TableName("community_notification")
public class CommunityNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收通知的用户ID */
    private Long userId;

    /** 通知类型: 1-帖子收到新评论, 2-评论收到新回复 */
    private Integer type;

    /** 相关帖子ID */
    private Long postId;

    /** 相关评论ID（评论类通知） */
    private Long replyId;

    /** 触发通知的用户ID */
    private Long fromUserId;

    /** 内容摘要 */
    private String contentSummary;

    /** 0-未读, 1-已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 非持久化：触发者昵称 */
    @TableField(exist = false)
    private String fromUserNickname;

    /** 非持久化：触发者头像 */
    @TableField(exist = false)
    private String fromUserAvatar;

    /** 非持久化：帖子标题 */
    @TableField(exist = false)
    private String postTitle;
}
