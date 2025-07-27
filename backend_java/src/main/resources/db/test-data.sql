-- 测试数据SQL - 用于开发和测试

-- 插入测试知识库
INSERT INTO knowledge_base (id, name, description, dataset_id, access_settings, owner_id, create_time, update_time, deleted) 
VALUES (
    'test-kb-001',
    '测试知识库',
    '用于开发和测试的知识库',
    'dataset-001',
    JSON_OBJECT(
        'hosts', JSON_ARRAY('localhost', '127.0.0.1'),
        'ports', JSON_ARRAY(8080, 3000, 3010),
        'ssl_ports', JSON_ARRAY(),
        'private_key', '',
        'public_key', '',
        'base_url', '',
        'trusted_proxies', NULL,
        'simple_auth', JSON_OBJECT('enabled', false, 'password', '')
    ),
    'user-test-001',
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description),
    update_time = NOW();

-- 插入测试应用
INSERT INTO app (id, kb_id, name, type, config, create_time, update_time, deleted)
VALUES 
    (
        'app-web-test-001',
        'test-kb-001',
        '测试Web应用',
        1,
        JSON_OBJECT(
            'welcome_message', '欢迎使用PandaWiki智能助手！',
            'theme', 'light',
            'avatar', '',
            'system_prompt', '你是一个专业的知识库助手。',
            'head_code', '<link rel="stylesheet" href="http://localhost:3000/widget-bot.css">',
            'body_code', '<script src="http://localhost:3000/widget-bot.js" data-kb-id="test-kb-001"></script>'
        ),
        NOW(),
        NOW(),
        0
    ),
    (
        'app-widget-test-001',
        'test-kb-001',
        '测试Widget应用',
        2,
        JSON_OBJECT(
            'widget_bot_settings', JSON_OBJECT(
                'is_open', true,
                'theme_mode', 'light',
                'btn_text', '在线客服',
                'btn_logo', ''
            ),
            'welcome_message', '您好，我是智能助手！',
            'theme', 'auto',
            'position', 'bottom-right',
            'size', 'medium'
        ),
        NOW(),
        NOW(),
        0
    )
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    type = VALUES(type),
    config = VALUES(config),
    update_time = NOW();

-- 插入测试节点数据
INSERT INTO node (id, kb_id, parent_id, type, status, visibility, title, content, summary, emoji, position, sort_index, create_time, update_time, deleted)
VALUES 
    (
        'node-root-001',
        'test-kb-001',
        NULL,
        1,
        2,
        2,
        '根目录',
        '',
        '知识库根目录',
        '📚',
        1.0,
        1,
        NOW(),
        NOW(),
        0
    ),
    (
        'node-doc-001',
        'test-kb-001',
        'node-root-001',
        2,
        2,
        2,
        '快速开始指南',
        '# 快速开始指南\n\n欢迎使用PandaWiki！这是一个强大的知识管理系统。\n\n## 主要功能\n\n1. **智能问答**：基于AI的问答系统\n2. **文档管理**：结构化的文档组织\n3. **协作编辑**：多人协作编辑文档\n4. **版本控制**：完整的版本管理\n\n## 开始使用\n\n1. 创建知识库\n2. 添加文档\n3. 开始聊天\n\n如有任何问题，请随时询问！',
        'PandaWiki快速开始指南，介绍主要功能和使用方法',
        '🚀',
        1.0,
        1,
        NOW(),
        NOW(),
        0
    ),
    (
        'node-doc-002',
        'test-kb-001',
        'node-root-001',
        2,
        2,
        2,
        'API 使用文档',
        '# API 使用文档\n\n## 聊天接口\n\n### POST /share/v1/chat/message\n\n用于发起聊天对话的SSE接口。\n\n**请求头：**\n- Content-Type: application/json\n- X-KB-ID: 知识库ID\n- x-simple-auth-password: 简单认证密码（可选）\n\n**请求体：**\n```json\n{\n  "message": "用户问题",\n  "conversation_id": "对话ID（可选）",\n  "nonce": "随机数（可选）",\n  "app_type": 1\n}\n```\n\n**响应：**\nSSE流式响应，包含以下事件类型：\n- conversation_id: 对话ID\n- nonce: 随机数\n- chunk_result: 相关文档片段\n- data: AI回答内容\n- done: 完成信号',
        'PandaWiki API使用文档，包含聊天接口的详细说明',
        '🔧',
        2.0,
        2,
        NOW(),
        NOW(),
        0
    ),
    (
        'node-doc-003',
        'test-kb-001',
        'node-root-001',
        2,
        2,
        2,
        '常见问题解答',
        '# 常见问题解答\n\n## Q1: 如何创建知识库？\n\nA: 在管理后台点击"创建知识库"按钮，填写相关信息即可。\n\n## Q2: 支持哪些文档格式？\n\nA: 目前支持Markdown、HTML、纯文本等格式。\n\n## Q3: 如何配置AI模型？\n\nA: 在设置页面的"模型配置"部分，可以配置OpenAI、Claude等模型。\n\n## Q4: 数据安全如何保障？\n\nA: 系统提供多层安全防护：\n- 访问控制\n- 数据加密\n- 审计日志\n- 权限管理\n\n## Q5: 如何备份数据？\n\nA: 系统支持自动备份和手动备份，详见备份设置页面。',
        '常见问题解答，帮助用户快速解决使用中的问题',
        '❓',
        3.0,
        3,
        NOW(),
        NOW(),
        0
    )
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    content = VALUES(content),
    summary = VALUES(summary),
    update_time = NOW(); 