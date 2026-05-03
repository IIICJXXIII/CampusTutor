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

    /** 标签，逗号分隔多个标签 */
    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    /** 状态: 1-正常, 0-已隐藏 */
    private Integer status;

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
