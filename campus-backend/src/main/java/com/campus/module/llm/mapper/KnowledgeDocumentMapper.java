package com.campus.module.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.llm.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识库文档Mapper
 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
    
    /**
     * 根据关键词搜索文档
     */
    @Select("""
        SELECT * FROM knowledge_document 
        WHERE status = 1 
        AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY id DESC
        LIMIT #{limit}
        """)
    List<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);
    
    /**
     * 根据文档类型查询
     */
    @Select("SELECT * FROM knowledge_document WHERE status = 1 AND doc_type = #{docType} ORDER BY id DESC")
    List<KnowledgeDocument> findByDocType(@Param("docType") String docType);
    
    /**
     * 根据适用角色查询
     */
    @Select("SELECT * FROM knowledge_document WHERE status = 1 AND (target_role = #{role} OR target_role = 'ALL') ORDER BY id DESC")
    List<KnowledgeDocument> findByTargetRole(@Param("role") String role);
    
    /**
     * 查询所有启用的文档
     */
    @Select("SELECT * FROM knowledge_document WHERE status = 1 ORDER BY id DESC")
    List<KnowledgeDocument> findAllEnabled();
    
    /**
     * 根据标签查询文档
     */
    @Select("""
        SELECT * FROM knowledge_document 
        WHERE status = 1 
        AND tags LIKE CONCAT('%', #{tag}, '%')
        ORDER BY id DESC
        LIMIT #{limit}
        """)
    List<KnowledgeDocument> findByTag(@Param("tag") String tag, @Param("limit") Integer limit);
}