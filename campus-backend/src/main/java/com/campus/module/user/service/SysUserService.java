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
}
