package com.campus.module.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户列表 VO
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;

    /** 角色: 1-教师, 2-家长 */
    private Integer role;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    private String phone;

    private LocalDateTime createTime;
}
