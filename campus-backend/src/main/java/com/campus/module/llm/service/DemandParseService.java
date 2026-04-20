package com.campus.module.llm.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import com.campus.module.llm.dto.DemandParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 需求智能解析服务
 * 使用LLM解析自然语言描述的家教需求
 */
@Service
public class DemandParseService {

    private static final Logger log = LoggerFactory.getLogger(DemandParseService.class);
    
    private final LlmClientService llmClient;
    
    public DemandParseService(LlmClientService llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * 系统提示词 - 定义LLM如何解析需求
     */
    /**
     * 系统提示词 - 定义LLM如何解析需求
     */
    private static final String SYSTEM_PROMPT = """
            你是一个家教需求解析助手。你的任务是从用户的自然语言描述中提取结构化的家教需求信息。
            
            请从用户输入中提取以下信息，并以JSON格式返回：
            
            {
              "subject": "辅导科目。必须且只能从以下列表中选择一项：['钢琴/乐器陪练', '美术/书法', '声乐/视唱练耳', '中考体育专项', '羽毛球/网球陪练', '篮球/足球指导', '少儿编程(Scratch/Python)', '机器人/3D打印', '科学实验/航模']。如果用户的描述不完全匹配，请推理并归类到最接近的一项",
              "grade": "年级，如：小学一年级、初一、高二等",
              "expectPrice": 数字，期望时薪（元/小时），如果用户说'左右'则取中间值,
              "teachMode": 数字，1表示上门、2表示网课、3表示均可,
              "preferGender": 数字，1表示男教员、2表示女教员、null表示不限,
              "educations": [数字数组]，学历要求：1本科在读 2本科毕业 3硕士在读 4硕士毕业 5博士,
              "scheduleRequire": "时间要求描述，如：周末上午、工作日晚上等",
              "address": "地址信息，如果提到的话",
              "detail": "其他具体要求或补充信息",
              "confidence": 0.0-1.0之间的数字，表示解析的置信度
            }
            
            🚨 注意事项（严格遵守）：
            1. 如果某项信息用户没有提及，则设为 null。
            2. 学历映射：本科在读=1，本科毕业=2，硕士在读=3，硕士毕业=4，博士=5。
            3. 价格如果是范围，取中间值；如果说"左右"，直接取那个数值。
            4. 【关于 detail 字段的特殊指令】：请将原文中对教师的额外要求（如：性格、学历背景、特定经验等）填入 detail。但是，'性别要求'（男/女）已经独立提取到了 preferGender 中，千万不要将性别要求写入 detail 字段！如果用户除了性别之外，没有提出任何其他对教师的额外要求，请严格将 detail 字段输出为 null 或空字符串，绝不要编造或重复已有信息！
            5. 只返回JSON，不要有其他文字。
            """;

    /**
     * 解析用户需求
     *
     * @param text 用户输入的自然语言描述
     * @return 解析结果
     */
    public DemandParseResult parse(String text) {
        DemandParseResult result = new DemandParseResult();
        result.setOriginalText(text);

        // 首先尝试使用规则解析（作为后备方案）
        DemandParseResult ruleResult = parseByRules(text);

        // 尝试使用LLM解析
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT));
        messages.add(ChatMessage.user(text));

        ChatResponse response = llmClient.chat(messages);

