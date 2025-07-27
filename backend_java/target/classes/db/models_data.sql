-- 插入BaiZhiCloud模型数据
-- 使用方法: 在MySQL客户端执行此SQL文件

USE pandawiki;

INSERT INTO models (
    id, 
    provider, 
    model, 
    api_key, 
    api_header, 
    base_url, 
    api_version, 
    type, 
    is_active, 
    prompt_tokens, 
    completion_tokens, 
    total_tokens,
    created_at,
    updated_at
) VALUES 
-- DeepSeek-V3 Chat模型 - 主要的对话模型，使用量最大
(
    '0e147fc5-3eef-4719-bd3e-6f424c3c892e',
    'BAIZHI_CLOUD',
    'deepseek-v3',
    'vr2G4a9ltBS6iCM9ag0TCTrEDprI9JmrO4726KdqRc3ePVRD',
    '',
    'https://model-square.app.baizhi.cloud/v1',
    '',
    'chat',
    TRUE,  -- 设为活跃状态，因为有大量使用记录
    22692737,
    266239,
    22958976,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- BGE-Reranker-V2-M3 重排序模型 (第一个实例)
(
    '5e686ae4-4b3e-4df9-8eb8-0a465b354190',
    'BAIZHI_CLOUD',
    'bge-reranker-v2-m3',
    'NSh1lLQFxGEmo3dbIEpu6X8rkW3BjteVDESu7b93xal7f7Uh',
    '',
    'https://model-square.app.baizhi.cloud/v1',
    '',
    'rerank',
    FALSE,
    0,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- BGE-M3 嵌入模型 - 用于文档向量化
(
    '7ca942e8-a98f-46e2-8efc-a8ddcbd90aea',
    'BAIZHI_CLOUD',
    'bge-m3',
    'vr2G4a9ltBS6iCM9ag0TCTrEDprI9JmrO4726KdqRc3ePVRD',
    '',
    'https://model-square.app.baizhi.cloud/v1',
    '',
    'embedding',
    FALSE,
    0,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- BGE-Reranker-V2-M3 重排序模型 (第二个实例)
(
    '6de51030-aec7-46bf-898d-0ece5965304e',
    'BAIZHI_CLOUD',
    'bge-reranker-v2-m3',
    'vr2G4a9ltBS6iCM9ag0TCTrEDprI9JmrO4726KdqRc3ePVRD',
    '',
    'https://model-square.app.baizhi.cloud/v1',
    '',
    'rerank',
    FALSE,
    0,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON DUPLICATE KEY UPDATE 
    provider = VALUES(provider),
    model = VALUES(model),
    api_key = VALUES(api_key),
    api_header = VALUES(api_header),
    base_url = VALUES(base_url),
    api_version = VALUES(api_version),
    type = VALUES(type),
    prompt_tokens = VALUES(prompt_tokens),
    completion_tokens = VALUES(completion_tokens),
    total_tokens = VALUES(total_tokens),
    updated_at = CURRENT_TIMESTAMP;

-- 查询确认数据插入成功
SELECT id, provider, model, type, is_active, total_tokens FROM models WHERE provider = 'BAIZHI_CLOUD'; 