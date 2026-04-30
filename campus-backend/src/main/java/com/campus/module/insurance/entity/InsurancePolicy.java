package com.campus.module.insurance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("insurance_policy")
public class InsurancePolicy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String policyNo;

    private String provider;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String orderNo;
}
