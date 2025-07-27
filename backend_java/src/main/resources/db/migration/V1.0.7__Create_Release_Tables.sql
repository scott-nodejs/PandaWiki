-- 创建发布版本表
CREATE TABLE release (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    kb_id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    tag VARCHAR(100) NOT NULL COMMENT '版本标签',
    message TEXT COMMENT '发布信息',
    publish_time DATETIME NOT NULL COMMENT '发布时间',
    status INT DEFAULT 1 COMMENT '发布状态 1:已发布 0:已撤回',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_kb_id (kb_id),
    INDEX idx_tag (tag),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布版本表';

-- 创建发布版本表
CREATE TABLE release_version (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    kb_id VARCHAR(32) NOT NULL COMMENT '知识库ID',
    tag VARCHAR(100) NOT NULL COMMENT '版本标签',
    message TEXT COMMENT '发布信息',
    publish_time DATETIME NOT NULL COMMENT '发布时间',
    status INT DEFAULT 1 COMMENT '发布状态 1:已发布 0:已撤回',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_kb_id (kb_id),
    INDEX idx_tag (tag),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布版本表';

-- 创建发布节点关联表
CREATE TABLE release_node (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    release_id VARCHAR(32) NOT NULL COMMENT '发布版本ID',
    node_id VARCHAR(32) NOT NULL COMMENT '节点ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_release_id (release_id),
    INDEX idx_node_id (node_id),
    UNIQUE KEY uk_release_node (release_id, node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布节点关联表'; 