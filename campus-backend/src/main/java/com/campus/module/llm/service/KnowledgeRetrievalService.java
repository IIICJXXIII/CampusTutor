package com.campus.module.llm.service;

import com.campus.module.llm.entity.KnowledgeDocument;
import com.campus.module.llm.entity.PromptTemplate;
import com.campus.module.llm.mapper.KnowledgeDocumentMapper;
import com.campus.module.llm.mapper.PromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeRetrievalService {

    private final KnowledgeDocumentMapper knowledgeMapper;
    private final PromptTemplateMapper templateMapper;

    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
            "自己", "这", "那", "什么", "怎么", "如何", "可以", "能", "想", "请问", "帮忙"
    );

    public List<KnowledgeDocument> retrieveRelevantDocs(String query, String role) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> keywords = extractKeywords(query);
        log.info("RAG关键词提取: query={}, keywords={}", query, keywords);
        
        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, KnowledgeDocument> docMap = new LinkedHashMap<>();
        for (String keyword : keywords) {
            try {
                List<KnowledgeDocument> results = knowledgeMapper.searchByKeyword(keyword, role, 5);
                log.info("关键词 '{}' 检索到 {} 条文档", keyword, results.size());
                for (KnowledgeDocument doc : results) {
                    docMap.putIfAbsent(doc.getId(), doc);
                }
            } catch (Exception e) {
                log.warn("搜索关键词 '{}' 失败: {}", keyword, e.getMessage());
            }
        }

        List<KnowledgeDocument> sortedDocs = new ArrayList<>(docMap.values());
        sortedDocs.sort((a, b) -> {
            int scoreA = calculateRelevanceScore(a, query, keywords);
            int scoreB = calculateRelevanceScore(b, query, keywords);
            return Integer.compare(scoreB, scoreA);
        });

        return sortedDocs.stream().limit(5).collect(Collectors.toList());
    }

    public String getPromptTemplate(String scene, Map<String, String> variables) {
        PromptTemplate template = templateMapper.findActiveByScene(scene);
        if (template == null) {
            log.warn("未找到场景 '{}' 的活跃模板", scene);
            return null;
        }

        String result = template.getTemplate();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }

    public String buildKnowledgeContext(List<KnowledgeDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【相关知识库内容】\n");
        for (KnowledgeDocument doc : docs) {
            sb.append("- ").append(doc.getTitle()).append("：")
                    .append(doc.getContent()).append("\n");
        }
        return sb.toString();
    }

    private List<String> extractKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        
        String[] words = query.split("[\\s,，。？！、；：\"'（）\\[\\]{}]+");
        for (String word : words) {
            word = word.trim();
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                keywords.add(word);
                if (word.length() >= 3) {
                    for (int i = 0; i <= word.length() - 2; i++) {
                        String sub = word.substring(i, Math.min(i + 2, word.length()));
                        if (sub.length() >= 2 && !STOP_WORDS.contains(sub)) {
                            keywords.add(sub);
                        }
                    }
                }
            }
        }
        
        return keywords.stream()
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
    }

    private int calculateRelevanceScore(KnowledgeDocument doc, String query, List<String> keywords) {
        int score = 0;
        String title = doc.getTitle() != null ? doc.getTitle().toLowerCase() : "";
        String content = doc.getContent() != null ? doc.getContent().toLowerCase() : "";
        String tags = doc.getTags() != null ? doc.getTags().toLowerCase() : "";
        String queryLower = query.toLowerCase();

        for (String keyword : keywords) {
            String keywordLower = keyword.toLowerCase();
            if (title.contains(keywordLower)) score += 10;
            if (tags.contains(keywordLower)) score += 8;
            if (content.contains(keywordLower)) score += 5;
        }

        if (queryLower.contains(title)) score += 15;

        return score;
    }
}
