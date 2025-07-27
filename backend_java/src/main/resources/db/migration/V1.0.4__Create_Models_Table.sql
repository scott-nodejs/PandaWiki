-- 创建models表
CREATE TABLE IF NOT EXISTS models (
    id VARCHAR(32) NOT NULL COMMENT '模型ID',
    provider VARCHAR(50) NOT NULL COMMENT '模型提供商',
    model VARCHAR(100) NOT NULL COMMENT '模型名称',
    api_key TEXT COMMENT 'API密钥',
    api_header TEXT COMMENT 'API请求头',
    base_url VARCHAR(500) COMMENT 'API基础URL',
    api_version VARCHAR(50) COMMENT 'API版本',
    type VARCHAR(20) NOT NULL DEFAULT 'chat' COMMENT '模型类型:chat,embedding,rerank',
    is_active BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否激活',
    prompt_tokens BIGINT NOT NULL DEFAULT 0 COMMENT '提示词Token数',
    completion_tokens BIGINT NOT NULL DEFAULT 0 COMMENT '完成Token数',
    total_tokens BIGINT NOT NULL DEFAULT 0 COMMENT '总Token数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_provider (provider),
    KEY idx_type (type),
    KEY idx_is_active (is_active),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型表';

-- 插入一些默认模型配置示例
INSERT INTO models (id, provider, model, type, is_active) VALUES 
('model_openai_gpt35', 'OPENAI', 'gpt-3.5-turbo', 'chat', TRUE),
('model_openai_gpt4', 'OPENAI', 'gpt-4', 'chat', FALSE),
('model_embedding_ada', 'OPENAI', 'text-embedding-ada-002', 'embedding', FALSE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP; 