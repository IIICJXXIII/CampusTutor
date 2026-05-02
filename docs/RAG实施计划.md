# RAG检索增强系统实施计划

> 基于现有代码架构制定，确保可落地实现

---

## 实施进度总览

| 阶段 | 状态 | 完成时间 |
|------|------|----------|
| 第一阶段：基础RAG（关键词检索） | ✅ 已完成 | 2026-05-02 |
| 第二阶段：语义检索（向量数据库） | 📋 待实施 | - |
| 第三阶段：知识库管理功能 | ✅ 已完成 | 2026-05-02 |

---

## 一、现状分析

### 1.1 已有基础

| 组件 | 状态 | 说明 |
|------|------|------|
| SQL表结构 | ✅ 已定义 | `knowledge_document`、`prompt_template`、`user_profile_ai` |
| LLM对话服务 | ✅ 已实现 | `ChatAssistantService.java` |
| LLM客户端 | ✅ 已实现 | `LlmClientService.java`（支持DeepSeek/OpenAI/通义千问） |
| API接口 | ✅ 已实现 | `/api/llm/chat`、`/api/llm/demand/parse` 等 |
| 上下文管理 | ✅ 已实现 | `ChatContextManager.java` |
| **RAG检索服务** | ✅ 已实现 | `KnowledgeRetrievalService.java` |
| **知识库管理接口** | ✅ 已实现 | `/api/knowledge/**` |

### 1.2 已完成组件

| 组件 | 状态 | 文件 |
|------|------|------|
| Java实体类 | ✅ 已实现 | `KnowledgeDocument.java` |
| Mapper层 | ✅ 已实现 | `KnowledgeDocumentMapper.java` |
| Service层 | ✅ 已实现 | `KnowledgeRetrievalService.java`、`KnowledgeDocumentService.java` |
| Controller层 | ✅ 已实现 | `KnowledgeController.java` |
| 知识库数据 | ✅ 已初始化 | 12条文档数据 |

### 1.3 待实现部分

| 组件 | 状态 | 需要实现 |
|------|------|----------|
| 向量数据库 | 📋 待实施 | Milvus或其他向量库 |
| 向量化服务 | 📋 待实施 | 文本Embedding服务 |

---

## 二、分阶段实施计划

### 第一阶段：基础RAG（关键词检索）—— 预计2-3天

**目标**：实现基于关键词的知识库检索，无需向量数据库

#### 2.1.1 创建实体类

```java
// campus-backend/src/main/java/com/campus/module/llm/entity/KnowledgeDocument.java
@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String docType;      // RULE/FAQ/LESSON_PLAN/COMMENT/TEACHING_EXPERIENCE
    private String source;
    private String tags;
    private String targetRole;   // TEACHER/PARENT/ALL
    private String applicableSubjects;
    private String applicableGrades;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

```java
// campus-backend/src/main/java/com/campus/module/llm/entity/PromptTemplate.java
@Data
@TableName("prompt_template")
public class PromptTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String scene;        // DEMAND_CONSULT/TUTOR_RECOMMEND/LESSON_PLAN/COMMENT_POLISH/GENERAL_QA
    private String template;
    private String variables;    // JSON格式
    private Integer isActive;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

#### 2.1.2 创建Mapper

```java
// campus-backend/src/main/java/com/campus/module/llm/mapper/KnowledgeDocumentMapper.java
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
    // 基于关键词模糊匹配
    @Select("SELECT * FROM knowledge_document WHERE status = 1 " +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%') OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (target_role = 'ALL' OR target_role = #{role}) " +
            "ORDER BY created_time DESC LIMIT #{limit}")
    List<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword, 
                                             @Param("role") String role, 
                                             @Param("limit") int limit);
}
```

#### 2.1.3 创建知识库检索服务

