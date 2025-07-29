# Node内容向量化存储实现说明

## 🎯 功能概述

在新增或更新node content时，系统会自动将内容放入`embeddingStoreIngestor`向量库中，实现智能检索和RAG（检索增强生成）功能。

## 🔧 实现架构

### 1. 核心组件

- **EmbeddingStoreIngestor**: 负责文档向量化和存储
- **EmbeddingStore**: 向量存储后端（当前使用InMemoryEmbeddingStore）
- **DocumentSplitter**: 文档分割器（每300字符分割，重叠20字符）
- **ThreadLocalUtils**: 元数据传递工具

### 2. 数据流程

```
Node创建/更新 → 向量化处理 → 存储到向量库 → RAG检索使用
```

## 📝 详细实现

### NodeServiceImpl修改

#### **依赖注入**
```java
private final EmbeddingStoreIngestor embeddingStoreIngestor;
private final EmbeddingStore<TextSegment> embeddingStore;
```

#### **创建节点时向量化**
```java
// 在createNode方法中
if (StringUtils.hasText(node.getContent())) {
    try {
        ingestNodeContent(node);
        log.info("节点内容已向量化存储 - nodeId: {}, kbId: {}", node.getId(), node.getKbId());
    } catch (Exception e) {
        log.error("向量化存储失败 - nodeId: {}, error: {}", node.getId(), e.getMessage(), e);
    }
}
```

#### **更新节点时重新向量化**
```java
// 在updateNode方法中
if (StringUtils.hasText(node.getContent())) {
    try {
        ingestNodeContent(node);
        log.info("节点内容已重新向量化存储 - nodeId: {}, kbId: {}", node.getId(), node.getKbId());
    } catch (Exception e) {
        log.error("向量化存储失败 - nodeId: {}, error: {}", node.getId(), e.getMessage(), e);
    }
}
```

#### **向量化处理方法**
```java
private void ingestNodeContent(Node node) {
    try {
        // 设置元数据到ThreadLocal，供documentTransformer使用
        ThreadLocalUtils.set("knowledgeLibId", node.getKbId());
        ThreadLocalUtils.set("nodeId", node.getId());
        
        // 创建Document对象
        Document document = Document.from(node.getContent());
        
        // 添加节点相关的元数据
        document.metadata().put("nodeId", node.getId());
        document.metadata().put("nodeName", node.getName());
        document.metadata().put("kbId", node.getKbId());
        document.metadata().put("nodeType", String.valueOf(node.getType()));
        document.metadata().put("createdAt", node.getCreatedAt().toString());
        
        // 使用ingestor进行向量化存储
        embeddingStoreIngestor.ingest(document);
        
        log.debug("文档向量化完成 - nodeId: {}, contentLength: {}", 
            node.getId(), node.getContent().length());
            
    } finally {
        // 清理ThreadLocal
        ThreadLocalUtils.remove("knowledgeLibId");
        ThreadLocalUtils.remove("nodeId");
    }
}
```

### 元数据配置

在`AiConfig.java`中的`documentTransformer`会自动添加元数据：

```java
.documentTransformer(dc -> {
    String memoryId = (String) ThreadLocalUtils.get("memoryId");
    String knowledgeLibId = (String) ThreadLocalUtils.get("knowledgeLibId");

    if(StringUtils.hasText(memoryId))
        dc.metadata().put("memoryId", memoryId);
    if(StringUtils.hasText(knowledgeLibId))
        dc.metadata().put("knowledgeLibId", knowledgeLibId);
    return dc;
})
```

## 🧪 测试接口

新增了测试接口来验证向量化功能：

### 1. 创建向量化节点
```bash
POST /api/test/node-vector?content=测试内容
```

### 2. 向量搜索测试
```bash
GET /api/test/vector-search?question=请介绍向量化存储
```

### 3. 现有测试接口
```bash
# 健康检查
GET /api/test/health

# 基本聊天测试
GET /api/test/chat?message=hello

# 新会话AI测试
GET /api/test/chat-new?message=hello

# chunk_result格式测试
GET /api/test/chunk-result?message=test
```

## 📊 元数据结构

每个向量化的文档段落包含以下元数据：

| 字段 | 说明 | 示例 |
|------|------|------|
| nodeId | 节点ID | "abc123" |
| nodeName | 节点名称 | "API文档" |
| kbId | 知识库ID | "test-kb-001" |
| nodeType | 节点类型 | "2" (文档) |
| createdAt | 创建时间 | "2025-01-29T16:30:00" |
| knowledgeLibId | 知识库ID（来自ThreadLocal） | "test-kb-001" |

## 🔍 RAG检索使用

向量化的内容会在以下场景被检索使用：

1. **智能问答**: 通过`ContentRetrieverFactory`检索相关内容
2. **相似文档推荐**: 基于向量相似度
3. **知识库搜索**: 语义搜索而非关键词搜索

## ⚠️ 注意事项

### 当前限制

1. **向量库**: 使用`InMemoryEmbeddingStore`，重启后数据丢失
2. **删除功能**: 节点删除时的向量清理功能待实现
3. **更新策略**: 目前是覆盖式更新，未做增量处理

### 生产环境建议

1. **使用持久化向量库**:
   ```java
   // 替换为PostgreSQL + pgvector
   @Bean
   EmbeddingStore<TextSegment> initEmbeddingStore() {
       return PgVectorEmbeddingStore.builder()
           .host("localhost")
           .port(5432)
           .database("vectordb")
           .user("user")
           .password("password")
           .table("embeddings")
           .dimension(1536)
           .build();
   }
   ```

2. **实现向量删除功能**:
   ```java
   // 在deleteNode时清理对应向量
   List<String> embeddingIds = embeddingStore.search(
       EmbeddingSearchRequest.builder()
           .filter(metadataKey("nodeId").isEqualTo(nodeId))
           .build()
   ).matches().stream()
       .map(EmbeddingMatch::embeddingId)
       .collect(Collectors.toList());
   
   embeddingStore.removeAll(embeddingIds);
   ```

3. **性能优化**:
   - 异步向量化处理
   - 批量向量化
   - 缓存热点向量

## 🚀 使用效果

实现后的系统将提供：

- ✅ **自动向量化**: Node创建/更新时自动向量化存储
- ✅ **智能检索**: 基于语义相似度的内容检索  
- ✅ **RAG增强**: AI问答时自动检索相关文档
- ✅ **元数据过滤**: 支持按知识库、节点类型等过滤
- ✅ **调试接口**: 完整的测试和验证接口

通过这个实现，牛小库的智能问答功能将大大增强，能够基于实际存储的文档内容提供更准确的回答！ 