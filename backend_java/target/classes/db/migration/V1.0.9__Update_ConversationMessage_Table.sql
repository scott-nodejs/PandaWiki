-- 更新会话消息表结构，使其与Go后端保持一致
-- 添加缺失的字段

-- 添加app_id字段
ALTER TABLE conversation_message 
ADD COLUMN app_id VARCHAR(32) COMMENT '应用ID' AFTER conversation_id;

-- 添加provider字段
ALTER TABLE conversation_message 
ADD COLUMN provider VARCHAR(50) COMMENT '模型提供商' AFTER content;

-- 添加model字段
ALTER TABLE conversation_message 
ADD COLUMN model VARCHAR(100) COMMENT '模型名称' AFTER provider;

-- 添加prompt_tokens字段
ALTER TABLE conversation_message 
ADD COLUMN prompt_tokens INT NOT NULL DEFAULT 0 COMMENT '提示词Token数' AFTER model;

-- 添加completion_tokens字段
ALTER TABLE conversation_message 
ADD COLUMN completion_tokens INT NOT NULL DEFAULT 0 COMMENT '完成Token数' AFTER prompt_tokens;

-- 修改total_tokens字段类型
ALTER TABLE conversation_message 
MODIFY COLUMN total_tokens INT NOT NULL DEFAULT 0 COMMENT 'Token总数';

-- 添加remote_ip字段
ALTER TABLE conversation_message 
ADD COLUMN remote_ip VARCHAR(50) COMMENT '远程IP地址' AFTER completion_tokens;

-- 添加索引
ALTER TABLE conversation_message 
ADD KEY idx_app_id (app_id);

-- 更新现有记录的app_id（如果conversation表存在的话）
UPDATE conversation_message cm 
LEFT JOIN conversation c ON cm.conversation_id = c.id 
SET cm.app_id = c.app_id 
WHERE cm.app_id IS NULL AND c.app_id IS NOT NULL; 