```java
// campus-backend/src/main/java/com/campus/module/llm/service/KnowledgeRetrievalService.java
@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    private final KnowledgeDocumentMapper knowledgeMapper;
    private final PromptTemplateMapper templateMapper;
    
    /**
     * 基于关键词检索知识库
     */
    public List<KnowledgeDocument> retrieveRelevantDocs(String query, String role) {
        // 1. 分词提取关键词
        List<String> keywords = extractKeywords(query);
        
        // 2. 检索知识库
        List<KnowledgeDocument> results = new ArrayList<>();
        for (String keyword : keywords) {
            results.addAll(knowledgeMapper.searchByKeyword(keyword, role, 5));
        }
        
        // 3. 去重并按相关性排序
        return deduplicateAndRank(results, query);
    }
    
    /**
     * 获取Prompt模板
     */
    public String getPromptTemplate(String scene, Map<String, String> variables) {
        PromptTemplate template = templateMapper.selectOne(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getScene, scene)
                .eq(PromptTemplate::getIsActive, 1)
        );
        
        if (template == null) return null;
        
        // 变量替换
        String result = template.getTemplate();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
    
    private List<String> extractKeywords(String query) {
        // 简单分词：按空格、逗号分割，过滤停用词
        return Arrays.stream(query.split("[\\s,，。？！]"))
            .filter(word -> word.length() >= 2)
            .filter(word -> !isStopWord(word))
            .collect(Collectors.toList());
    }
}
```

#### 2.1.4 集成到ChatAssistantService

修改 `ChatAssistantService.java`：

```java
@Service
@RequiredArgsConstructor
public class ChatAssistantService {
    
    private final KnowledgeRetrievalService knowledgeService;  // 新增
    
    public ChatResponse chat(List<ChatMessage> messages, String scene, String previousSummary) {
        // 1. 获取用户最新问题
        String userQuery = messages.get(messages.size() - 1).getContent();
        
        // 2. 检索相关知识库文档（RAG）
        List<KnowledgeDocument> relevantDocs = knowledgeService.retrieveRelevantDocs(
            userQuery, 
            UserContext.getRole() == 1 ? "TEACHER" : "PARENT"
        );
        
        // 3. 构建增强的System Prompt
        String enhancedPrompt = buildEnhancedPrompt(relevantDocs);
        
        // 4. 构建消息列表
        List<ChatMessage> fullMessages = new ArrayList<>();
        fullMessages.add(ChatMessage.system(enhancedPrompt));
        fullMessages.addAll(messages);
        
        // 5. 调用LLM
        return llmClient.chat(fullMessages, buildAllTools());
    }
    
    private String buildEnhancedPrompt(List<KnowledgeDocument> docs) {
        StringBuilder sb = new StringBuilder(UNIFIED_SYSTEM_PROMPT);
        
        if (!docs.isEmpty()) {
            sb.append("\n\n【相关知识库内容】\n");
            for (KnowledgeDocument doc : docs) {
                sb.append("- ").append(doc.getTitle()).append("：")
                  .append(doc.getContent()).append("\n");
            }
        }
        
        return sb.toString();
    }
}
```

---

### 第二阶段：语义检索（向量数据库）—— 预计3-5天

**目标**：集成Milvus向量数据库，实现语义相似度检索

#### 2.2.1 添加依赖

```xml
<!-- pom.xml -->
<!-- Milvus Java SDK -->
<dependency>
    <groupId>io.milvus</groupId>
    <artifactId>milvus-sdk-java</artifactId>
    <version>2.3.4</version>
</dependency>

<!-- 文本向量化（使用DeepSeek或OpenAI的Embedding API） -->
<!-- 无需额外依赖，复用现有HTTP客户端 -->
```

#### 2.2.2 配置Milvus连接

```properties
# application.properties
milvus.host=localhost
milvus.port=19530
milvus.database=campus_tutor
milvus.collection=knowledge_vectors
```

#### 2.2.3 创建向量服务

