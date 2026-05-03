package com.campus.module.llm.controller;

import com.campus.common.result.Result;
import com.campus.module.llm.dto.*;
import com.campus.module.llm.service.ChatAssistantService;
import com.campus.module.llm.service.DemandParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "智能服务", description = "需求解析、智能对话")
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private static final Logger log = LoggerFactory.getLogger(LlmController.class);

    private final DemandParseService demandParseService;
    private final ChatAssistantService chatAssistantService;

    @Operation(summary = "智能解析需求", description = "使用AI从自然语言描述中提取结构化的家教需求信息")
    @PostMapping("/demand/parse")
    public Result<DemandParseResult> parseDemand(@Valid @RequestBody DemandParseRequest request) {
        try {
            log.info("收到需求解析请求: {}", request.getText());
            DemandParseResult result = demandParseService.parse(request.getText());
            return Result.success(result);
        } catch (Exception e) {
            log.error("需求解析失败: ", e);
            return Result.fail("需求解析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "智能对话", description = "与AI助手进行对话，获取家教相关帮助")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("收到对话请求: messages数量={}", request.getMessages() != null ? request.getMessages().size() : 0);
            ChatResponse response = chatAssistantService.chat(
                    request.getMessages(), request.getScene(), request.getSummary());
            if (response.getSuccess()) {
                return Result.success(response);
            }
            return Result.fail(response.getError());
        } catch (Exception e) {
            log.error("对话处理失败: ", e);
            return Result.fail("对话处理失败: " + e.getMessage());
        }
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
                request.getStudentInfo()
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
                request.getStudentInfo()
            );
            return Result.success("润色成功", polished);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "测试JSON解析", description = "测试JSON反序列化是否正常")
    @PostMapping("/test-json")
    public Result<Object> testJson(@RequestBody ChatRequest request) {
        try {
            log.info("测试JSON解析: messages={}", request.getMessages());
            if (request.getMessages() != null) {
                for (ChatMessage msg : request.getMessages()) {
                    log.info("消息: role={}, content={}", msg.getRole(), msg.getContent());
                }
            }
            return Result.success(request);
        } catch (Exception e) {
            log.error("JSON解析测试失败: ", e);
            return Result.fail("JSON解析测试失败: " + e.getMessage());
        }
    }

    @Operation(summary = "简单测试", description = "简单测试端点")
    @PostMapping("/simple-test")
    public Result<String> simpleTest(@RequestBody String rawBody) {
        log.info("收到原始请求体: {}", rawBody);
        return Result.success("收到: " + rawBody);
    }

    @Operation(summary = "Map测试", description = "使用Map接收JSON")
    @PostMapping("/map-test")
    public Result<Object> mapTest(@RequestBody java.util.Map<String, Object> body) {
        log.info("收到Map: {}", body);
        return Result.success(body);
    }
}
