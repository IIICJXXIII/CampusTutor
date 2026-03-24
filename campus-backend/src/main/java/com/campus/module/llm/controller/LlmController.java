package com.campus.module.llm.controller;

import com.campus.common.result.Result;
import com.campus.module.llm.dto.*;
import com.campus.module.llm.entity.UserProfileAi;
import com.campus.module.llm.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * LLM智能服务控制器
 */
@Tag(name = "智能服务", description = "需求解析、智能对话、RAG增强服务")
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final DemandParseService demandParseService;
    private final ChatAssistantService chatAssistantService;
    private final KnowledgeDocumentService knowledgeDocumentService;
    private final PromptTemplateService promptTemplateService;
    private final UserProfileAiService userProfileAiService;

    /**
     * 解析家教需求
     */
    @Operation(summary = "智能解析需求", description = "使用AI从自然语言描述中提取结构化的家教需求信息")
    @PostMapping("/demand/parse")
    public Result<DemandParseResult> parseDemand(@Valid @RequestBody DemandParseRequest request) {
        DemandParseResult result = demandParseService.parse(request.getText());
        return Result.success(result);
    }

    /**
     * 智能对话
     */
    @Operation(summary = "智能对话", description = "与AI助手进行对话，获取家教相关帮助")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatAssistantService.chat(
                request.getMessages(), request.getScene(), request.getSummary());
        if (response.getSuccess()) {
            return Result.success(response);
        }
        return Result.fail(response.getError());
    }

    /**
     * RAG增强智能对话
     */
    @Operation(summary = "RAG增强智能对话", description = "使用RAG检索增强的智能对话，提供更准确、个性化的回答")
    @PostMapping("/rag/chat")
    public Result<RagChatResponse> ragChat(@Valid @RequestBody RagChatRequest request) {
        RagChatResponse response = chatAssistantService.ragChat(request);
        if (response.getSuccess()) {
            return Result.success(response);
        }
        return Result.fail(response.getError());
    }

    /**
     * RAG知识检索
     */
    @Operation(summary = "RAG知识检索", description = "从知识库中检索相关信息")
    @PostMapping("/rag/search")
    public Result<RagSearchResult> ragSearch(@Valid @RequestBody RagSearchRequest request) {
        RagSearchResult result = knowledgeDocumentService.ragSearch(request);
        return Result.success(result);
    }

    /**
     * 快速问答
     */
    @Operation(summary = "快速问答", description = "快速获取问题答案，无需上下文")
    @GetMapping("/quick-answer")
    public Result<String> quickAnswer(@RequestParam String question) {
        String answer = chatAssistantService.quickAnswer(question);
        return Result.success(answer);
    }

    /**
     * 快速问答（支持用户ID）
     */
    @Operation(summary = "快速问答（个性化）", description = "快速获取问题答案，支持用户个性化")
    @GetMapping("/quick-answer/personalized")
    public Result<String> quickAnswerPersonalized(
            @RequestParam String question,
            @RequestParam(required = false) Long userId) {
        String answer = chatAssistantService.quickAnswer(question, userId);
        return Result.success(answer);
    }

    /**
     * 生成教案
     */
    @Operation(summary = "生成教案", description = "根据学生情况和科目生成详细教案")
    @PostMapping("/lesson/plan")
    public Result<String> generateLessonPlan(@Valid @RequestBody LessonPlanRequest request) {
        try {
            String plan = chatAssistantService.generateLessonPlan(
                request.getSubject(), 
                request.getStudentLevel(), 
                request.getLessonDuration(), 
                request.getStudentInfo(),
                request.getUserId()
            );
            return Result.success("生成成功", plan);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 润色评语
     */
    @Operation(summary = "润色评语", description = "将简单评语润色为专业、温馨的反馈")
    @PostMapping("/lesson/comment")
    public Result<String> polishComment(@Valid @RequestBody PolishCommentRequest request) {
        try {
            String polished = chatAssistantService.polishComment(
                request.getRawComment(), 
                request.getSubject(), 
                request.getStudentInfo(),
                request.getUserId()
            );
            return Result.success("润色成功", polished);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取用户画像
     */
    @Operation(summary = "获取用户画像", description = "获取用户的AI个性化画像")
    @GetMapping("/profile/{userId}")
    public Result<Object> getUserProfile(@PathVariable Long userId) {
        try {
            var features = userProfileAiService.getUserPersonalizationFeatures(userId);
            return Result.success(features);
        } catch (Exception e) {
            return Result.fail("获取用户画像失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户画像
     */
    @Operation(summary = "更新用户画像", description = "更新用户的AI个性化画像")
    @PostMapping("/profile")
    public Result<Void> updateUserProfile(@Valid @RequestBody UserProfileAi profile) {
        try {
            userProfileAiService.saveOrUpdateProfile(profile);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.fail("更新用户画像失败：" + e.getMessage());
        }
    }

    /**
     * 获取Prompt模板
     */
    @Operation(summary = "获取Prompt模板", description = "获取指定场景的Prompt模板")
    @GetMapping("/prompt/{scene}")
    public Result<Object> getPromptTemplate(@PathVariable String scene) {
        try {
            var mapping = promptTemplateService.getSceneMapping();
            String templateScene = mapping.get(scene);
            if (templateScene == null) {
                return Result.fail("未找到场景对应的模板");
            }
            
            var template = promptTemplateService.getActiveTemplateByScene(templateScene);
            if (template == null) {
                return Result.fail("未找到启用的模板");
            }
            
            return Result.success(template);
        } catch (Exception e) {
            return Result.fail("获取Prompt模板失败：" + e.getMessage());
        }
    }

    /**
     * 初始化知识库
     */
    @Operation(summary = "初始化知识库", description = "导入初始知识库数据（仅限管理员）")
    @PostMapping("/knowledge/init")
    public Result<Void> initKnowledgeBase() {
        try {
            knowledgeDocumentService.importInitialKnowledgeBase();
            return Result.success("知识库初始化成功");
        } catch (Exception e) {
            return Result.fail("知识库初始化失败：" + e.getMessage());
        }
    }

    /**
     * 初始化Prompt模板
     */
    @Operation(summary = "初始化Prompt模板", description = "导入初始Prompt模板数据（仅限管理员）")
    @PostMapping("/prompt/init")
    public Result<Void> initPromptTemplates() {
        try {
            promptTemplateService.importInitialTemplates();
            return Result.success("Prompt模板初始化成功");
        } catch (Exception e) {
            return Result.fail("Prompt模板初始化失败：" + e.getMessage());
        }
    }

    /**
     * 初始化用户画像
     */
    @Operation(summary = "初始化用户画像", description = "导入初始用户画像数据（仅限管理员）")
    @PostMapping("/profile/init")
    public Result<Void> initUserProfiles() {
        try {
            userProfileAiService.importInitialProfiles();
            return Result.success("用户画像初始化成功");
        } catch (Exception e) {
            return Result.fail("用户画像初始化失败：" + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @Operation(summary = "AI服务健康检查", description = "检查AI相关服务是否正常")
    @GetMapping("/health")
    public Result<Object> healthCheck() {
        try {
            var healthInfo = new java.util.HashMap<String, Object>();
            healthInfo.put("timestamp", java.time.LocalDateTime.now().toString());
            healthInfo.put("service", "AI智能服务");
            healthInfo.put("status", "正常");
            
            // 检查知识库
            var knowledgeCount = knowledgeDocumentService.count();
            healthInfo.put("knowledgeDocuments", knowledgeCount);
            
            // 检查Prompt模板
            var activeTemplates = promptTemplateService.getAllActiveTemplates();
            healthInfo.put("activePromptTemplates", activeTemplates.size());
            
            // 检查用户画像
            var userProfiles = userProfileAiService.count();
            healthInfo.put("userProfiles", userProfiles);
            
            return Result.success(healthInfo);
        } catch (Exception e) {
            return Result.fail("健康检查失败：" + e.getMessage());
        }
    }
}