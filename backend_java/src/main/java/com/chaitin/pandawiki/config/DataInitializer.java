package com.chaitin.pandawiki.config;

import com.alibaba.fastjson.JSONObject;
import com.chaitin.pandawiki.entity.App;
import com.chaitin.pandawiki.entity.KnowledgeBase;
import com.chaitin.pandawiki.entity.Node;
import com.chaitin.pandawiki.mapper.AppMapper;
import com.chaitin.pandawiki.mapper.KnowledgeBaseMapper;
import com.chaitin.pandawiki.mapper.NodeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据初始化器 - 在应用启动时初始化测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final AppMapper appMapper;
    private final NodeMapper nodeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化测试数据...");

        try {
            initTestKnowledgeBase();
            initTestApps();
            initTestNodes();
            log.info("测试数据初始化完成！");
        } catch (Exception e) {
            log.error("初始化测试数据失败", e);
        }
    }

    /**
     * 初始化测试知识库
     */
    private void initTestKnowledgeBase() {
    }

    /**
     * 初始化测试应用
     */
    private void initTestApps() {
    }

    private void createAppIfNotExists(String appId, String kbId, String name, Integer type, Map<String, Object> config) {
        App existingApp = appMapper.selectById(appId);
        if (existingApp != null) {
            log.info("测试应用 {} 已存在，跳过创建", appId);
            return;
        }

        App app = new App();
        app.setId(appId);
        app.setKbId(kbId);
        app.setName(name);
        app.setType(type);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        // 创建应用设置
        App.AppSettings settings = new App.AppSettings();
        settings.setWelcomeStr((String) config.get("welcome_message"));
        settings.setTitle(name);
        app.setSettings(JSONObject.toJSONString(settings));

        appMapper.insert(app);
        log.info("创建测试应用: {} ({})", name, type);
    }

    /**
     * 初始化测试节点
     */
    private void initTestNodes() {
        String kbId = "test-kb-001";

        // 根节点
        createNodeIfNotExists("node-root-001", kbId, null, 1, "根目录", "",
            "知识库根目录", "📚", 1.0, 1);

        // 文档节点
        createNodeIfNotExists("node-doc-001", kbId, "node-root-001", 2, "快速开始指南",
            "# 快速开始指南\n\n欢迎使用PandaWiki！这是一个强大的知识管理系统。\n\n" +
            "## 主要功能\n\n1. **智能问答**：基于AI的问答系统\n2. **文档管理**：结构化的文档组织\n" +
            "3. **协作编辑**：多人协作编辑文档\n4. **版本控制**：完整的版本管理\n\n" +
            "## 开始使用\n\n1. 创建知识库\n2. 添加文档\n3. 开始聊天\n\n如有任何问题，请随时询问！",
            "PandaWiki快速开始指南，介绍主要功能和使用方法", "🚀", 1.0, 1);

        createNodeIfNotExists("node-doc-002", kbId, "node-root-001", 2, "API 使用文档",
            "# API 使用文档\n\n## 聊天接口\n\n### POST /share/v1/chat/message\n\n" +
            "用于发起聊天对话的SSE接口。\n\n**请求头：**\n- Content-Type: application/json\n" +
            "- X-KB-ID: 知识库ID\n- x-simple-auth-password: 简单认证密码（可选）\n\n" +
            "**请求体：**\n```json\n{\n  \"message\": \"用户问题\",\n  \"conversation_id\": \"对话ID（可选）\",\n" +
            "  \"nonce\": \"随机数（可选）\",\n  \"app_type\": 1\n}\n```\n\n" +
            "**响应：**\nSSE流式响应，包含以下事件类型：\n- conversation_id: 对话ID\n" +
            "- nonce: 随机数\n- chunk_result: 相关文档片段\n- data: AI回答内容\n- done: 完成信号",
            "PandaWiki API使用文档，包含聊天接口的详细说明", "🔧", 2.0, 2);

        createNodeIfNotExists("node-doc-003", kbId, "node-root-001", 2, "常见问题解答",
            "# 常见问题解答\n\n## Q1: 如何创建知识库？\n\nA: 在管理后台点击\"创建知识库\"按钮，填写相关信息即可。\n\n" +
            "## Q2: 支持哪些文档格式？\n\nA: 目前支持Markdown、HTML、纯文本等格式。\n\n" +
            "## Q3: 如何配置AI模型？\n\nA: 在设置页面的\"模型配置\"部分，可以配置OpenAI、Claude等模型。\n\n" +
            "## Q4: 数据安全如何保障？\n\nA: 系统提供多层安全防护：\n- 访问控制\n- 数据加密\n- 审计日志\n- 权限管理\n\n" +
            "## Q5: 如何备份数据？\n\nA: 系统支持自动备份和手动备份，详见备份设置页面。",
            "常见问题解答，帮助用户快速解决使用中的问题", "❓", 3.0, 3);
    }

    private void createNodeIfNotExists(String nodeId, String kbId, String parentId, Integer type,
                                     String name, String content, String summary, String emoji,
                                     Double position, Integer sort) {
        Node existingNode = nodeMapper.selectById(nodeId);
        if (existingNode != null) {
            log.info("测试节点 {} 已存在，跳过创建", nodeId);
            return;
        }

        Node node = new Node();
        node.setId(nodeId);
        node.setKbId(kbId);
        node.setParentId(parentId);
        node.setType(type);
        node.setStatus(2); // 已完成
        node.setVisibility(2); // 公开
        node.setName(name);
        node.setContent(content);
        node.setSummary(summary);
        node.setEmoji(emoji);
        node.setPosition(position);
        node.setSort(sort);
        node.setDeleted(false);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());

        nodeMapper.insert(node);
        log.info("创建测试节点: {}", name);
    }
}
