package com.campus.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.user.entity.SysUser;

/**
 * 系统用户 Service 接口
 */
public interface SysUserService extends IService<SysUser> {

    SysUser getByUsername(String username);

    SysUser getByOpenid(String openid);

    boolean existsByUsername(String username);

    boolean register(SysUser user);

    boolean updateStatus(Long userId, Integer status);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     * 
     * @param username    用户名
     * @param newPassword 新密码
     */
    void resetPassword(String username, String newPassword);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    //void updatePassword(Long userId, String oldPassword, String newPassword);
}
