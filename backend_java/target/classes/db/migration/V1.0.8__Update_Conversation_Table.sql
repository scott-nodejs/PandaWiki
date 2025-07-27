-- 更新对话表结构
ALTER TABLE conversation 
ADD COLUMN IF NOT EXISTS app_id VARCHAR(32) COMMENT '应用ID',
ADD COLUMN IF NOT EXISTS app_name VARCHAR(100) COMMENT '应用名称',
ADD COLUMN IF NOT EXISTS app_type INT DEFAULT 2 COMMENT '应用类型',
ADD COLUMN IF NOT EXISTS subject VARCHAR(500) COMMENT '对话主题',
ADD COLUMN IF NOT EXISTS remote_ip VARCHAR(50) COMMENT '远程IP',
ADD COLUMN IF NOT EXISTS info JSON COMMENT '用户信息';

-- 删除旧的字段（如果存在）
ALTER TABLE conversation 
DROP COLUMN IF EXISTS user_id,
DROP COLUMN IF EXISTS title;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_conversation_app_id ON conversation(app_id);
CREATE INDEX IF NOT EXISTS idx_conversation_remote_ip ON conversation(remote_ip);
CREATE INDEX IF NOT EXISTS idx_conversation_subject ON conversation(subject); 