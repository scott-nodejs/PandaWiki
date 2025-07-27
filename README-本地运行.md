# 🐼 PandaWiki Backend 本地运行指南

## 📋 环境要求

### 必需软件
1. **Go 1.24.3+** - [下载地址](https://golang.org/dl/)
2. **Docker Desktop** - [下载地址](https://www.docker.com/products/docker-desktop/)
3. **Git** - [下载地址](https://git-scm.com/downloads)

### 可选软件
- **VSCode** + Go扩展 - 推荐的开发环境
- **DataGrip** 或 **pgAdmin** - 数据库管理工具

## 🚀 快速开始

### 方法一：使用启动脚本（推荐）

```powershell
# 1. 确保在项目根目录
cd E:\product\pandawiki\PandaWiki

# 2. 运行启动脚本
.\run-local.ps1

# 3. 按照脚本提示运行后续命令
```

### 方法二：手动步骤

#### 1. 启动依赖服务
```bash
# 启动Docker服务
docker-compose -f docker-compose.dev.yml up -d

# 查看服务状态
docker-compose -f docker-compose.dev.yml ps
```

#### 2. 安装Go工具
```bash
# 安装代码生成工具
go install github.com/google/wire/cmd/wire@latest
go install github.com/swaggo/swag/cmd/swag@latest
```

#### 3. 生成代码
```bash
cd backend

# 下载依赖
go mod download

# 生成Swagger文档
swag fmt
swag init -g cmd/api/main.go --pd

# 生成依赖注入代码
wire cmd/api/wire.go
wire cmd/consumer/wire.go
wire cmd/migrate/wire.go
```

#### 4. 运行服务

**终端1 - 数据库迁移：**
```bash
cd backend
go run cmd/migrate/main.go cmd/migrate/wire_gen.go
```

**终端2 - API服务：**
```bash
cd backend

# 设置配置文件路径
$env:CONFIG_FILE="config/config.local.yml"

go run cmd/api/main.go cmd/api/wire_gen.go
```

**终端3 - 消费者服务：**
```bash
cd backend

# 设置配置文件路径
$env:CONFIG_FILE="config/config.local.yml"

go run cmd/consumer/main.go cmd/consumer/wire_gen.go
```

## 🌐 服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| API服务 | http://localhost:8000 | 主要API接口 |
| Swagger文档 | http://localhost:8000/swagger/index.html | API文档 |
| PostgreSQL | localhost:5432 | 数据库 |
| Redis | localhost:6379 | 缓存 |
| MinIO管理界面 | http://localhost:9001 | 对象存储管理 |
| NATS监控 | http://localhost:8222 | 消息队列监控 |

## 🔧 开发配置

### 数据库连接
```
主机: localhost
端口: 5432
数据库: panda-wiki
用户名: panda-wiki
密码: panda-wiki-secret
```

### MinIO配置
```
访问密钥: s3panda-wiki
秘密密钥: panda-wiki-s3-secret
控制台: http://localhost:9001
```

### 默认管理员账户
```
用户名: admin
密码: admin123
```

## 🛠️ 常用命令

### 查看服务状态
```bash
docker-compose -f docker-compose.dev.yml ps
```

### 查看服务日志
```bash
# 查看所有服务日志
docker-compose -f docker-compose.dev.yml logs

# 查看特定服务日志
docker-compose -f docker-compose.dev.yml logs postgres
docker-compose -f docker-compose.dev.yml logs redis
```

### 停止服务
```bash
# 停止所有Docker服务
docker-compose -f docker-compose.dev.yml down

# 停止并删除数据卷（谨慎使用）
docker-compose -f docker-compose.dev.yml down -v
```

### 重启服务
```bash
# 重启所有服务
docker-compose -f docker-compose.dev.yml restart

# 重启特定服务
docker-compose -f docker-compose.dev.yml restart postgres
```

## 🔍 故障排除

### 常见问题

#### 1. Docker服务启动失败
```bash
# 检查Docker是否运行
docker info

# 检查端口占用
netstat -ano | findstr :5432
netstat -ano | findstr :6379
netstat -ano | findstr :8000
```

#### 2. Go编译错误
```bash
# 清理模块缓存
go clean -modcache

# 重新下载依赖
go mod download

# 更新依赖
go mod tidy
```

#### 3. 数据库连接失败
```bash
# 检查PostgreSQL容器状态
docker-compose -f docker-compose.dev.yml logs postgres

# 手动连接测试
docker exec -it panda-wiki-postgres psql -U panda-wiki -d panda-wiki
```

#### 4. 代码生成失败
```bash
# 检查工具是否安装
wire version
swag --version

# 重新安装工具
go install github.com/google/wire/cmd/wire@latest
go install github.com/swaggo/swag/cmd/swag@latest
```

### 日志位置
- **API服务日志**: 控制台输出
- **Docker服务日志**: `docker-compose logs`
- **Go工具日志**: 控制台输出

## 📝 开发提示

### 热重载开发
推荐使用 `air` 工具进行热重载开发：

```bash
# 安装air
go install github.com/air-verse/air@latest

# 在backend目录下创建 .air.toml 配置文件
# 然后运行
air
```

### VSCode配置
在 `.vscode/settings.json` 中添加：
```json
{
    "go.toolsManagement.checkForUpdates": "local",
    "go.useLanguageServer": true,
    "go.gopath": "",
    "go.goroot": "",
    "go.lintOnSave": "package",
    "go.vetOnSave": "package",
    "go.formatTool": "goimports",
    "go.lintTool": "golint"
}
```

### 调试配置
在 `.vscode/launch.json` 中添加：
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "name": "Debug API",
            "type": "go",
            "request": "launch",
            "mode": "auto",
            "program": "${workspaceFolder}/backend/cmd/api",
            "env": {
                "CONFIG_FILE": "config/config.local.yml"
            },
            "cwd": "${workspaceFolder}/backend"
        }
    ]
}
```

## 🎯 下一步

1. **配置AI模型** - 在管理界面配置Chat、Embedding、Rerank模型
2. **创建知识库** - 创建第一个知识库并上传文档
3. **测试API** - 使用Swagger文档测试各种API接口
4. **开发功能** - 开始开发新功能或修改现有功能

## 📞 获取帮助

如果遇到问题，可以：
1. 查看项目的GitHub Issues
2. 阅读官方文档
3. 加入社区讨论群 