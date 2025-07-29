# 文件解析器SPI实现说明

## 🎯 功能概述

实现了基于SPI（Service Provider Interface）设计模式的静态文件解析功能，支持多种文件格式解析为Markdown格式。当CrawlerService检测到静态文件时，会自动调用相应的解析器进行处理。

## 🏗️ 架构设计

### SPI架构

```
backend_java/src/main/java/com/chaitin/pandawiki/parse/
├── spi/
│   └── FileParser.java                    # SPI接口定义
├── impl/
│   ├── AbstractFileParser.java           # 抽象基类
│   ├── TxtFileParser.java                # TXT文件解析器
│   ├── MarkdownFileParser.java           # Markdown文件解析器
│   ├── HtmlFileParser.java               # HTML文件解析器
│   ├── WordFileParser.java               # Word文档解析器
│   ├── ExcelFileParser.java              # Excel文档解析器
│   ├── PdfFileParser.java                # PDF文档解析器
│   └── PowerPointFileParser.java         # PowerPoint文档解析器
└── FileParserFactory.java                # 解析器工厂
```

### SPI配置

```
backend_java/src/main/resources/META-INF/services/
└── com.chaitin.pandawiki.parse.spi.FileParser
```

## 📝 支持的文件格式

| 格式 | 扩展名 | 解析器 | 依赖库 | 优先级 | 特性 |
|------|--------|--------|--------|--------|------|
| 文本文件 | txt, text, log | TxtFileParser | 无 | 50 | 智能格式化 |
| Markdown | md, markdown, mdown, mkd | MarkdownFileParser | 无 | 10 | 原生支持 |
| HTML | html, htm, xhtml | HtmlFileParser | 无 | 60 | HTML转Markdown |
| Word文档 | docx, doc | WordFileParser | Apache POI | 70 | 完整文档解析 |
| Excel文档 | xlsx, xls | ExcelFileParser | Apache POI | 80 | 表格转换 |
| PowerPoint | pptx, ppt | PowerPointFileParser | Apache POI | 85 | 幻灯片内容 |
| PDF文档 | pdf | PdfFileParser | Apache PDFBox | 90 | **全页面提取+智能格式化** |

## 🔧 核心组件

### 1. FileParser SPI接口

```java
public interface FileParser {
    List<String> getSupportedExtensions();
    String parseToMarkdown(InputStream inputStream, String fileName) throws Exception;
    String getParserName();
    default int getPriority() { return 100; }
}
```

### 2. FileParserFactory 工厂类

- **自动发现**：使用`ServiceLoader`自动加载所有解析器实现
- **优先级管理**：支持解析器优先级，数值越小优先级越高
- **扩展名映射**：建立文件扩展名到解析器的映射关系
- **线程安全**：使用`ConcurrentHashMap`保证并发安全

### 3. AbstractFileParser 基类

提供公共工具方法：
- `readInputStream()` - 读取输入流为字符串
- `cleanText()` - 清理和格式化文本
- `textToMarkdown()` - 将普通文本转换为Markdown
- `escapeMarkdown()` - 转义Markdown特殊字符
- `createFileMetadata()` - 生成文件元信息

## 🔌 CrawlerService集成

### 流程图

```
HTTP请求 → CrawlerService → 检测文件类型
                              ↓
                         静态文件？
                           ↙    ↘
                       是        否
                       ↓          ↓
               FileParserFactory  调用爬虫服务
                       ↓
               选择对应解析器
                       ↓
               解析为Markdown
                       ↓
               返回解析结果
```

### 检测逻辑

```java
private boolean isStaticFile(String url) {
    String fileName = extractFileNameFromUrl(url);
    return fileParserFactory.isSupported(fileName);
}
```

### 解析流程

```java
private ScrapeResponse parseStaticFile(String fileUrl, String kbId) {
    // 1. 下载文件内容
    byte[] fileContent = downloadFile(fileUrl);
    
    // 2. 解析文件
    String fileName = extractFileNameFromUrl(fileUrl);
    String markdownContent = fileParserFactory.parseToMarkdown(
        new ByteArrayInputStream(fileContent), fileName);
    
    // 3. 构建响应
    ScrapeResponse result = new ScrapeResponse();
    result.setTitle(generateTitleFromFileName(fileName));
    result.setContent(markdownContent);
    
    return result;
}
```

## 🧪 测试接口

### TestFileParserController

提供以下测试接口：

```bash
# 健康检查
GET /api/test/parser/health

# 获取支持的文件格式
GET /api/test/parser/supported-formats

# 获取所有解析器信息
GET /api/test/parser/parsers

# 检查文件是否支持解析
GET /api/test/parser/check-support?fileName=example.pdf

# 上传文件并解析
POST /api/test/parser/parse-file
```

### 使用示例

```bash
# 1. 检查支持的格式
curl http://localhost:8080/api/test/parser/supported-formats

# 2. 检查特定文件是否支持
curl "http://localhost:8080/api/test/parser/check-support?fileName=test.docx"

# 3. 上传文件解析
curl -X POST -F "file=@document.pdf" http://localhost:8080/api/test/parser/parse-file

# 4. 通过CrawlerService解析静态文件
curl -X POST "http://localhost:8080/api/crawler/scrape" \
  -H "Content-Type: application/json" \
  -d '{"url": "http://example.com/document.pdf", "kb_id": "test-kb"}'
```

## 📦 依赖配置

### 可选依赖（按需添加）

```xml
<!-- Apache POI - 支持Office文档 -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.3</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-scratchpad</artifactId>
    <version>5.2.3</version>
</dependency>

<!-- Apache PDFBox - 支持PDF文档 -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.29</version>
</dependency>
```

