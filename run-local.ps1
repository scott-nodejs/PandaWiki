# PandaWiki Backend 本地开发启动脚本

Write-Host "🐼 PandaWiki Backend 本地开发环境启动" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

# 检查Docker是否运行
Write-Host "📋 检查环境..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "✅ Docker 运行正常" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker 未运行，请先启动 Docker Desktop" -ForegroundColor Red
    exit 1
}

# 检查Go环境
try {
    $goVersion = go version
    Write-Host "✅ Go 环境: $goVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Go 未安装，请先安装 Go 1.24.3+" -ForegroundColor Red
    Write-Host "   下载地址: https://golang.org/dl/" -ForegroundColor Yellow
    exit 1
}

# 启动依赖服务
Write-Host "`n🚀 启动依赖服务..." -ForegroundColor Yellow
docker-compose -f docker-compose.dev.yml up -d

# 等待服务启动
Write-Host "⏳ 等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 检查服务状态
Write-Host "`n🔍 检查服务状态..." -ForegroundColor Yellow
$services = @("postgres", "redis", "nats", "minio")
foreach ($service in $services) {
    $status = docker-compose -f docker-compose.dev.yml ps -q $service
    if ($status) {
        Write-Host "✅ $service 运行正常" -ForegroundColor Green
    } else {
        Write-Host "❌ $service 启动失败" -ForegroundColor Red
    }
}

# 进入backend目录
Write-Host "`n📁 进入backend目录..." -ForegroundColor Yellow
Set-Location -Path "backend"

# 下载Go依赖
Write-Host "📦 下载Go依赖..." -ForegroundColor Yellow
go mod download

# 生成代码（如果有wire等工具）
Write-Host "🔨 生成代码..." -ForegroundColor Yellow
try {
    # 安装wire工具
    go install github.com/google/wire/cmd/wire@latest
    # 安装swag工具
    go install github.com/swaggo/swag/cmd/swag@latest
    
    # 生成代码
    swag fmt
    swag init -g cmd/api/main.go --pd
    wire cmd/api/wire.go
    wire cmd/consumer/wire.go
    wire cmd/migrate/wire.go
    
    Write-Host "✅ 代码生成完成" -ForegroundColor Green
} catch {
    Write-Host "⚠️  代码生成失败，但可以继续运行" -ForegroundColor Yellow
}

Write-Host "`n🎯 准备工作完成！现在您可以运行以下命令:" -ForegroundColor Green
Write-Host ""
Write-Host "# 1. 运行数据库迁移" -ForegroundColor Cyan
Write-Host "go run cmd/migrate/main.go cmd/migrate/wire_gen.go" -ForegroundColor White
Write-Host ""
Write-Host "# 2. 启动API服务" -ForegroundColor Cyan
Write-Host "go run cmd/api/main.go cmd/api/wire_gen.go" -ForegroundColor White
Write-Host ""
Write-Host "# 3. 启动消费者服务 (新终端)" -ForegroundColor Cyan
Write-Host "go run cmd/consumer/main.go cmd/consumer/wire_gen.go" -ForegroundColor White
Write-Host ""
Write-Host "🌐 服务地址:" -ForegroundColor Green
Write-Host "- API服务: http://localhost:8000" -ForegroundColor White
Write-Host "- Swagger文档: http://localhost:8000/swagger/index.html" -ForegroundColor White
Write-Host "- PostgreSQL: localhost:5432" -ForegroundColor White
Write-Host "- Redis: localhost:6379" -ForegroundColor White
Write-Host "- MinIO: http://localhost:9001 (admin/password: s3panda-wiki/panda-wiki-s3-secret)" -ForegroundColor White
Write-Host "- NATS: http://localhost:8222" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  注意: 使用配置文件 config/config.local.yml" -ForegroundColor Yellow 