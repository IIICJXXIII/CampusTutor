package com.campus.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
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

/**
 * 订单模块控制器
 */
@Tag(name = "订单模块", description = "订单创建、支付、管理")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final CourseOrderService orderService;

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
    public Result<Void> confirm(@PathVariable Long id) {
        Long parentId = UserContext.getUserId();
        orderService.confirmOrder(parentId, id);
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
    public Result<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        Long userId = UserContext.getUserId();
        orderService.cancelOrder(userId, id, reason);
        return Result.success();
    }

    @Operation(summary = "教员确认开课")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        Long tutorId = UserContext.getUserId();
        orderService.confirmStart(tutorId, id);
        return Result.success();
    }

    @Operation(summary = "完成订单")
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        orderService.completeOrder(id);
        return Result.success();
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<CourseOrder> detail(@PathVariable Long id) {
        CourseOrder order = orderService.getById(id);
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

    @Operation(summary = "创建微信支付参数")
    @PostMapping("/wechat-pay")
    public Result<java.util.Map<String, String>> createWechatPay(
            @RequestParam Long orderId,
            @RequestParam String openid) {
        Long userId = UserContext.getUserId();
        var payParams = orderService.createWechatPayParams(userId, orderId, openid);
        return Result.success(payParams);
    }

    @Operation(summary = "申请退款")
    @PostMapping("/refund")
    public Result<String> applyRefund(
            @RequestParam Long orderId,
            @RequestParam java.math.BigDecimal refundAmount,
            @RequestParam String reason) {
        Long userId = UserContext.getUserId();
        String refundNo = orderService.applyRefund(userId, orderId, refundAmount, reason);
        return Result.success(refundNo);
    }
}
