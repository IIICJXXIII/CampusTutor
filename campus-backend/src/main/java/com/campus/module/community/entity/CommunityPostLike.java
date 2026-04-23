package com.campus.module.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community_post_like")
public class CommunityPostLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
