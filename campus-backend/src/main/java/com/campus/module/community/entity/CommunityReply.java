package com.campus.module.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community_reply")
public class CommunityReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long rootId;

    private Long parentId;

    private Long replyToId;

    private Long replyToUserId;

    private Long userId;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String authorNickname;

    @TableField(exist = false)
    private String authorAvatar;

    @TableField(exist = false)
    private String replyToNickname;

    @TableField(exist = false)
    private Boolean liked;
}
