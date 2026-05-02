package com.campus.module.llm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.Result;
import com.campus.module.llm.entity.KnowledgeDocument;
import com.campus.module.llm.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "知识库管理", description = "RAG知识库文档管理接口")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeDocumentService knowledgeService;

    @Operation(summary = "分页查询知识库", description = "分页查询知识库文档列表")
    @GetMapping("/list")
    public Result<Page<KnowledgeDocument>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String docType,
            @RequestParam(required = false) String keyword) {
        return Result.success(knowledgeService.list(page, size, docType, keyword));
    }

    @Operation(summary = "获取文档详情", description = "根据ID获取知识库文档详情")
    @GetMapping("/{id}")
    public Result<KnowledgeDocument> getById(@PathVariable Long id) {
        KnowledgeDocument doc = knowledgeService.getById(id);
        if (doc == null) {
            return Result.fail(404, "文档不存在");
        }
        return Result.success(doc);
    }

    @Operation(summary = "创建知识文档", description = "创建新的知识库文档")
    @PostMapping
    public Result<KnowledgeDocument> create(@RequestBody KnowledgeDocument doc) {
        try {
            return Result.success(knowledgeService.create(doc));
        } catch (Exception e) {
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新知识文档", description = "更新已有的知识库文档")
    @PutMapping("/{id}")
    public Result<KnowledgeDocument> update(@PathVariable Long id, @RequestBody KnowledgeDocument doc) {
        try {
            return Result.success(knowledgeService.update(id, doc));
        } catch (Exception e) {
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除知识文档", description = "软删除知识库文档")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }

    @Operation(summary = "搜索知识库", description = "基于关键词搜索知识库文档")
    @GetMapping("/search")
    public Result<List<KnowledgeDocument>> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "ALL") String role,
            @RequestParam(defaultValue = "5") int limit) {
        return Result.success(knowledgeService.search(query, role, limit));
    }

    @Operation(summary = "获取文档类型列表", description = "获取所有支持的文档类型")
    @GetMapping("/doc-types")
    public Result<List<String>> listDocTypes() {
        return Result.success(knowledgeService.listDocTypes());
    }
}
