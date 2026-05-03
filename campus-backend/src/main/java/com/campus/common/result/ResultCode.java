package com.campus.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_EXIST(5001, "用户不存在"),
    USER_DISABLED(5002, "账号已被禁用"),
    USERNAME_EXIST(5003, "用户名已存在"),
    PASSWORD_ERROR(5004, "密码错误"),
    TOKEN_EXPIRED(5005, "登录已过期，请重新登录"),
    TOKEN_INVALID(5006, "无效的Token"),

    TUTOR_NOT_VERIFIED(5101, "教员资质未认证"),
    TUTOR_UNDER_REVIEW(5102, "教员资质审核中"),
    TUTOR_REJECTED(5103, "教员资质审核被驳回"),

    ORDER_NOT_FOUND(5201, "订单不存在"),
    ORDER_STATUS_ERROR(5202, "订单状态异常"),
    BALANCE_NOT_ENOUGH(5203, "余额不足"),
    REFUND_FAILED(5204, "退款失败"),
    REFUND_AMOUNT_INVALID(5205, "退款金额无效"),
    PAYMENT_FAILED(5206, "支付失败"),

    // 社区模块错误码 (53xx)
    POST_TITLE_EMPTY(5301, "帖子标题不能为空"),
    POST_CONTENT_EMPTY(5302, "帖子内容不能为空"),
    POST_NOT_FOUND(5303, "帖子不存在");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
