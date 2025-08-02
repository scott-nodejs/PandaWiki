# SimpleMinerU 使用指南

## 🎯 **设计理念**

SimpleMinerU 专门为使用七牛云存储的场景设计，直接使用七牛云 URL 进行解析，无需临时存储。

## 📋 **配置说明**

### **配置文件：application-mineru.yml**

```yaml
mineru:
  api:
    base-url: https://mineru.net
    token: your_mineru_token_here
    enabled: true
    timeout: 300
    poll-interval: 5
    is-ocr: true
    enable-formula: true
    enable-table: true
    language: auto
    model-version: v2
```

### **环境变量（可选）**

```bash
export MINERU_API_TOKEN=your_token_here
export MINERU_ENABLED=true
```

## 🚀 **使用方式**

### **方式1：直接调用解析器**

```java
@Autowired
private SimpleMinerUPdfParser minerUParser;

// 直接使用七牛云URL解析
String qiniuUrl = "https://your-bucket.domain.com/files/document.pdf";
String markdown = minerUParser.parseWithMinerUUrl(qiniuUrl);
```

### **方式2：通过文件解析工厂**

```java
@Autowired
private FileParserFactory fileParserFactory;

// 传入七牛云URL作为文件名
String qiniuUrl = "https://your-bucket.domain.com/files/document.pdf";
FileParser parser = fileParserFactory.getParser(qiniuUrl);
String markdown = parser.parseToMarkdown(null, qiniuUrl);
```

### **方式3：业务服务集成**

```java
@Service
public class DocumentService {
    
    @Autowired
    private SimpleMinerUPdfParser minerUParser;
    
    public String parseQiniuDocument(String qiniuUrl) {
        try {
            log.info("开始解析七牛云文档: {}", qiniuUrl);
            
            // 直接使用七牛云URL解析
            String markdown = minerUParser.parseWithMinerUUrl(qiniuUrl);
            
            log.info("解析成功，内容长度: {}", markdown.length());
            return markdown;
            
        } catch (Exception e) {
            log.error("解析失败: {}", qiniuUrl, e);
            throw new RuntimeException("文档解析失败: " + e.getMessage());
        }
    }
}
```

## 🔄 **解析流程**

1. **URL 验证** - 检查是否为有效的 HTTP/HTTPS URL
2. **配置检查** - 验证 MinerU API 配置是否完整
3. **创建任务** - 调用 MinerU API 创建解析任务
4. **轮询状态** - 定期检查任务进度
5. **下载结果** - 获取解析后的 ZIP 文件
6. **提取内容** - 从 ZIP 中提取 Markdown 文件
7. **返回结果** - 返回最终的 Markdown 内容

## 📊 **优势对比**

| 特性 | SimpleMinerU | 原版MinerU | PDFBox |
|------|-------------|-----------|--------|
| 七牛云直接解析 | ✅ | ❌ | ❌ |
| 临时存储需求 | ❌ | ✅ | ❌ |
| 公式识别 | ✅ | ✅ | ❌ |
| 表格解析 | ✅ | ✅ | 部分 |
| OCR 功能 | ✅ | ✅ | ❌ |
| 配置复杂度 | 低 | 高 | 低 |

## ⚡ **性能优化**

### **1. 并发处理**
```java
@Service
public class BatchDocumentService {
    
    @Autowired
    private SimpleMinerUPdfParser minerUParser;
    
    @Async
    public CompletableFuture<String> parseDocumentAsync(String qiniuUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return minerUParser.parseWithMinerUUrl(qiniuUrl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public List<String> parseMultipleDocuments(List<String> urls) {
        List<CompletableFuture<String>> futures = urls.stream()
            .map(this::parseDocumentAsync)
            .collect(Collectors.toList());
            
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
}
```

### **2. 缓存机制**
```java
@Cacheable(value = "mineru-cache", key = "#qiniuUrl")
public String parseWithCache(String qiniuUrl) {
    return minerUParser.parseWithMinerUUrl(qiniuUrl);
}
```

## 🔍 **调试和监控**

### **启用调试日志**
```yaml
logging:
  level:
    com.chaitin.pandawiki.parse.impl.SimpleMinerUPdfParser: DEBUG
    com.chaitin.pandawiki.config.ConfigurationDebugger: INFO
```

### **监控关键指标**
- 解析成功率
- 平均解析时间
- API 调用频率
- 错误类型分布

## ⚠️ **注意事项**

1. **URL 可访问性** - 确保七牛云 URL 可以被 MinerU 服务访问
2. **文件大小限制** - MinerU 对文件大小有限制
3. **并发限制** - 注意 API 调用频率限制
4. **错误处理** - 实现适当的重试和降级机制

## 🛠️ **故障排除**

### **常见问题**

1. **配置未生效**
   - 检查 Profile 是否激活
   - 验证配置文件语法
   - 查看配置调试日志

2. **URL 无法访问**
   - 验证七牛云 URL 格式
   - 检查文件权限设置
   - 测试直接下载

3. **解析超时**
   - 调整 timeout 配置
   - 检查文件大小
   - 优化网络环境

## 📝 **示例代码**

完整的使用示例：

```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    
    @Autowired
    private SimpleMinerUPdfParser minerUParser;
    
    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parseDocument(
            @RequestParam String qiniuUrl) {
        
        try {
            // 验证URL格式
            if (!qiniuUrl.startsWith("https://")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "无效的七牛云URL"));
            }
            
            // 解析文档
            String markdown = minerUParser.parseWithMinerUUrl(qiniuUrl);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "markdown", markdown,
                "length", markdown.length(),
                "parser", "SimpleMinerU"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
}
```

这样配置后，您就可以直接使用七牛云 URL 进行 PDF 解析了！ 