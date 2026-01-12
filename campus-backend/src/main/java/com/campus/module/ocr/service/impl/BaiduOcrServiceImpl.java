package com.campus.module.ocr.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.module.ocr.dto.OcrResultDTO;
import com.campus.module.ocr.service.DoubaoVisionService;
import com.campus.module.ocr.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 百度 OCR 服务实现
 * 使用百度AI开放平台的OCR服务
 */
@Slf4j
@Service
public class BaiduOcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(BaiduOcrServiceImpl.class);

    @Autowired
    private DoubaoVisionService doubaoVisionService;

    @Value("${baidu.ocr.api-key:}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key:}")
    private String secretKey;

    @Value("${baidu.ocr.enabled:false}")
    private boolean enabled;

    // Access Token 缓存
    private String accessToken;
    private long tokenExpireTime;

    // 百度OCR API地址
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String IDCARD_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard";
    private static final String GENERAL_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";

    @Override
    public OcrResultDTO recognizeStudentCard(String imageUrl) {
        OcrResultDTO result = new OcrResultDTO();

        if (!enabled) {
            return mockStudentCardResult();
        }

        try {
            // 使用通用文字识别
            String text = recognizeGeneral(imageUrl);
            if (StrUtil.isBlank(text)) {
                result.setSuccess(false);
                result.setErrorMsg("无法识别图片内容");
                return result;
            }

            // 统一使用豆包大模型智能解析
            log.info("【OCR URL模式】原始文本: \n{}", text);
            result = doubaoVisionService.parseStudentCardWithLLM(text);

        } catch (Exception e) {
            log.error("学生证OCR识别失败", e);
            result.setSuccess(false);
            result.setErrorMsg("OCR识别失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public OcrResultDTO recognizeIdCardFront(String imageUrl) {
        OcrResultDTO result = new OcrResultDTO();

        if (!enabled) {
            return mockIdCardFrontResult();
        }

        try {
            String token = getAccessToken();
            String url = IDCARD_URL + "?access_token=" + token;

            Map<String, Object> params = new HashMap<>();
            params.put("url", imageUrl);
            params.put("id_card_side", "front");

            HttpResponse response = HttpRequest.post(url)
                    .form(params)
                    .execute();

            JSONObject json = JSONUtil.parseObj(response.body());
            if (json.containsKey("error_code")) {
                result.setSuccess(false);
                result.setErrorMsg(json.getStr("error_msg"));
                return result;
            }

            // 解析身份证信息
            JSONObject wordsResult = json.getJSONObject("words_result");
            result.setSuccess(true);
            result.setRealName(getWordsValue(wordsResult, "姓名"));
            result.setIdCard(getWordsValue(wordsResult, "公民身份号码"));
            result.setGender(getWordsValue(wordsResult, "性别"));
            result.setNation(getWordsValue(wordsResult, "民族"));
            result.setBirthDate(getWordsValue(wordsResult, "出生"));
            result.setAddress(getWordsValue(wordsResult, "住址"));

        } catch (Exception e) {
            log.error("身份证OCR识别失败", e);
            result.setSuccess(false);
            result.setErrorMsg("OCR识别失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public OcrResultDTO recognizeIdCardBack(String imageUrl) {
        OcrResultDTO result = new OcrResultDTO();
        result.setSuccess(true);
        return result;
    }

    @Override
    public String recognizeGeneral(String imageUrl) {
        if (!enabled) {
            return "模拟识别结果\n姓名：张三\n学校：北京大学";
        }

        try {
            String token = getAccessToken();
            String url = GENERAL_URL + "?access_token=" + token;

            Map<String, Object> params = new HashMap<>();
            params.put("url", imageUrl);

            HttpResponse response = HttpRequest.post(url)
                    .form(params)
                    .execute();

            JSONObject json = JSONUtil.parseObj(response.body());
            if (json.containsKey("error_code")) {
                log.error("OCR API错误: {}", json.getStr("error_msg"));
                return null;
            }

            StringBuilder sb = new StringBuilder();
            json.getJSONArray("words_result").forEach(item -> {
                JSONObject obj = (JSONObject) item;
                sb.append(obj.getStr("words")).append("\n");
            });

            return sb.toString();
        } catch (Exception e) {
            log.error("通用OCR识别请求失败", e);
            return null;
        }
    }

    @Override
    public OcrResultDTO recognizeStudentCardByBase64(String imageBase64) {
        OcrResultDTO result = new OcrResultDTO();

        if (!enabled) {
            return mockStudentCardResult();
        }

        try {
            // 使用Base64进行通用文字识别
            String text = recognizeGeneralByBase64(imageBase64);
            log.info("【OCR Base64模式】原始文本: \n{}", text);

            if (StrUtil.isBlank(text)) {
                result.setSuccess(false);
                result.setErrorMsg("无法识别图片内容");
                return result;
            }

            // 使用豆包大模型智能解析
            result = doubaoVisionService.parseStudentCardWithLLM(text);

        } catch (Exception e) {
            log.error("学生证Base64 OCR识别失败", e);
            result.setSuccess(false);
            result.setErrorMsg("OCR识别失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 通用文字识别（Base64模式）
     */
    private String recognizeGeneralByBase64(String imageBase64) {
        if (!enabled) {
            return "模拟识别结果\n姓名：张三\n学校：北京大学";
        }

        try {
            String token = getAccessToken();
            String url = GENERAL_URL + "?access_token=" + token;

            String base64Data = imageBase64.trim();
            // 处理Base64前缀
            if (base64Data.startsWith("\"")) base64Data = base64Data.substring(1);
            if (base64Data.endsWith("\"")) base64Data = base64Data.substring(0, base64Data.length() - 1);
            if (base64Data.contains(",")) base64Data = base64Data.substring(base64Data.indexOf(",") + 1);

            // 百度API要求 base64 必须进行 URLEncode (Hutool 的 form 方法通常会自动处理，但直接传 String 比较保险)
            // 这里直接用 hutool 的 form 发送，key 为 image
            HttpResponse response = HttpRequest.post(url)
                    .form("image", base64Data)
                    .execute();

            String body = response.body();
            JSONObject json = JSONUtil.parseObj(body);
            if (json.containsKey("error_code")) {
                log.error("OCR Base64 API错误: {}", json.getStr("error_msg"));
                return null;
            }

            StringBuilder sb = new StringBuilder();
            json.getJSONArray("words_result").forEach(item -> {
                JSONObject obj = (JSONObject) item;
                sb.append(obj.getStr("words")).append("\n");
            });

            return sb.toString();
        } catch (Exception e) {
            log.error("通用OCR Base64识别请求失败", e);
            return null;
        }
    }

    /**
     * 获取百度 Access Token
     */
    private String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;

        HttpResponse response = HttpRequest.post(url).execute();
        JSONObject json = JSONUtil.parseObj(response.body());

        if (json.containsKey("error")) {
            throw new RuntimeException("获取Access Token失败: " + json.getStr("error_description"));
        }

        accessToken = json.getStr("access_token");
        int expiresIn = json.getInt("expires_in");
        tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
        return accessToken;
    }

    private String getWordsValue(JSONObject wordsResult, String key) {
        if (wordsResult.containsKey(key)) {
            return wordsResult.getJSONObject(key).getStr("words");
        }
        return null;
    }

    private OcrResultDTO mockStudentCardResult() {
        OcrResultDTO result = new OcrResultDTO();
        result.setSuccess(true);
        result.setRealName("张三");
        result.setUniversityName("北京大学");
        result.setMajor("计算机科学与技术");
        result.setStudentId("2022001234");
        result.setEnrollYear(Calendar.getInstance().get(Calendar.YEAR) - 2);
        return result;
    }

    private OcrResultDTO mockIdCardFrontResult() {
        OcrResultDTO result = new OcrResultDTO();
        result.setSuccess(true);
        result.setRealName("张三");
        result.setIdCard("110101200001011234");
        result.setGender("男");
        result.setNation("汉");
        result.setBirthDate("2000年01月01日");
        result.setAddress("北京市海淀区中关村南大街5号");
        return result;
    }
}