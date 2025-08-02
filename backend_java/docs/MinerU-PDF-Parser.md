# MinerU PDF解析器使用说明

## 概述

MinerU PDF解析器是一个基于 MinerU API 的高质量 PDF 文档解析服务，支持智能文档理解、公式识别、表格解析、OCR 等功能。当 MinerU 解析失败时，会自动回退到本地 PDFBox 解析器。

## 功能特性

### 🚀 核心功能
- **智能解析**：基于 MinerU v2 模型，支持复杂文档结构理解
- **公式识别**：自动识别并转换数学公式为 LaTeX 格式
- **表格解析**：精确提取表格内容并转换为 Markdown 格式
- **多语言 OCR**：支持 84 种语言的文本识别
- **自动回退**：MinerU 失败时自动使用 PDFBox 解析器

### 💡 技术特点
- **SPI 架构**：遵循服务提供者接口模式，易于扩展
- **异步处理**：支持长时间运行的解析任务
- **资源管理**：自动清理临时文件，防止磁盘空间浪费
- **配置灵活**：支持环境变量和配置文件多种配置方式

## 配置说明

### 1. 基础配置

在 `application.yml` 中添加以下配置：

```yaml
# 引入 MinerU 配置
spring:
  profiles:
    include: mineru

# 启用定时任务（用于文件清理）
spring:
  task:
    scheduling:
      enable: true
```

### 2. MinerU API 配置

```yaml
mineru:
  api:
    # MinerU API Token（必需）
    token: "your-mineru-api-token"
    
    # API 服务地址
    base-url: "https://mineru.net"
    
    # 是否启用 MinerU 解析器
    enabled: true
    
    # 解析超时时间（秒）
    timeout: 300
    
    # 轮询间隔（秒）
    poll-interval: 5
    
    # OCR 功能
    is-ocr: true
    
    # 公式识别
    enable-formula: true
    
    # 表格识别
    enable-table: true
    
    # 文档语言（auto/ch/en等）
    language: "auto"
    
    # 模型版本（推荐 v2）
    model-version: "v2"
```

### 3. 临时文件存储配置

```yaml
temp:
  storage:
    local:
      # 启用本地临时存储
      enabled: true
      
      # 存储路径
      base-path: "/tmp/pandawiki-temp"
      
      # 文件访问 URL
      base-url: "http://localhost:8080/temp"
      
      # 自动清理配置
      cleanup:
        enabled: true
        interval-minutes: 60    # 清理间隔
        retention-hours: 24     # 文件保留时间
```

### 4. 环境变量配置

```bash
# MinerU API Token
export MINERU_API_TOKEN="your-token-here"

# 启用/禁用 MinerU
export MINERU_ENABLED=true

# 临时存储路径
export TEMP_STORAGE_PATH="/opt/pandawiki/temp"

# 其他可选配置
export MINERU_TIMEOUT=600
export MINERU_LANGUAGE=ch
export TEMP_RETENTION_HOURS=48
```

## 获取 MinerU API Token

1. 访问 [MinerU 官网](https://mineru.net)
2. 注册账号并登录
3. 在个人中心申请 API Token
4. 将 Token 配置到应用中

## 使用说明

### 1. 程序化使用

```java
@Autowired
private MinerUPdfParser minerUPdfParser;

public void parseDocument() {
    try (InputStream pdfStream = new FileInputStream("document.pdf")) {
        String markdown = minerUPdfParser.parseToMarkdown(pdfStream, "document.pdf");
        System.out.println("解析结果：" + markdown);
    } catch (Exception e) {
        log.error("解析失败", e);
    }
}
```

### 2. 解析器优先级

系统会按照以下优先级选择解析器：

1. **MinerUPdfParser** (优先级: 100) - 优先使用
2. **PdfFileParser** (优先级: 90) - 回退方案

### 3. 错误处理

解析器具有完善的错误处理机制：

- **配置检查**：启动时检查 Token 和配置
- **自动回退**：MinerU 失败时使用 PDFBox
- **资源清理**：异常时自动清理临时文件
- **详细日志**：提供详细的错误信息和调试日志

## 监控和维护

### 1. 健康检查

访问以下端点检查服务状态：

```bash
# 检查临时文件服务
curl http://localhost:8080/temp/health

# 应该返回: "healthy" 或 "disabled"
```

### 2. 存储统计

```java
@Autowired
private TempFileCleanupTask cleanupTask;

public void checkStorage() {
    TempFileCleanupTask.StorageStats stats = cleanupTask.getStorageStats();
    log.info("存储统计: {}", stats);
}
```

### 3. 手动清理

```java
@Autowired
private TempFileCleanupTask cleanupTask;

public void manualCleanup() {
    cleanupTask.triggerCleanup();
}
```

## 性能优化

### 1. 内存管理

- 解析大文件时注意内存使用
- 设置合适的 JVM 堆内存大小
- 监控临时文件磁盘使用

### 2. 网络优化

- 确保到 MinerU API 的网络连接稳定
- 根据网络情况调整超时时间
- 考虑部署在网络延迟较低的区域

### 3. 并发控制

```yaml
# 限制并发解析任务数量
server:
  tomcat:
    threads:
      max: 50
    max-connections: 1000
```

## 故障排除

### 常见问题

1. **Token 无效**
   ```
   错误：MinerU API错误: Token 错误
   解决：检查 Token 是否正确，是否已过期
   ```

2. **网络超时**
   ```
   错误：MinerU解析超时
   解决：增加 timeout 配置值，检查网络连接
   ```

3. **临时文件服务不可用**
   ```
   错误：临时文件存储服务不可用
   解决：检查存储路径权限，确保目录可写
   ```

4. **文件上传失败**
   ```
   错误：文件上传到临时存储失败
   解决：检查磁盘空间，确保存储路径正确
   ```

### 日志级别配置

```yaml
logging:
  level:
    com.chaitin.pandawiki.parse.spi.MinerUPdfParser: INFO
    com.chaitin.pandawiki.service.impl.LocalTempFileStorageService: DEBUG
    com.chaitin.pandawiki.task.TempFileCleanupTask: INFO
```

## 安全注意事项

1. **Token 安全**：不要在代码中硬编码 Token
2. **文件权限**：确保临时文件目录的访问权限正确
3. **网络安全**：使用 HTTPS 访问 MinerU API
4. **数据清理**：及时清理临时文件，避免敏感数据泄露

## 版本兼容性

- **Java**: 8+
- **Spring Boot**: 2.x+
- **MinerU API**: v4+

## 更新日志

### v1.0.0
- 初始版本发布
- 支持 MinerU API v4
- 实现自动回退机制
- 添加临时文件管理
- 支持定时清理任务

## 许可证

本项目采用与主项目相同的许可证。

## 技术支持

如果遇到问题，请：

1. 查看日志文件
2. 检查配置是否正确
3. 确认网络连接正常
4. 联系技术支持团队 