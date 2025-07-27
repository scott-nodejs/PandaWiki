-- 为node表添加缺少的字段以匹配Go后端
ALTER TABLE node 
ADD COLUMN status TINYINT DEFAULT 2 COMMENT '节点状态(1=处理中,2=已完成)',
ADD COLUMN visibility TINYINT DEFAULT 2 COMMENT '可见性(1=私有,2=公开)',
ADD COLUMN summary TEXT COMMENT '节点摘要',
ADD COLUMN emoji VARCHAR(10) DEFAULT '' COMMENT '表情符号',
ADD COLUMN position DOUBLE COMMENT '位置/排序位置';

-- 为现有记录设置默认值
UPDATE node SET 
    status = 2,
    visibility = 2,
    emoji = '',
    position = sort_index
WHERE status IS NULL; 