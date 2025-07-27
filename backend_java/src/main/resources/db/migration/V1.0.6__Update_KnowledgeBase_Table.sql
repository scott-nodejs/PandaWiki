-- 更新knowledge_base表，添加dataset_id和access_settings字段
ALTER TABLE knowledge_base 
ADD COLUMN dataset_id VARCHAR(50) COMMENT '数据集ID',
ADD COLUMN access_settings JSON COMMENT '访问设置';

-- 为现有记录设置默认的access_settings
UPDATE knowledge_base 
SET access_settings = JSON_OBJECT(
    'hosts', JSON_ARRAY('localhost'),
    'ports', JSON_ARRAY(8080),
    'ssl_ports', JSON_ARRAY(),
    'private_key', '',
    'public_key', '',
    'base_url', '',
    'trusted_proxies', NULL,
    'simple_auth', JSON_OBJECT('enabled', false, 'password', '')
)
WHERE access_settings IS NULL; 