package com.campus.module.user.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import com.campus.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
    public boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(SysUser user) {
        if (existsByUsername(user.getUsername())) {
            throw new BusinessException(5003, "用户名已存在");
        }
        String encryptPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
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
            throw new BusinessException(5001, "用户不存在");
        }
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String username, String newPassword) {
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }
        // 密码加密
        String encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encryptedPassword);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        // 验证旧密码
        String storedPassword = user.getPassword();
        boolean validOldPassword;
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            validOldPassword = BCrypt.checkpw(oldPassword, storedPassword);
        } else {
            String encryptedOldPassword = SecureUtil.md5(oldPassword);
            validOldPassword = encryptedOldPassword.equals(storedPassword);
        }
        if (!validOldPassword) {
            throw new BusinessException(5004, "密码错误");
        }

        // 密码加密
        String encryptedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encryptedNewPassword);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePasswordByPhone(String phone, String oldPassword, String newPassword) {
        SysUser user = getByUsername(phone);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        String storedPassword = user.getPassword();
        boolean validOldPassword;
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            validOldPassword = BCrypt.checkpw(oldPassword, storedPassword);
        } else {
            String encryptedOldPassword = SecureUtil.md5(oldPassword);
            validOldPassword = encryptedOldPassword.equals(storedPassword);
        }
        if (!validOldPassword) {
            throw new BusinessException(5004, "旧密码错误");
        }

        String encryptedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encryptedNewPassword);
        updateById(user);
    }
}
