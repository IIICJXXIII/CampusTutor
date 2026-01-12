package com.campus.module.booking.controller;

import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.booking.DTO.CreateBookingRequest;
import com.campus.module.booking.service.BookingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 预约控制器
 */
@Tag(name = "预约管理", description = "教师与家长双向预约功能")
@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRequestService bookingRequestService;

    @Operation(summary = "创建预约请求")
    @PostMapping("/create")
    public Result<Long> createBookingRequest(@Valid @RequestBody CreateBookingRequest request) {
        Long userId = UserContext.getUserId();
        Long bookingId = bookingRequestService.createBookingRequest(userId, request);
        return Result.success("预约请求已发送", bookingId);
    }

    @Operation(summary = "教师确认预约请求")
    @PostMapping("/confirm/{bookingId}")
    public Result<Void> confirmBookingRequest(@PathVariable Long bookingId) {
        Long userId = UserContext.getUserId();
        bookingRequestService.confirmBookingRequest(userId, bookingId);
        return Result.success("预约请求已确认");
    }

    @Operation(summary = "教师拒绝预约请求")
    @PostMapping("/reject/{bookingId}")
    public Result<Void> rejectBookingRequest(
            @PathVariable Long bookingId,
            @RequestParam String reason) {
        Long userId = UserContext.getUserId();
        bookingRequestService.rejectBookingRequest(userId, bookingId, reason);
        return Result.success("预约请求已拒绝");
    }

    @Operation(summary = "家长取消预约请求")
    @PostMapping("/cancel/{bookingId}")
    public Result<Void> cancelBookingRequest(@PathVariable Long bookingId) {
        Long userId = UserContext.getUserId();
        bookingRequestService.cancelBookingRequest(userId, bookingId);
        return Result.success("预约请求已取消");
    }

    @Operation(summary = "获取家长的预约请求列表")
    @GetMapping("/parent/list")
    public Result<?> getParentBookingRequests() {
        Long userId = UserContext.getUserId();
        return Result.success(bookingRequestService.getParentBookingRequests(userId));
    }

    @Operation(summary = "获取教师的预约请求列表")
    @GetMapping("/tutor/list")
    public Result<?> getTutorBookingRequests() {
        Long userId = UserContext.getUserId();
        return Result.success(bookingRequestService.getTutorBookingRequests(userId));
    }
}