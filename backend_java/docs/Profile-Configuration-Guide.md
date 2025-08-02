# Spring Boot Profile 配置指南

## 📝 **Profile 基础概念**

Spring Boot Profile 用于管理不同环境的配置，让同一套代码可以在开发、测试、生产等环境中使用不同的配置。

## 🗂️ **配置文件结构**

```
src/main/resources/
├── application.yml              # 主配置文件（公共配置）
├── application-dev.yml          # 开发环境配置
├── application-test.yml         # 测试环境配置
├── application-prod.yml         # 生产环境配置
├── application-mineru.yml       # MinerU 功能配置
└── application-database.yml     # 数据库专用配置
```

## ⚙️ **Profile 激活方式**

### 1. **在配置文件中激活**

```yaml
# application.yml
spring:
  profiles:
    active: dev  # 默认激活开发环境
    include:
      - mineru   # 总是加载 MinerU 配置
      - database # 总是加载数据库配置
```

### 2. **通过启动参数激活**

```bash
# 方式 1：使用 --spring.profiles.active
java -jar app.jar --spring.profiles.active=prod

# 方式 2：使用环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar

# 方式 3：同时激活多个 profile
java -jar app.jar --spring.profiles.active=prod,mineru

# 方式 4：Maven 启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. **在 IDE 中配置**

```bash
# IntelliJ IDEA 运行配置
VM options: -Dspring.profiles.active=dev

# Eclipse 运行配置
Program arguments: --spring.profiles.active=dev
```

## 📋 **配置示例**

### **主配置文件（application.yml）**

```yaml
# 公共配置，所有环境共享
spring:
  application:
    name: pandawiki
  profiles:
    include:
      - mineru  # 引入 MinerU 配置

# 默认配置
server:
  port: 8080

# 通用日志配置
logging:
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### **开发环境（application-dev.yml）**

```yaml
# 开发环境特定配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pandawiki_dev
    username: root
    password: 123456

logging:
  level:
    com.chaitin.pandawiki: DEBUG

# 开发环境 MinerU 配置
mineru:
  api:
    timeout: 120  # 开发环境短超时
```

### **生产环境（application-prod.yml）**

```yaml
# 生产环境特定配置
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

logging:
  level:
    root: WARN
    com.chaitin.pandawiki: INFO

# 生产环境 MinerU 配置
mineru:
  api:
    timeout: 600  # 生产环境长超时
```

## 🎯 **Profile 优先级**

配置加载优先级（高到低）：

1. **命令行参数**: `--spring.profiles.active=prod`
2. **环境变量**: `SPRING_PROFILES_ACTIVE=prod`
3. **application.yml中的spring.profiles.active**
4. **默认profile**

## 🔧 **最佳实践**

### 1. **Profile 命名约定**

```yaml
application-{环境}.yml     # 环境相关
application-{功能}.yml     # 功能相关

# 示例
application-dev.yml       # 开发环境
application-prod.yml      # 生产环境
application-mineru.yml    # MinerU 功能
application-redis.yml     # Redis 配置
```

### 2. **配置分离原则**

```yaml
# ✅ 好的做法：功能相关配置独立
application-mineru.yml    # MinerU 所有配置
application-database.yml  # 数据库所有配置

# ❌ 避免：在环境配置中混合功能配置
application-dev.yml       # 不要在这里配置 MinerU 详细参数
```

### 3. **环境变量使用**

```yaml
# 敏感信息使用环境变量
spring:
  datasource:
    username: ${DB_USERNAME:defaultuser}
    password: ${DB_PASSWORD:defaultpass}

mineru:
  api:
    token: ${MINERU_API_TOKEN:}
```

## 🚀 **实际应用场景**

### **场景1：本地开发**
```bash
# 启动开发环境，包含 MinerU 和调试日志
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### **场景2：测试环境**
```bash
# 启动测试环境，禁用 MinerU，使用内存数据库
java -jar app.jar --spring.profiles.active=test,no-mineru
```

### **场景3：生产部署**
```bash
# 生产环境，通过环境变量配置
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://prod-db:3306/pandawiki
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export MINERU_API_TOKEN=your_production_token
java -jar app.jar
```

## 🔍 **调试 Profile 配置**

### **查看当前激活的 Profile**

```java
@Component
public class ProfileChecker implements CommandLineRunner {
    
    @Autowired
    private Environment environment;
    
    @Override
    public void run(String... args) {
        log.info("当前激活的 Profiles: {}", 
            Arrays.toString(environment.getActiveProfiles()));
        log.info("默认 Profiles: {}", 
            Arrays.toString(environment.getDefaultProfiles()));
    }
}
```

### **配置验证日志**

```yaml
# 在 application.yml 中添加
logging:
  level:
    org.springframework.boot.context.config: DEBUG  # 显示配置加载过程
    org.springframework.core.env: DEBUG             # 显示环境变量
```

## ⚠️ **常见问题**

### **问题1：Profile 不生效**
```bash
# 检查拼写
--spring.profiles.active=dev  # ✅ 正确
--spring.profile.active=dev   # ❌ 错误（少了s）
```

### **问题2：配置冲突**
```yaml
# ❌ 在多个文件中定义相同配置会冲突
# application.yml
mineru:
  api:
    enabled: true

# application-dev.yml  
mineru:
  api:
    enabled: false  # 这会覆盖上面的配置
```

### **问题3：YAML 语法错误**
```yaml
# ❌ 错误：重复的键
spring:
  profiles:
    active: dev
spring:          # 重复的键
  datasource:
    url: xxx

# ✅ 正确
spring:
  profiles:
    active: dev
  datasource:
    url: xxx
```

## 📊 **当前项目 Profile 配置**

### **推荐的启动方式**

```bash
# 开发环境（本地调试）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 测试环境
java -jar app.jar --spring.profiles.active=test

# 生产环境
export SPRING_PROFILES_ACTIVE=prod
export MINERU_API_TOKEN=your_token
java -jar app.jar
```

这样配置后，您的 MinerU 解析器就能根据不同环境使用不同的配置参数了！ 