## 🚀 扩展指南

### 添加新的解析器

1. **创建解析器实现**：
```java
public class CustomFileParser extends AbstractFileParser {
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("custom", "ext");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        // 实现解析逻辑
        return markdownContent;
    }
    
    @Override
    public String getParserName() {
        return "自定义文件解析器";
    }
}
```

2. **更新SPI配置**：
在`META-INF/services/com.chaitin.pandawiki.parse.spi.FileParser`中添加：
```
com.example.CustomFileParser
```

3. **自动生效**：
重启应用后，新解析器会自动被发现和注册。

### 优先级管理

- 数值越小，优先级越高
- 如果多个解析器支持同一扩展名，优先级高的会被选中
- 建议优先级范围：核心格式(10-50)，Office文档(70-90)，其他格式(100+)

## ⚠️ 注意事项

### 当前限制

1. **依赖管理**：Office文档和PDF解析需要额外依赖，使用反射避免编译时依赖
2. **内存使用**：大文件解析可能消耗较多内存
3. **格式兼容**：某些复杂格式可能解析不完整

### PDF解析器特别说明

**🆕 最新改进** (v2.0):

#### **全页面提取保证**
- ✅ **双重提取策略**：优先一次性提取所有页面，失败时自动切换到逐页提取
- ✅ **页面范围控制**：明确设置`setStartPage(1)`和`setEndPage(totalPages)`确保不遗漏
- ✅ **提取验证**：检查提取结果，空内容时自动尝试备用方案

#### **智能格式化处理**
- 🎯 **标题识别**：自动识别全大写文本、编号标题等格式
- 🎯 **列表处理**：智能识别和格式化各种列表项（数字、字母、符号）
- 🎯 **段落优化**：清理多余空白符，保持合理的段落分隔
- 🎯 **Markdown转换**：将PDF结构转换为标准Markdown格式

#### **错误处理和日志**
- 📊 **详细日志**：记录总页数、每页提取状态、内容长度等信息
- 🛡️ **容错机制**：单页失败不影响其他页面处理
- 🔍 **调试友好**：提供清晰的错误信息和处理状态

#### **使用示例**
```bash
# 测试7页PDF文档解析
curl -X POST -F "file=@document-7pages.pdf" \
  http://localhost:8080/api/test/parser/parse-file

# 预期输出格式：
# ---
# 文件名: document-7pages.pdf
# 解析时间: 2025-01-29T17:45:00
# ---
# 
# # PDF文档
# 
# **PDF信息**: 共 7 页
# 
# ---
# 
# ## 第 1 页
# 
# ### 标题内容
# 文档正文内容...
# 
# ---
# 
# ## 第 2 页
# 
# 继续的内容...
```

### 最佳实践

1. **错误处理**：所有解析器都实现了优雅的错误处理
2. **日志记录**：详细的解析过程日志便于调试
3. **资源管理**：正确关闭输入流和文档对象
4. **编码处理**：统一使用UTF-8编码

## 🔍 故障排除

### 常见问题

1. **解析器未生效**：
   - 检查SPI配置文件是否正确
   - 确认解析器类在classpath中

2. **依赖缺失错误**：
   - 查看日志中的ClassNotFoundException
   - 根据错误添加对应的Maven依赖

3. **解析结果异常**：
   - 检查文件格式是否正确
   - 查看解析器日志输出

### 调试方法

```bash
# 1. 检查解析器注册状态
curl http://localhost:8080/api/test/parser/parsers

# 2. 测试特定文件支持
curl "http://localhost:8080/api/test/parser/check-support?fileName=test.pdf"

# 3. PDF解析专用调试
# 上传PDF文件进行测试解析
curl -X POST -F "file=@your-document.pdf" \
  http://localhost:8080/api/test/parser/parse-file

# 4. 查看应用启动日志
# 搜索 "文件解析器初始化" 相关日志

# 5. PDF解析日志关键字
# 搜索以下关键字来诊断PDF解析问题：
# - "PDF文档总页数"
# - "成功提取PDF全部内容"
# - "一次性提取失败，尝试逐页提取"
# - "提取PDF第X页内容"
# - "第X页提取完成"
```

#### **PDF解析常见问题排查**

1. **只解析了部分页面**：
   - 检查日志中是否有"PDF文档总页数: X"
   - 查看是否有"一次性提取失败"的警告
   - 确认逐页提取日志是否覆盖所有页面

2. **格式混乱**：
   - 查看原PDF是否包含复杂布局（表格、图片等）
   - 检查是否有"标题识别"、"列表处理"相关日志
   - 尝试不同的PDF文件对比结果

3. **内容为空**：
   - 确认PDF是否为扫描版（图片PDF）
   - 检查日志中的"内容长度"信息
   - 验证PDF文件是否可正常打开

4. **依赖缺失**：
   - 查看是否有"Apache PDFBox library not found"错误
   - 添加PDFBox依赖后重启应用

## 🎉 总结

通过SPI设计模式实现的文件解析功能具有以下优势：

- ✅ **可扩展性**：易于添加新的文件格式支持
- ✅ **模块化**：每个解析器独立实现，互不干扰  
- ✅ **灵活性**：支持优先级管理和动态发现
- ✅ **可测试性**：提供完整的测试接口
- ✅ **健壮性**：全面的错误处理和日志记录
- ✅ **集成性**：与现有CrawlerService无缝集成

该实现为牛小库提供了强大的文档处理能力，支持将各种格式的文档转换为Markdown并进行向量化存储！ 