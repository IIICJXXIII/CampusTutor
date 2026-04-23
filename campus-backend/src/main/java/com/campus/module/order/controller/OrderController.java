package com.campus.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.Result;
import com.campus.common.result.ResultCode;
import com.campus.module.order.dto.AcceptDemandRequest;
import com.campus.module.order.dto.CreateOrderRequest;
import com.campus.module.order.dto.PayOrderRequest;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.service.CourseOrderService;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 订单模块控制器
 */
@Tag(name = "订单模块", description = "订单创建、支付、管理")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final CourseOrderService orderService;

    private Long resolveOrderId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单ID不能为空");
        }
        if (id.startsWith("CT")) {
            CourseOrder order = orderService.getOne(new LambdaQueryWrapper<CourseOrder>()
                    .eq(CourseOrder::getOrderNo, id));
            if (order == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
            }
            return order.getId();
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无效的订单ID格式");
        }
    }

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody CreateOrderRequest request) {
        Long parentId = UserContext.getUserId();
        Long orderId = orderService.createOrder(parentId, request);
        return Result.success(orderId);
    }

    @Operation(summary = "教师接单", description = "教师基于需求帖接单，创建待确认订单")
    @PostMapping("/accept")
    public Result<Long> accept(@Valid @RequestBody AcceptDemandRequest request) {
        Long tutorId = UserContext.getUserId();
        Long orderId = orderService.acceptDemand(tutorId, request);
        return Result.success(orderId);
    }

    @Operation(summary = "家长确认订单", description = "家长确认教师接单，订单变为待支付状态")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable String id) {
        Long parentId = UserContext.getUserId();
        orderService.confirmOrder(parentId, resolveOrderId(id));
        return Result.success();
    }

    @Operation(summary = "教师确认预约", description = "教师确认家长的直接预约订单，订单变为待支付状态")
    @PostMapping("/{id}/tutor-confirm")
    public Result<Void> tutorConfirm(@PathVariable String id) {
        Long tutorId = UserContext.getUserId();
        orderService.tutorConfirmOrder(tutorId, resolveOrderId(id));
        return Result.success();
    }

    @Operation(summary = "教师拒绝预约", description = "教师拒绝家长的直接预约订单，订单将被取消")
    @PostMapping("/{id}/tutor-reject")
    public Result<Void> tutorReject(@PathVariable String id, @RequestParam(required = false) String reason) {
        Long tutorId = UserContext.getUserId();
        orderService.tutorRejectOrder(tutorId, resolveOrderId(id), reason);
        return Result.success();
    }

    @Operation(summary = "支付订单")
    @PostMapping("/pay")
    public Result<Map<String, String>> pay(@Valid @RequestBody PayOrderRequest request) {
        Long userId = UserContext.getUserId();
        Map<String, String> payParams = orderService.payOrder(userId, request);
        return Result.success(payParams);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable String id, @RequestParam(required = false) String reason) {
        Long userId = UserContext.getUserId();
        orderService.cancelOrder(userId, resolveOrderId(id), reason);
        return Result.success();
    }

    @Operation(summary = "教员确认开课")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable String id) {
        Long tutorId = UserContext.getUserId();
        orderService.confirmStart(tutorId, resolveOrderId(id));
        return Result.success();
    }

    @Operation(summary = "完成订单")
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable String id) {
        Long userId = UserContext.getUserId();
        orderService.completeOrder(userId, resolveOrderId(id));
        return Result.success();
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<CourseOrder> detail(@PathVariable String id) {
        Long userId = UserContext.getUserId();
        Integer role = UserContext.getRole();
        CourseOrder order = orderService.getById(resolveOrderId(id));
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }

        boolean isAdmin = role != null && role == 0;
        boolean isOwner = order.getParentId().equals(userId) || order.getTutorId().equals(userId);
        if (!isAdmin && !isOwner) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看该订单");
        }

        return Result.success(order);
    }

    @Operation(summary = "家长订单列表")
    @GetMapping("/parent/list")
    public Result<IPage<CourseOrder>> parentList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long parentId = UserContext.getUserId();
        IPage<CourseOrder> result = orderService.listParentOrders(parentId, status, page, size);
        return Result.success(result);
    }

    @Operation(summary = "教员订单列表")
    @GetMapping("/tutor/list")
    public Result<IPage<CourseOrder>> tutorList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long tutorId = UserContext.getUserId();
        IPage<CourseOrder> result = orderService.listTutorOrders(tutorId, status, page, size);
        return Result.success(result);
    }

    @Operation(summary = "申请退款")
    @PostMapping("/refund")
    public Result<String> applyRefund(
            @RequestParam String orderId,
            @RequestParam java.math.BigDecimal refundAmount,
            @RequestParam String reason) {
        Long userId = UserContext.getUserId();
        String refundNo = orderService.applyRefund(userId, resolveOrderId(orderId), refundAmount, reason);
        return Result.success(refundNo);
    }

    @Operation(summary = "申请退款(前端简化版)", description = "前端通过订单ID和原因申请退款，退款金额自动计算")
    @PostMapping("/{id}/refund")
    public Result<String> applyRefundSimple(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {
        Long userId = UserContext.getUserId();
        Long orderId = resolveOrderId(id);
        CourseOrder order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getParentId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只能对自己的订单申请退款");
        }
        java.math.BigDecimal refundAmount = order.getTotalAmount();
        String refundNo = orderService.applyRefund(userId, orderId, refundAmount, reason);
        return Result.success(refundNo);
    }
}