```java
// campus-backend/src/main/java/com/campus/module/llm/service/EmbeddingService.java
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final LlmConfig llmConfig;
    
    /**
     * 调用DeepSeek/OpenAI Embedding API获取文本向量
     */
    public float[] getEmbedding(String text) {
        String url = llmConfig.getBaseUrl() + "/embeddings";
        
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "text-embedding-3-small");
        requestBody.set("input", text);
        
        HttpResponse response = HttpRequest.post(url)
            .header("Authorization", "Bearer " + llmConfig.getApiKey())
            .body(requestBody.toString())
            .execute();
        
        JSONObject result = JSONUtil.parseObj(response.body());
        JSONArray embedding = result.getJSONArray("data")
            .getJSONObject(0)
            .getJSONArray("embedding");
        
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.getFloat(i);
        }
        return vector;
    }
}
```

```java
// campus-backend/src/main/java/com/campus/module/llm/service/MilvusService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class MilvusService {

    @Value("${milvus.host:localhost}")
    private String milvusHost;
    
    @Value("${milvus.port:19530}")
    private int milvusPort;
    
    private MilvusServiceClient milvusClient;
    
    @PostConstruct
    public void init() {
        try {
            milvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                    .withHost(milvusHost)
                    .withPort(milvusPort)
                    .build()
            );
            log.info("Milvus连接成功");
        } catch (Exception e) {
            log.warn("Milvus连接失败，将降级到关键词检索: {}", e.getMessage());
        }
    }
    
    /**
     * 向量相似度搜索
     */
    public List<Long> searchSimilar(float[] queryVector, int topK) {
        if (milvusClient == null) return Collections.emptyList();
        
        // 执行向量搜索
        R<SearchResults> response = milvusClient.search(
            SearchParam.newBuilder()
                .withCollectionName("knowledge_vectors")
                .withVectors(Collections.singletonList(queryVector))
                .withTopK(topK)
                .build()
        );
        
        if (response.getStatus() != R.Status.Success.getCode()) {
            return Collections.emptyList();
        }
        
        return response.getData().getResults().get(0).stream()
            .map(score -> score.getLongID())
            .collect(Collectors.toList());
    }
    
    /**
     * 插入向量
     */
    public void insertVector(Long docId, float[] vector) {
        if (milvusClient == null) return;
        
        milvusClient.insert(
            InsertParam.newBuilder()
                .withCollectionName("knowledge_vectors")
                .withFields(Collections.singletonList(
                    new FieldType.Builder()
                        .withName("id")
                        .withDataType(DataType.Int64)
                        .build()
                ))
                .build()
        );
    }
}
```

#### 2.2.4 升级知识库检索服务

```java
@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    private final KnowledgeDocumentMapper knowledgeMapper;
    private final EmbeddingService embeddingService;
    private final MilvusService milvusService;
    
    /**
     * 混合检索：向量检索 + 关键词检索
     */
    public List<KnowledgeDocument> retrieveRelevantDocs(String query, String role) {
        List<KnowledgeDocument> results = new ArrayList<>();
        
        // 1. 向量语义检索
        try {
            float[] queryVector = embeddingService.getEmbedding(query);
            List<Long> docIds = milvusService.searchSimilar(queryVector, 5);
            if (!docIds.isEmpty()) {
                results.addAll(knowledgeMapper.selectBatchIds(docIds));
            }
        } catch (Exception e) {
            log.warn("向量检索失败，降级到关键词检索: {}", e.getMessage());
        }
        
        // 2. 关键词检索（作为补充或降级方案）
        List<String> keywords = extractKeywords(query);
        for (String keyword : keywords) {
            results.addAll(knowledgeMapper.searchByKeyword(keyword, role, 3));
        }
        
        // 3. 去重排序
        return deduplicateAndRank(results, query);
    }
}
```

---

### 第三阶段：知识库管理功能 —— 预计2天

**目标**：实现知识库的增删改查和管理功能

#### 2.3.1 创建管理接口

