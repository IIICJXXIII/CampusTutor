package com.campus.module.user.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import com.campus.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统用户 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUser getByOpenid(String openid) {
        return sysUserMapper.selectByOpenid(openid);
    }

    @Override
    public boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(SysUser user) {
        if (existsByUsername(user.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
        String encryptPassword = SecureUtil.md5(user.getPassword());
        user.setPassword(encryptPassword);
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String username, String newPassword) {
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 密码加密
        String encryptedPassword = SecureUtil.md5(newPassword);
        user.setPassword(encryptedPassword);
        updateById(user);
    }
}
