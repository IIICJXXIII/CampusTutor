package com.campus.module.llm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.llm.dto.RagSearchRequest;
import com.campus.module.llm.dto.RagSearchResult;
import com.campus.module.llm.entity.KnowledgeDocument;
import com.campus.module.llm.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库文档服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentService extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument> {
    
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    
    /**
     * 创建或更新知识文档
     */
    public KnowledgeDocument saveOrUpdateDocument(KnowledgeDocument document) {
        if (document.getId() == null) {
            document.setCreatedTime(LocalDateTime.now());
            document.setStatus(1); // 默认启用
        }
        document.setUpdatedTime(LocalDateTime.now());
        
        saveOrUpdate(document);
        log.info("保存知识文档成功，ID: {}, 标题: {}", document.getId(), document.getTitle());
        return document;
    }
    
    /**
     * 关键词检索文档
     */
    public List<KnowledgeDocument> searchByKeyword(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        
        List<KnowledgeDocument> documents = knowledgeDocumentMapper.searchByKeyword(keyword, limit != null ? limit : 10);
        log.debug("关键词检索: {}, 结果数量: {}", keyword, documents.size());
        return documents;
    }
    
    /**
     * 根据文档类型查询
     */
    public List<KnowledgeDocument> findByDocType(String docType) {
        return knowledgeDocumentMapper.findByDocType(docType);
    }
    
    /**
     * 根据适用角色查询
     */
    public List<KnowledgeDocument> findByTargetRole(String role) {
        return knowledgeDocumentMapper.findByTargetRole(role);
    }
    
    /**
     * 根据标签查询
     */
    public List<KnowledgeDocument> findByTag(String tag, Integer limit) {
        return knowledgeDocumentMapper.findByTag(tag, limit != null ? limit : 10);
    }
    
    /**
     * 获取所有启用的文档
     */
    public List<KnowledgeDocument> getAllEnabledDocuments() {
        return knowledgeDocumentMapper.findAllEnabled();
    }
    
    /**
     * RAG混合检索
     */
    public RagSearchResult ragSearch(RagSearchRequest request) {
        long startTime = System.currentTimeMillis();
        RagSearchResult result = new RagSearchResult();
        RagSearchResult.SearchStats stats = new RagSearchResult.SearchStats();
        
        List<KnowledgeDocument> allDocuments = new ArrayList<>();
        List<String> relevantTexts = new ArrayList<>();
        
        // 1. 关键词检索
        if (Boolean.TRUE.equals(request.getEnableKeywordSearch())) {
            List<KnowledgeDocument> keywordResults = searchByKeyword(request.getQuery(), request.getTopK());
            allDocuments.addAll(keywordResults);
            stats.setKeywordHits(keywordResults.size());
            
            // 提取相关文本片段
            keywordResults.forEach(doc -> {
                String summary = extractRelevantSummary(doc.getContent(), request.getQuery());
                if (summary != null) {
                    relevantTexts.add(summary);
                }
            });
        }
        
        // 2. 语义检索（这里简化实现，实际应该使用向量数据库）
        if (Boolean.TRUE.equals(request.getEnableSemanticSearch())) {
            // 模拟语义检索：根据文档类型和角色过滤
            List<KnowledgeDocument> semanticResults = new ArrayList<>();
            if (StringUtils.hasText(request.getUserRole())) {
                semanticResults.addAll(findByTargetRole(request.getUserRole()));
            }
            
            // 去重
            semanticResults = semanticResults.stream()
                    .filter(doc -> !allDocuments.contains(doc))
                    .limit(request.getTopK())
                    .collect(Collectors.toList());
            
            allDocuments.addAll(semanticResults);
            stats.setSemanticHits(semanticResults.size());
            
            // 提取相关文本片段
            semanticResults.forEach(doc -> {
                String summary = extractRelevantSummary(doc.getContent(), request.getQuery());
                if (summary != null) {
                    relevantTexts.add(summary);
                }
            });
        }
        
        // 3. 过滤和排序
        List<KnowledgeDocument> filteredDocuments = filterAndSortDocuments(allDocuments, request);
        stats.setFilteredDocuments(allDocuments.size() - filteredDocuments.size());
        stats.setRetrievedDocuments(filteredDocuments.size());
        stats.setTotalDocuments((int) count()); // 总文档数
        
        // 4. 构建结果
        List<RagSearchResult.KnowledgeDocumentDTO> documentDTOs = filteredDocuments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        result.setDocuments(documentDTOs);
        result.setRelevantTexts(relevantTexts);
        result.setSearchTimeMs(System.currentTimeMillis() - startTime);
        result.setSearchMode(request.getEnableKeywordSearch() && request.getEnableSemanticSearch() ? "HYBRID" : 
                           request.getEnableKeywordSearch() ? "KEYWORD" : "SEMANTIC");
        result.setHasRelevantContent(!filteredDocuments.isEmpty());
        result.setStats(stats);
        
        // 计算相关性分数
        if (!filteredDocuments.isEmpty()) {
            double maxScore = filteredDocuments.stream()
                    .mapToDouble(doc -> calculateRelevanceScore(doc, request))
                    .max()
                    .orElse(0.0);
            double avgScore = filteredDocuments.stream()
                    .mapToDouble(doc -> calculateRelevanceScore(doc, request))
                    .average()
                    .orElse(0.0);
            result.setMaxScore(maxScore);
            result.setAvgScore(avgScore);
        }
        
        log.info("RAG检索完成: query={}, 模式={}, 结果数={}, 耗时={}ms", 
                request.getQuery(), result.getSearchMode(), filteredDocuments.size(), result.getSearchTimeMs());
        
        return result;
    }
    
    /**
     * 过滤和排序文档
     */
    private List<KnowledgeDocument> filterAndSortDocuments(List<KnowledgeDocument> documents, RagSearchRequest request) {
        return documents.stream()
                .filter(doc -> doc.getStatus() == 1) // 只保留启用的
                .filter(doc -> {
                    // 根据最小分数过滤
                    double score = calculateRelevanceScore(doc, request);
                    return score >= request.getMinScore();
                })
                .filter(doc -> {
                    // 根据用户角色过滤
                    if (StringUtils.hasText(request.getUserRole())) {
                        return "ALL".equals(doc.getTargetRole()) || request.getUserRole().equals(doc.getTargetRole());
                    }
                    return true;
                })
                .sorted((d1, d2) -> {
                    // 按相关性分数降序排序
                    double score1 = calculateRelevanceScore(d1, request);
                    double score2 = calculateRelevanceScore(d2, request);
                    return Double.compare(score2, score1);
                })
                .limit(request.getTopK())
                .collect(Collectors.toList());
    }
    
    /**
     * 计算文档相关性分数（简化版）
     */
    private double calculateRelevanceScore(KnowledgeDocument doc, RagSearchRequest request) {
        double score = 0.0;
        
        // 1. 关键词匹配分数
        String query = request.getQuery().toLowerCase();
        String title = doc.getTitle().toLowerCase();
        String content = doc.getContent().toLowerCase();
        
        if (title.contains(query)) {
            score += 0.5;
        }
        if (content.contains(query)) {
            score += 0.3;
        }
        
        // 2. 角色匹配分数
        if (StringUtils.hasText(request.getUserRole()) && request.getUserRole().equals(doc.getTargetRole())) {
            score += 0.2;
        }
        
        // 3. 文档类型权重
        switch (doc.getDocType()) {
            case "RULE":
                score += 0.1; // 规则文档权重较高
                break;
            case "FAQ":
                score += 0.05; // FAQ权重中等
                break;
        }
        
        return Math.min(score, 1.0); // 限制在0-1之间
    }
    
    /**
     * 提取相关文本摘要
     */
    private String extractRelevantSummary(String content, String query) {
        if (!StringUtils.hasText(content) || !StringUtils.hasText(query)) {
            return null;
        }
        
        // 简化实现：找到包含查询词的句子
        String[] sentences = content.split("[。！？.!?]");
        for (String sentence : sentences) {
            if (sentence.contains(query)) {
                return sentence.trim() + "。";
            }
        }
        
        // 如果没有找到，返回前100个字符
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
    
    /**
     * 转换为DTO
     */
    private RagSearchResult.KnowledgeDocumentDTO convertToDTO(KnowledgeDocument document) {
        RagSearchResult.KnowledgeDocumentDTO dto = new RagSearchResult.KnowledgeDocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setContentSummary(extractRelevantSummary(document.getContent(), ""));
        dto.setDocType(document.getDocType());
        dto.setSource(document.getSource());
        
        // 解析标签
        if (StringUtils.hasText(document.getTags())) {
            dto.setTags(Arrays.asList(document.getTags().split(",")));
        }
        
        dto.setTargetRole(document.getTargetRole());
        
        // 解析适用科目
        if (StringUtils.hasText(document.getApplicableSubjects())) {
            dto.setApplicableSubjects(Arrays.asList(document.getApplicableSubjects().split(",")));
        }
        
        // 解析适用年级
        if (StringUtils.hasText(document.getApplicableGrades())) {
            dto.setApplicableGrades(Arrays.asList(document.getApplicableGrades().split(",")));
        }
        
        return dto;
    }
    
    /**
     * 批量导入初始知识库数据
     */
    public void importInitialKnowledgeBase() {
        log.info("开始导入初始知识库数据...");
        
        // 平台规则文档
        KnowledgeDocument rule1 = new KnowledgeDocument();
        rule1.setTitle("平台服务协议");
        rule1.setContent("校园智教平台服务协议：1. 平台收取10%服务费；2. 课时费托管机制；3. 教员需完成实名认证；4. 家长可申请退款；5. 争议处理流程。");
        rule1.setDocType("RULE");
        rule1.setSource("平台官方");
        rule1.setTags("规则,协议,费用");
        rule1.setTargetRole("ALL");
        saveOrUpdateDocument(rule1);
        
        // 常见问题
        KnowledgeDocument faq1 = new KnowledgeDocument();
        faq1.setTitle("如何发布家教需求？");
        faq1.setContent("发布家教需求步骤：1. 登录家长账号；2. 进入发布需求页面；3. 填写学生信息；4. 选择科目和年级；5. 设置期望价格；6. 选择授课方式；7. 确认发布。");
        faq1.setDocType("FAQ");
        faq1.setSource("平台帮助中心");
        faq1.setTags("需求发布,家长指南");
        faq1.setTargetRole("PARENT");
        saveOrUpdateDocument(faq1);
        
        // 教案模板
        KnowledgeDocument lessonPlan1 = new KnowledgeDocument();
        lessonPlan1.setTitle("初中数学教案模板");
        lessonPlan1.setContent("初中数学教案结构：1. 教学目标；2. 教学重点难点；3. 教学过程（导入、讲解、练习、总结）；4. 作业布置；5. 教学反思。");
        lessonPlan1.setDocType("LESSON_PLAN");
        lessonPlan1.setSource("优秀教员分享");
        lessonPlan1.setTags("数学,初中,教案");
        lessonPlan1.setTargetRole("TEACHER");
        lessonPlan1.setApplicableSubjects("数学");
        lessonPlan1.setApplicableGrades("初中");
        saveOrUpdateDocument(lessonPlan1);
        
        // 评语模板
        KnowledgeDocument comment1 = new KnowledgeDocument();
        comment1.setTitle("学生评语模板");
        comment1.setContent("优秀评语要素：1. 肯定学生进步；2. 具体指出优点；3. 提出改进建议；4. 表达鼓励和期待；5. 语言温暖专业。");
        comment1.setDocType("COMMENT");
        comment1.setSource("教学经验分享");
        comment1.setTags("评语,反馈,家长沟通");
        comment1.setTargetRole("TEACHER");
        saveOrUpdateDocument(comment1);
        
        log.info("初始知识库数据导入完成，共导入4个文档");
    }
}