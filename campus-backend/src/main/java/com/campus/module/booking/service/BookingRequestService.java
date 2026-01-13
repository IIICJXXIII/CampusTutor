package com.campus.module.booking.service;

import com.campus.module.booking.entity.BookingRequest;
import com.campus.module.booking.DTO.CreateBookingRequest;

import java.util.List;

/**
 * 预约请求服务接口
 */
public interface BookingRequestService {

    /**
     * 创建预约请求
     * @param userId 用户ID
     * @param request 创建预约请求
     * @return 预约请求ID
     */
    Long createBookingRequest(Long userId, CreateBookingRequest request);

    /**
     * 教师确认预约请求
     * @param userId 教师用户ID
     * @param bookingId 预约请求ID
     */
    void confirmBookingRequest(Long userId, Long bookingId);

    /**
     * 教师拒绝预约请求
     * @param userId 教师用户ID
     * @param bookingId 预约请求ID
     * @param reason 拒绝原因
     */
    void rejectBookingRequest(Long userId, Long bookingId, String reason);

    /**
     * 家长取消预约请求
     * @param userId 家长用户ID
     * @param bookingId 预约请求ID
     */
    void cancelBookingRequest(Long userId, Long bookingId);

    /**
     * 获取家长的预约请求列表
     * @param parentId 家长用户ID
     * @return 预约请求列表
     */
    List<BookingRequest> getParentBookingRequests(Long parentId);

    /**
     * 获取教师的预约请求列表
     * @param tutorId 教师用户ID
     * @return 预约请求列表
     */
    List<BookingRequest> getTutorBookingRequests(Long tutorId);
}