package com.campus.module.llm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.llm.entity.KnowledgeDocument;
import com.campus.module.llm.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeMapper;
    private final KnowledgeRetrievalService retrievalService;

    public Page<KnowledgeDocument> list(int page, int size, String docType, String keyword) {
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getStatus, 1);
        
        if (docType != null && !docType.isEmpty()) {
            wrapper.eq(KnowledgeDocument::getDocType, docType);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(KnowledgeDocument::getTitle, keyword)
                .or().like(KnowledgeDocument::getContent, keyword)
                .or().like(KnowledgeDocument::getTags, keyword)
            );
        }
        
        wrapper.orderByDesc(KnowledgeDocument::getCreatedTime);
        
        return knowledgeMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public KnowledgeDocument getById(Long id) {
        return knowledgeMapper.selectById(id);
    }

    @Transactional
    public KnowledgeDocument create(KnowledgeDocument doc) {
        doc.setStatus(1);
        doc.setCreatedTime(LocalDateTime.now());
        doc.setUpdatedTime(LocalDateTime.now());
        knowledgeMapper.insert(doc);
        log.info("创建知识文档: id={}, title={}", doc.getId(), doc.getTitle());
        return doc;
    }

    @Transactional
    public KnowledgeDocument update(Long id, KnowledgeDocument doc) {
        KnowledgeDocument existing = knowledgeMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("文档不存在: " + id);
        }
        
        existing.setTitle(doc.getTitle());
        existing.setContent(doc.getContent());
        existing.setDocType(doc.getDocType());
        existing.setSource(doc.getSource());
        existing.setTags(doc.getTags());
        existing.setTargetRole(doc.getTargetRole());
        existing.setApplicableSubjects(doc.getApplicableSubjects());
        existing.setApplicableGrades(doc.getApplicableGrades());
        existing.setUpdatedTime(LocalDateTime.now());
        
        knowledgeMapper.updateById(existing);
        log.info("更新知识文档: id={}", id);
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        KnowledgeDocument doc = knowledgeMapper.selectById(id);
        if (doc != null) {
            doc.setStatus(0);
            doc.setUpdatedTime(LocalDateTime.now());
            knowledgeMapper.updateById(doc);
            log.info("删除知识文档: id={}", id);
        }
    }

    public List<KnowledgeDocument> search(String query, String role, int limit) {
        return retrievalService.retrieveRelevantDocs(query, role != null ? role : "ALL")
                .stream()
                .limit(limit)
                .toList();
    }

    public List<String> listDocTypes() {
        return List.of("RULE", "FAQ", "LESSON_PLAN", "COMMENT", "TEACHING_EXPERIENCE", "POLICY");
    }
}
