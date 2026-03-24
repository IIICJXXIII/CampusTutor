package com.campus.module.llm.service;

import com.campus.module.llm.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG增强的聊天服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagEnhancedChatService {
    
    private final LlmClientService llmClientService;
    private final KnowledgeDocumentService knowledgeDocumentService;
    private final PromptTemplateService promptTemplateService;
    private final UserProfileAiService userProfileAiService;
    
    /**
     * RAG增强的智能对话
     */
    public RagChatResponse ragChat(RagChatRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 准备RAG检索
            RagSearchResult ragResult = null;
            RagChatResponse.RagInfo ragInfo = new RagChatResponse.RagInfo();
            ragInfo.setEnabled(Boolean.TRUE.equals(request.getEnableRag()));
            
            if (Boolean.TRUE.equals(request.getEnableRag())) {
                ragResult = performRagSearch(request);
                ragInfo.setHasRelevantContent(ragResult.getHasRelevantContent());
                ragInfo.setSearchTimeMs(ragResult.getSearchTimeMs());
                ragInfo.setMaxRelevanceScore(ragResult.getMaxScore());
                
                // 构建相关文档引用
                if (ragResult.getDocuments() != null && !ragResult.getDocuments().isEmpty()) {
                    List<RagChatResponse.KnowledgeDocumentRef> docRefs = ragResult.getDocuments().stream()
                            .map(doc -> {
                                RagChatResponse.KnowledgeDocumentRef ref = new RagChatResponse.KnowledgeDocumentRef();
                                ref.setId(doc.getId());
                                ref.setTitle(doc.getTitle());
                                ref.setDocType(doc.getDocType());
                                ref.setRelevanceScore(doc.getRelevanceScore());
                                return ref;
                            })
                            .collect(Collectors.toList());
                    ragInfo.setRelevantDocuments(docRefs);
                }
            }
            
            // 2. 准备个性化信息
            RagChatResponse.PersonalizationInfo personalizationInfo = preparePersonalizationInfo(request);
            
            // 3. 构建增强的Prompt
            List<ChatMessage> enhancedMessages = buildEnhancedMessages(request, ragResult, personalizationInfo);
            
            // 4. 调用LLM
            ChatResponse llmResponse = llmClientService.chat(enhancedMessages);
            
            // 5. 构建响应
            RagChatResponse response;
            if (llmResponse.getSuccess()) {
                response = RagChatResponse.success(
                        llmResponse.getContent(),
                        llmResponse.getTokensUsed(),
                        ragInfo,
                        personalizationInfo
                );
                
                // 提取使用的知识片段
                if (ragResult != null && ragResult.getRelevantTexts() != null) {
                    List<String> usedSnippets = extractUsedKnowledgeSnippets(
                            llmResponse.getContent(), 
                            ragResult.getRelevantTexts()
                    );
                    ragInfo.setUsedKnowledgeSnippets(usedSnippets);
                }
            } else {
                response = RagChatResponse.fail(llmResponse.getError());
            }
            
            response.setResponseTimeMs(System.currentTimeMillis() - startTime);
            
            // 6. 记录交互历史（异步）
            recordInteractionHistory(request, response, ragResult);
            
            log.info("RAG增强对话完成，用户ID: {}, 场景: {}, 耗时: {}ms", 
                    request.getUserId(), request.getScene(), response.getResponseTimeMs());
            
            return response;
            
        } catch (Exception e) {
            log.error("RAG增强对话失败: {}", e.getMessage(), e);
            return RagChatResponse.fail("AI服务暂时不可用，请稍后重试");
        }
    }
    
    /**
     * 执行RAG检索
     */
    private RagSearchResult performRagSearch(RagChatRequest request) {
        // 获取最后一条用户消息作为查询
        String query = extractQueryFromMessages(request.getMessages());
        if (!StringUtils.hasText(query)) {
            return new RagSearchResult();
        }
        
        // 构建检索请求
        RagSearchRequest searchRequest = new RagSearchRequest();
        searchRequest.setQuery(query);
        searchRequest.setScene(request.getScene());
        searchRequest.setUserRole(request.getUserRole());
        searchRequest.setTopK(request.getRagConfig() != null ? request.getRagConfig().getTopK() : 3);
        searchRequest.setMinScore(request.getRagConfig() != null ? request.getRagConfig().getMinScore() : 0.4);
        
        // 执行检索
        return knowledgeDocumentService.ragSearch(searchRequest);
    }
    
    /**
     * 准备个性化信息
     */
    private RagChatResponse.PersonalizationInfo preparePersonalizationInfo(RagChatRequest request) {
        RagChatResponse.PersonalizationInfo info = new RagChatResponse.PersonalizationInfo();
        
        if (request.getUserId() != null && 
            request.getPersonalization() != null && 
            Boolean.TRUE.equals(request.getPersonalization().getUseProfile())) {
            
            // 获取用户画像特征
            Map<String, Object> features = userProfileAiService.getUserPersonalizationFeatures(request.getUserId());
            if (!features.isEmpty()) {
                info.setUsedProfile(true);
                info.setUsedProfileFeatures(new ArrayList<>(features.keySet()));
            }
        }
        
        // 记录使用的Prompt模板
        Map<String, String> sceneMapping = promptTemplateService.getSceneMapping();
        String templateScene = sceneMapping.get(request.getScene());
        if (templateScene != null) {
            info.setPromptTemplate(templateScene);
        }
        
        // 记录回答风格
        if (request.getPersonalization() != null) {
            info.setResponseStyle(request.getPersonalization().getTone() + "/" + 
                                 request.getPersonalization().getResponseLength());
        }
        
        return info;
    }
    
    /**
     * 构建增强的消息列表
     */
    private List<ChatMessage> buildEnhancedMessages(RagChatRequest request, 
                                                   RagSearchResult ragResult,
                                                   RagChatResponse.PersonalizationInfo personalizationInfo) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 1. 系统提示词（使用Prompt模板）
        String systemPrompt = buildEnhancedSystemPrompt(request, ragResult, personalizationInfo);
        if (systemPrompt != null) {
            messages.add(ChatMessage.system(systemPrompt));
        }
        
        // 2. 历史消息
        messages.addAll(request.getMessages());
        
        return messages;
    }
    
    /**
     * 构建增强的系统提示词
     */
    private String buildEnhancedSystemPrompt(RagChatRequest request, 
                                            RagSearchResult ragResult,
                                            RagChatResponse.PersonalizationInfo personalizationInfo) {
        // 获取场景映射
        Map<String, String> sceneMapping = promptTemplateService.getSceneMapping();
        String templateScene = sceneMapping.get(request.getScene());
        if (templateScene == null) {
            templateScene = "GENERAL_QA"; // 默认场景
        }
        
        // 准备模板变量
        Map<String, Object> variables = new HashMap<>();
        
        // 基础变量
        variables.put("userRole", request.getUserRole());
        variables.put("currentTime", LocalDateTime.now().toString());
        
        // RAG增强内容
        if (ragResult != null && ragResult.getHasRelevantContent()) {
            // 平台规则知识
            String platformRules = extractPlatformRules(ragResult);
            if (StringUtils.hasText(platformRules)) {
                variables.put("platformRules", platformRules);
                variables.put("relevantKnowledge", platformRules);
            }
            
            // 教案模板知识
            String lessonPlanTemplates = extractLessonPlanTemplates(ragResult);
            if (StringUtils.hasText(lessonPlanTemplates)) {
                variables.put("lessonPlanTemplates", lessonPlanTemplates);
            }
            
            // 评语模板知识
            String commentTemplates = extractCommentTemplates(ragResult);
            if (StringUtils.hasText(commentTemplates)) {
                variables.put("commentTemplates", commentTemplates);
            }
        }
        
        // 用户画像个性化
        if (request.getUserId() != null && personalizationInfo.getUsedProfile()) {
            // 生成个性化提示
            String basePrompt = promptTemplateService.renderTemplate(templateScene, variables);
            if (basePrompt != null) {
                return userProfileAiService.generatePersonalizedPrompt(request.getUserId(), basePrompt);
            }
        }
        
        // 渲染普通模板
        return promptTemplateService.renderTemplate(templateScene, variables);
    }
    
    /**
     * 从消息中提取查询
     */
    private String extractQueryFromMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        
        // 获取最后一条用户消息
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if ("user".equals(message.getRole()) && StringUtils.hasText(message.getContent())) {
                return message.getContent();
            }
        }
        
        return "";
    }
    
    /**
     * 提取平台规则知识
     */
    private String extractPlatformRules(RagSearchResult ragResult) {
        if (ragResult.getDocuments() == null) {
            return "";
        }
        
        return ragResult.getDocuments().stream()
                .filter(doc -> "RULE".equals(doc.getDocType()) || "FAQ".equals(doc.getDocType()))
                .map(doc -> doc.getTitle() + "：" + doc.getContentSummary())
                .collect(Collectors.joining("\n"));
    }
    
    /**
     * 提取教案模板知识
     */
    private String extractLessonPlanTemplates(RagSearchResult ragResult) {
        if (ragResult.getDocuments() == null) {
            return "";
        }
        
        return ragResult.getDocuments().stream()
                .filter(doc -> "LESSON_PLAN".equals(doc.getDocType()))
                .map(doc -> doc.getTitle() + "：" + doc.getContentSummary())
                .collect(Collectors.joining("\n"));
    }
    
    /**
     * 提取评语模板知识
     */
    private String extractCommentTemplates(RagSearchResult ragResult) {
        if (ragResult.getDocuments() == null) {
            return "";
        }
        
        return ragResult.getDocuments().stream()
                .filter(doc -> "COMMENT".equals(doc.getDocType()))
                .map(doc -> doc.getTitle() + "：" + doc.getContentSummary())
                .collect(Collectors.joining("\n"));
    }
    
    /**
     * 提取使用的知识片段
     */
    private List<String> extractUsedKnowledgeSnippets(String response, List<String> knowledgeSnippets) {
        if (knowledgeSnippets == null || knowledgeSnippets.isEmpty()) {
            return Collections.emptyList();
        }
        
        return knowledgeSnippets.stream()
                .filter(snippet -> response.contains(snippet.substring(0, Math.min(20, snippet.length()))))
                .collect(Collectors.toList());
    }
    
    /**
     * 记录交互历史
     */
    private void recordInteractionHistory(RagChatRequest request, 
                                         RagChatResponse response,
                                         RagSearchResult ragResult) {
        if (request.getUserId() == null) {
            return;
        }
        
        try {
            // 构建对话历史记录
            List<Map<String, String>> conversationHistory = new ArrayList<>();
            
            // 添加用户消息
            if (request.getMessages() != null) {
                for (ChatMessage message : request.getMessages()) {
                    if ("user".equals(message.getRole())) {
                        Map<String, String> record = new HashMap<>();
                        record.put("role", "user");
                        record.put("content", message.getContent());
                        record.put("time", LocalDateTime.now().toString());
                        conversationHistory.add(record);
                    }
                }
            }
            
            // 添加AI响应
            if (response.getSuccess()) {
                Map<String, String> record = new HashMap<>();
                record.put("role", "assistant");
                record.put("content", response.getContent());
                record.put("time", LocalDateTime.now().toString());
                if (ragResult != null && ragResult.getHasRelevantContent()) {
                    record.put("ragEnabled", "true");
                    record.put("ragScore", String.valueOf(ragResult.getMaxScore()));
                }
                conversationHistory.add(record);
            }
            
            // 分析对话历史并更新用户画像
            if (!conversationHistory.isEmpty()) {
                userProfileAiService.analyzeConversationAndUpdateProfile(request.getUserId(), conversationHistory);
            }
            
        } catch (Exception e) {
            log.error("记录交互历史失败: {}", e.getMessage());
        }
    }
    
    /**
     * 快速问答（简化版，无RAG）
     */
    public String quickAnswer(String question, Long userId) {
        try {
            RagChatRequest request = new RagChatRequest();
            request.setMessages(Collections.singletonList(ChatMessage.user(question)));
            request.setScene("general");
            request.setUserId(userId);
            request.setEnableRag(false); // 快速问答不启用RAG
            
            RagChatResponse response = ragChat(request);
            if (response.getSuccess()) {
                return response.getContent();
            } else {
                return "抱歉，暂时无法回答您的问题。";
            }
        } catch (Exception e) {
            log.error("快速问答失败: {}", e.getMessage(), e);
            return "抱歉，服务暂时不可用。";
        }
    }
    
    /**
     * 生成教案（RAG增强版）
     */
    public String generateLessonPlan(String subject, String studentLevel, 
                                    String lessonDuration, String studentInfo, Long userId) {
        try {
            // 构建RAG检索请求
            RagSearchRequest searchRequest = new RagSearchRequest();
            searchRequest.setQuery(subject + " " + studentLevel + " 教案");
            searchRequest.setScene("lesson_plan");
            searchRequest.setUserRole("TEACHER");
            searchRequest.setTopK(3);
            
            // 检索相关教案模板
            RagSearchResult ragResult = knowledgeDocumentService.ragSearch(searchRequest);
            
            // 构建聊天请求
            RagChatRequest request = new RagChatRequest();
            request.setMessages(Collections.singletonList(
                    ChatMessage.user("请为以下情况生成详细教案：\n科目：" + subject + 
                                    "\n学生水平：" + studentLevel + 
                                    "\n课时时长：" + lessonDuration + 
                                    "\n学生情况：" + studentInfo)
            ));
            request.setScene("lesson_plan");
            request.setUserId(userId);
            request.setEnableRag(true);
            
            RagChatResponse response = ragChat(request);
            if (response.getSuccess()) {
                return response.getContent();
            } else {
                throw new RuntimeException("AI教案生成失败：" + response.getError());
            }
        } catch (Exception e) {
            log.error("生成教案失败: {}", e.getMessage(), e);
            throw new RuntimeException("教案生成服务暂时不可用");
        }
    }
    
    /**
     * 润色评语（RAG增强版）
     */
    public String polishComment(String rawComment, String subject, 
                               String studentInfo, Long userId) {
        try {
            // 构建RAG检索请求
            RagSearchRequest searchRequest = new RagSearchRequest();
            searchRequest.setQuery("评语 润色 模板");
            searchRequest.setScene("comment_polish");
            searchRequest.setUserRole("TEACHER");
            searchRequest.setTopK(2);
            
            // 检索相关评语模板
            RagSearchResult ragResult = knowledgeDocumentService.ragSearch(searchRequest);
            
            // 构建聊天请求
            RagChatRequest request = new RagChatRequest();
            request.setMessages(Collections.singletonList(
                    ChatMessage.user("请润色以下评语：\n原始评语：" + rawComment + 
                                    "\n科目：" + subject + 
                                    "\n学生情况：" + studentInfo)
            ));
            request.setScene("comment_polish");
            request.setUserId(userId);
            request.setEnableRag(true);
            
            RagChatResponse response = ragChat(request);
            if (response.getSuccess()) {
                return response.getContent();
            } else {
                throw new RuntimeException("AI评语润色失败：" + response.getError());
            }
        } catch (Exception e) {
            log.error("润色评语失败: {}", e.getMessage(), e);
            throw new RuntimeException("评语润色服务暂时不可用");
        }
    }
}