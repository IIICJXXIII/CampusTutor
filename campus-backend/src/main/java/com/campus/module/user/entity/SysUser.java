package com.campus.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    @TableField("avatar")
    private String avatarUrl;

    private Integer role;

    private Integer status;

    private Integer gender;

    private String wechat;

    private String region;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
