package com.campus.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectByUsername(@Param("username") String username);

    SysUser selectByOpenid(@Param("openid") String openid);
}