```java
// campus-backend/src/main/java/com/campus/module/llm/controller/KnowledgeController.java
@Tag(name = "知识库管理", description = "RAG知识库文档管理")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeDocumentService knowledgeService;
    private final EmbeddingService embeddingService;
    private final MilvusService milvusService;
    
    @PostMapping
    @Operation(summary = "添加知识文档")
    public Result<Long> addDocument(@RequestBody KnowledgeDocumentRequest request) {
        // 1. 保存到MySQL
        KnowledgeDocument doc = knowledgeService.save(request);
        
        // 2. 生成向量并存储到Milvus
        float[] vector = embeddingService.getEmbedding(doc.getContent());
        milvusService.insertVector(doc.getId(), vector);
        
        return Result.success(doc.getId());
    }
    
    @GetMapping("/search")
    @Operation(summary = "检索知识库")
    public Result<List<KnowledgeDocument>> search(@RequestParam String query) {
        return Result.success(knowledgeService.retrieveRelevantDocs(query, "ALL"));
    }
}
```

---

## 三、文件清单

### 3.1 需要新建的文件

```
campus-backend/src/main/java/com/campus/module/llm/
├── entity/
│   ├── KnowledgeDocument.java
│   ├── PromptTemplate.java
│   └── UserProfileAi.java
├── mapper/
│   ├── KnowledgeDocumentMapper.java
│   ├── PromptTemplateMapper.java
│   └── UserProfileAiMapper.java
├── service/
│   ├── KnowledgeRetrievalService.java
│   ├── EmbeddingService.java
│   ├── MilvusService.java
│   └── KnowledgeDocumentService.java
├── controller/
│   └── KnowledgeController.java
└── dto/
    └── KnowledgeDocumentRequest.java
```

### 3.2 需要修改的文件

```
campus-backend/src/main/java/com/campus/module/llm/service/
├── ChatAssistantService.java  # 集成RAG检索
└── DemandParseService.java    # 可选：增强需求解析

campus-backend/pom.xml         # 添加Milvus依赖
campus-backend/src/main/resources/application.properties  # 添加Milvus配置
```

---

## 四、实施优先级

| 优先级 | 阶段 | 工作量 | 效果 |
|--------|------|--------|------|
| P0 | 第一阶段：基础RAG | 2-3天 | 实现关键词检索，立即可用 |
| P1 | 第二阶段：语义检索 | 3-5天 | 提升检索准确性 |
| P2 | 第三阶段：管理功能 | 2天 | 方便维护知识库 |

---

## 五、注意事项

### 5.1 降级策略

所有向量相关功能都需要降级策略：

```java
public List<KnowledgeDocument> retrieveRelevantDocs(String query, String role) {
    // 优先向量检索
    try {
        if (milvusService.isAvailable()) {
            return vectorSearch(query);
        }
    } catch (Exception e) {
        log.warn("向量检索失败，降级到关键词检索");
    }
    
    // 降级到关键词检索
    return keywordSearch(query, role);
}
```

### 5.2 成本控制

- Embedding API调用有成本，建议缓存向量结果
- 可以在MySQL中增加`embedding`字段存储向量，避免重复计算

### 5.3 数据初始化

执行SQL脚本初始化知识库数据：

```bash
mysql -u root -p campus_tutor_db < campus-backend/sql/final_rag_setup.sql
```

---

## 六、测试验证

### 6.1 第一阶段测试

```bash
# 测试关键词检索
curl -X POST http://localhost:8080/api/llm/chat \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{"role": "user", "content": "钢琴启蒙怎么教？"}]
  }'
```

### 6.2 第二阶段测试

```bash
# 测试语义检索（需要Milvus运行）
# 启动Milvus
docker run -d --name milvus -p 19530:19530 milvusdb/milvus:latest

# 测试向量检索
curl -X GET "http://localhost:8080/api/knowledge/search?query=孩子想学音乐"
```

---

*文档创建时间: 2026年5月*
*预计总工作量: 7-10天*
