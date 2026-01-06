package com.campus.module.ocr.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.module.ocr.dto.OcrResultDTO;
import com.campus.module.ocr.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 百度 OCR 服务实现
 * 
 * 使用百度AI开放平台的OCR服务
 * 文档: https://ai.baidu.com/ai-doc/OCR/1k3h7y3db
 */
@Slf4j
@Service
public class BaiduOcrServiceImpl implements OcrService {

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
            // 模拟模式：返回模拟数据
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

            // 解析学生证信息
            result = parseStudentCardText(text);
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
            // 模拟模式
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
        // 背面主要是签发机关和有效期，根据需求可扩展
        return result;
    }

    @Override
    public String recognizeGeneral(String imageUrl) {
        if (!enabled) {
            return "模拟识别结果\n姓名：张三\n学校：北京大学\n专业：计算机科学与技术\n学号：2022001234\n入学日期：2022年9月";
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
                log.error("OCR识别失败: {}", json.getStr("error_msg"));
                return null;
            }

            // 拼接所有识别出的文字
            StringBuilder sb = new StringBuilder();
            json.getJSONArray("words_result").forEach(item -> {
                JSONObject obj = (JSONObject) item;
                sb.append(obj.getStr("words")).append("\n");
            });

            return sb.toString();
        } catch (Exception e) {
            log.error("通用OCR识别失败", e);
            return null;
        }
    }

    /**
     * 获取百度 Access Token
     */
    private String getAccessToken() {
        // 检查缓存
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
        tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L; // 提前1分钟过期

        log.info("百度OCR Access Token刷新成功");
        return accessToken;
    }

    /**
     * 解析学生证文本
     */
    private OcrResultDTO parseStudentCardText(String text) {
        OcrResultDTO result = new OcrResultDTO();
        result.setSuccess(true);

        // 使用正则表达式提取信息
        // 姓名
        Pattern namePattern = Pattern.compile("姓名[：:](\\S+)");
        Matcher nameMatcher = namePattern.matcher(text);
        if (nameMatcher.find()) {
            result.setRealName(nameMatcher.group(1));
        }

        // 学校
        Pattern schoolPattern = Pattern.compile("(\\S+大学|\\S+学院)");
        Matcher schoolMatcher = schoolPattern.matcher(text);
        if (schoolMatcher.find()) {
            result.setUniversityName(schoolMatcher.group(1));
        }

        // 专业
        Pattern majorPattern = Pattern.compile("专业[：:](\\S+)");
        Matcher majorMatcher = majorPattern.matcher(text);
        if (majorMatcher.find()) {
            result.setMajor(majorMatcher.group(1));
        }

        // 学号
        Pattern idPattern = Pattern.compile("学号[：:](\\d+)");
        Matcher idMatcher = idPattern.matcher(text);
        if (idMatcher.find()) {
            result.setStudentId(idMatcher.group(1));
        }

        // 入学年份
        Pattern yearPattern = Pattern.compile("(20\\d{2})年");
        Matcher yearMatcher = yearPattern.matcher(text);
        if (yearMatcher.find()) {
            result.setEnrollYear(Integer.parseInt(yearMatcher.group(1)));
        }

        return result;
    }

    /**
     * 从百度返回结果中提取字段值
     */
    private String getWordsValue(JSONObject wordsResult, String key) {
        if (wordsResult.containsKey(key)) {
            return wordsResult.getJSONObject(key).getStr("words");
        }
        return null;
    }

    /**
     * 模拟学生证识别结果
     */
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

    /**
     * 模拟身份证识别结果
     */
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
