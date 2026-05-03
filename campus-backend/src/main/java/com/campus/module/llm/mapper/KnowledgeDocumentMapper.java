package com.campus.module.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.llm.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

    @Select("SELECT * FROM knowledge_document WHERE status = 1 " +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%') " +
            "OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (target_role = 'ALL' OR target_role = #{role}) " +
            "ORDER BY created_time DESC LIMIT #{limit}")
    List<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword,
                                             @Param("role") String role,
                                             @Param("limit") int limit);

    @Select("SELECT * FROM knowledge_document WHERE status = 1 " +
            "AND doc_type = #{docType} " +
            "AND (target_role = 'ALL' OR target_role = #{role}) " +
            "ORDER BY created_time DESC LIMIT #{limit}")
    List<KnowledgeDocument> findByDocType(@Param("docType") String docType,
                                           @Param("role") String role,
                                           @Param("limit") int limit);
}
