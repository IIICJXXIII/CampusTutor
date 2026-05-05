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

    private Long userId;

    /** 通知类型: 1-帖子收到新评论, 2-评论收到新回复 */
    private Integer type;

    private Long postId;

    private Long replyId;

    private Long fromUserId;

    private String contentSummary;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String fromUserNickname;

    @TableField(exist = false)
    private String fromUserAvatar;

    @TableField(exist = false)
    private String postTitle;
}
