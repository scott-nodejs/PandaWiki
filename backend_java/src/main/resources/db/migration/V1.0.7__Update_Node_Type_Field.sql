-- 修改node表的type字段，从VARCHAR改为TINYINT
-- 1=文件夹, 2=文档

-- 首先将现有的字符串值转换为数字
UPDATE node SET type = '1' WHERE type IN ('folder', 'FOLDER', 'Folder');
UPDATE node SET type = '2' WHERE type IN ('document', 'DOCUMENT', 'Document');

-- 修改字段类型
ALTER TABLE node MODIFY COLUMN type TINYINT NOT NULL COMMENT '节点类型(1=文件夹,2=文档)';

-- 确保数据一致性
UPDATE node SET type = 2 WHERE type NOT IN (1, 2); 