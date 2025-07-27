-- 页面访问统计表
CREATE TABLE IF NOT EXISTS `stat_page` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `kb_id` VARCHAR(64) NOT NULL COMMENT '知识库ID',
    `node_id` VARCHAR(64) DEFAULT NULL COMMENT '节点ID',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '用户ID',
    `session_id` VARCHAR(128) DEFAULT NULL COMMENT '会话ID',
    `scene` INT NOT NULL COMMENT '场景类型: 1=欢迎页面, 2=节点页面, 3=聊天页面, 4=认证页面',
    `ip` VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
    `ua` TEXT DEFAULT NULL COMMENT '用户代理',
    `browser_name` VARCHAR(100) DEFAULT NULL COMMENT '浏览器名称',
    `browser_os` VARCHAR(100) DEFAULT NULL COMMENT '浏览器操作系统',
    `referer` TEXT DEFAULT NULL COMMENT '来源页面',
    `referer_host` VARCHAR(255) DEFAULT NULL COMMENT '来源站点',
    `country` VARCHAR(100) DEFAULT NULL COMMENT '国家/地区',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_kb_id` (`kb_id`),
    INDEX `idx_scene` (`scene`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_browser_name` (`browser_name`),
    INDEX `idx_browser_os` (`browser_os`),
    INDEX `idx_referer_host` (`referer_host`),
    INDEX `idx_kb_scene` (`kb_id`, `scene`),
    INDEX `idx_kb_created` (`kb_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面访问统计表'; 