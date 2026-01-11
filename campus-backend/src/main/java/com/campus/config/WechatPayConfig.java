package com.campus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户API密钥
     */
    private String mchKey;

    /**
     * API v3密钥
     */
    private String apiV3Key;

    /**
     * 商户证书序列号
     */
    private String serialNo;

    /**
     * 商户私钥路径
     */
    private String privateKeyPath;

    /**
     * 微信支付平台证书路径
     */
    private String platformCertPath;

    /**
     * 支付回调地址
     */
    private String notifyUrl;

    /**
     * 退款回调地址
     */
    private String refundNotifyUrl;

    /**
     * 是否启用沙箱环境
     */
    private boolean sandbox = true;
}
