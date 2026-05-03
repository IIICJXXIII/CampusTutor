package com.campus.module.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("community_post")
public class CommunityPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer topicType;

    private String title;

    private String content;

    private String images;

    private Integer viewCount;

    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String authorNickname;

    @TableField(exist = false)
    private String authorAvatar;

    @TableField(exist = false)
    private Integer replyCount;

    @TableField(exist = false)
    private Boolean liked;
}
