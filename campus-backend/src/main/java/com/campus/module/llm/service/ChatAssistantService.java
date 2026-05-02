package com.campus.module.llm.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import com.campus.module.llm.entity.KnowledgeDocument;
import com.campus.module.map.service.MapService;
import com.campus.module.match.dto.TutorSearchRequest;
import com.campus.module.match.dto.TutorSearchResult;
import com.campus.module.match.service.MatchService;
import com.campus.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantService {

    private final LlmClientService llmClient;
    private final MatchService matchService;
    private final MapService mapService;
    private final ChatContextManager contextManager;
    private final KnowledgeRetrievalService knowledgeService;

    /**
     * 全局统一系统提示词
     * 将原来分散在 demand / tutor / general 三个场景中的能力融合到单一 Prompt 中，
     * 由大模型根据对话上下文自主决定调用哪些 Tools。
     */
    private static final String UNIFIED_SYSTEM_PROMPT = """
            你是"校园智教"素质教育家教平台的AI助手，名叫"Campus AI"。你具备以下全部能力，根据用户的意图自主选择最合适的行动方式。

            ═══════════════════════════
            ▍平台简介
            ═══════════════════════════
            • 这是一个专注于**素质教育**的家教服务平台，连接家长和大学生教员
            • 受国家"双减"政策影响，本平台**不提供**语文、数学、英语、物理、化学、生物、政治、历史、地理等学科类辅导
            • 平台提供 **三大类、九个方向** 的素质教育课程：
              ┌ 艺术素养：钢琴/乐器陪练、美术/书法、声乐/视唱练耳
              ├ 体育健康：中考体育专项、羽毛球/网球陪练、篮球/足球指导
              └ 科创STEAM：少儿编程(Scratch/Python)、机器人/3D打印、科学实验/航模
            • 所有教员都经过实名认证和学历认证
            • 支持上门教学和在线网课两种授课方式
            • 提供课时托管和评价系统保障服务质量

            ═══════════════════════════
            ▍你的核心能力
            ═══════════════════════════

            【能力一：需求收集 & 发布引导】
            当用户（通常是家长）想为孩子找老师时，你需要循序渐进地引导收集以下信息：
            1. 孩子的年级、想学的素质教育方向（从上述9个方向中选择）
            2. 对教员的期望（性别、学历、价格区间）
            3. 授课方式（上门/网课）和时间安排
            当用户提供了足够信息（至少有科目方向）后，你必须主动调用 search_tutors 工具实际搜索匹配教员，向用户展示搜索结果，而不是空口说"已记录需求"。

            【能力二：精准条件搜索】
            当用户提出了明确的筛选条件（如指定性别、价格上限、授课方式、科目方向等），调用 search_tutors 工具进行精准检索。

            【能力三：智能推荐】
            当用户的需求比较模糊（如：推荐好老师、看看有什么老师、附近有谁），调用 recommend_nearby_tutors 工具进行基于深度学习的推荐。此工具需要用户的地理位置和科目方向，如果用户未提供，必须主动追问。

            【能力四：平台问答 & 客服】
            你可以直接回答以下问题，无需调用任何工具：
            • 平台使用流程、注册问题
            • 教员认证和审核流程
            • 发布需求的步骤和注意事项
            • 支付方式、退费政策
            • 安全保障措施
            • 试课与签约流程
            • 各素质教育方向的课时费行情参考
            • 关于双减政策的解读（为什么不提供学科辅导）

            【能力五：教学辅助】
            你也可以回答素质教育相关的教学问题（如练琴技巧、体育训练方法、编程学习路径、和家长/学生沟通的技巧等）。

            ═══════════════════════════
            ▍行动准则
            ═══════════════════════════
            1. 当用户描述了至少包含【科目方向】的需求时，必须调用 search_tutors
            2. 当用户要求"推荐""看看"或需求模糊时，优先调用 recommend_nearby_tutors
            3. **如果用户询问学科类辅导（如数学、英语、物理等），你必须礼貌告知：根据国家"双减"政策，本平台不提供学科类辅导，但可以为孩子推荐优质的素质教育课程，并引导用户了解平台提供的9个素质教育方向**
            4. 平台知识类问题直接回答，绝不编造不确定的信息
            5. 回复要简洁友好，控制在合理长度
            6. 始终用中文回复
            """;

    /**
     * 智能对话（统一入口）
     *
     * @param messages        历史消息
     * @param scene           场景标识（已废弃，保留仅为向后兼容，不影响逻辑）
     * @param previousSummary 上一次的历史摘要（可选）
     * @return 回复
     */
    public ChatResponse chat(List<ChatMessage> messages, String scene, String previousSummary) {
        ChatContextManager.ManagedContext managedContext =
                contextManager.manageContext(messages, previousSummary);

        String userQuery = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).getRole())) {
                userQuery = messages.get(i).getContent();
                break;
            }
        }

        String role = "ALL";
        Long userId = UserContext.getUserId();
        if (userId != null) {
            Integer userRole = UserContext.getRole();
            if (userRole != null) {
                role = userRole == 1 ? "TEACHER" : "PARENT";
            }
        }

        List<KnowledgeDocument> relevantDocs = Collections.emptyList();
        if (!userQuery.isEmpty()) {
            try {
                relevantDocs = knowledgeService.retrieveRelevantDocs(userQuery, role);
                log.info("RAG检索到 {} 条相关文档", relevantDocs.size());
            } catch (Exception e) {
                log.warn("RAG检索失败，继续无知识库对话: {}", e.getMessage());
            }
        }

        String enhancedPrompt = UNIFIED_SYSTEM_PROMPT;
        if (!relevantDocs.isEmpty()) {
            String knowledgeContext = knowledgeService.buildKnowledgeContext(relevantDocs);
            enhancedPrompt = UNIFIED_SYSTEM_PROMPT + knowledgeContext;
        }

        List<ChatMessage> fullMessages = new ArrayList<>();
        fullMessages.add(ChatMessage.system(enhancedPrompt));

        if (managedContext.hasSummary()) {
            fullMessages.add(ChatMessage.system(
                    "【之前的对话摘要】" + managedContext.getSummary()));
        }

        fullMessages.addAll(managedContext.getMessages());

        JSONArray tools = buildAllTools();

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
                        executeSearchTutors(function, toolCallId, fullMessages);
                    } else if ("recommend_nearby_tutors".equals(functionName)) {
                        executeRecommendNearby(function, toolCallId, fullMessages);
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

    /**
     * 执行 search_tutors 工具调用
     */
    private void executeSearchTutors(JSONObject function, String toolCallId, List<ChatMessage> fullMessages) {
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

            fullMessages.add(ChatMessage.toolResult(toolCallId, finalResultJson));

        } catch (Exception e) {
            log.error("执行本地工具search_tutors失败", e);
            fullMessages.add(ChatMessage.toolResult(toolCallId, "{\"error\": \"内部执行失败\"}"));
        }
    }

    /**
     * 执行 recommend_nearby_tutors 工具调用
     */
    private void executeRecommendNearby(JSONObject function, String toolCallId, List<ChatMessage> fullMessages) {
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
    }

    /**
     * 构建全部工具定义
     * 无条件挂载，由大模型自主判断何时调用
     */
    private JSONArray buildAllTools() {
        JSONArray toolsArray = new JSONArray();

        // ── Tool 1: search_tutors ──
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
        subjectParam1.set("description", "素质教育科目方向，必须从以下9项中选择：钢琴/乐器陪练、美术/书法、声乐/视唱练耳、中考体育专项、羽毛球/网球陪练、篮球/足球指导、少儿编程(Scratch/Python)、机器人/3D打印、科学实验/航模");
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
        keywordParam1.set("description", "额外的细节要求（如果在用户描述中提及特殊的经验或背景，例如：音乐学院背景、国家级运动员、竞赛获奖、耐心等）。");
        properties1.set("keyword", keywordParam1);

        parameters1.set("properties", properties1);
        parameters1.set("required", new JSONArray().put("subject"));

        function1.set("parameters", parameters1);
        tool1.set("function", function1);

        toolsArray.put(tool1);

        // ── Tool 2: recommend_nearby_tutors ──
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
        subParam.set("description", "想找的素质教育方向（如：钢琴、编程、篮球）。这是一个硬性要求，如果用户没提供，大模型必须主动在对话中询问用户想学什么方向。");
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

        ChatResponse response = chat(messages, null, null);
        if (response.getSuccess()) {
            return response.getContent();
        } else {
            return "抱歉，暂时无法回答您的问题。您可以联系人工客服获取帮助。";
        }
    }

    /**
     * 教案生成场景的系统提示词
     */
    private static final String LESSON_PLAN_SYSTEM_PROMPT = """
            你是"校园智教"平台的AI教学赋能官，专业的教案生成助手。
            你的任务是为大学生教员生成详细的课程教案。
            
            教案要求：
            1. 结构清晰：包含热身、主要内容、练习、游戏、总结等环节
            2. 时间合理：根据给定的课时时长分配时间
            3. 针对性强：根据学生水平和科目特点设计内容
            4. 实用性高：提供具体的教学方法和练习内容
            5. 语言专业：使用专业的教学术语，但保持易懂
            
            输出格式：
            - 教案标题
            - 适用学生：[学生情况]
            - 课时时长：[时长]
            - 教学目标：[具体目标]
            - 教学准备：[需要的器材/材料]
            - 教学流程：
              1. 环节一：[名称] - [时间]
                 - 内容：[详细描述]
                 - 方法：[教学方法]
              2. 环节二：[名称] - [时间]
                 ...
            - 注意事项：[安全、教学重点等]
            - 课后作业：[可选]
            """;

    /**
     * 评语润色场景的系统提示词
     */
    private static final String COMMENT_POLISH_SYSTEM_PROMPT = """
            你是"校园智教"平台的AI教学赋能官，专业的评语润色助手。
            你的任务是将教员的简单评语润色为专业、温馨的家长反馈。
            
            润色要求：
            1. 语言温暖：使用亲切、鼓励的语气
            2. 专业表达：使用教育专业术语，体现专业性
            3. 具体详细：将简单描述扩展为具体的观察和分析
            4. 正面引导：突出学生的进步和优点
            5. 建设性建议：提供具体的改进方向
            6. 家长友好：让家长感受到教师的用心和专业
            
            输出格式：
            - 开头：亲切的问候
            - 主体：详细的学习情况反馈
            - 优点：学生的进步和闪光点
            - 建议：具体的改进方向
            - 结尾：鼓励和期待
            """;

    /**
     * 生成教案
     *
     * @param subject      教学科目
     * @param studentLevel 学生水平
     * @param lessonDuration 课时时长
     * @param studentInfo  学生情况
     * @return 教案内容
     */
    public String generateLessonPlan(String subject, String studentLevel, String lessonDuration, String studentInfo) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(LESSON_PLAN_SYSTEM_PROMPT));
        messages.add(ChatMessage.user("请为以下情况生成详细教案：\n" +
                "科目：" + subject + "\n" +
                "学生水平：" + studentLevel + "\n" +
                "课时时长：" + lessonDuration + "\n" +
                "学生情况：" + studentInfo));
        
        ChatResponse response = llmClient.chat(messages);
        if (!response.getSuccess()) {
            throw new RuntimeException("AI教案生成失败：" + response.getError());
        }
        return response.getContent();
    }

    /**
     * 润色评语
     *
     * @param rawComment 原始评语
     * @param subject    教学科目
     * @param studentInfo 学生情况
     * @return 润色后的评语
     */
    public String polishComment(String rawComment, String subject, String studentInfo) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(COMMENT_POLISH_SYSTEM_PROMPT));
        messages.add(ChatMessage.user("请润色以下评语：\n" +
                "原始评语：" + rawComment + "\n" +
                "科目：" + subject + "\n" +
                "学生情况：" + studentInfo));
        
        ChatResponse response = llmClient.chat(messages);
        if (!response.getSuccess()) {
            throw new RuntimeException("AI评语润色失败：" + response.getError());
        }
        return response.getContent();
    }
}
