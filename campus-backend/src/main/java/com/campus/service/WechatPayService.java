package com.campus.service;

import com.campus.config.WechatPayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务（模拟实现）
 */
@Service
public class WechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayService.class);

    private final WechatPayConfig wechatPayConfig;

    public WechatPayService(WechatPayConfig wechatPayConfig) {
        this.wechatPayConfig = wechatPayConfig;
    }

    @PostConstruct
    public void init() {
        // 初始化模拟微信支付服务
        log.info("初始化模拟微信支付服务");
    }

    /**
     * 创建JSAPI支付订单
     *
     * @param orderNo    订单号
     * @param amount     金额（分）
     * @param description 商品描述
     * @param openid     用户openid
     * @return 支付参数
     */
    public Map<String, String> createJsapiPay(String orderNo, Integer amount, String description, String openid) {
        log.info("模拟创建JSAPI支付订单: orderNo={}, amount={}, description={}, openid={}", 
                orderNo, amount, description, openid);
        
        // 模拟返回支付参数
        Map<String, String> payParams = new HashMap<>();
        payParams.put("appId", wechatPayConfig.getAppId());
        payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        payParams.put("nonceStr", "test_nonce_" + System.currentTimeMillis());
        payParams.put("package", "prepay_id=test_prepay_" + System.currentTimeMillis());
        payParams.put("signType", "RSA");
        payParams.put("paySign", "test_sign_" + System.currentTimeMillis());
        
        return payParams;
    }

    /**
     * 查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    public Map<String, String> queryOrder(String orderNo) {
        log.info("模拟查询订单: orderNo={}", orderNo);
        
        // 模拟返回订单信息
        Map<String, String> orderInfo = new HashMap<>();
        orderInfo.put("outTradeNo", orderNo);
        orderInfo.put("transactionId", "test_transaction_" + System.currentTimeMillis());
        orderInfo.put("tradeState", "SUCCESS");
        orderInfo.put("tradeStateDesc", "支付成功");
        
        return orderInfo;
    }

    /**
     * 关闭订单
     *
     * @param orderNo 订单号
     */
    public void closeOrder(String orderNo) {
        log.info("模拟关闭订单: orderNo={}", orderNo);
    }

    /**
     * 创建退款
     *
     * @param refundNo 退款单号
     * @param orderNo  订单号
     * @param totalAmount 总金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款信息
     */
    public Map<String, String> createRefund(String refundNo, String orderNo, Integer totalAmount, Integer refundAmount) {
        log.info("模拟创建退款: refundNo={}, orderNo={}, totalAmount={}, refundAmount={}", 
                refundNo, orderNo, totalAmount, refundAmount);
        
        // 模拟返回退款信息
        Map<String, String> refundInfo = new HashMap<>();
        refundInfo.put("refundId", "test_refund_" + System.currentTimeMillis());
        refundInfo.put("outRefundNo", refundNo);
        refundInfo.put("outTradeNo", orderNo);
        refundInfo.put("refundStatus", "SUCCESS");
        
        return refundInfo;
    }

    /**
     * 解析支付回调通知
     *
     * @param body      通知体
     * @param signature 签名
     * @param serial    证书序列号
     * @param nonce     随机串
     * @param timestamp 时间戳
     * @return 解析结果
     */
    public Map<String, String> parseNotify(String body, String signature, String serial, String nonce, String timestamp) {
        log.info("模拟解析支付回调通知: body={}, signature={}, serial={}, nonce={}, timestamp={}",
                body, signature, serial, nonce, timestamp);
        
        // 模拟返回解析结果
        Map<String, String> transaction = new HashMap<>();
        transaction.put("outTradeNo", "test_order_" + System.currentTimeMillis());
        transaction.put("transactionId", "test_transaction_" + System.currentTimeMillis());
        transaction.put("tradeState", "SUCCESS");
        
        return transaction;
    }
}
