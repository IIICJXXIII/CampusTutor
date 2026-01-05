package com.campus.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.order.dto.CreateOrderRequest;
import com.campus.module.order.dto.PayOrderRequest;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.service.CourseOrderService;
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

    @Operation(summary = "支付订单")
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody PayOrderRequest request) {
        Long userId = UserContext.getUserId();
        orderService.payOrder(userId, request);
        return Result.success();
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
}
