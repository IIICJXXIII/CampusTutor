package com.campus.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户名/手机号 */
    private String username;

    /** 加密密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    @TableField("avatar")
    private String avatarUrl;

    /** 角色: 0-管理员, 1-教员, 2-家长 */
    private Integer role;

    /** 微信OpenID */
    private String openid;

    /** 状态: 1-正常, 0-禁用 */
    private Integer status;

    /** 性别: 0-未知, 1-男, 2-女 */
    private Integer gender;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 显式的getter方法
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Integer getRole() {
        return role;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getGender() {
        return gender;
    }
}
