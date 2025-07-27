-- 创建数据库
CREATE DATABASE IF NOT EXISTS pandawiki DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE pandawiki;

-- 知识库表
CREATE TABLE IF NOT EXISTS knowledge_base (
    id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    name VARCHAR(100) NOT NULL COMMENT '知识库名称',
    description TEXT COMMENT '知识库描述',
    dataset_id VARCHAR(50) COMMENT '数据集ID',
    access_settings JSON COMMENT '访问设置',
    owner_id VARCHAR(32) NOT NULL COMMENT '所有者ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_owner (owner_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 节点表
CREATE TABLE IF NOT EXISTS node (
    id VARCHAR(32) NOT NULL COMMENT '节点ID',
    kb_id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    parent_id VARCHAR(32) COMMENT '父节点ID',
    type TINYINT NOT NULL COMMENT '节点类型(1=文件夹,2=文档)',
    status TINYINT DEFAULT 2 COMMENT '节点状态(1=处理中,2=已完成)',
    visibility TINYINT DEFAULT 2 COMMENT '可见性(1=私有,2=公开)',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    summary TEXT COMMENT '节点摘要',
    emoji VARCHAR(10) DEFAULT '' COMMENT '表情符号',
    position DOUBLE COMMENT '位置/排序位置',
    sort_index INT NOT NULL DEFAULT 0 COMMENT '排序索引',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_kb_parent (kb_id, parent_id),
    KEY idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点表';

-- 用户访问日志表
CREATE TABLE IF NOT EXISTS user_access_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    kb_id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    node_id VARCHAR(32) NOT NULL COMMENT '节点ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    access_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    duration INT NOT NULL DEFAULT 0 COMMENT '停留时间(秒)',
    PRIMARY KEY (id),
    KEY idx_kb_access_time (kb_id, access_time),
    KEY idx_node_access_time (node_id, access_time),
    KEY idx_user_access_time (user_id, access_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户访问日志表';

-- 会话表
CREATE TABLE IF NOT EXISTS conversation (
    id VARCHAR(32) NOT NULL COMMENT '会话ID',
    kb_id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    title VARCHAR(200) COMMENT '会话标题',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_kb_create_time (kb_id, create_time),
    KEY idx_user_create_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 会话消息表
CREATE TABLE IF NOT EXISTS conversation_message (
    id VARCHAR(32) NOT NULL COMMENT '消息ID',
    conversation_id VARCHAR(32) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色:user,assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    total_tokens INT NOT NULL DEFAULT 0 COMMENT 'Token总数',
    response_time INT COMMENT '响应时间(毫秒)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_conversation_create_time (conversation_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话消息表';

-- 模型使用日志表
CREATE TABLE IF NOT EXISTS model_usage_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    kb_id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    model_id VARCHAR(32) NOT NULL COMMENT '模型ID',
    total_tokens INT NOT NULL DEFAULT 0 COMMENT 'Token总数',
    response_time INT COMMENT '响应时间(毫秒)',
    status VARCHAR(20) NOT NULL COMMENT '状态:SUCCESS,FAILED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_kb_create_time (kb_id, create_time),
    KEY idx_model_create_time (model_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型使用日志表';

-- 应用表
CREATE TABLE IF NOT EXISTS app (
    id VARCHAR(32) NOT NULL COMMENT '应用ID',
    kb_id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    name VARCHAR(100) NOT NULL COMMENT '应用名称',
    type VARCHAR(20) NOT NULL COMMENT '应用类型:WEB,WIDGET,BOT',
    config JSON COMMENT '应用配置',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_kb_type (kb_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表'; 