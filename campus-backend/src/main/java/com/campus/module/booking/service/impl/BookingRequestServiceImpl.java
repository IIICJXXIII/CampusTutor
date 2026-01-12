package com.campus.module.booking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.booking.DTO.CreateBookingRequest;
import com.campus.module.booking.entity.BookingRequest;
import com.campus.module.booking.mapper.BookingRequestMapper;
import com.campus.module.booking.service.BookingRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预约请求服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingRequestServiceImpl extends ServiceImpl<BookingRequestMapper, BookingRequest>
        implements BookingRequestService {

    private final BookingRequestMapper bookingRequestMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBookingRequest(Long userId, CreateBookingRequest request) {
        // 创建预约请求
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setParentId(userId);
        bookingRequest.setTutorId(request.getTutorId());
        bookingRequest.setStudentId(request.getStudentId());
        bookingRequest.setSubject(request.getSubject());
        bookingRequest.setGrade(request.getGrade());
        bookingRequest.setBookingDate(request.getBookingDate());
        bookingRequest.setStartTime(request.getStartTime());
        bookingRequest.setEndTime(request.getEndTime());
        bookingRequest.setStatus(0); // 待教师确认
        bookingRequest.setRemark(request.getRemark());

        save(bookingRequest);

        log.info("家长 {} 创建预约请求: 教师={}, 日期={}, 时间={}-{}",
                userId, request.getTutorId(), request.getBookingDate(),
                request.getStartTime(), request.getEndTime());

        return bookingRequest.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBookingRequest(Long userId, Long bookingId) {
        // 获取预约请求
        BookingRequest bookingRequest = getById(bookingId);
        if (bookingRequest == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约请求不存在");
        }

        // 验证权限
        if (!bookingRequest.getTutorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此预约请求");
        }

        // 验证状态
        if (bookingRequest.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约请求状态不正确");
        }

        // 更新状态
        bookingRequest.setStatus(1); // 教师已确认
        updateById(bookingRequest);

        log.info("教师 {} 确认预约请求: {}", userId, bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectBookingRequest(Long userId, Long bookingId, String reason) {
        // 获取预约请求
        BookingRequest bookingRequest = getById(bookingId);
        if (bookingRequest == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约请求不存在");
        }

        // 验证权限
        if (!bookingRequest.getTutorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此预约请求");
        }

        // 验证状态
        if (bookingRequest.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约请求状态不正确");
        }

        // 更新状态
        bookingRequest.setStatus(2); // 教师已拒绝
        bookingRequest.setRemark(reason);
        updateById(bookingRequest);

        log.info("教师 {} 拒绝预约请求: {}, 原因: {}", userId, bookingId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBookingRequest(Long userId, Long bookingId) {
        // 获取预约请求
        BookingRequest bookingRequest = getById(bookingId);
        if (bookingRequest == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约请求不存在");
        }

        // 验证权限
        if (!bookingRequest.getParentId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此预约请求");
        }

        // 验证状态
        if (bookingRequest.getStatus() != 0 && bookingRequest.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约请求状态不正确");
        }

        // 更新状态
        bookingRequest.setStatus(3); // 家长已取消
        updateById(bookingRequest);

        log.info("家长 {} 取消预约请求: {}", userId, bookingId);
    }

    @Override
    public List<BookingRequest> getParentBookingRequests(Long parentId) {
        return lambdaQuery()
                .eq(BookingRequest::getParentId, parentId)
                .orderByDesc(BookingRequest::getCreateTime)
                .list();
    }

    @Override
    public List<BookingRequest> getTutorBookingRequests(Long tutorId) {
        return lambdaQuery()
                .eq(BookingRequest::getTutorId, tutorId)
                .orderByDesc(BookingRequest::getCreateTime)
                .list();
    }
}