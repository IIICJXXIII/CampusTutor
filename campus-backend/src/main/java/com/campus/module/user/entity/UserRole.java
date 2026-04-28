package com.campus.module.user.entity;

/**
 * 用户角色枚举
 */
public enum UserRole {

    ADMIN(0, "管理员"),
    TUTOR(1, "教员"),
    PARENT(2, "家长");

    private final Integer code;
    private final String desc;

    UserRole(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static UserRole getByCode(Integer code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
