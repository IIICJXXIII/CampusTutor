package com.campus.module.order.controller;

import com.campus.service.WechatPayService;
import com.campus.module.order.service.CourseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 支付回调控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/pay/notify")
public class PayNotifyController {

    private final WechatPayService wechatPayService;
    private final CourseOrderService orderService;

    public PayNotifyController(WechatPayService wechatPayService, CourseOrderService orderService) {
        this.wechatPayService = wechatPayService;
        this.orderService = orderService;
    }

    /**
     * 微信支付回调通知
     */
    @PostMapping("/wechat")
    public String wechatPayNotify(HttpServletRequest request) {
        try {
            // 读取通知体
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            // 获取签名等参数
            String signature = request.getHeader("Wechatpay-Signature");
            String serial = request.getHeader("Wechatpay-Serial");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String timestamp = request.getHeader("Wechatpay-Timestamp");

            log.info("收到微信支付通知: body={}, signature={}, serial={}, nonce={}, timestamp={}",
                    body, signature, serial, nonce, timestamp);

            // 解析通知
            var transaction = wechatPayService.parseNotify(body.toString(), signature, serial, nonce, timestamp);

            // 处理支付结果
            String orderNo = transaction.get("outTradeNo");
            String transactionId = transaction.get("transactionId");
            String tradeState = transaction.get("tradeState");

            log.info("订单支付状态: orderNo={}, transactionId={}, tradeState={}",
                    orderNo, transactionId, tradeState);

            // 根据支付状态处理订单
            if ("SUCCESS".equals(tradeState)) {
                // 处理支付成功逻辑
                // 这里需要调用订单服务更新订单状态
                // orderService.handlePaySuccess(orderNo, transactionId);
            }

            // 返回成功响应
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("处理微信支付通知失败: {}", e.getMessage(), e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    /**
     * 微信退款回调通知
     */
    @PostMapping("/wechat/refund")
    public String wechatRefundNotify(HttpServletRequest request) {
        try {
            // 读取通知体
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            // 获取签名等参数
            String signature = request.getHeader("Wechatpay-Signature");
            String serial = request.getHeader("Wechatpay-Serial");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String timestamp = request.getHeader("Wechatpay-Timestamp");

            log.info("收到微信退款通知: body={}, signature={}, serial={}, nonce={}, timestamp={}",
                    body, signature, serial, nonce, timestamp);

            // 解析通知并处理
            // 这里需要根据实际的退款通知格式进行解析和处理

            // 返回成功响应
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("处理微信退款通知失败: {}", e.getMessage(), e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }
}
