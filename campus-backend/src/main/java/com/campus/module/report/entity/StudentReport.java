package com.campus.module.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_report")
public class StudentReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long studentId;

    private Integer reportType;

    private String scoreChartData;

    private String tutorComment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
