package com.campus.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信服务工具类
 */
@Slf4j
@Service
public class WechatService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wechat.app-id:}")
    private String appId;

    @Value("${wechat.app-secret:}")
    private String appSecret;

    /**
     * 通过code获取openid和session_key
     */
    public Map<String, String> code2Session(String code) {
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
            log.warn("微信配置未设置，使用模拟数据");
            return createMockSessionData(code);
        }

        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            log.debug("微信code2Session响应: {}", responseBody);

            JSONObject json = JSONUtil.parseObj(responseBody);
            String openid = json.getStr("openid");
            String sessionKey = json.getStr("session_key");
            String errcode = json.getStr("errcode");
            String errmsg = json.getStr("errmsg");

            if (StrUtil.isNotBlank(errcode) && !"0".equals(errcode)) {
                log.error("微信登录失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new BusinessException("微信登录失败: " + errmsg);
            }

            if (StrUtil.isBlank(openid)) {
                log.error("微信登录失败，未获取到openid");
                throw new BusinessException("微信登录失败，未获取到用户标识");
            }

            Map<String, String> result = new HashMap<>();
            result.put("openid", openid);
            result.put("session_key", sessionKey);
            result.put("unionid", json.getStr("unionid"));
            return result;
        } catch (Exception e) {
            log.error("调用微信code2Session接口失败", e);
            throw new BusinessException("微信服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 解密用户信息
     */
    public Map<String, String> decryptUserInfo(String encryptedData, String iv, String sessionKey) {
        try {
            byte[] encryptedDataBytes = Base64.getDecoder().decode(encryptedData);
            byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKeyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedDataBytes);

            String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
            JSONObject json = JSONUtil.parseObj(decryptedText);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("openId", json.getStr("openId"));
            userInfo.put("nickName", json.getStr("nickName"));
            userInfo.put("avatarUrl", json.getStr("avatarUrl"));
            userInfo.put("gender", json.getStr("gender"));
            userInfo.put("city", json.getStr("city"));
            userInfo.put("province", json.getStr("province"));
            userInfo.put("country", json.getStr("country"));
            userInfo.put("unionId", json.getStr("unionId"));

            return userInfo;
        } catch (Exception e) {
            log.error("解密微信用户信息失败", e);
            throw new BusinessException("用户信息解密失败");
        }
    }

    /**
     * 创建模拟的session数据（用于开发环境）
     */
    private Map<String, String> createMockSessionData(String code) {
        log.info("使用模拟微信登录数据，code: {}", code);
        
        Map<String, String> result = new HashMap<>();
        // 基于code生成稳定的openid，便于测试
        String openid = "mock_openid_" + Math.abs(code.hashCode() % 1000000);
        result.put("openid", openid);
        result.put("session_key", "mock_session_key_" + System.currentTimeMillis());
        result.put("unionid", "mock_unionid_" + openid);
        
        return result;
    }

    /**
     * 验证微信配置是否完整
     */
    public boolean isConfigComplete() {
        return StrUtil.isNotBlank(appId) && StrUtil.isNotBlank(appSecret);
    }

    /**
     * 获取微信access_token
     */
    public String getAccessToken() {
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
            log.warn("微信配置未设置，使用模拟access_token");
            return "mock_access_token_" + System.currentTimeMillis();
        }

        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, appSecret);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            log.debug("获取access_token响应: {}", responseBody);

            JSONObject json = JSONUtil.parseObj(responseBody);
            String accessToken = json.getStr("access_token");
            String errcode = json.getStr("errcode");
            String errmsg = json.getStr("errmsg");

            if (StrUtil.isNotBlank(errcode) && !"0".equals(errcode)) {
                log.error("获取access_token失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new BusinessException("获取微信access_token失败: " + errmsg);
            }

            if (StrUtil.isBlank(accessToken)) {
                log.error("获取access_token失败，未获取到token");
                throw new BusinessException("获取微信access_token失败");
            }

            return accessToken;
        } catch (Exception e) {
            log.error("调用微信获取access_token接口失败", e);
            throw new BusinessException("微信服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 使用phone_code获取用户手机号
     */
    public String getPhoneNumber(String phoneCode) {
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
            log.warn("微信配置未设置，使用模拟手机号");
            return "13800138000"; // 模拟手机号
        }

        String accessToken = getAccessToken();
        String url = String.format(
                "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s",
                accessToken);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("code", phoneCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseBody = response.getBody();
            log.debug("获取手机号响应: {}", responseBody);

            JSONObject json = JSONUtil.parseObj(responseBody);
            String errcode = json.getStr("errcode");
            String errmsg = json.getStr("errmsg");

            if (StrUtil.isNotBlank(errcode) && !"0".equals(errcode)) {
                log.error("获取手机号失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new BusinessException("获取手机号失败: " + errmsg);
            }

            JSONObject phoneInfo = json.getJSONObject("phone_info");
            if (phoneInfo == null) {
                log.error("获取手机号失败，未获取到phone_info");
                throw new BusinessException("获取手机号失败");
            }

            String phoneNumber = phoneInfo.getStr("phoneNumber");
            if (StrUtil.isBlank(phoneNumber)) {
                log.error("获取手机号失败，phoneNumber为空");
                throw new BusinessException("获取手机号失败");
            }

            return phoneNumber;
        } catch (Exception e) {
            log.error("调用微信获取手机号接口失败", e);
            throw new BusinessException("获取手机号服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 获取微信小程序appId
     */
    public String getAppId() {
        return appId;
    }
}