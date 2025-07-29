# 🐮 牛小库

<div align="center">
  <img src="./docs/assets/images/logo.png" alt="牛小库 Logo" width="120">
  <h1>牛小库</h1>
  <p>AI 大模型驱动的开源知识库搭建系统</p>
</div>

## 💡 什么是牛小库？

牛小库 是一款 AI 大模型驱动的**开源知识库搭建系统**，帮助你快速构建智能化的 **产品文档、技术文档、FAQ、博客系统**，借助大模型的力量为你提供 **AI 创作、AI 问答、AI 搜索** 等能力。

## 📸 系统预览

| 牛小库 控制台                                 | Wiki 网站前台                                    |
| ------------------------------------------------ | ------------------------------------------------ |
| ![control-panel](./docs/assets/images/1-1.png) | ![knowledge-base](./docs/assets/images/1-2.png) |

## 📦 功能特性

- ✨ **AI 创作**：借助 AI 快速创建 Wiki 页面
- 🤖 **AI 问答**：基于知识库内容进行智能问答
- 🔍 **AI 搜索**：语义搜索快速找到相关内容
- 🌐 **多种访问**：支持网页端、API 接口等多种访问方式
- 📝 **丰富编辑**：支持 Markdown、富文本等多种编辑模式
- 🎨 **主题定制**：支持多种主题风格，满足不同场景需求
- 📱 **响应式设计**：完美适配桌面端和移动端
- 🔐 **权限管理**：灵活的用户和权限管理系统

## 🚀 快速开始

### 安装 牛小库

你需要一台支持 Docker 20.x 以上版本的 Linux 系统来安装 牛小库。

```bash
curl -fsSL https://get.docker.com | bash -s docker
wget https://github.com/chaitin/PandaWiki/releases/latest/download/docker-compose.yml
docker compose up -d
```

等待镜像拉取和启动完成后，使用以下命令查看服务状态和登录信息：

```bash
docker compose logs web | grep -E "(访问地址|用户名|密码|Access URL|Username|Password)"
```

> 关于安装与部署的更多细节请参考 [安装 牛小库](https://pandawiki.docs.baizhi.cloud/node/01971602-bb4e-7c90-99df-6d3c38cfd6d5)。

### 登录 牛小库

安装完成后，你将在命令行中看到类似下面的内容：

```
访问地址: http://your-server-ip:3000
用户名: admin
密码: your-random-password
```

使用浏览器打开上述内容中的 "访问地址"，你将看到 牛小库 的控制台登录入口，使用上述内容中的 "用户名" 和 "密码" 登录即可。

## ⚙️ 接入 AI 模型

> 牛小库 是由 AI 大模型驱动的 Wiki 系统，在未配置大模型的情况下 AI 创作、AI 问答、AI 搜索 等功能无法正常使用。

登录控制台后，进入 "系统设置" → "AI 模型" 页面，配置你的 AI 模型。

牛小库 支持 **OpenAI、Azure OpenAI、通义千问、智谱 AI、Kimi、DeepSeek** 等多种 AI 模型。

> 关于大模型的更多配置细节请参考 [接入 AI 模型](https://pandawiki.docs.baizhi.cloud/node/01971616-811c-70e1-82d9-706a202b8498)。

## 📚 创建知识库

"知识库" 是一组文档的集合，牛小库 将会根据知识库中的文档，为不同的知识库分别创建 "Wiki 网站"。

登录控制台后，点击右上角的 "新建知识库" 按钮，根据引导创建你的第一个知识库。

> 关于知识库的更多配置细节请参考 [知识库设置](https://pandawiki.docs.baizhi.cloud/node/01971b5e-5bea-76d2-9f89-a95f98347bb0)。

## 🎉 开始使用

如果你顺利完成了以上步骤，那么恭喜你，属于你的 牛小库 搭建成功，你可以：

- 📝 **创建文档**：在知识库中创建你的第一个文档
- 🤖 **AI 创作**：使用 AI 助手快速生成文档内容
- 🔍 **智能搜索**：体验语义搜索的强大功能
- 🌐 **分享知识**：将知识库发布为公开的 Wiki 网站
- 📱 **移动访问**：在手机端随时查看和编辑文档

## 🤝 贡献

我们欢迎所有形式的贡献，无论是新功能、bug 修复、文档改进还是其他任何改进。

欢迎提交 [Pull Request](https://github.com/chaitin/PandaWiki/pulls) 或创建 [Issue](https://github.com/chaitin/PandaWiki/issues) 来帮助改进项目。

请阅读我们的 [贡献指南](CONTRIBUTING.md) 了解如何开始贡献。

## 📄 许可证

本项目采用 [Apache 2.0](LICENSE) 许可证。

## ⭐ Star History

如果你觉得牛小库对你有帮助，请给我们一个 Star ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=chaitin/PandaWiki&type=Date)](https://www.star-history.com/#chaitin/PandaWiki&Date)
