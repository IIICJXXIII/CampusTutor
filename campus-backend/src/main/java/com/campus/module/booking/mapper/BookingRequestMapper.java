package com.campus.module.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.booking.entity.BookingRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约请求 Mapper
 */
@Mapper
public interface BookingRequestMapper extends BaseMapper<BookingRequest> {
}