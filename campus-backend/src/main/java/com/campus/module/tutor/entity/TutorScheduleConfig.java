package com.campus.module.tutor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教员时间配置表
 */
@Data
@TableName("tutor_schedule_config")
public class TutorScheduleConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教员档案ID
     */
    private Long tutorId;

    /**
     * 星期几：1-7
     */
    private Integer dayOfWeek;

    /**
     * 开始时间(HH:mm格式)
     */
    private String startTime;

    /**
     * 结束时间(HH:mm格式)
     */
    private String endTime;

    /**
     * 是否可用：0否 1是
     */
    private Integer available;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