        if (response.getSuccess() && response.getContent() != null) {
            try {
                // 提取JSON部分
                String jsonStr = extractJson(response.getContent());
                JSONObject json = JSONUtil.parseObj(jsonStr);

                result.setSuccess(true);
                result.setSubject(json.getStr("subject"));
                result.setGrade(json.getStr("grade"));

                if (json.get("expectPrice") != null) {
                    result.setExpectPrice(new BigDecimal(json.get("expectPrice").toString()));
                }

                result.setTeachMode(json.getInt("teachMode"));
                result.setPreferGender(json.getInt("preferGender"));

                if (json.getJSONArray("educations") != null) {
                    result.setEducations(json.getJSONArray("educations").toList(Integer.class));
                }

                result.setScheduleRequire(json.getStr("scheduleRequire"));
                result.setAddress(json.getStr("address"));
                result.setDetail(json.getStr("detail"));
                result.setConfidence(json.getDouble("confidence"));

                // 如果LLM解析结果缺失，用规则解析结果补充
                mergeResults(result, ruleResult);

                return result;

            } catch (Exception e) {
                log.warn("LLM响应解析失败: {}", e.getMessage());
                // LLM解析失败，使用规则解析结果
                ruleResult.setSuggestion("智能解析部分失败，已使用规则解析");
                return ruleResult;
            }
        } else {
            // LLM不可用，使用规则解析
            ruleResult.setSuggestion("智能服务暂不可用，已使用规则解析: " + 
                    (response.getError() != null ? response.getError() : ""));
            return ruleResult;
        }
    }

    /**
     * 使用规则进行解析（后备方案）
     */
    private DemandParseResult parseByRules(String text) {
        DemandParseResult result = new DemandParseResult();
        result.setOriginalText(text);
        result.setSuccess(true);
        result.setConfidence(0.6);

        // 科目解析
        if (text.contains("钢琴") || text.contains("乐器") || text.contains("吉他") || text.contains("小提琴")) {
            result.setSubject("钢琴/乐器陪练");
        } else if (text.contains("美术") || text.contains("画画") || text.contains("画") || text.contains("书法")) {
            result.setSubject("美术/书法");
        } else if (text.contains("唱歌") || text.contains("声乐") || text.contains("视唱")) {
            result.setSubject("声乐/视唱练耳");
        } else if (text.contains("体育") || text.contains("中考") || text.contains("跑步") || text.contains("跳绳")) {
            result.setSubject("中考体育专项");
        } else if (text.contains("羽毛") || text.contains("网球")) {
            result.setSubject("羽毛球/网球陪练");
        } else if (text.contains("篮球") || text.contains("足球")) {
            result.setSubject("篮球/足球指导");
        } else if (text.contains("编程") || text.contains("Python") || text.contains("Scratch") || text.contains("代码")) {
            result.setSubject("少儿编程(Scratch/Python)");
        } else if (text.contains("机器") || text.contains("3D打印") || text.contains("乐高")) {
            result.setSubject("机器人/3D打印");
        } else if (text.contains("科学") || text.contains("实验") || text.contains("航模")) {
            result.setSubject("科学实验/航模");
        }

        // 年级解析
        Pattern gradePattern = Pattern.compile("(小学[一二三四五六]年级|初[一二三]|高[一二三]|大[一二三四]|学前|幼儿园)");
        Matcher gradeMatcher = gradePattern.matcher(text);
        if (gradeMatcher.find()) {
            result.setGrade(gradeMatcher.group(1));
        }

        // 价格解析
        Pattern pricePattern = Pattern.compile("(\\d+)\\s*(元|块|左右)?\\s*(/|每)?\\s*(小时|课时|h)?");
        Matcher priceMatcher = pricePattern.matcher(text);
        if (priceMatcher.find()) {
            result.setExpectPrice(new BigDecimal(priceMatcher.group(1)));
        }

        // 授课方式解析
        if (text.contains("上门")) {
            result.setTeachMode(1);
        } else if (text.contains("网课") || text.contains("线上") || text.contains("远程")) {
            result.setTeachMode(2);
        }

        // 性别偏好解析
        if (text.contains("女老师") || text.contains("女教师") || text.contains("女教员")) {
            result.setPreferGender(2);
        } else if (text.contains("男老师") || text.contains("男教师") || text.contains("男教员")) {
            result.setPreferGender(1);
        }

        // 学历解析
        List<Integer> educations = new ArrayList<>();
        if (text.contains("本科")) {
            educations.add(2);
        }
        if (text.contains("硕士") || text.contains("研究生")) {
            educations.add(4);
        }
        if (text.contains("博士")) {
            educations.add(5);
        }
        if (!educations.isEmpty()) {
            result.setEducations(educations);
        }

        // 时间解析
        Pattern timePattern = Pattern.compile("(周[一二三四五六日末]|工作日|每天|晚上|上午|下午|傍晚)");
        Matcher timeMatcher = timePattern.matcher(text);
        StringBuilder timeStr = new StringBuilder();
        while (timeMatcher.find()) {
            if (timeStr.length() > 0) timeStr.append(" ");
            timeStr.append(timeMatcher.group(1));
        }
        if (timeStr.length() > 0) {
            result.setScheduleRequire(timeStr.toString());
        }

        return result;
    }

    /**
     * 合并LLM和规则解析的结果
     */
    private void mergeResults(DemandParseResult llmResult, DemandParseResult ruleResult) {
        if (llmResult.getSubject() == null) {
            llmResult.setSubject(ruleResult.getSubject());
        }
        if (llmResult.getGrade() == null) {
            llmResult.setGrade(ruleResult.getGrade());
        }
        if (llmResult.getExpectPrice() == null) {
            llmResult.setExpectPrice(ruleResult.getExpectPrice());
        }
        if (llmResult.getTeachMode() == null) {
            llmResult.setTeachMode(ruleResult.getTeachMode());
        }
        if (llmResult.getPreferGender() == null) {
            llmResult.setPreferGender(ruleResult.getPreferGender());
        }
        if (llmResult.getEducations() == null || llmResult.getEducations().isEmpty()) {
            llmResult.setEducations(ruleResult.getEducations());
        }
        if (llmResult.getScheduleRequire() == null) {
            llmResult.setScheduleRequire(ruleResult.getScheduleRequire());
        }
    }

    /**
     * 从LLM响应中提取JSON
     */
    private String extractJson(String content) {
        // 尝试直接解析
        content = content.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            return content;
        }

        // 提取代码块中的JSON
        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 提取花括号内的内容
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }

        return content;
    }
}
