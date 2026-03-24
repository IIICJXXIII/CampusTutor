package com.campus.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.common.exception.BusinessException;
import com.campus.config.WechatPayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

/**
 * 真实微信支付服务实现
 * 注意：由于微信支付需要商户号和API证书，这里提供的是框架代码
 * 实际使用时需要配置真实的商户信息
 */
@Slf4j
@Service
public class RealWechatPayService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final WechatPayConfig wechatPayConfig;

    @Value("${wechat.pay.enabled:false}")
    private boolean enabled;

    @Value("${wechat.pay.app-id:}")
    private String appId;

    @Value("${wechat.pay.mch-id:}")
    private String mchId;

    @Value("${wechat.pay.api-key:}")
    private String apiKey;

    @Value("${wechat.pay.notify-url:}")
    private String notifyUrl;

    public RealWechatPayService(WechatPayConfig wechatPayConfig) {
        this.wechatPayConfig = wechatPayConfig;
    }

    @PostConstruct
    public void init() {
        if (enabled && isConfigComplete()) {
            log.info("微信支付服务已启用，配置完整");
        } else if (enabled) {
            log.warn("微信支付服务已启用，但配置不完整，将使用模拟模式");
        } else {
            log.info("微信支付服务未启用，使用模拟模式");
        }
    }

    /**
     * 创建JSAPI支付订单
     */
    public Map<String, String> createJsapiPay(String orderNo, Integer amount, String description, String openid) {
        if (!enabled || !isConfigComplete()) {
            log.info("微信支付未启用或配置不完整，使用模拟支付");
            return createMockPayParams(orderNo, amount, description, openid);
        }

        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appid", appId);
            requestBody.put("mchid", mchId);
            requestBody.put("description", description);
            requestBody.put("out_trade_no", orderNo);
            requestBody.put("notify_url", notifyUrl);
            requestBody.put("amount", Map.of(
                    "total", amount,
                    "currency", "CNY"
            ));
            requestBody.put("payer", Map.of("openid", openid));

            // 生成随机字符串和时间戳
            String nonceStr = generateNonceStr();
            String timestamp = String.valueOf(Instant.now().getEpochSecond());

            // 构建签名
            String signature = generateSignature(requestBody, nonceStr, timestamp);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "WECHATPAY2-SHA256-RSA2048 " + signature);
            headers.set("Accept", "application/json");

            // 发送请求
            String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject responseJson = JSONUtil.parseObj(response.getBody());
                String prepayId = responseJson.getStr("prepay_id");
                
                if (StrUtil.isNotBlank(prepayId)) {
                    return generatePayParams(prepayId, timestamp, nonceStr);
                } else {
                    throw new BusinessException("微信支付创建失败，未获取到prepay_id");
                }
            } else {
                log.error("微信支付创建失败: status={}, body={}", 
                        response.getStatusCode(), response.getBody());
                throw new BusinessException("微信支付创建失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("微信支付创建异常", e);
            throw new BusinessException("微信支付服务异常，请稍后重试");
        }
    }

    /**
     * 生成支付参数
     */
    private Map<String, String> generatePayParams(String prepayId, String timestamp, String nonceStr) {
        Map<String, String> payParams = new HashMap<>();
        payParams.put("appId", appId);
        payParams.put("timeStamp", timestamp);
        payParams.put("nonceStr", nonceStr);
        payParams.put("package", "prepay_id=" + prepayId);
        payParams.put("signType", "RSA");
        
        // 生成签名
        String paySign = generatePaySign(payParams);
        payParams.put("paySign", paySign);
        
        return payParams;
    }

    /**
     * 生成支付签名
     */
    private String generatePaySign(Map<String, String> params) {
        try {
            // 按字典序排序
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            
            StringBuilder signStr = new StringBuilder();
            for (String key : keys) {
                if ("signType".equals(key) || "paySign".equals(key)) {
                    continue;
                }
                if (signStr.length() > 0) {
                    signStr.append("&");
                }
                signStr.append(key).append("=").append(params.get(key));
            }
            
            // 添加API密钥
            signStr.append("&key=").append(apiKey);
            
            // 计算MD5签名
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(signStr.toString().getBytes(StandardCharsets.UTF_8));
            
            // 转换为大写十六进制
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("生成支付签名失败", e);
            return "mock_sign_" + System.currentTimeMillis();
        }
    }

    /**
     * 生成API签名
     */
    private String generateSignature(Map<String, Object> body, String nonceStr, String timestamp) {
        try {
            // 构建签名字符串
            String method = "POST";
            String url = "/v3/pay/transactions/jsapi";
            String bodyStr = JSONUtil.toJsonStr(body);
            
            String signStr = method + "\n" +
                    url + "\n" +
                    timestamp + "\n" +
                    nonceStr + "\n" +
                    bodyStr + "\n";
            
            // 使用HMAC-SHA256签名
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));
            
            // Base64编码
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("生成API签名失败", e);
            return "mock_signature";
        }
    }

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 创建模拟支付参数
     */
    private Map<String, String> createMockPayParams(String orderNo, Integer amount, String description, String openid) {
        log.info("创建模拟支付参数: orderNo={}, amount={}, description={}, openid={}", 
                orderNo, amount, description, openid);
        
        Map<String, String> payParams = new HashMap<>();
        payParams.put("appId", wechatPayConfig.getAppId());
        payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        payParams.put("nonceStr", "mock_nonce_" + System.currentTimeMillis());
        payParams.put("package", "prepay_id=mock_prepay_" + System.currentTimeMillis());
        payParams.put("signType", "RSA");
        payParams.put("paySign", "mock_sign_" + System.currentTimeMillis());
        
        return payParams;
    }

    /**
     * 查询订单
     */
    public Map<String, String> queryOrder(String orderNo) {
        log.info("查询订单: orderNo={}", orderNo);
        
        // 模拟返回
        Map<String, String> orderInfo = new HashMap<>();
        orderInfo.put("outTradeNo", orderNo);
        orderInfo.put("transactionId", "mock_transaction_" + System.currentTimeMillis());
        orderInfo.put("tradeState", "SUCCESS");
        orderInfo.put("tradeStateDesc", "支付成功");
        
        return orderInfo;
    }

    /**
     * 关闭订单
     */
    public void closeOrder(String orderNo) {
        log.info("关闭订单: orderNo={}", orderNo);
    }

    /**
     * 创建退款
     */
    public Map<String, String> createRefund(String refundNo, String orderNo, Integer totalAmount, Integer refundAmount) {
        log.info("创建退款: refundNo={}, orderNo={}, totalAmount={}, refundAmount={}", 
                refundNo, orderNo, totalAmount, refundAmount);
        
        // 模拟返回
        Map<String, String> refundInfo = new HashMap<>();
        refundInfo.put("refundId", "mock_refund_" + System.currentTimeMillis());
        refundInfo.put("outRefundNo", refundNo);
        refundInfo.put("outTradeNo", orderNo);
        refundInfo.put("refundStatus", "SUCCESS");
        
        return refundInfo;
    }

    /**
     * 解析支付回调通知
     */
    public Map<String, String> parseNotify(String body, String signature, String serial, String nonce, String timestamp) {
        log.info("解析支付回调通知: body={}, signature={}, serial={}, nonce={}, timestamp={}",
                body, signature, serial, nonce, timestamp);
        
        // 模拟返回
        Map<String, String> transaction = new HashMap<>();
        transaction.put("outTradeNo", "mock_order_" + System.currentTimeMillis());
        transaction.put("transactionId", "mock_transaction_" + System.currentTimeMillis());
        transaction.put("tradeState", "SUCCESS");
        
        return transaction;
    }

    /**
     * 验证配置是否完整
     */
    private boolean isConfigComplete() {
        return StrUtil.isNotBlank(appId) && 
               StrUtil.isNotBlank(mchId) && 
               StrUtil.isNotBlank(apiKey) && 
               StrUtil.isNotBlank(notifyUrl);
    }
}