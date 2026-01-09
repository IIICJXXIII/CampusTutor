package com.campus.module.ocr.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.module.ocr.dto.OcrResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 豆包视觉大模型服务
 * 用于智能解析学生证OCR结果，带正则解析备选方案
 */
@Slf4j
@Service
public class DoubaoVisionService {

    @Value("${doubao.vision.api-key:0134f705-9ea1-42e6-99ec-5d9156af5cac}")
    private String apiKey;

    @Value("${doubao.vision.model:doubao-seed-1-8-251228}")
    private String model;

    @Value("${doubao.vision.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    @Value("${doubao.vision.enabled:true}")
    private boolean enabled;

    /**
     * 使用视觉大模型解析学生证文本（带智能正则备选）
     */
    public OcrResultDTO parseStudentCardWithLLM(String ocrText) {
        // 先尝试LLM解析
        if (enabled) {
            try {
                String prompt = buildParsePrompt(ocrText);
                String llmResponse = callDoubaoApi(prompt);

                if (llmResponse != null) {
                    OcrResultDTO result = parseJsonResponse(llmResponse);
                    if (result.getSuccess()) {
                        return result;
                    }
                }
            } catch (Exception e) {
                log.warn("LLM解析失败，使用正则备选: {}", e.getMessage());
            }
        }

        // LLM失败或未启用时，使用智能正则解析
        log.info("使用智能正则解析OCR文本");
        return parseWithRegex(ocrText);
    }

    /**
     * 智能正则解析（备选方案）
     */
    private OcrResultDTO parseWithRegex(String text) {
        OcrResultDTO result = new OcrResultDTO();
        result.setSuccess(true);

        String[] lines = text.split("\n");

        // 解析姓名：找到"姓名"后的第一个中文名字
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("姓名") && i + 1 < lines.length) {
                // 跳过"学生假期火车优惠证"等干扰项
                for (int j = i + 1; j < Math.min(i + 3, lines.length); j++) {
                    String candidate = lines[j].trim();
                    if (candidate.length() >= 2 && candidate.length() <= 4
                            && !candidate.contains("证") && !candidate.contains("卡")) {
                        result.setRealName(candidate);
                        break;
                    }
                }
                break;
            }
        }

        // 解析学号：找到连续数字（10位以上）
        Pattern idPattern = Pattern.compile("\\d{10,}");
        Matcher idMatcher = idPattern.matcher(text);
        if (idMatcher.find()) {
            result.setStudentId(idMatcher.group());
        }

        // 解析学校/学院
        for (String line : lines) {
            if (line.contains("学院") && !line.contains("使用") && !line.contains("内页")) {
                result.setUniversityName(line.replace("·勿折、妥存", "").trim());
                break;
            }
            if (line.contains("大学")) {
                result.setUniversityName(line.trim());
                break;
            }
        }

        // 解析专业
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("专") || lines[i].contains("业")) {
                // 查找"专"和"业"附近的专业名称
                for (int j = i; j < Math.min(i + 3, lines.length); j++) {
                    String line = lines[j].trim();
                    if (line.contains("工程") || line.contains("科学") || line.contains("技术")
                            || line.contains("管理") || line.contains("设计") || line.contains("医学")
                            || line.contains("教育") || line.contains("艺术") || line.contains("法学")) {
                        result.setMajor(line.replace(",", "").replace("，", "").trim());
                        break;
                    }
                }
                if (result.getMajor() != null)
                    break;
            }
        }

        // 解析入学年份
        Pattern yearPattern = Pattern.compile("入学日期[\\s,，]*(20\\d{2})");
        Matcher yearMatcher = yearPattern.matcher(text.replace("\n", " "));
        if (yearMatcher.find()) {
            result.setEnrollYear(Integer.parseInt(yearMatcher.group(1)));
        } else {
            // 备选：查找20xx年
            Pattern yearPattern2 = Pattern.compile("(20\\d{2})年");
            Matcher yearMatcher2 = yearPattern2.matcher(text);
            if (yearMatcher2.find()) {
                result.setEnrollYear(Integer.parseInt(yearMatcher2.group(1)));
            }
        }

        log.info("正则解析结果: 姓名={}, 学号={}, 学院={}, 专业={}, 入学年份={}",
                result.getRealName(), result.getStudentId(), result.getUniversityName(),
                result.getMajor(), result.getEnrollYear());

        return result;
    }

    private String buildParsePrompt(String ocrText) {
        return "你是一个学生证信息提取助手。请从以下OCR识别的学生证文本中提取关键信息，并以JSON格式返回。\n\n" +
                "OCR识别文本：\n" + ocrText + "\n\n" +
                "请提取以下字段（如果无法识别则返回null）：\n" +
                "- realName: 姓名\n" +
                "- universityName: 学校名称\n" +
                "- major: 专业\n" +
                "- studentId: 学号\n" +
                "- enrollYear: 入学年份（仅数字，如2023）\n\n" +
                "只返回JSON，不要其他文字。格式：\n" +
                "{\"realName\":\"张三\",\"universityName\":\"北京大学\",\"major\":\"计算机科学\",\"studentId\":\"2023001\",\"enrollYear\":2023}";
    }

    private String callDoubaoApi(String prompt) {
        String url = baseUrl + "/chat/completions";

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);
        requestBody.set("max_tokens", 500);
        requestBody.set("temperature", 0.1);

        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", prompt);
        messages.add(userMessage);
        requestBody.set("messages", messages);

        log.debug("豆包请求: {}", requestBody);

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(30000)
                .execute();

        String responseBody = response.body();
        log.debug("豆包响应: {}", responseBody);

        if (!response.isOk()) {
            log.error("豆包API调用失败: {} - {}", response.getStatus(), responseBody);
            return null;
        }

        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getStr("content");
        }

        return null;
    }

    private OcrResultDTO parseJsonResponse(String jsonStr) {
        OcrResultDTO result = new OcrResultDTO();

        try {
            int start = jsonStr.indexOf("{");
            int end = jsonStr.lastIndexOf("}");
            if (start >= 0 && end > start) {
                jsonStr = jsonStr.substring(start, end + 1);
            }

            JSONObject json = JSONUtil.parseObj(jsonStr);

            result.setSuccess(true);
            result.setRealName(json.getStr("realName"));
            result.setUniversityName(json.getStr("universityName"));
            result.setMajor(json.getStr("major"));
            result.setStudentId(json.getStr("studentId"));

            Object enrollYear = json.get("enrollYear");
            if (enrollYear != null) {
                if (enrollYear instanceof Integer) {
                    result.setEnrollYear((Integer) enrollYear);
                } else {
                    try {
                        result.setEnrollYear(Integer.parseInt(enrollYear.toString()));
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }

            log.info("学生证LLM解析成功: 姓名={}, 学校={}, 专业={}",
                    result.getRealName(), result.getUniversityName(), result.getMajor());

        } catch (Exception e) {
            log.error("JSON解析失败: {}", jsonStr, e);
            result.setSuccess(false);
            result.setErrorMsg("JSON解析失败");
        }

        return result;
    }
}
