package com.campus.common.context;

import lombok.Data;

/**
 * 用户上下文 - 存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setUser(Long userId, Integer role) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setRole(role);
        USER_HOLDER.set(userInfo);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = USER_HOLDER.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getRole() {
        UserInfo userInfo = USER_HOLDER.get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    /**
     * 获取当前用户信息
     */
    public static UserInfo getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 清除当前用户信息 (请求结束时调用)
     */
    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return USER_HOLDER.get() != null;
    }

    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        private Long userId;
        private Integer role;
    }
}
