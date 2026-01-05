package com.campus.module.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.wallet.entity.SysWallet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户钱包 Mapper 接口
 */
@Mapper
public interface SysWalletMapper extends BaseMapper<SysWallet> {
}
