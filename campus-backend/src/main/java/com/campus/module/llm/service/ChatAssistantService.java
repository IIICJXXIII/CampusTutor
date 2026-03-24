package com.campus.module.llm.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import com.campus.module.map.service.MapService;
import com.campus.module.match.dto.TutorSearchRequest;
import com.campus.module.match.dto.TutorSearchResult;
import com.campus.module.match.service.MatchService;
import com.campus.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能客服服务
 * 提供家教平台相关的问答服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantService {

    private final LlmClientService llmClient;
    private final MatchService matchService;
    private final MapService mapService;
    private final ChatContextManager contextManager;

    /**
     * 需求咨询场景的系统提示词
     */
    private static final String DEMAND_SYSTEM_PROMPT = """
            你是"校园智教"家教平台的AI助手。你的任务是帮助家长发布家教需求。

            平台功能介绍：
            1. 家长可以发布家教需求，描述孩子的年级、科目、学习问题等
            2. 系统会智能匹配合适的大学生教员
            3. 家长可以查看教员的学校、专业、教学评价等信息
            4. 确认后可以预约试课、签约正式课程

            你需要：
            1. 引导家长描述孩子的学习需求（年级、科目、学习困难等）
            2. 询问对教员的期望（性别、学历、价格等）
            3. 确认授课方式（上门/网课）和时间安排
            4. 收集完信息后，告知家长可以提交需求了

            回复要简洁友好，不要太长。用中文回复。
            """;

    /**
     * 教员推荐场景的系统提示词
     */
    private static final String TUTOR_SYSTEM_PROMPT = """
            你是"校园智教"家教平台的AI助手。你的任务是帮助家长了解和选择合适的教员。

            你需要：
            1. 解答关于教员资质、认证流程的问题
            2. 说明平台的教员筛选标准
            3. 帮助家长理解如何查看教员评价
            4. 解释试课、签约、退费等流程

            回复要专业、简洁。用中文回复。
            """;

    /**
     * 通用问答场景的系统提示词
     */
    private static final String GENERAL_SYSTEM_PROMPT = """
            你是"校园智教"家教平台的AI客服助手。

            平台介绍：
            - 这是一个连接家长和大学生教员的家教服务平台
            - 所有教员都经过实名认证和学历认证
            - 支持上门家教和在线网课两种授课方式
            - 提供课时托管和评价系统保障服务质量

            你可以回答：
            - 平台使用问题
            - 发布需求流程
            - 教员认证流程
            - 支付和退费政策
            - 安全保障措施

            回复要友好、简洁、专业。用中文回复。如果问题超出你的知识范围，建议联系人工客服。
            """;

    /**
     * 智能对话
     *
     * @param messages        历史消息
     * @param scene           场景: demand, tutor, general
     * @param previousSummary 上一次的历史摘要（可选）
     * @return 回复
     */
    public ChatResponse chat(List<ChatMessage> messages, String scene, String previousSummary) {
        // 根据场景选择系统提示词
        String systemPrompt = switch (scene) {
            case "demand" -> DEMAND_SYSTEM_PROMPT;
            case "tutor" -> TUTOR_SYSTEM_PROMPT;
            default -> GENERAL_SYSTEM_PROMPT;
        };

        // 使用上下文管理器处理历史消息（滑动窗口 + 摘要压缩）
        ChatContextManager.ManagedContext managedContext =
                contextManager.manageContext(messages, previousSummary);

        // 构建完整消息列表
        List<ChatMessage> fullMessages = new ArrayList<>();
        fullMessages.add(ChatMessage.system(systemPrompt));

        // 如果有历史摘要，作为系统消息注入，为LLM提供长期记忆
        if (managedContext.hasSummary()) {
            fullMessages.add(ChatMessage.system(
                    "【之前的对话摘要】" + managedContext.getSummary()));
        }

        fullMessages.addAll(managedContext.getMessages());

        JSONArray tools = null;
        if ("tutor".equals(scene) || "general".equals(scene)) {
            tools = buildTutorSearchTool();
        }

        int maxDepth = 3;
        while (maxDepth-- > 0) {
            ChatResponse response = llmClient.chat(fullMessages, tools);

            if (!response.getSuccess() || !response.hasToolCalls()) {
                // 将最新的摘要附加到响应中，供前端存储
                response.setSummary(managedContext.getSummary());
                return response;
            }

            JSONArray toolCalls = response.getToolCalls();
            fullMessages.add(ChatMessage.assistantWithTool(toolCalls));

            for (int i = 0; i < toolCalls.size(); i++) {
                JSONObject toolCall = toolCalls.getJSONObject(i);
                if ("function".equals(toolCall.getStr("type"))) {
                    JSONObject function = toolCall.getJSONObject("function");
                    String functionName = function.getStr("name");
                    String toolCallId = toolCall.getStr("id");

                    if ("search_tutors".equals(functionName)) {
                        String argumentsStr = function.getStr("arguments");
                        JSONObject arguments = JSONUtil.parseObj(argumentsStr);
                        String subject = arguments.getStr("subject");
                        String grade = arguments.getStr("grade");
                        Integer teachMode = arguments.getInt("teachMode");
                        String location = arguments.getStr("location");
                        Integer maxPrice = arguments.getInt("maxPrice");
                        Integer gender = arguments.getInt("gender");
                        String keyword = arguments.getStr("keyword");

                        try {
                            TutorSearchRequest request = new TutorSearchRequest();
                            request.setSubject(subject);
                            request.setGrade(grade);
                            if (teachMode != null) {
                                request.setTeachMode(teachMode);
                            }
                            if (gender != null) {
                                request.setGender(gender);
                            }
                            if (maxPrice != null) {
                                request.setMaxPrice(java.math.BigDecimal.valueOf(maxPrice));
                            }
                            if (keyword != null && !keyword.trim().isEmpty()) {
                                request.setKeyword(keyword);
                            }
                            if (location != null && !location.trim().isEmpty()) {
                                com.campus.module.map.dto.GeocoderResult geoResult = mapService.geocode(location);
                                if (geoResult != null && geoResult.getStatus() != null && geoResult.getStatus() == 0) {
                                    if (geoResult.getResultData() != null && geoResult.getResultData().getLocation() != null) {
                                        request.setLatitude(geoResult.getResultData().getLocation().getLat());
                                        request.setLongitude(geoResult.getResultData().getLocation().getLng());
                                        request.setRadius(15.0);
                                    }
                                }
                            }
                            
                            request.setPage(1);
                            // 如果有专门的细节要求，多查出几条数据给大模型做阅读理解和筛选
                            request.setSize(keyword != null && !keyword.trim().isEmpty() ? 8 : 3);

                            IPage<TutorSearchResult> matchResult = matchService.searchTutors(request);

                            List<JSONObject> simplifyResults = new ArrayList<>();
                            for (TutorSearchResult result : matchResult.getRecords()) {
                                JSONObject obj = new JSONObject();
                                obj.set("realName", result.getRealName());
                                obj.set("universityName", result.getUniversityName());
                                obj.set("major", result.getMajor());
                                
                                // 加入自我介绍，截断防止token超限
                                String intro = result.getIntroduction();
                                if (intro != null) {
                                    if (intro.length() > 200) {
                                        intro = intro.substring(0, 200) + "...";
                                    }
                                    obj.set("introduction", intro);
                                }

                                if (result instanceof com.campus.module.match.dto.MatchScoreResult scoreResult) {
                                    obj.set("matchScore", scoreResult.getMatchScore());
                                    obj.set("matchTags", scoreResult.getMatchTags());
                                }
                                simplifyResults.add(obj);
                            }
                            
                            String finalResultJson;
                            if (keyword != null && !keyword.trim().isEmpty()) {
                                JSONObject responseObj = new JSONObject();
                                responseObj.set("system_instruction", "用户提出了额外的细节要求：'" + keyword + "'。请你仔细阅读以下候选教员的'introduction'(自我介绍)，依靠你的语义理解判断哪些教员的经历符合该要求（例如'辅导过高考'在语义上等于'高考经验'）。如果找到符合的，优先推荐他们；如果所有候选人的自我介绍都不符合，请如实说明'目前没有完全符合该细节要求的老师'，但依然推荐列表中最优秀的几位作为备选。");
                                responseObj.set("results", simplifyResults);
                                finalResultJson = responseObj.toString();
                            } else {
                                finalResultJson = JSONUtil.toJsonStr(simplifyResults);
                            }

                            fullMessages.add(
                                    ChatMessage.toolResult(toolCallId, finalResultJson));

                        } catch (Exception e) {
                            log.error("执行本地工具search_tutors失败", e);
                            fullMessages.add(ChatMessage.toolResult(toolCallId, "{\"error\": \"内部执行失败\"}"));
                        }
                    } else if ("recommend_nearby_tutors".equals(functionName)) {
                        String argumentsStr = function.getStr("arguments");
                        JSONObject arguments = JSONUtil.parseObj(argumentsStr);
                        String location = arguments.getStr("location");
                        String subject = arguments.getStr("subject");

                        try {
                            double lat = 39.90923; // 默认北京
                            double lng = 116.397428;

                            if (location != null && !location.trim().isEmpty()) {
                                com.campus.module.map.dto.GeocoderResult geoResult = mapService.geocode(location);
                                if (geoResult != null && geoResult.getStatus() != null && geoResult.getStatus() == 0) {
                                    if (geoResult.getResultData() != null && geoResult.getResultData().getLocation() != null) {
                                        lat = geoResult.getResultData().getLocation().getLat();
                                        lng = geoResult.getResultData().getLocation().getLng();
                                    }
                                }
                            }

                            Long userId = UserContext.getUserId();
                            if (userId == null) {
                                userId = 0L;
                            }

                            List<TutorSearchResult> recommendedTutors = matchService.getRecommendedTutors(userId, lng, lat, 15.0, subject);

                            if (recommendedTutors.size() > 5) {
                                recommendedTutors = recommendedTutors.subList(0, 5);
                            }

                            List<JSONObject> simplifyResults = new ArrayList<>();
                            for (TutorSearchResult result : recommendedTutors) {
                                JSONObject obj = new JSONObject();
                                obj.set("realName", result.getRealName());
                                obj.set("universityName", result.getUniversityName());
                                obj.set("major", result.getMajor());
                                
                                String intro = result.getIntroduction();
                                if (intro != null) {
                                    if (intro.length() > 200) {
                                        intro = intro.substring(0, 200) + "...";
                                    }
                                    obj.set("introduction", intro);
                                }

                                if (result instanceof com.campus.module.match.dto.MatchScoreResult scoreResult) {
                                    obj.set("matchScore", scoreResult.getMatchScore());
                                    obj.set("deepFmScore", scoreResult.getDeepFmScore());
                                    obj.set("matchTags", scoreResult.getMatchTags());
                                }
                                simplifyResults.add(obj);
                            }

                            JSONObject responseObj = new JSONObject();
                            responseObj.set("system_instruction", "这是基于平台最新版 DeepFM 深度学习算法为您推荐的附近优质教员。请你向用户友善地解释：'我已经利用基于深度学习的智能推荐算法(DeepFM)，结合您的地理位置（" + (location != null ? location : "默认") + "）和科目需求找到了以下几位非常符合您的优质老师...' 然后再简单介绍。");
                            responseObj.set("results", simplifyResults);

                            fullMessages.add(ChatMessage.toolResult(toolCallId, responseObj.toString()));

                        } catch (Exception e) {
                            log.error("执行本地工具recommend_nearby_tutors失败", e);
                            fullMessages.add(ChatMessage.toolResult(toolCallId, "{\"error\": \"内部执行失败\"}"));
                        }
                    } else {
                        log.warn("大模型调用了未定义的工具: {}", functionName);
                        fullMessages.add(ChatMessage.toolResult(toolCallId, "{\"error\": \"未找到该工具，请使用已定义的工具或直接回复文本\"}"));
                    }
                }
            }
        }

        ChatResponse limitResponse = ChatResponse.fail("已达到最大工具调用次数限制，请明确您的需求。");
        limitResponse.setSummary(managedContext.getSummary());
        return limitResponse;
    }

    private JSONArray buildTutorSearchTool() {
        JSONArray toolsArray = new JSONArray();

        // 原本的 search_tutors
        JSONObject tool1 = new JSONObject();
        tool1.set("type", "function");

        JSONObject function1 = new JSONObject();
        function1.set("name", "search_tutors");
        function1.set("description", "当用户提出了明确的筛选条件（如明确指定了性别、期望价格、授课方式等）时使用此工具过滤匹配的家教老师。");

        JSONObject parameters1 = new JSONObject();
        parameters1.set("type", "object");

        JSONObject properties1 = new JSONObject();
        JSONObject subjectParam1 = new JSONObject();
        subjectParam1.set("type", "string");
        subjectParam1.set("description", "科目名称，如：数学、英语、物理");
        properties1.set("subject", subjectParam1);

        JSONObject gradeParam1 = new JSONObject();
        gradeParam1.set("type", "string");
        gradeParam1.set("description", "年级名称，如：初一、高二、小学");
        properties1.set("grade", gradeParam1);

        JSONObject teachModeParam1 = new JSONObject();
        teachModeParam1.set("type", "integer");
        teachModeParam1.set("description", "授课方式（1: 上门家教, 2: 在线网课）");
        properties1.set("teachMode", teachModeParam1);

        JSONObject locationParam1 = new JSONObject();
        locationParam1.set("type", "string");
        locationParam1.set("description", "上门家教的大致服务地址（如：北京市海淀区中关村大街）");
        properties1.set("location", locationParam1);

        JSONObject maxPriceParam1 = new JSONObject();
        maxPriceParam1.set("type", "integer");
        maxPriceParam1.set("description", "家长能接受的最高课时费（元/小时）");
        properties1.set("maxPrice", maxPriceParam1);

        JSONObject genderParam1 = new JSONObject();
        genderParam1.set("type", "integer");
        genderParam1.set("description", "期望的教员性别（1: 男的, 2: 女的）");
        properties1.set("gender", genderParam1);

        JSONObject keywordParam1 = new JSONObject();
        keywordParam1.set("type", "string");
        keywordParam1.set("description", "额外的细节要求（如果在用户描述中提及特殊的经验或背景，例如：高考辅导经验、耐心、考研等）。");
        properties1.set("keyword", keywordParam1);

        parameters1.set("properties", properties1);
        parameters1.set("required", new JSONArray().put("subject"));

        function1.set("parameters", parameters1);
        tool1.set("function", function1);
        
        toolsArray.put(tool1);

        // 新增的 recommend_nearby_tutors
        JSONObject tool2 = new JSONObject();
        tool2.set("type", "function");
        JSONObject function2 = new JSONObject();
        function2.set("name", "recommend_nearby_tutors");
        function2.set("description", "当用户的需求比较模糊（如：推荐、看看、好老师、附近的），或者用户没有给出具体的筛选条件时，优先使用此工具。此算法基于深度学习模型，使用用户的地理位置召回附近的优秀老师并进行隐式兴趣排分。");
        
        JSONObject params2 = new JSONObject();
        params2.set("type", "object");
        
        JSONObject props2 = new JSONObject();
        
        JSONObject locParam = new JSONObject();
        locParam.set("type", "string");
        locParam.set("description", "用户当前地理位置（如：北京市海淀区中关村）。这是一个硬性要求，如果用户没提供，大模型必须主动在对话中询问用户的位置。");
        props2.set("location", locParam);

        JSONObject subParam = new JSONObject();
        subParam.set("type", "string");
        subParam.set("description", "想找的辅导学科（如：数学、物理）。这是一个硬性要求，如果用户没提供，大模型必须主动在对话中询问用户的学科需求。");
        props2.set("subject", subParam);

        params2.set("properties", props2);
        // 通过设置 required 强制 LLM 在由于缺失这两个参数而无法调用工具时，主动追问用户
        params2.set("required", new JSONArray().put("location").put("subject"));
        
        function2.set("parameters", params2);
        tool2.set("function", function2);
        
        toolsArray.put(tool2);

        return toolsArray;
    }

    /**
     * 快速问答（无历史上下文）
     *
     * @param question 问题
     * @return 回答
     */
    public String quickAnswer(String question) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.user(question));

        ChatResponse response = chat(messages, "general", null);
        if (response.getSuccess()) {
            return response.getContent();
        } else {
            return "抱歉，暂时无法回答您的问题。您可以联系人工客服获取帮助。";
        }
    }
}
