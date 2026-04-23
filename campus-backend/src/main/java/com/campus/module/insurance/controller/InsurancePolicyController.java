package com.campus.module.insurance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.insurance.entity.InsurancePolicy;
import com.campus.module.insurance.service.InsurancePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "保险单")
@RestController
@RequestMapping("/api/insurance")
@RequiredArgsConstructor
public class InsurancePolicyController {

    private final InsurancePolicyService insurancePolicyService;

    @Operation(summary = "获取保险单列表")
    @GetMapping("/list")
    public Result<IPage<InsurancePolicy>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(insurancePolicyService.listByUserId(userId, status, page, size));
    }

    @Operation(summary = "获取保险单详情")
    @GetMapping("/{id}")
    public Result<InsurancePolicy> getDetail(@PathVariable Long id) {
        return Result.success(insurancePolicyService.getById(id));
    }

    @Operation(summary = "根据订单获取保险单")
    @GetMapping("/order/{orderId}")
    public Result<InsurancePolicy> getByOrder(@PathVariable Long orderId) {
        return Result.success(insurancePolicyService.getByOrderId(orderId));
    }
}
