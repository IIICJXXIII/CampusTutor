package com.campus.module.demand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tutor_application")
public class TutorApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long demandId;

    private Long tutorId;

    private Long tutorProfileId;

    private Integer totalHours;

    private String remark;

    private Integer status;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
