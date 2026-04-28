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

import java.io.File;
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

    @Autowired
    private DoubaoVisionService doubaoVisionService;

    @Value("${baidu.ocr.api-key:}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key:}")
    private String secretKey;

    @Value("${baidu.ocr.enabled:false}")
    private boolean enabled;
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

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

        // 检查imageUrl是否为空
        if (StrUtil.isBlank(imageUrl)) {
            result.setSuccess(false);
            result.setErrorMsg("图片URL不能为空");
            log.error("识别学生证失败: 图片URL为空");
            return result;
        }

        if (!enabled) {
            result.setSuccess(false);
            result.setErrorMsg("OCR服务未启用");
            log.warn("OCR服务未启用，无法识别图片: {}", imageUrl);
            return result;
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

        // 检查imageUrl是否为空
        if (StrUtil.isBlank(imageUrl)) {
            result.setSuccess(false);
            result.setErrorMsg("图片URL不能为空");
            log.error("识别身份证正面失败: 图片URL为空");
            return result;
        }

        log.info("开始识别身份证正面，图片URL: {}", imageUrl);

        if (!enabled) {
            result.setSuccess(false);
            result.setErrorMsg("OCR服务未启用");
            log.warn("OCR服务未启用，无法识别身份证正面: {}", imageUrl);
            return result;
        }

        try {
            String token = getAccessToken();
            if (token == null) {
                log.error("无法获取Access Token，OCR服务不可用");
                result.setSuccess(false);
                result.setErrorMsg("OCR服务不可用");
                return result;
            }
            
            // 使用Base64方式，避免百度OCR服务器无法访问本地URL
            log.info("使用Base64方式处理身份证正面图片: {}", imageUrl);
            String base64Image = downloadImageToBase64(imageUrl);
            if (base64Image == null) {
                log.error("下载图片失败: {}", imageUrl);
                result.setSuccess(false);
                result.setErrorMsg("图片下载失败");
                return result;
            }
            
            String url = IDCARD_URL + "?access_token=" + token;

            // 处理Base64数据
            String base64Data = base64Image.trim();
            if (base64Data.startsWith("\"")) base64Data = base64Data.substring(1);
            if (base64Data.endsWith("\"")) base64Data = base64Data.substring(0, base64Data.length() - 1);
            if (base64Data.contains(",")) base64Data = base64Data.substring(base64Data.indexOf(",") + 1);

            Map<String, Object> params = new HashMap<>();
            params.put("image", base64Data);
            params.put("id_card_side", "front");

            HttpResponse response = HttpRequest.post(url)
                    .form(params)
                    .timeout(15000)
                    .execute();

            JSONObject json = JSONUtil.parseObj(response.body());
            if (json.containsKey("error_code")) {
                String errorMsg = json.getStr("error_msg");
                log.error("身份证正面OCR API错误: {}", errorMsg);
                result.setSuccess(false);
                result.setErrorMsg("OCR识别失败: " + errorMsg);
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
            log.error("OCR服务未启用");
            return null;
        }

        try {
            String token = getAccessToken();
            if (token == null) {
                log.error("无法获取Access Token，OCR服务不可用");
                return null;
            }
            
            // 直接使用Base64方式，避免URL方式的参数问题
            log.info("使用Base64方式处理图片: {}", imageUrl);
            String base64Image = downloadImageToBase64(imageUrl);
            if (base64Image != null) {
                String text = recognizeGeneralByBase64(base64Image, token);
                if (text != null) {
                    return text;
                }
            }
            
            log.error("Base64方式OCR识别失败");
            return null;
            
        } catch (Exception e) {
            log.error("通用OCR识别请求失败", e);
            return null;
        }
    }
    
    /**
     * 通过URL方式识别通用文字
     */
    private String recognizeGeneralByUrl(String imageUrl, String token) {
        try {
            String url = GENERAL_URL + "?access_token=" + token;
            Map<String, Object> params = new HashMap<>();
            params.put("url", imageUrl);

            HttpResponse response = HttpRequest.post(url)
                    .timeout(15000)
                    .form(params)
                    .execute();

            JSONObject json = JSONUtil.parseObj(response.body());
            if (json.containsKey("error_code")) {
                log.error("OCR URL方式错误: {}", json.getStr("error_msg"));
                return null;
            }

            StringBuilder sb = new StringBuilder();
            json.getJSONArray("words_result").forEach(item -> {
                JSONObject obj = (JSONObject) item;
                sb.append(obj.getStr("words")).append("\n");
            });

            return sb.toString();
        } catch (Exception e) {
            log.error("URL方式OCR识别失败", e);
            return null;
        }
    }
    
    /**
     * 通过Base64方式识别通用文字
     */
    private String recognizeGeneralByBase64(String base64Image, String token) {
        try {
            String url = GENERAL_URL + "?access_token=" + token;
            
            // 处理Base64数据
            String base64Data = base64Image.trim();
            if (base64Data.startsWith("\"")) base64Data = base64Data.substring(1);
            if (base64Data.endsWith("\"")) base64Data = base64Data.substring(0, base64Data.length() - 1);
            if (base64Data.contains(",")) base64Data = base64Data.substring(base64Data.indexOf(",") + 1);

            HttpResponse response = HttpRequest.post(url)
                    .form("image", base64Data)
                    .timeout(15000)
                    .execute();

            JSONObject json = JSONUtil.parseObj(response.body());
            if (json.containsKey("error_code")) {
                log.error("OCR Base64方式错误: {}", json.getStr("error_msg"));
                return null;
            }

            StringBuilder sb = new StringBuilder();
            json.getJSONArray("words_result").forEach(item -> {
                JSONObject obj = (JSONObject) item;
                sb.append(obj.getStr("words")).append("\n");
            });

            return sb.toString();
        } catch (Exception e) {
            log.error("Base64方式OCR识别失败", e);
            return null;
        }
    }
    
    /**
     * 下载图片并转换为Base64
     */
    private String downloadImageToBase64(String imageUrl) {
        try {
            // 如果是本地URL，需要处理一下
            if (imageUrl.contains("localhost") || imageUrl.contains("127.0.0.1")) {
                // 本地文件，直接读取
                String filePath = imageUrl.replace("http://localhost:8080/uploads", uploadPath);
                File file = new File(filePath);
                if (file.exists()) {
                    byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
                    return java.util.Base64.getEncoder().encodeToString(fileBytes);
                }
            }
            
            // 远程URL，下载
            HttpResponse response = HttpRequest.get(imageUrl)
                    .timeout(10000)
                    .execute();
            
            if (response.getStatus() == 200) {
                byte[] imageBytes = response.bodyBytes();
                return java.util.Base64.getEncoder().encodeToString(imageBytes);
            }
            
            return null;
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
            return null;
        }
    }

    @Override
    public OcrResultDTO recognizeStudentCardByBase64(String imageBase64) {
        OcrResultDTO result = new OcrResultDTO();

        if (!enabled) {
            result.setSuccess(false);
            result.setErrorMsg("OCR服务未启用");
            log.warn("OCR服务未启用，无法识别学生证(Base64)");
            return result;
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
            log.error("OCR服务未启用");
            return null;
        }

        try {
            String token = getAccessToken();
            if (token == null) {
                log.error("无法获取Access Token，OCR服务不可用");
                return null;
            }
            
            String url = GENERAL_URL + "?access_token=" + token;

            String base64Data = imageBase64.trim();
            // 处理Base64前缀
            if (base64Data.startsWith("\"")) base64Data = base64Data.substring(1);
            if (base64Data.endsWith("\"")) base64Data = base64Data.substring(0, base64Data.length() - 1);
            if (base64Data.contains(",")) base64Data = base64Data.substring(base64Data.indexOf(",") + 1);

            HttpResponse response = HttpRequest.post(url)
                    .form("image", base64Data)
                    .timeout(15000)
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
        // 检查API Key和Secret Key是否配置
        if (StrUtil.isBlank(apiKey) || StrUtil.isBlank(secretKey)) {
            log.warn("百度OCR API Key或Secret Key未配置，使用模拟模式");
            enabled = false;
            return null;
        }
        
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        try {
            String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;

            HttpResponse response = HttpRequest.post(url)
                    .timeout(10000) // 10秒超时
                    .execute();
            
            if (response.getStatus() != 200) {
                log.error("获取Access Token失败，HTTP状态码: {}", response.getStatus());
                throw new RuntimeException("获取Access Token失败: HTTP " + response.getStatus());
            }
            
            JSONObject json = JSONUtil.parseObj(response.body());

            if (json.containsKey("error")) {
                String errorMsg = json.getStr("error_description");
                log.error("获取Access Token失败: {}", errorMsg);
                throw new RuntimeException("获取Access Token失败: " + errorMsg);
            }

            accessToken = json.getStr("access_token");
            int expiresIn = json.getInt("expires_in", 2592000); // 默认30天
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
            log.info("获取Access Token成功，有效期: {}秒", expiresIn);
            return accessToken;
        } catch (Exception e) {
            log.error("获取Access Token异常，降级到模拟模式", e);
            enabled = false; // 禁用OCR服务，使用模拟数据
            return null;
        }
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