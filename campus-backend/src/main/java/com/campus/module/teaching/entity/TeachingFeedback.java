package com.campus.module.teaching.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课时反馈评价表
 */
@Data
@TableName("teaching_feedback")
public class TeachingFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private Long orderId;

    private Long fromUserId;

    private Integer rating;

    private String tags;

    private String content;

    private Integer isAnonymous;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
