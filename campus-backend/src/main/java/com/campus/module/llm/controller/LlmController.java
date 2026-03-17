package com.campus.module.llm.controller;

import com.campus.common.result.Result;
import com.campus.module.llm.dto.*;
import com.campus.module.llm.service.ChatAssistantService;
import com.campus.module.llm.service.DemandParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * LLM智能服务控制器
 */
@Tag(name = "智能服务", description = "需求解析、智能对话")
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final DemandParseService demandParseService;
    private final ChatAssistantService chatAssistantService;

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
        ChatResponse response = chatAssistantService.chat(request.getMessages(), request.getScene());
        if (response.getSuccess()) {
            return Result.success(response);
        }
        return Result.fail(response.getError());
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
    public Result<String> generateLessonPlan(
            @RequestParam String subject,
            @RequestParam String studentLevel,
            @RequestParam String lessonDuration,
            @RequestParam String studentInfo) {
        String plan = chatAssistantService.generateLessonPlan(subject, studentLevel, lessonDuration, studentInfo);
        return Result.success(plan);
    }

    /**
     * 润色评语
     */
    @Operation(summary = "润色评语", description = "将简单评语润色为专业、温馨的反馈")
    @PostMapping("/lesson/comment")
    public Result<String> polishComment(
            @RequestParam String rawComment,
            @RequestParam String subject,
            @RequestParam String studentInfo) {
        String polished = chatAssistantService.polishComment(rawComment, subject, studentInfo);
        return Result.success(polished);
    }
}
